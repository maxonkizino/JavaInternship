package com.javainternship.controller;

import com.javainternship.dto.request.create.CreateUserRequest;
import com.javainternship.dto.request.update.UpdateUserRequest;
import com.javainternship.dto.request.update.SetUserStatusRequest;
import com.javainternship.dto.response.UserResponse;
import com.javainternship.service.interf.UserService;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/users")
@AllArgsConstructor
public class UserController {

    private final UserService service;

    @GetMapping
    public ResponseEntity<Page<UserResponse>> getAllUsers(@RequestParam(required = false) String name,
                                                            @RequestParam(required = false) String surname,
                                                            @RequestParam(defaultValue = "0") int page,
                                                            @RequestParam(defaultValue = "10") int size) {
        Page<UserResponse> result = service.searchUsers(name, surname, PageRequest.of(page, size));
        return ResponseEntity.ok(result);
    }

    @GetMapping("/{id}")
    public ResponseEntity<UserResponse> getUserById(@PathVariable Long id) {
        return ResponseEntity.ok(service.findUserById(id));
    }

    @GetMapping("/by-email/{email}")
    public ResponseEntity<UserResponse> getUserByEmail(@PathVariable String email) {
        return ResponseEntity.ok(service.findUserByEmail(email));
    }

    @PostMapping
    public ResponseEntity<UserResponse> createUser(@Valid @RequestBody CreateUserRequest request) {
        UserResponse response = service.createUser(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PutMapping("/{id}")
    public ResponseEntity<UserResponse> updateUser(@PathVariable Long id,
                                                   @Valid @RequestBody UpdateUserRequest request) {
        UserResponse response = service.updateUser(request, id);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteUser(@PathVariable Long id) {
        service.deleteUser(id);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{id}/status")
    public ResponseEntity<Void> setUserStatus(@PathVariable Long id,
                                                 @Valid @RequestBody SetUserStatusRequest request) {
        if (Boolean.TRUE.equals(request.getActive())) {
            service.activateUser(id);
        } else {
            service.deactivateUser(id);
        }
        return ResponseEntity.noContent().build();
    }
}
