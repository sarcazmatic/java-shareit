package ru.practicum.shareit.comment.mapper;

import lombok.experimental.UtilityClass;
import ru.practicum.shareit.comment.dto.CommentDtoRequest;
import ru.practicum.shareit.comment.dto.CommentDtoResponse;
import ru.practicum.shareit.comment.model.Comment;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.model.User;

import java.time.LocalDateTime;

@UtilityClass
public class CommentMapper {

    public CommentDtoResponse toCommentDto(Comment comment) {
        return new CommentDtoResponse(
                comment.getId(),
                comment.getText(),
                comment.getUser().getName(),
                comment.getCreated()
        );
    }

    public Comment toComment(CommentDtoRequest commentDtoRequest, Item item, User user, LocalDateTime created) {
        return Comment.builder()
                .text(commentDtoRequest.getText())
                .created(created)
                .item(item)
                .user(user)
                .build();
    }
}
