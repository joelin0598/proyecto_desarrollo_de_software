package his.application.usecases;
import his.application.dto.RegisterRequestAdmin;
import his.application.dto.UpdateHospitalStaffUserRequest;
import his.application.dto.UserMaintenanceResponse;
import java.util.List;
public interface UserMaintenanceUseCase {
    List<UserMaintenanceResponse> listHospitalStaffUsers();
    UserMaintenanceResponse createHospitalStaffUser(RegisterRequestAdmin request);
    UserMaintenanceResponse updateHospitalStaffUser(Long userId, UpdateHospitalStaffUserRequest request);
    UserMaintenanceResponse suspendHospitalStaffUser(Long userId, String adminEmail);
    void deleteHospitalStaffUser(Long userId, String adminEmail);

}
