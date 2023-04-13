package ru.practicum.shareit.item.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.exceptions.NotFoundException;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.mapper.ItemMapper;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.repository.ItemRepository;
import ru.practicum.shareit.user.repository.UserRepository;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ItemServiceImpl implements ItemService {

    private final ItemRepository itemRepository;
    private final UserRepository userRepository;

    @Override
    public ItemDto create(long userId, ItemDto itemDto) {
        Item item = ItemMapper.fromItemDtoToItem(itemDto);

        if (!userRepository.checkExistId(userId)) {
            throw new NotFoundException("Пользователь с таким id не найден");
        } else {
            itemRepository.create(item, userId);
            return ItemMapper.toItemDto(item);
        }
    }

    @Override
    public ItemDto getById(long id) {
        if (!itemRepository.checkItemIdExist(id)) {
            throw new NotFoundException("Предмета с таким id не найден");
        } else {
            return ItemMapper.toItemDto(itemRepository.getById(id));
        }
    }

    @Override
    public List<ItemDto> getAll(long userId) {
        List<Item> items = itemRepository.getAll(userId);

        return items.stream()
                .map(item -> ItemMapper.toItemDto(item))
                .collect(Collectors.toList());
    }

    @Override
    public ItemDto update(long userId, ItemDto itemDto, long itemId) {
        Item itemToUpdate = itemRepository.getById(itemId);

        if (!userRepository.checkExistId(userId)) {
            throw new NotFoundException("Пользователь с таким id не найден");
        }
        if (itemToUpdate.getOwner() != null && itemToUpdate.getOwner().getId() != userId) {
            throw new NotFoundException("Нет подходящего предмета для пользователя с таким id");
        }

        if (itemDto.getName() != null) {
            itemToUpdate.setName(itemDto.getName());
        }
        if (itemDto.getDescription() != null) {
            itemToUpdate.setDescription(itemDto.getDescription());
        }
        if (itemDto.getAvailable() != null) {
            itemToUpdate.setAvailable(itemDto.getAvailable());
        }
        if (itemDto.getRequest() != null) {
            itemToUpdate.setRequest(itemDto.getRequest());
        }
        return ItemMapper.toItemDto(itemToUpdate);
    }

    @Override
    public List<ItemDto> getSearchResults(String text) {
        List<Item> items = itemRepository.getSearchResults(text);

        return items.stream()
                .map(item -> ItemMapper.toItemDto(item))
                .collect(Collectors.toList());
    }
}
