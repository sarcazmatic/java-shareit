package ru.practicum.shareit.comment.dto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CommentDtoRequest {

    private String text;

}
