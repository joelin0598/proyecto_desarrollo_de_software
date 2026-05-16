package his.application.services;

import his.application.dto.TriageListItemsResponse;
import his.application.dto.TriageRequest;
import his.application.dto.TriageResponse;
import his.application.usecases.TriageUseCase;
import his.domain.models.Patient;
import his.domain.models.Priority;
import his.domain.models.VitalSigns;
import his.domain.ports.HospitalStaffRepository;
import his.domain.ports.PatientRepository;
import his.domain.ports.UserRepository;
import his.domain.ports.VitalSignsRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * CU 2.0 — Orquestación de ingreso y triaje hospitalario.
 *
 * Flujo:
 *  1. Resolve personalId desde email JWT → User → HospitalStaff
 *  2. FA01: buscar paciente por DPI; si no existe, crear nuevo (sin cuenta web)
 *  3. Construir VitalSigns con los datos del request
 *  4. calculatePriority() en el dominio — lógica de RN04 encapsulada en VitalSigns
 *  5. Persistir y retornar TriageResponse con prioridad real
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class TriageService implements TriageUseCase {

    private final PatientRepository patientRepository;
    private final VitalSignsRepository vitalSignsRepository;
    private final HospitalStaffRepository hospitalStaffRepository;
    private final UserRepository userRepository;

    @Override
    @Transactional
    public TriageResponse execute(TriageRequest request, String emailPersonal) {

        // 1. Resolver personalId del personal autenticado que registra el triaje
        Long personalId = resolvePersonalId(emailPersonal);

        // 2. FA01 — Buscar paciente por DPI; crear si es primera visita
        boolean pacienteNuevo = false;
        Patient patient = patientRepository.findByDpi(request.getDpi()).orElse(null);

        if (patient == null) {
            log.info("FA01: Paciente con DPI={} no encontrado. Creando nuevo expediente.", request.getDpi());
            patient = createWalkInPatient(request);
            pacienteNuevo = true;
        } else {
            log.info("Paciente existente encontrado con DPI={}, pacienteId={}", request.getDpi(), patient.getPacienteId());
        }

        // 3. Construir y persistir los signos vitales
        VitalSigns vitalSigns = VitalSigns.builder()
                .pacienteId(patient.getPacienteId())
                .personalId(personalId)
                .citaMedicaId(null)  // en triaje de urgencia no hay cita médica previa
                .presionSistolica(request.getPresionSistolica())
                .presionDiastolica(request.getPresionDiastolica())
                .frecuenciaCardiaca(request.getFrecuenciaCardiaca())
                .temperatura(request.getTemperatura())
                .saturacionOxigeno(request.getSaturacionOxigeno())
                .pesoKg(request.getPesoKg())
                .tallaCm(request.getTallaCm())
                .build();

        // 4. RN04 — Clasificación de prioridad: lógica en el dominio, NO en el frontend
        vitalSigns.calculatePriority();
        log.info("Prioridad asignada a pacienteId={}: {}", patient.getPacienteId(), vitalSigns.getPriority());

        // 5. FA03 — Detectar emergencia extrema (código rojo)
        boolean alertaEmergencia = vitalSigns.getPriority() == Priority.ROJO;
        if (alertaEmergencia) {
            log.warn("FA03: ALERTA ROJA para pacienteId={}. Signos vitales críticos.", patient.getPacienteId());
        }

        vitalSigns = vitalSignsRepository.save(vitalSigns);

        return TriageResponse.builder()
                .pacienteId(patient.getPacienteId())
                .nombreCompleto(patient.getNombreCompleto())
                .dpi(patient.getDpi())
                .pacienteNuevo(pacienteNuevo)
                .signosVitalesId(vitalSigns.getSignosVitalesId())
                .prioridad(vitalSigns.getPriority())
                .alertaEmergencia(alertaEmergencia)
                .presionSistolica(vitalSigns.getPresionSistolica())
                .presionDiastolica(vitalSigns.getPresionDiastolica())
                .frecuenciaCardiaca(vitalSigns.getFrecuenciaCardiaca())
                .temperatura(vitalSigns.getTemperatura())
                .saturacionOxigeno(vitalSigns.getSaturacionOxigeno())
                .pesoKg(vitalSigns.getPesoKg())
                .tallaCm(vitalSigns.getTallaCm())
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public List<TriageListItemsResponse> listarTriajesRecientes() {
        return vitalSignsRepository.findAllRecent().stream()
                .map(this::toListItemResponse)
                .toList();
    }

    private TriageListItemsResponse toListItemResponse(VitalSigns vitalSigns) {
        Patient patient = patientRepository.findById(vitalSigns.getPacienteId())
                .orElseThrow(() -> new IllegalArgumentException(
                        "No se encontro paciente para pacienteId=" + vitalSigns.getPacienteId()));

        Priority prioridad = vitalSigns.getPriority();
        boolean alertaEmergencia = prioridad == Priority.ROJO;

        return TriageListItemsResponse.builder()
                .signosVitalesId(vitalSigns.getSignosVitalesId())
                .pacienteId(patient.getPacienteId())
                .fechaHoraRegistro(vitalSigns.getCreatedAt())
                .nombreCompleto(patient.getNombreCompleto())
                .dpi(patient.getDpi())
                .prioridad(prioridad)
                .alertaEmergencia(alertaEmergencia)
                .presionSistolica(vitalSigns.getPresionSistolica())
                .presionDiastolica(vitalSigns.getPresionDiastolica())
                .frecuenciaCardiaca(vitalSigns.getFrecuenciaCardiaca())
                .temperatura(vitalSigns.getTemperatura())
                .saturacionOxigeno(vitalSigns.getSaturacionOxigeno())
                .pesoKg(vitalSigns.getPesoKg())
                .tallaCm(vitalSigns.getTallaCm())
                .build();
    }
    /**
     * Resuelve el personalId del empleado hospitalario autenticado.
     * Ruta: email → User → HospitalStaff.personalId
     */
    private Long resolvePersonalId(String emailPersonal) {
        var user = userRepository.findByEmail(emailPersonal)
                .orElseThrow(() -> new IllegalArgumentException(
                        "No se encontró el usuario autenticado con email: " + emailPersonal));

        return hospitalStaffRepository.findByUsuarioId(user.getUserId())
                .orElseThrow(() -> new IllegalArgumentException(
                        "El usuario autenticado no tiene un perfil de personal hospitalario registrado"))
                .getPersonalId();
    }

    /**
     * FA01 — Crea un nuevo paciente sin cuenta web (walk-in / primera visita).
     */
    private Patient createWalkInPatient(TriageRequest request) {
        Patient newPatient = Patient.builder()
                .usuarioId(null)   // sin cuenta web; otro CU gestiona la vinculación
                .nombreCompleto(request.getNombreCompleto())
                .dpi(request.getDpi())
                .genero(request.getGenero())
                .emailContacto(request.getEmailContacto())
                .fechaNacimiento(request.getFechaNacimiento())
                .direccion(request.getDireccion())
                .telefono(request.getTelefono())
                .contactoEmergencia(request.getContactoEmergencia())
                .telefonoEmergencia(request.getTelefonoEmergencia())
                .aseguradoraId(request.getAseguradoraId())
                .polizaSeguro(request.getPolizaSeguro())
                .build();

        newPatient.validateDpiIfPresent();
        return patientRepository.save(newPatient);
    }
}
