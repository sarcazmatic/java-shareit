package ru.practicum.shareit.request.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.request.dto.ItemRequestDtoRequest;
import ru.practicum.shareit.request.dto.ItemRequestDtoResponse;
import ru.practicum.shareit.request.service.ItemRequestService;

import java.util.List;


@RestController
@RequestMapping(path = "/requests")
@RequiredArgsConstructor
public class ItemRequestController {

    private final ItemRequestService itemRequestService;

    public static final String USER_ID = "X-Sharer-User-Id";

    @PostMapping
    public ItemRequestDtoResponse createItemRequest(@RequestHeader(USER_ID) Long userId,
                                                    @RequestBody ItemRequestDtoRequest itemRequestDto) {
        return itemRequestService.createItemRequest(userId, itemRequestDto);
    }

    @GetMapping()
    public List<ItemRequestDtoResponse> getAllMyItemRequest(@RequestHeader(USER_ID) Long userId) {
        return itemRequestService.getAllMyItemRequest(userId);
    }

    @GetMapping("/{id}")
    public ItemRequestDtoResponse getItemRequestById(@RequestHeader(USER_ID) long userId, @PathVariable Long id) {
        return itemRequestService.getItemRequestById(userId, id);
    }

    @GetMapping("/all")
    public List<ItemRequestDtoResponse> getAllRequestsForUser(@RequestHeader(USER_ID) Long userId,
                                                             @RequestParam(required = false, defaultValue = "0") Integer from,
                                                             @RequestParam(required = false, defaultValue = "10") Integer size) {
        return itemRequestService.findAll(userId, from, size);
    }
}
