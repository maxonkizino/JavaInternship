package com.javainternship.mapper;


import com.javainternship.dto.request.create.CreateUserRequest;
import com.javainternship.dto.request.update.UpdateUserRequest;
import com.javainternship.dto.response.UserResponse;
import com.javainternship.model.User;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

import java.util.List;

@Mapper(componentModel = "spring")
public interface UserMapper {



    public User toUser(CreateUserRequest request);

    public void toUser(UpdateUserRequest request, @MappingTarget User user);

    public UserResponse toUserResponse(User user);

    public List<UserResponse> toUserResponses(List<User> requests);
}
