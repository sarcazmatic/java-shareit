package ru.practicum.shareit.comment.dto;

import lombok.*;
import ru.practicum.shareit.utility.Create;
import ru.practicum.shareit.utility.Update;

import javax.validation.constraints.NotBlank;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CommentDtoRequest {

    @NotBlank(groups = {Create.class, Update.class})
    private String text;

}
