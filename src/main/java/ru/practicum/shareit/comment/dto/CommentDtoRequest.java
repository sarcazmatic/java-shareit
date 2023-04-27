package ru.practicum.shareit.comment.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import ru.practicum.shareit.utility.Create;
import ru.practicum.shareit.utility.Update;

import javax.validation.constraints.NotBlank;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class CommentDtoRequest {

    private Long id;
    @NotBlank(groups = {Create.class, Update.class})
    private String text;
    private String authorName;
    private LocalDateTime created;
}
