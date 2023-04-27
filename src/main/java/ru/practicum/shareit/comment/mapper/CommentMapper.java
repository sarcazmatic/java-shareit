package ru.practicum.shareit.comment.mapper;

import lombok.experimental.UtilityClass;
import ru.practicum.shareit.comment.dto.CommentDtoRequest;
import ru.practicum.shareit.comment.dto.CommentDtoResponse;
import ru.practicum.shareit.comment.model.Comment;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.model.User;

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

    public Comment toComment(CommentDtoRequest commentDtoRequest, Item item, User user) {
        return Comment.builder()
                .id(commentDtoRequest.getId())
                .text(commentDtoRequest.getText())
                .item(item)
                .user(user)
                .created(commentDtoRequest.getCreated())
                .build();
    }
}
