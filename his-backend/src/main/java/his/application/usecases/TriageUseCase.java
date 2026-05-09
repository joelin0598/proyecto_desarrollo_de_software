package his.application.usecases;

import his.application.dto.TriageRequest;
import his.application.dto.TriageResponse;

/**
 * Puerto del caso de uso CU 2.0 — Ingreso y clasificación de urgencia.
 * El emailPersonal proviene del JWT del usuario autenticado; permite resolver el personalId en el servicio.
 */
public interface TriageUseCase {
    TriageResponse execute(TriageRequest request, String emailPersonal);
}
