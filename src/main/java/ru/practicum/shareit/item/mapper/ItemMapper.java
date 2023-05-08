package ru.practicum.shareit.item.mapper;

import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;

import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.comment.model.Comment;
import ru.practicum.shareit.item.dto.ItemDtoRequest;
import ru.practicum.shareit.item.dto.ItemDtoResponse;
import ru.practicum.shareit.item.dto.ItemDtoWithBooking;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.request.model.ItemRequest;

import java.util.List;
import java.util.stream.Collectors;

@UtilityClass
@Slf4j
public class ItemMapper {

    public ItemDtoResponse toItemDto(Item item) {
        Long requestId = (item.getRequest() == null ? null : item.getRequest().getId());

        log.info("Собираем вещь в ДТО");
        return ItemDtoResponse.builder()
                .id(item.getId())
                .name(item.getName())
                .description(item.getDescription())
                .requestId(requestId)
                .available(item.getAvailable()).build();
    }

    public ItemDtoRequest toItemDtoReq(Item item) {
        Long requestId = (item.getRequest() == null ? null : item.getRequest().getId());

        log.info("Собираем вещь в ДТО");
        return ItemDtoRequest.builder()
                .id(item.getId())
                .name(item.getName())
                .description(item.getDescription())
                .requestId(requestId)
                .available(item.getAvailable()).build();
    }

    public Item toItem(ItemDtoResponse itemDtoResponse) {

        Item item = new Item();
        if (itemDtoResponse.getRequestId() != null) {
            ItemRequest itemRequest = new ItemRequest();
            itemRequest.setId(itemDtoResponse.getRequestId());
            item.setRequest(itemRequest);
        }
        item.setId(itemDtoResponse.getId());
        item.setName(itemDtoResponse.getName());
        item.setDescription(itemDtoResponse.getDescription());
        item.setAvailable(itemDtoResponse.getAvailable());
        return item;
    }

    public Item toItemTemp(ItemDtoRequest itemDtoRequest) {
        Item item = new Item();
        if (itemDtoRequest.getRequestId() != null) {
            ItemRequest itemRequest = new ItemRequest();
            itemRequest.setId(itemDtoRequest.getRequestId());
            item.setRequest(itemRequest);
        }

        log.info("Собираем вещь из ДТО");
        return Item.builder()
                .id(itemDtoRequest.getId())
                .name(itemDtoRequest.getName())
                .description(itemDtoRequest.getDescription())
                .available(itemDtoRequest.getAvailable())
                .request(item.getRequest())
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
