package com.javainternship.controller;

import com.javainternship.dto.request.create.CreateUserRequest;
import com.javainternship.dto.request.update.UpdateUserRequest;
import com.javainternship.dto.request.update.SetUserStatusRequest;
import com.javainternship.dto.response.UserResponse;
import com.javainternship.logging.ControllerLogger;
import com.javainternship.service.UserService;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/users")
@AllArgsConstructor
public class UserController {

    private final UserService service;
    private final ControllerLogger controllerLogger;

    @GetMapping
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN', 'ROLE_USER')")
    public ResponseEntity<Page<UserResponse>> getAllUsers(@RequestParam(required = false) String name,
                                                            @RequestParam(required = false) String surname,
                                                            @RequestParam(defaultValue = "0") int page,
                                                            @RequestParam(defaultValue = "10") int size) {
        controllerLogger.methodCalled("UserController", "getAllUsers", name, surname, page, size);
        Page<UserResponse> result = service.searchUsers(name, surname, PageRequest.of(page, size));
        return ResponseEntity.ok(result);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN', 'ROLE_USER')")
    public ResponseEntity<UserResponse> getUserById(@PathVariable Long id) {
        controllerLogger.methodCalled("UserController", "getUserById", id);
        return ResponseEntity.ok(service.findUserById(id));
    }

    @GetMapping("/by-email/{email}")
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN', 'ROLE_USER')")
    public ResponseEntity<UserResponse> getUserByEmail(@PathVariable String email) {
        controllerLogger.methodCalled("UserController", "getUserByEmail", email);
        return ResponseEntity.ok(service.findUserByEmail(email));
    }

    @PostMapping
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public ResponseEntity<UserResponse> createUser(@Valid @RequestBody CreateUserRequest request) {
        controllerLogger.methodCalled("UserController", "createUser", request.getEmail());
        UserResponse response = service.createUser(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN', 'ROLE_USER')")
    public ResponseEntity<UserResponse> updateUser(@PathVariable Long id,
                                                   @Valid @RequestBody UpdateUserRequest request) {
        controllerLogger.methodCalled("UserController", "updateUser", id);
        UserResponse response = service.updateUser(request, id);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN', 'ROLE_USER')")
    public ResponseEntity<Void> deleteUser(@PathVariable Long id) {
        controllerLogger.methodCalled("UserController", "deleteUser", id);
        service.deleteUser(id);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{id}/status")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public ResponseEntity<Void> setUserStatus(@PathVariable Long id,
                                                 @Valid @RequestBody SetUserStatusRequest request) {
        controllerLogger.methodCalled("UserController", "setUserStatus", id, request.getActive());
        if (Boolean.TRUE.equals(request.getActive())) {
            service.activateUser(id);
        } else {
            service.deactivateUser(id);
        }
        return ResponseEntity.noContent().build();
    }
}
