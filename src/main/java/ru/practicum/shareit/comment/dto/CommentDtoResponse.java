package ru.practicum.shareit.comment.dto;

import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@AllArgsConstructor
@Builder
public class CommentDtoResponse {

    private Long id;
    private String text;
    private String authorName;
    private LocalDateTime created;
}
