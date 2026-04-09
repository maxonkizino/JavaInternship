package com.javainternship.service.impl;

import com.javainternship.dto.request.create.CreateUserRequest;
import com.javainternship.dto.request.update.UpdateUserRequest;
import com.javainternship.dto.response.UserResponse;
import com.javainternship.exception.UserNotFoundException;
import com.javainternship.mapper.UserMapper;
import com.javainternship.model.User;
import com.javainternship.repository.UserRepository;
import com.javainternship.security.SecurityUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ActiveProfiles("test")
class UserServiceImplTest {

    @Mock
    private UserRepository repository;
    @Mock
    private UserMapper mapper;

    @Mock
    private SecurityUtils securityUtils;

    @InjectMocks
    private UserServiceImpl service;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        when(securityUtils.isCurrentUserAdmin()).thenReturn(true);
        when(securityUtils.getCurrentUserId()).thenReturn(1L);
        when(securityUtils.isOwnerOrAdmin(anyLong())).thenReturn(true);
    }

    @Test
    void findUserByEmail_returnsDto() {
        String email = "test@example.com";
        User user = new User();
        user.setId(1L);
        UserResponse response = new UserResponse();

        when(repository.findOne((Specification<User>) any())).thenReturn(Optional.of(user));
        when(mapper.toUserResponse(user)).thenReturn(response);

        UserResponse result = service.findUserByEmail(email);

        assertThat(result).isSameAs(response);
        verify(repository).findOne((Specification<User>) any());
        verify(mapper).toUserResponse(user);
    }

    @Test
    void findUserByEmail_throwsWhenNotFound() {
        when(repository.findOne((Specification<User>) any())).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.findUserByEmail("missing@example.com"))
                .isInstanceOf(UserNotFoundException.class);
    }

    @Test
    void findAllUsers_returnsListOfDtos() {
        List<User> users = List.of(new User());
        List<UserResponse> responses = List.of(new UserResponse());

        when(repository.findAll((Specification<User>) any())).thenReturn(users);
        when(mapper.toUserResponses(users)).thenReturn(responses);

        List<UserResponse> result = service.findAllUsers();

        assertThat(result).isEqualTo(responses);
        verify(repository).findAll((Specification<User>) any());
        verify(mapper).toUserResponses(users);
    }

    @Test
    void searchUsers_returnsPageOfDtos() {
        List<User> users = List.of(new User());
        Page<User> page = new PageImpl<>(users);
        UserResponse response = new UserResponse();

        when(repository.findAll((Specification<User>) any(), any(Pageable.class))).thenReturn(page);
        when(mapper.toUserResponse(any(User.class))).thenReturn(response);

        Page<UserResponse> result = service.searchUsers("John", "Doe", Pageable.unpaged());

        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0)).isSameAs(response);
        verify(repository).findAll((Specification<User>) any(), eq(Pageable.unpaged()));
    }

    @Test
    void findUserById_returnsDto() {
        User user = new User();
        UserResponse response = new UserResponse();

        when(repository.findOne((Specification<User>) any())).thenReturn(Optional.of(user));
        when(mapper.toUserResponse(user)).thenReturn(response);

        UserResponse result = service.findUserById(1L);

        assertThat(result).isSameAs(response);
    }


    @Test
    void updateUser_updatesExisting() {
        UpdateUserRequest request = new UpdateUserRequest();
        User existing = new User();
        UserResponse response = new UserResponse();

        when(repository.findOne((Specification<User>) any())).thenReturn(Optional.of(existing));
        doNothing().when(mapper).toUser(request, existing);
        when(repository.save(existing)).thenReturn(existing);
        when(mapper.toUserResponse(existing)).thenReturn(response);

        UserResponse result = service.updateUser(request, 1L);

        assertThat(result).isSameAs(response);
        verify(mapper).toUser(request, existing);
        verify(repository).save(existing);
    }

    @Test
    void deleteUser_deletesById() {
        User user = new User();
        user.setActive(true);
        when(repository.findOne((Specification<User>) any())).thenReturn(Optional.of(user));
        when(repository.save(user)).thenReturn(user);

        service.deleteUser(1L);

        assertThat(user.isActive()).isFalse();
        verify(repository).findOne((Specification<User>) any());
        verify(repository).save(user);
    }

    @Test
    void activateUser_setsActiveTrue() {
        User user = new User();
        user.setActive(false);

        when(repository.findById(1L)).thenReturn(Optional.of(user));
        when(repository.save(user)).thenReturn(user);

        service.activateUser(1L);

        assertThat(user.isActive()).isTrue();
        verify(repository).save(user);
    }

    @Test
    void deactivateUser_setsActiveFalse() {
        User user = new User();
        user.setActive(true);

        when(repository.findById(1L)).thenReturn(Optional.of(user));
        when(repository.save(user)).thenReturn(user);

        service.deactivateUser(1L);

        assertThat(user.isActive()).isFalse();
        verify(repository).save(user);
    }

    @Test
    void activateUser_throwsWhenNotFound() {
        when(repository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.activateUser(999L))
                .isInstanceOf(UserNotFoundException.class);
    }

    @Test
    void createUserThroughGateway_savesActiveUser() {
        CreateUserRequest request = new CreateUserRequest();
        User mapped = new User();
        mapped.setActive(false);
        User saved = new User();
        saved.setId(5L);
        saved.setActive(true);
        UserResponse response = new UserResponse();

        when(mapper.toUser(request)).thenReturn(mapped);
        when(repository.save(mapped)).thenReturn(saved);
        when(mapper.toUserResponse(saved)).thenReturn(response);

        UserResponse result = service.createUserThroughGateway(request);

        assertThat(mapped.isActive()).isTrue();
        assertThat(result).isSameAs(response);
        verify(repository).save(mapped);
        verify(mapper).toUserResponse(saved);
    }

    @Test
    void rollbackGatewayRegistration_setsUserInactive() {
        User user = new User();
        user.setId(7L);
        user.setActive(true);
        when(repository.findById(7L)).thenReturn(Optional.of(user));
        when(repository.save(user)).thenReturn(user);

        service.rollbackGatewayRegistration(7L);

        assertThat(user.isActive()).isFalse();
        verify(repository).findById(7L);
        verify(repository).save(user);
    }

    @Test
    void rollbackGatewayRegistration_throwsWhenUserMissing() {
        when(repository.findById(404L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.rollbackGatewayRegistration(404L))
                .isInstanceOf(UserNotFoundException.class);
        verify(repository).findById(404L);
        verify(repository, never()).save(any(User.class));
    }

    private void asRegularUser(long userId) {
        reset(securityUtils);
        when(securityUtils.isCurrentUserAdmin()).thenReturn(false);
        when(securityUtils.getCurrentUserId()).thenReturn(userId);
        when(securityUtils.isOwnerOrAdmin(anyLong())).thenAnswer(inv -> {
            Long id = inv.getArgument(0);
            return id != null && id.equals(userId);
        });
    }

    @Test
    void findUserById_throwsAccessDenied_whenNotOwner() {
        asRegularUser(10L);

        assertThatThrownBy(() -> service.findUserById(99L))
                .isInstanceOf(AccessDeniedException.class);
        verify(repository, never()).findOne((Specification<User>) any());
    }

    @Test
    void findUserByEmail_throwsAccessDenied_whenNotOwnerOfProfile() {
        asRegularUser(10L);
        User user = new User();
        user.setId(99L);
        when(repository.findOne((Specification<User>) any())).thenReturn(Optional.of(user));

        assertThatThrownBy(() -> service.findUserByEmail("other@example.com"))
                .isInstanceOf(AccessDeniedException.class);
        verify(mapper, never()).toUserResponse(any());
    }

    @Test
    void searchUsers_throwsAccessDenied_whenNonAdminAndCurrentUserIdMissing() {
        reset(securityUtils);
        when(securityUtils.isCurrentUserAdmin()).thenReturn(false);
        when(securityUtils.getCurrentUserId()).thenReturn(null);

        assertThatThrownBy(() -> service.searchUsers(null, null, Pageable.unpaged()))
                .isInstanceOf(AccessDeniedException.class);
        verify(repository, never()).findAll((Specification<User>) any(), any(Pageable.class));
    }

    @Test
    void searchUsers_nonAdmin_queriesRepository() {
        asRegularUser(10L);
        Page<User> page = new PageImpl<>(List.of());
        when(repository.findAll((Specification<User>) any(), any(Pageable.class))).thenReturn(page);

        service.searchUsers("n", "s", Pageable.unpaged());

        verify(repository).findAll((Specification<User>) any(), any(Pageable.class));
    }

    @Test
    void createUser_throwsAccessDenied_whenNotAdmin() {
        asRegularUser(10L);

        assertThatThrownBy(() -> service.createUser(new CreateUserRequest()))
                .isInstanceOf(AccessDeniedException.class);
        verify(repository, never()).save(any());
    }

    @Test
    void findAllUsers_throwsAccessDenied_whenNotAdmin() {
        asRegularUser(10L);

        assertThatThrownBy(() -> service.findAllUsers())
                .isInstanceOf(AccessDeniedException.class);
        verify(repository, never()).findAll((Specification<User>) any());
    }

    @Test
    void updateUser_throwsAccessDenied_whenNotOwner() {
        asRegularUser(10L);

        assertThatThrownBy(() -> service.updateUser(new UpdateUserRequest(), 99L))
                .isInstanceOf(AccessDeniedException.class);
        verify(repository, never()).findOne((Specification<User>) any());
    }

    @Test
    void deleteUser_throwsAccessDenied_whenNotOwner() {
        asRegularUser(10L);

        assertThatThrownBy(() -> service.deleteUser(99L))
                .isInstanceOf(AccessDeniedException.class);
        verify(repository, never()).findOne((Specification<User>) any());
    }

    @Test
    void activateUser_throwsAccessDenied_whenNotAdmin() {
        asRegularUser(10L);

        assertThatThrownBy(() -> service.activateUser(1L))
                .isInstanceOf(AccessDeniedException.class);
        verify(repository, never()).findById(anyLong());
    }

    @Test
    void deactivateUser_throwsAccessDenied_whenNotAdmin() {
        asRegularUser(10L);

        assertThatThrownBy(() -> service.deactivateUser(1L))
                .isInstanceOf(AccessDeniedException.class);
        verify(repository, never()).findById(anyLong());
    }
}