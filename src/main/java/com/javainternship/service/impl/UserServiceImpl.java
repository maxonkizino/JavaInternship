package com.javainternship.service.impl;


import com.javainternship.dto.request.create.CreateUserRequest;
import com.javainternship.dto.request.update.UpdateUserRequest;
import com.javainternship.dto.response.UserResponse;
import com.javainternship.exception.UserNotFoundException;
import com.javainternship.mapper.UserMapper;
import com.javainternship.model.User;
import com.javainternship.model.specification.UserSpecification;
import com.javainternship.repository.UserRepository;
import com.javainternship.service.interf.UserService;
import jakarta.transaction.Transactional;
import lombok.AllArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@AllArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository repository;
    private final UserMapper mapper;


    @Override
    @Cacheable(cacheNames = "usersByEmail", key = "#email")
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
    public Page<UserResponse> searchUsers(String name, String surname, Pageable pageable) {
        Specification<User> spec = Specification
                .where(UserSpecification.hasName(name))
                .and(UserSpecification.hasSurname(surname));

        Page<User> page = repository.findAll(spec, pageable);
        return page.map(mapper::toUserResponse);
    }

    @Override
    @Cacheable(cacheNames = "usersById", key = "#id")
    public UserResponse findUserById(Long id) {
        User user = repository.findById(id).orElseThrow(()->new UserNotFoundException("User not found"));
        return mapper.toUserResponse(user);
    }

    @Override
    @Transactional
    @Caching(
            evict = {
                    @CacheEvict(cacheNames = "usersByEmail", allEntries = true),
                    @CacheEvict(cacheNames = "usersById", allEntries = true)
            }
    )
    public UserResponse createUser(CreateUserRequest request) {
        User user = mapper.toUser(request);
        user = repository.save(user);
        return mapper.toUserResponse(repository.save(user));
    }

    @Override
    @Transactional
    @Caching(
            evict = {
                    @CacheEvict(cacheNames = "usersByEmail", allEntries = true),
                    @CacheEvict(cacheNames = "usersById", key = "#id")
            }
    )
    public UserResponse updateUser(UpdateUserRequest request, Long id) {
        User user = repository.findById(id).orElseThrow(()->new UserNotFoundException("User not found"));
        mapper.toUser(request,user);
        repository.save(user);
        return mapper.toUserResponse(user);
    }

    @Override
    @Transactional
    @Caching(
            evict = {
                    @CacheEvict(cacheNames = "usersByEmail", allEntries = true),
                    @CacheEvict(cacheNames = "usersById", key = "#id")
            }
    )
    public void deleteUser(Long id) {
        repository.deleteById(id);
    }

    @Override
    @Transactional
    @Caching(
            evict = {
                    @CacheEvict(cacheNames = "usersByEmail", allEntries = true),
                    @CacheEvict(cacheNames = "usersById", key = "#id")
            }
    )
    public void activateUser(Long id) {
        User user = repository.findById(id)
                .orElseThrow(() -> new UserNotFoundException("User not found"));
        user.setActive(true);
        repository.save(user);
    }

    @Override
    @Transactional
    @Caching(
            evict = {
                    @CacheEvict(cacheNames = "usersByEmail", allEntries = true),
                    @CacheEvict(cacheNames = "usersById", key = "#id")
            }
    )
    public void deactivateUser(Long id) {
        User user = repository.findById(id)
                .orElseThrow(() -> new UserNotFoundException("User not found"));
        user.setActive(false);
        repository.save(user);
    }
}
