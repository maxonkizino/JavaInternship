package com.javainternship.service.impl;

import com.javainternship.dto.request.update.UpdateUserRequest;
import com.javainternship.dto.response.UserResponse;
import com.javainternship.exception.UserNotFoundException;
import com.javainternship.mapper.UserMapper;
import com.javainternship.model.User;
import com.javainternship.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ActiveProfiles("test")
class UserServiceImplTest {

    @Mock
    private UserRepository repository;
    @Mock
    private UserMapper mapper;

    @InjectMocks
    private UserServiceImpl service;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void findUserByEmail_returnsDto() {
        String email = "test@example.com";
        User user = new User();
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
}