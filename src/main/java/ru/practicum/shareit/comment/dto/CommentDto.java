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
public class CommentDto {

    private Long id;
    @NotBlank(groups = {Create.class, Update.class})
    private String text;
    private String authorName;
    private LocalDateTime created;
}
