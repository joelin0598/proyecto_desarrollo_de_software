package his.application.usecases;
import his.application.dto.AddLaboratoryResultRequest;
import his.application.dto.CreateLaboratoryOrderRequest;
import his.application.dto.LaboratoryOrderResponse;
import java.util.List;
public interface LaboratoryUseCase {
    LaboratoryOrderResponse createOrder(CreateLaboratoryOrderRequest req, String emailLaboratorista);
    LaboratoryOrderResponse receiveSample(Long ordenLaboratorioId, String emailLaboratorista);
    LaboratoryOrderResponse rejectSample(Long ordenLaboratorioId, String motivo, String emailLaboratorista);
    LaboratoryOrderResponse addResult(AddLaboratoryResultRequest req, String emailLaboratorista);
    List<LaboratoryOrderResponse> getOrdersByDetalle(Long citaMedicaDetalleId);
    LaboratoryOrderResponse getOrder(Long ordenLaboratorioId);
    List<LaboratoryOrderResponse> getResultsByPatient(Long patientId);
}