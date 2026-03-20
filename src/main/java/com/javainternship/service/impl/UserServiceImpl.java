package com.javainternship.service.impl;


import com.javainternship.dto.request.create.CreateUserRequest;
import com.javainternship.dto.request.update.UpdateUserRequest;
import com.javainternship.dto.response.UserResponse;
import com.javainternship.exception.UserNotFoundException;
import com.javainternship.mapper.UserMapper;
import com.javainternship.model.User;
import com.javainternship.model.specification.UserSpecification;
import com.javainternship.repository.UserRepository;
import com.javainternship.service.UserService;
import lombok.AllArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


import java.util.List;

@Service
@AllArgsConstructor
public class UserServiceImpl implements UserService {
    private static final String USER_NOT_FOUND = "User not found";

    private final UserRepository repository;
    private final UserMapper mapper;


    @Override
    @Cacheable(cacheNames = "usersByEmail", key = "#email")
    public UserResponse findUserByEmail(String email) {
        Specification<User> spec = Specification
                .where(UserSpecification.hasEmail(email))
                .and(UserSpecification.isActive());

        User user = repository.findOne(spec)
                .orElseThrow(() -> new UserNotFoundException("User not found with email: " + email));
        return mapper.toUserResponse(user);
    }

    @Override
    public List<UserResponse> findAllUsers() {
        Specification<User> spec = Specification
                .where(UserSpecification.isActive());

        List<User> users = repository.findAll(spec);
        return mapper.toUserResponses(users);
    }

    @Override
    public Page<UserResponse> searchUsers(String name, String surname, Pageable pageable) {
        Specification<User> spec = Specification
                .where(UserSpecification.isActive())
                .and(UserSpecification.hasName(name))
                .and(UserSpecification.hasSurname(surname));

        Page<User> page = repository.findAll(spec, pageable);
        return page.map(mapper::toUserResponse);
    }

    @Override
    @Cacheable(cacheNames = "usersById", key = "#id")
    public UserResponse findUserById(Long id) {
        Specification<User> spec = Specification
                .where(UserSpecification.hasId(id))
                .and(UserSpecification.isActive());

        User user = repository.findOne(spec)
                .orElseThrow(() -> new UserNotFoundException(USER_NOT_FOUND));
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
        user.setActive(true);
        user = repository.save(user);
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
    public UserResponse updateUser(UpdateUserRequest request, Long id) {
        Specification<User> spec = Specification
                .where(UserSpecification.hasId(id))
                .and(UserSpecification.isActive());

        User user = repository.findOne(spec)
                .orElseThrow(() -> new UserNotFoundException(USER_NOT_FOUND));
        mapper.toUser(request,user);
        repository.save(user);
        return mapper.toUserResponse(user);
    }

    @Override
    @Transactional
    @Caching(
            evict = {
                    @CacheEvict(cacheNames = "usersByEmail", allEntries = true),
                    @CacheEvict(cacheNames = "usersById", key = "#id"),
                    // User active state affects visibility of user's cards.
                    // Must evict card caches too to avoid returning soft-deleted data from Redis.
                    @CacheEvict(cacheNames = "cardsByUserId", allEntries = true),
                    @CacheEvict(cacheNames = "cardsByNumber", allEntries = true),
                    @CacheEvict(cacheNames = "cardsById", allEntries = true)
            }
    )
    public void deleteUser(Long id) {
        User user = repository.findById(id)
                .orElseThrow(() -> new UserNotFoundException(USER_NOT_FOUND));
        user.setActive(false);
        repository.save(user);
    }

    @Override
    @Transactional
    @Caching(
            evict = {
                    @CacheEvict(cacheNames = "usersByEmail", allEntries = true),
                    @CacheEvict(cacheNames = "usersById", key = "#id"),
                    @CacheEvict(cacheNames = "cardsByUserId", allEntries = true),
                    @CacheEvict(cacheNames = "cardsByNumber", allEntries = true),
                    @CacheEvict(cacheNames = "cardsById", allEntries = true)
            }
    )
    public void activateUser(Long id) {
        User user = repository.findById(id)
                .orElseThrow(() -> new UserNotFoundException(USER_NOT_FOUND));
        user.setActive(true);
        repository.save(user);
    }

    @Override
    @Transactional
    @Caching(
            evict = {
                    @CacheEvict(cacheNames = "usersByEmail", allEntries = true),
                    @CacheEvict(cacheNames = "usersById", key = "#id"),
                    @CacheEvict(cacheNames = "cardsByUserId", allEntries = true),
                    @CacheEvict(cacheNames = "cardsByNumber", allEntries = true),
                    @CacheEvict(cacheNames = "cardsById", allEntries = true)
            }
    )
    public void deactivateUser(Long id) {
        User user = repository.findById(id)
                .orElseThrow(() -> new UserNotFoundException(USER_NOT_FOUND));
        user.setActive(false);
        repository.save(user);
    }
}
