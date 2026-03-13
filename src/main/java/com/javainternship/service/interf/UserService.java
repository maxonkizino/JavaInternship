package com.javainternship.service.interf;

import com.javainternship.dto.request.create.CreateUserRequest;

import com.javainternship.dto.request.update.UpdateUserRequest;
import com.javainternship.dto.response.UserResponse;
import com.javainternship.model.User;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.util.List;


public interface UserService {

    public UserResponse findUserByEmail(String email);

    public List<UserResponse> findAllUsers();

    public UserResponse findUserById(Long id);







    public UserResponse createUser(CreateUserRequest request);


    public UserResponse updateUser(UpdateUserRequest request,Long id);


    public void deleteUser(Long id);




}
