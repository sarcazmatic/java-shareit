package ru.practicum.shareit.item.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.comment.dto.CommentDtoRequest;
import ru.practicum.shareit.item.ItemClient;
import ru.practicum.shareit.item.dto.ItemDtoRequest;
import ru.practicum.shareit.utility.Create;
import ru.practicum.shareit.utility.Update;

import javax.validation.ValidationException;


@Controller
@RequestMapping(path = "/items")
@RequiredArgsConstructor
@Slf4j
@Validated
public class ItemController {
    private final ItemClient itemClient;

    @GetMapping
    public ResponseEntity<Object> getListItemByUserId(@RequestHeader("X-Sharer-User-Id") Long userId,
                                                      @RequestParam(required = false, defaultValue = "0") Integer from,
                                                      @RequestParam(required = false, defaultValue = "10") Integer size) {
        log.info("Get all items by users id = {}", userId);
        return itemClient.getListItemByUserId(userId, from, size);
    }

    @GetMapping("/{itemId}")
    public ResponseEntity<Object> getItemById(@RequestHeader("X-Sharer-User-Id") Long userId,
                                              @PathVariable Long itemId) {
        log.info("Get item by id {}", itemId);
        return itemClient.getItemById(userId, itemId);
    }

    @PostMapping
    public ResponseEntity<Object> createItem(@RequestHeader("X-Sharer-User-Id") Long userId, @Validated({Create.class}) @RequestBody ItemDtoRequest itemDtoRequest) {
        log.info("Creating item {}, itemId={}", itemDtoRequest);
        return itemClient.createItem(userId, itemDtoRequest);
    }

    @PostMapping("/{itemId}/comment")
    public ResponseEntity<Object> addComment(@RequestHeader("X-Sharer-User-Id") Long userId,
                                         @PathVariable Long itemId,
                                         @Validated({Create.class}) @RequestBody CommentDtoRequest commentDtoRequest) {
        return itemClient.addComment(itemId, userId, commentDtoRequest);
    }

    @PatchMapping("/{itemId}")
    public ResponseEntity<Object> updateItem(@RequestHeader("X-Sharer-User-Id") Long userId,
                                             @PathVariable Long itemId,
                                             @Validated({Update.class}) @RequestBody ItemDtoRequest itemDtoRequest) {
        log.info("Partial update for item {}", itemDtoRequest);
        return itemClient.updateItem(userId, itemId, itemDtoRequest);
    }

    @GetMapping("/search")
    public ResponseEntity<Object> searchItem(@RequestHeader("X-Sharer-User-Id") Long userId,
                                            @RequestParam(defaultValue = "") String text,
                                            @RequestParam(required = false, defaultValue = "0") Integer from,
                                            @RequestParam(required = false, defaultValue = "10") Integer size) {
            return itemClient.searchItem(userId, text, from, size);
    }

}
