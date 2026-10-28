package fu.de190423.de190423_userservice.service;

import fu.de190423.de190423_userservice.dto.ApiResponse;
import fu.de190423.de190423_userservice.dto.UserDTO;
import org.springframework.data.domain.Pageable;

public interface UserService {
    ApiResponse createUser(UserDTO dto);
    ApiResponse updateUser(Long userId, UserDTO dto);
    ApiResponse getUserById(Long userId);
    ApiResponse deactivateUser(Long userId);
    ApiResponse getUserList(String role, String status, Pageable pageable);
}
