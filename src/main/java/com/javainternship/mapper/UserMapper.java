package com.javainternship.mapper;


import com.javainternship.dto.request.create.CreateUserRequest;
import com.javainternship.dto.request.update.UpdateUserRequest;
import com.javainternship.dto.response.UserResponse;
import com.javainternship.model.User;
import org.mapstruct.*;

import java.util.List;

@Mapper(componentModel = "spring")
public interface UserMapper {



    public User toUser(CreateUserRequest request);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    public void toUser(UpdateUserRequest request, @MappingTarget User user);

    public UserResponse toUserResponse(User user);

    public List<UserResponse> toUserResponses(List<User> requests);
}
