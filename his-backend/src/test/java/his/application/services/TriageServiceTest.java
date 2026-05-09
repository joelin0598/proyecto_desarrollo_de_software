package his.application.services;

import his.application.dto.TriageRequest;
import his.domain.models.Priority;
import his.domain.models.VitalSigns;
import his.domain.ports.VitalSignsRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TriageServiceTest {

    @Mock
    private VitalSignsRepository vitalSignsRepository;

    private TriageService triageService;

    @BeforeEach
    void setUp() {
        triageService = new TriageService(vitalSignsRepository);
    }

    @Test
    void execute_savesTriageWithCalculatedPriority_whenRequestIsValid() {
        // Arrange
        TriageRequest request = TriageRequest.builder()
                .pacienteId(10L)
                .personalId(20L)
                .citaMedicaId(30L)
                .presionSistolica(120)
                .presionDiastolica(80)
                .frecuenciaCardiaca(76)
                .temperatura(40.1)
                .saturacionOxigeno(97)
                .tallaCm(170)
                .pesoKg(70)
                .build();

        when(vitalSignsRepository.save(any(VitalSigns.class))).thenAnswer(invocation -> {
            VitalSigns saved = invocation.getArgument(0);
            saved.setSignosVitalesId(999L);
            return saved;
        });

        // Act
        VitalSigns result = triageService.execute(request);

        // Assert
        assertNotNull(result);
        assertEquals(999L, result.getSignosVitalesId());
        assertEquals(Priority.ROJO, result.getPriority());

        ArgumentCaptor<VitalSigns> captor = ArgumentCaptor.forClass(VitalSigns.class);
        verify(vitalSignsRepository).save(captor.capture());
        VitalSigns persisted = captor.getValue();
        assertEquals(10L, persisted.getPacienteId());
        assertEquals(20L, persisted.getPersonalId());
        assertEquals(30L, persisted.getCitaMedicaId());
        assertEquals(Priority.ROJO, persisted.getPriority());
    }

    @Test
    void execute_throwsException_whenSaturationIsOutOfRange() {
        // Arrange
        TriageRequest request = TriageRequest.builder()
                .pacienteId(10L)
                .personalId(20L)
                .presionSistolica(120)
                .presionDiastolica(80)
                .frecuenciaCardiaca(76)
                .temperatura(36.8)
                .saturacionOxigeno(120)
                .tallaCm(170)
                .pesoKg(70)
                .build();

        // Act
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () -> triageService.execute(request));

        // Assert
        assertEquals("La saturación de oxígeno está fuera de rango clínico", ex.getMessage());
        verify(vitalSignsRepository, never()).save(any());
    }

    @Test
    void execute_throwsException_whenDiastolicIsGreaterThanSystolic() {
        // Arrange
        TriageRequest request = TriageRequest.builder()
                .pacienteId(10L)
                .personalId(20L)
                .presionSistolica(100)
                .presionDiastolica(110)
                .frecuenciaCardiaca(76)
                .temperatura(36.8)
                .saturacionOxigeno(98)
                .tallaCm(170)
                .pesoKg(70)
                .build();

        // Act
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () -> triageService.execute(request));

        // Assert
        assertEquals("La presión diastólica no puede ser mayor o igual a la sistólica", ex.getMessage());
        verify(vitalSignsRepository, never()).save(any());
    }
}


