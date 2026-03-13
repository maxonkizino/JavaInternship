package com.javainternship.service.impl;


import com.javainternship.dto.request.create.CreateUserRequest;

import com.javainternship.dto.request.update.UpdateUserRequest;
import com.javainternship.dto.response.UserResponse;
import com.javainternship.exception.UserNotFoundException;
import com.javainternship.mapper.UserMapper;
import com.javainternship.model.User;
import com.javainternship.repository.UserRepository;
import com.javainternship.service.interf.UserService;
import jakarta.transaction.Transactional;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@AllArgsConstructor
public class UserServiceImpl implements UserService {

    private UserRepository repository;
    private UserMapper mapper;


    @Override
    public UserResponse findUserByEmail(String email) {
        User user = repository.findByEmail(email).orElseThrow(()->new UserNotFoundException("User not found with email: " + email));
        return mapper.toUserResponse(user);
    }

    @Override
    public List<UserResponse> findAllUsers() {
        List<User> users = repository.findAll();
        return mapper.toUserResponses(users);
    }

    @Override
    public UserResponse findUserById(Long id) {
        User user = repository.findById(id).orElseThrow(()->new UserNotFoundException("User not found"));
        return mapper.toUserResponse(user);
    }

    @Override
    @Transactional
    public UserResponse createUser(CreateUserRequest request) {
        User user = mapper.toUser(request);
        user = repository.save(user);
        return mapper.toUserResponse(repository.save(user));
    }

    @Override
    @Transactional
    public UserResponse updateUser(UpdateUserRequest request, Long id) {
        User user = repository.findById(id).orElseThrow(()->new UserNotFoundException("User not found"));
        mapper.toUser(request,user);
        repository.save(user);
        return mapper.toUserResponse(user);
    }

    @Override
    @Transactional
    public void deleteUser(Long id) {
        repository.deleteById(id);
    }
}
