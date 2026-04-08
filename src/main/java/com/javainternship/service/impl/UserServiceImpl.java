package com.javainternship.service.impl;


import com.javainternship.dto.request.create.CreateUserRequest;
import com.javainternship.dto.request.update.UpdateUserRequest;
import com.javainternship.dto.response.UserResponse;
import com.javainternship.exception.UserNotFoundException;
import com.javainternship.mapper.UserMapper;
import com.javainternship.model.User;
import com.javainternship.model.specification.UserSpecification;
import com.javainternship.repository.UserRepository;
import com.javainternship.security.SecurityUtils;
import com.javainternship.service.UserService;
import lombok.AllArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Caching;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


import java.util.List;

@Service
@AllArgsConstructor
public class UserServiceImpl implements UserService {
    private static final String USER_NOT_FOUND = "User not found";
    private static final String ACCESS_DENIED_USER = "Access denied to user";
    private static final String ACCESS_DENIED_SEARCH = "Access denied: cannot search other users";
    private static final String ADMIN_ONLY = "Admin only";

    private final UserRepository repository;
    private final UserMapper mapper;
    private final SecurityUtils securityUtils;


    @Override
    @Transactional(readOnly = true)
    public UserResponse findUserByEmail(String email) {
        Specification<User> spec = Specification
                .where(UserSpecification.hasEmail(email))
                .and(UserSpecification.isActive());

        User user = repository.findOne(spec)
                .orElseThrow(() -> new UserNotFoundException("User not found with email: " + email));
        if (!securityUtils.isOwnerOrAdmin(user.getId())) {
            throw new AccessDeniedException(ACCESS_DENIED_USER);
        }
        return mapper.toUserResponse(user);
    }

    @Override
    @Transactional(readOnly = true)
    public List<UserResponse> findAllUsers() {
        requireAdmin();
        Specification<User> spec = Specification
                .where(UserSpecification.isActive());

        List<User> users = repository.findAll(spec);
        return mapper.toUserResponses(users);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<UserResponse> searchUsers(String name, String surname, Pageable pageable) {
        Specification<User> spec = Specification
                .where(UserSpecification.isActive())
                .and(UserSpecification.hasName(name))
                .and(UserSpecification.hasSurname(surname));

        if (!securityUtils.isCurrentUserAdmin()) {
            Long currentUserId = securityUtils.getCurrentUserId();
            if (currentUserId == null) {
                throw new AccessDeniedException(ACCESS_DENIED_SEARCH);
            }
            spec = spec.and(UserSpecification.hasId(currentUserId));
        }

        Page<User> page = repository.findAll(spec, pageable);
        return page.map(mapper::toUserResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public UserResponse findUserById(Long id) {
        if (!securityUtils.isOwnerOrAdmin(id)) {
            throw new AccessDeniedException(ACCESS_DENIED_USER);
        }
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
        requireAdmin();
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
        if (!securityUtils.isOwnerOrAdmin(id)) {
            throw new AccessDeniedException(ACCESS_DENIED_USER);
        }
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
                    @CacheEvict(cacheNames = "cardsByUserId", allEntries = true),
                    @CacheEvict(cacheNames = "cardsByNumber", allEntries = true),
                    @CacheEvict(cacheNames = "cardsById", allEntries = true)
            }
    )
    public void deleteUser(Long id) {
        if (!securityUtils.isOwnerOrAdmin(id)) {
            throw new AccessDeniedException(ACCESS_DENIED_USER);
        }
        Specification<User> spec = Specification
                .where(UserSpecification.hasId(id))
                .and(UserSpecification.isActive());

        User user = repository.findOne(spec)
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
        requireAdmin();
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
        requireAdmin();
        User user = repository.findById(id)
                .orElseThrow(() -> new UserNotFoundException(USER_NOT_FOUND));
        user.setActive(false);
        repository.save(user);
    }

    private void requireAdmin() {
        if (!securityUtils.isCurrentUserAdmin()) {
            throw new AccessDeniedException(ADMIN_ONLY);
        }
    }
}
