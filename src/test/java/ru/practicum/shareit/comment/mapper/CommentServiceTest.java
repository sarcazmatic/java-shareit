package ru.practicum.shareit.comment.mapper;

import org.junit.jupiter.api.Test;
import ru.practicum.shareit.booking.enums.BookingStatus;
import ru.practicum.shareit.booking.enums.State;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.comment.dto.CommentDtoResponse;
import ru.practicum.shareit.comment.model.Comment;
import ru.practicum.shareit.item.dto.ItemDtoWithBooking;
import ru.practicum.shareit.item.mapper.ItemMapper;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.mapper.UserMapper;
import ru.practicum.shareit.user.model.User;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertNotNull;

public class CommentServiceTest {

    @Test
    void TestItemMapperTest(){
        User user16 = new User(16L, "user16", "user16@mail.ru");
        Item item16 = new Item(16L, "item16", "description16", true, user16, null);
        Comment comment = new Comment(1L, "hi", item16, user16, LocalDateTime.now().minusDays(1));
        CommentDtoResponse commentDtoResponse = CommentMapper.toCommentDto(comment);

        assertNotNull(commentDtoResponse);
    }

}
