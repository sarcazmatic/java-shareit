package ru.practicum.shareit.request.service;

import ru.practicum.shareit.request.dto.ItemRequestDtoRequest;
import ru.practicum.shareit.request.dto.ItemRequestDtoResponse;

import java.util.List;

public interface ItemRequestService {

    ItemRequestDtoResponse createItemRequest(Long userId, ItemRequestDtoRequest itemRequestDto);

    List<ItemRequestDtoResponse> getAllMyItemRequest(Long userId);

    ItemRequestDtoResponse getItemRequestById(Long userId, Long itemRequestId);

    List<ItemRequestDtoResponse> findAll(Long userId, int from, int size);

}
