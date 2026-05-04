package com.javainternship.service;

import com.javainternship.dto.request.create.CreateUserRequest;
import com.javainternship.dto.request.update.UpdateUserRequest;
import com.javainternship.dto.response.UserResponse;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface UserService {

    UserResponse findUserByEmail(String email);

    List<UserResponse> findAllUsers();

    Page<UserResponse> searchUsers(String name, String surname, Pageable pageable);

    UserResponse findUserById(Long id);

    UserResponse createUser(CreateUserRequest request);

    UserResponse updateUser(UpdateUserRequest request, Long id);

    void deleteUser(Long id);

    void activateUser(Long id);

    void deactivateUser(Long id);

    /** Creates a user profile during gateway-orchestrated signup (no admin JWT). */
    UserResponse createUserThroughGateway(CreateUserRequest request);

    /** Compensates failed auth registration by deactivating the user profile. */
    void rollbackGatewayRegistration(Long userId);
}

