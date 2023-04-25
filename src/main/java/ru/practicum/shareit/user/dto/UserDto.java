package ru.practicum.shareit.user.dto;

import lombok.Builder;
import lombok.Data;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotNull;

@Data
@Builder
public class UserDto {
    private long id;
    @NotNull
    private String name;
    @NotNull
    @Email
    private String email;
}
