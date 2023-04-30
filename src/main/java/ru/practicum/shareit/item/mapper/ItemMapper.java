package ru.practicum.shareit.item.mapper;

import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;

import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.comment.model.Comment;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.dto.ItemDtoWithBooking;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.model.User;

import java.util.List;
import java.util.stream.Collectors;

@UtilityClass
@Slf4j
public class ItemMapper {

    public ItemDto toItemDto(Item item) {

        log.info("Собираем вещь в ДТО");
        return ItemDto.builder()
                .id(item.getId())
                .name(item.getName())
                .description(item.getDescription())
                .available(item.getAvailable()).build();
    }

    public Item toItem(ItemDto itemDto, User user) {

        log.info("Собираем вещь из ДТО");
        return Item.builder()
                .id(itemDto.getId())
                .name(itemDto.getName())
                .description(itemDto.getDescription())
                .available(itemDto.getAvailable())
                .owner(user)
                .build();
    }

    public Item toItemTemp(ItemDto itemDto) {

        log.info("Собираем вещь из ДТО");
        return Item.builder()
                .id(itemDto.getId())
                .name(itemDto.getName())
                .description(itemDto.getDescription())
                .available(itemDto.getAvailable())
                .build();
    }

    public ItemDtoWithBooking toItemDtoWithBooking(List<Comment> commentList, Booking lastBooking,
                                                          Booking nextBooking, Item item) {

        List<ItemDtoWithBooking.Comment> comments = commentList.stream()
                .map(comment -> {
                    ItemDtoWithBooking.Comment comment1 = new ItemDtoWithBooking.Comment();
                    comment1.setId(comment.getId());
                    comment1.setText(comment.getText());
                    comment1.setAuthorName(comment.getUser().getName());
                    comment1.setCreated(comment.getCreated());
                    return comment1;
                }).collect(Collectors.toList());

        ItemDtoWithBooking.Booking lstBooking = new ItemDtoWithBooking.Booking();
        if (lastBooking != null) {
            lstBooking.setId(lastBooking.getId());
            lstBooking.setBookerId(lastBooking.getBooker().getId());
        } else {
            lstBooking = null;
        }

        ItemDtoWithBooking.Booking nextBooking1 = new ItemDtoWithBooking.Booking();
        if (nextBooking != null) {
            nextBooking1.setId(nextBooking.getId());
            nextBooking1.setBookerId(nextBooking.getBooker().getId());
        } else {
            nextBooking1 = null;
        }

        return ItemDtoWithBooking.builder()
                .id(item.getId())
                .name(item.getName())
                .description(item.getDescription())
                .available(item.getAvailable())
                .lastBooking(lstBooking)
                .nextBooking(nextBooking1)
                .comments(comments)
                .build();
    }

}
