package ru.practicum.shareit.item.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.enums.BookingStatus;
import ru.practicum.shareit.booking.repository.BookingRepository;
import ru.practicum.shareit.comment.dto.CommentDtoRequest;
import ru.practicum.shareit.comment.model.Comment;
import ru.practicum.shareit.exceptions.NotFoundException;
import ru.practicum.shareit.exceptions.ValidationException;
import ru.practicum.shareit.comment.mapper.CommentMapper;
import ru.practicum.shareit.item.mapper.ItemMapper;
import ru.practicum.shareit.comment.dto.CommentDtoResponse;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.dto.ItemDtoWithBooking;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.comment.repository.CommentRepository;
import ru.practicum.shareit.item.repository.ItemRepository;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.repository.UserRepository;

import javax.transaction.Transactional;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.toList;

@Service
@Slf4j
@RequiredArgsConstructor
public class ItemServiceImpl implements ItemService {

    private final ItemRepository itemDbStorage;
    private final UserRepository userDbStorage;
    private final BookingRepository bookingDbStorage;
    private final CommentRepository commentDbStorage;

    @Override
    public List<ItemDtoWithBooking> getListItemByUserId(Long userId) {
        Map<Item, List<Comment>> commentsMap = commentDbStorage.findAllByItemIn(itemDbStorage.findAllByOwnerIdOrderById(userId), Sort.by(Sort.Direction.DESC, "Created"))
                .stream()
                .collect(Collectors.groupingBy(c -> c.getItem())); //делаем группы по итемам, которые в поле итем в комменте
        //тут мы делаем мапу – Итем-Комменты к итему
        Map<Item, List<Booking>> bookingsMap = bookingDbStorage.findAllByItemInAndStatus(itemDbStorage.findAllByOwnerIdOrderById(userId), BookingStatus.APPROVED, Sort.by(Sort.Direction.DESC, "Start"))
                .stream()
                .collect(Collectors.groupingBy(b -> b.getItem()));

        List<Item> items = itemDbStorage.findAllByOwnerIdOrderById(userId);

        return items.stream()
                .map(item -> addBookingsAndComments(item, bookingsMap.getOrDefault(item, List.of()), commentsMap.getOrDefault(item, List.of())))
                .collect(Collectors.toList());
    }

    @Override
    public ItemDtoWithBooking getItemById(Long itemId, Long userId) {
        Item item = itemDbStorage.findById(itemId)
                .orElseThrow(() -> new NotFoundException(String.format("Вещь с ID %s не найдена", itemId)));
        List<Comment> commentList = getCommentsByItemId(item);

        if (userId.equals(item.getOwner().getId())) {
            LocalDateTime localDateTime = LocalDateTime.now();
            Booking lastBooking;
            Booking nextBooking;
            Optional<Booking> lastBookingOpt = bookingDbStorage.findFirstByItem_IdAndEndBeforeAndStatusOrderByEndDesc(itemId, localDateTime, BookingStatus.APPROVED);
            if (lastBookingOpt.isEmpty()) {
                lastBooking = bookingDbStorage.findFirstByItem_IdAndEndAfterAndStatusOrderByEndDesc(itemId, localDateTime, BookingStatus.APPROVED);
            } else {
                lastBooking = lastBookingOpt.get();
            }
            Optional<Booking> nextBookingOpt = bookingDbStorage
                    .findTopByItem_IdAndStartAfterAndStatusOrderByStartAsc(itemId, localDateTime, BookingStatus.APPROVED);
            if (nextBookingOpt.isEmpty()) {
                nextBooking = null;
            } else {
                nextBooking = nextBookingOpt.get();
            }
            return ItemMapper.toItemDtoWithBooking(commentList, lastBooking, nextBooking, item);
        } else {
            return ItemMapper.toItemDtoWithBooking(commentList, null, null, item);
        }
    }

    @Transactional
    public ItemDto createItem(Long userId, ItemDto itemDto) {
        Item item = ItemMapper.toItemTemp(itemDto);

        User user = userDbStorage.findById(userId)
                .orElseThrow(() -> new NotFoundException(String.format("Пользователь с ID %s не найден", userId)));
        item.setOwner(user);
        log.info("Создали вещь с ID {}", item.getId());
        return ItemMapper.toItemDto(itemDbStorage.save(item));
    }

    @Transactional
    @Override
    public ItemDto updateItem(Long userId, Long itemId, ItemDto itemDto) {
        Item updatedItem = itemDbStorage.findById(itemId)
                .orElseThrow(() -> new NotFoundException(String.format("Предмет с ID %s не найден", itemId)));
        User user = userDbStorage.findById(userId)
                .orElseThrow(() -> new NotFoundException(String.format("Пользователь с ID %s не найден", userId)));
        Item item = ItemMapper.toItem(itemDto, user);
        if (!updatedItem.getOwner().getId().equals(userId))
            throw new NotFoundException("Предмет не доступен для брони");

        String updatedDescription = item.getDescription();
        if (updatedDescription != null && !updatedDescription.isBlank()) {
            updatedItem.setDescription(updatedDescription);
        }
        String updatedName = item.getName();
        if (updatedName != null && !updatedName.isBlank()) {
            updatedItem.setName(updatedName);
        }
        if (item.getAvailable() != null) {
            updatedItem.setAvailable(item.getAvailable());
        }

        return ItemMapper.toItemDto(itemDbStorage.save(updatedItem));
    }

    @Override
    public List<ItemDto> searchItem(String text) {

        return itemDbStorage.searchItem(text)
                .stream()
                .map(ItemMapper::toItemDto)
                .collect(toList());
    }

    @Transactional
    @Override
    public CommentDtoResponse addComment(Long itemId, Long userId, CommentDtoRequest commentDtoRequest) {

        User user = userDbStorage.findById(userId)
                .orElseThrow(() -> new NotFoundException(String.format("Пользователь с ID %s не найден", userId)));
        Item item = itemDbStorage.findById(itemId)
                .orElseThrow(() -> new NotFoundException(String.format("Предмет с ID %s не найден", itemId)));
        Comment comment = CommentMapper.toComment(commentDtoRequest, item, user);

        List<Booking> booking = bookingDbStorage.findByBookerIdStatePast(comment.getUser().getId(),
                LocalDateTime.now());
        if (booking.isEmpty()) {
            throw new ValidationException("Не найдено брони у этого пользователя");
        }

        comment.setCreated(LocalDateTime.now());
        commentDbStorage.save(comment);
        log.info("Создан комментарий с ID {}", comment.getId());

        return CommentMapper.toCommentDto(comment);
    }

    public List<Comment> getCommentsByItemId(Item item) {
        return commentDbStorage.findByItem_IdOrderByCreatedDesc(item.getId());
    }

    private static ItemDtoWithBooking addBookingsAndComments(Item item, List<Booking> bookings, List<Comment> comments) {
        Booking lastBooking = bookings.stream()
                .filter(b -> !b.getStart().isAfter(LocalDateTime.now()))
                .filter(b -> !b.getEnd().isAfter(LocalDateTime.now()))
                .findFirst()
                .orElse(null);
        Booking nextBooking = bookings.stream()
                .filter(b -> !b.getStart().isBefore(LocalDateTime.now()))
                .reduce((first, second) -> second).orElse(null);
        List<Comment> itemComments = comments.stream()
                .filter(comment -> comment.getItem().equals(item))
                .collect(Collectors.toList());

        return ItemMapper.toItemDtoWithBooking(itemComments, lastBooking, nextBooking, item);
    }
}
