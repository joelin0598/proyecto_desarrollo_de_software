package his.application.services;

import his.adapters.exception.DuplicateEmailException;
import his.adapters.exception.InvalidPasswordFormatException;
import his.application.dto.AuthResponse;
import his.application.dto.RegisterRequest;
import his.domain.models.Patient;
import his.domain.models.Role;
import his.domain.models.User;
import his.domain.ports.HospitalStaffRepository;
import his.domain.ports.PatientRepository;
import his.domain.ports.UserRepository;
import his.infrastructure.security.JwtService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private UserRepository userRepository;
    @Mock
    private PatientRepository patientRepository;
    @Mock
    private HospitalStaffRepository hospitalStaffRepository;
    @Mock
    private PasswordEncoder passwordEncoder;
    @Mock
    private JwtService jwtService;
    @Mock
    private AuthenticationManager authenticationManager;

    private AuthService authService;

    @BeforeEach
    void setUp() {
        authService = new AuthService(
                userRepository,
                patientRepository,
                hospitalStaffRepository,
                passwordEncoder,
                jwtService,
                authenticationManager
        );
    }

    @Test
    void register_createsPatientUser_whenRequestIsValid() {
        // Arrange
        RegisterRequest request = RegisterRequest.builder()
                .firstName("Ana")
                .lastName("Perez")
                .email("ana.perez@example.com")
                .password("Abc123!")
                .build();

        when(userRepository.existsByEmail(request.getEmail())).thenReturn(false);
        when(passwordEncoder.encode(request.getPassword())).thenReturn("hashed-password");
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> {
            User user = invocation.getArgument(0);
            user.setUserId(101L);
            return user;
        });
        when(patientRepository.save(any(Patient.class))).thenAnswer(invocation -> {
            Patient patient = invocation.getArgument(0);
            patient.setPacienteId(501L);
            return patient;
        });
        when(jwtService.generateToken(any(User.class))).thenReturn("jwt-token");

        // Act
        AuthResponse response = authService.register(request);

        // Assert
        assertNotNull(response);
        assertEquals("jwt-token", response.getToken());
        assertNotNull(response.getUser());
        assertEquals(101L, response.getUser().getId());
        assertEquals("ana.perez@example.com", response.getUser().getEmail());
        assertEquals("Ana", response.getUser().getFirstName());
        assertEquals("Perez", response.getUser().getLastName());
        assertEquals(Role.PACIENTE, response.getUser().getRole());

        verify(userRepository).save(any(User.class));
        verify(patientRepository).save(any(Patient.class));
    }

    @Test
    void register_throwsDuplicateEmailException_whenEmailAlreadyExists() {
        // Arrange
        RegisterRequest request = RegisterRequest.builder()
                .firstName("Ana")
                .lastName("Perez")
                .email("ana.perez@example.com")
                .password("Abc123!")
                .build();

        when(userRepository.existsByEmail(request.getEmail())).thenReturn(true);

        // Act + Assert
        assertThrows(DuplicateEmailException.class, () -> authService.register(request));

        verify(userRepository, never()).save(any());
        verify(patientRepository, never()).save(any());
    }

    @Test
    void register_throwsInvalidPasswordFormatException_whenPasswordDoesNotMeetPolicy() {
        // Arrange
        RegisterRequest request = RegisterRequest.builder()
                .firstName("Ana")
                .lastName("Perez")
                .email("ana.perez@example.com")
                .password("abcdef")
                .build();

        when(userRepository.existsByEmail(request.getEmail())).thenReturn(false);

        // Act + Assert
        assertThrows(InvalidPasswordFormatException.class, () -> authService.register(request));

        verify(userRepository, never()).save(any());
        verify(patientRepository, never()).save(any());
    }
}


