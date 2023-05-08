package ru.practicum.shareit.request.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.exceptions.NotFoundException;
import ru.practicum.shareit.exceptions.ValidationException;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.repository.ItemRepository;
import ru.practicum.shareit.request.dto.ItemRequestDtoRequest;
import ru.practicum.shareit.request.dto.ItemRequestDtoResponse;
import ru.practicum.shareit.request.mapper.ItemRequestMapper;
import ru.practicum.shareit.request.model.ItemRequest;
import ru.practicum.shareit.request.repository.ItemRequestRepository;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.repository.UserRepository;
import ru.practicum.shareit.utility.PageableMaker;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
public class ItemRequestServiceImpl implements ItemRequestService {

    private final ItemRequestRepository itemRequestRepository;
    private final UserRepository userRepository;
    private final ItemRepository itemRepository;

    @Override
    public ItemRequestDtoResponse createItemRequest(Long userId, ItemRequestDtoRequest itemRequestDtoRequest) {
        LocalDateTime ldt = LocalDateTime.now();

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException(String.format("Пользователь с ID %s не найден", userId)));
        ItemRequest itemRequest = ItemRequestMapper.toItemRequest(itemRequestDtoRequest, user);
        validate(itemRequest);
        itemRequest.setCreated(ldt);

        log.info("Create ItemRequest with ID {}", itemRequest.getId());
        itemRequestRepository.save(itemRequest);
        return ItemRequestMapper.toItemRequestDtoResponse(itemRequest, null);
    }

    @Override
    public List<ItemRequestDtoResponse> getAllMyItemRequest(Long userId) {
        userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException(String.format("Пользователь с ID %s не найден", userId)));

        Map<Long, List<Item>> requestItemMap = extractItemsToRequests();
        return itemRequestRepository.findAllByRequester_IdOrderByCreatedAsc(userId)
                .stream()
                .map(itemRequest
                        -> ItemRequestMapper.toItemRequestDtoResponse(itemRequest, requestItemMap.get(itemRequest.getId())))
                .collect(Collectors.toList());
    }

    @Override
    public ItemRequestDtoResponse getItemRequestById(Long userId, Long itemRequestId) {
        userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException(String.format("Пользователь с таким ID не найден")));

        ItemRequest itemRequest = itemRequestRepository.findById(itemRequestId)
                .orElseThrow(() -> new NotFoundException(String.format("Запрос не существует!")));
        List<Item> items = itemRepository.findAllByRequest_Id(itemRequest.getId(), Sort.by("id").descending());

        return ItemRequestMapper.toItemRequestDtoResponse(itemRequest, items);
    }

    @Override
    public List<ItemRequestDtoResponse> findAll(Long userId, int from, int size) {
        Pageable pageable = PageableMaker.makePageable(from, size, Sort.by("created").ascending());
        Map<Long, List<Item>> requestItemMap = extractItemsToRequests();
        return itemRequestRepository.findByRequester_IdNot(userId, pageable)
                .stream()
                .map(itemRequest
                        -> ItemRequestMapper.toItemRequestDtoResponse(itemRequest, requestItemMap.get(itemRequest.getId())))
                .collect(Collectors.toList());
    }

    private Map<Long, List<Item>> extractItemsToRequests() {
        Map<Long, List<Item>> requestItemMap = new HashMap<>();
        List<Item> itemList = itemRepository.findAllByRequestIsPresent();
        List<ItemRequest> itemRequestList = itemRequestRepository.findAll();
        for (ItemRequest itemRequest : itemRequestList) {
            List<Item> itemsToAdd = new ArrayList<>();
            for (Item item : itemList) {
                if (item.getRequest().getId().equals(itemRequest.getId()))
                    itemsToAdd.add(item);
            }
            requestItemMap.put(itemRequest.getId(), itemsToAdd);
        }
        return requestItemMap;
    }

    private void validate(ItemRequest itemRequest) {
        if (itemRequest.getDescription() == null || itemRequest.getDescription().isBlank()) {
            throw new ValidationException("Описание не должно быть пустым");
        }
    }

}
