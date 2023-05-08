package ru.practicum.shareit.item.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.booking.enums.BookingStatus;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.repository.BookingRepository;
import ru.practicum.shareit.comment.dto.CommentDtoRequest;
import ru.practicum.shareit.comment.dto.CommentDtoResponse;
import ru.practicum.shareit.comment.mapper.CommentMapper;
import ru.practicum.shareit.comment.model.Comment;
import ru.practicum.shareit.comment.repository.CommentRepository;
import ru.practicum.shareit.exceptions.NotFoundException;
import ru.practicum.shareit.exceptions.ValidationException;
import ru.practicum.shareit.item.dto.ItemDtoRequest;
import ru.practicum.shareit.item.dto.ItemDtoResponse;
import ru.practicum.shareit.item.dto.ItemDtoWithBooking;
import ru.practicum.shareit.item.mapper.ItemMapper;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.repository.ItemRepository;
import ru.practicum.shareit.request.model.ItemRequest;
import ru.practicum.shareit.request.repository.ItemRequestRepository;
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

    private final ItemRepository itemRepository;
    private final ItemRequestRepository itemRequestRepository;
    private final UserRepository userRepository;
    private final BookingRepository bookingDbStorage;
    private final CommentRepository commentDbStorage;

    @Override
    public List<ItemDtoWithBooking> getListItemByUserId(Long userId, Pageable pageable) {
        Map<Item, List<Comment>> commentsMap = commentDbStorage.findAllByItemIdIn(itemRepository.findAllIdByOwnerId(userId, pageable), Sort.by(Sort.Direction.DESC, "Created"))
                .stream()
                .collect(Collectors.groupingBy(c -> c.getItem()));

        Map<Item, List<Booking>> bookingsMap = bookingDbStorage.findAllByItemIdInAndStatus(itemRepository.findAllIdByOwnerId(userId, pageable), BookingStatus.APPROVED, Sort.by(Sort.Direction.DESC, "Start"))
                .stream()
                .collect(Collectors.groupingBy(b -> b.getItem()));

        List<Item> items = itemRepository.findAllByOwnerId(userId, pageable);

        return items.stream()
                .map(item -> addBookingsAndComments(item, bookingsMap.getOrDefault(item, List.of()), commentsMap.getOrDefault(item, List.of())))
                .collect(Collectors.toList());
    }

    @Override
    public ItemDtoWithBooking getItemById(Long itemId, Long userId) {
        Item item = itemRepository.findById(itemId)
                .orElseThrow(() -> new NotFoundException(String.format("Вещь с ID %s не найдена", itemId)));
        List<Comment> commentList = getCommentsByItemId(item);

        if (userId.equals(item.getOwner().getId())) {
            LocalDateTime ldtNow = LocalDateTime.now();
            Booking lastBooking;
            Booking nextBooking;
            Optional<Booking> lastBookingOpt = bookingDbStorage.findFirstByItemIdAndEndBeforeAndStatusOrderByEndDesc(itemId, ldtNow, BookingStatus.APPROVED);
            if (lastBookingOpt.isEmpty()) {
                lastBooking = bookingDbStorage.findFirstByItemIdAndEndAfterAndStatusOrderByEndDesc(itemId, ldtNow, BookingStatus.APPROVED);
            } else {
                lastBooking = lastBookingOpt.get();
            }
            Optional<Booking> nextBookingOpt = bookingDbStorage
                    .findTopByItemIdAndStartAfterAndStatusOrderByStartAsc(itemId, ldtNow, BookingStatus.APPROVED);
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
    public ItemDtoResponse createItem(Long userId, ItemDtoRequest itemDtoRequest) {
        Item item = ItemMapper.toItemTemp(itemDtoRequest);

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException(String.format("Пользователь с ID %s не найден", userId)));
        item.setOwner(user);

        if (itemDtoRequest.getRequestId() != null) {
            ItemRequest itemRequest = itemRequestRepository.findById(itemDtoRequest.getRequestId())
                    .orElseThrow(() -> new NotFoundException("Запрос не существует!"));
            item.setRequest(itemRequest);
        }

        log.info("Создали вещь с ID {}", item.getId());
        return ItemMapper.toItemDto(itemRepository.save(item));
    }

    @Transactional
    @Override
    public ItemDtoResponse updateItem(Long userId, Long itemId, ItemDtoResponse itemDtoResponse) {
        Optional<Item> updatedItemOpt = itemRepository.findById(itemId);
        Item updatedItem;
        if (updatedItemOpt.isEmpty()) {
            throw new NotFoundException(String.format("Предмет с ID %s не найден", itemId));
        } else {
            updatedItem = updatedItemOpt.get();
        }
        Item item = ItemMapper.toItem(itemDtoResponse);
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

        return ItemMapper.toItemDto(itemRepository.save(updatedItem));
    }

    @Override
    public List<ItemDtoResponse> searchItem(String text, Pageable pageable) {
        String lowerText = text.toLowerCase();
        return itemRepository.searchForItemWithText(lowerText, pageable)
                .stream()
                .map(ItemMapper::toItemDto)
                .collect(toList());
    }

    @Transactional
    @Override
    public CommentDtoResponse addComment(Long itemId, Long userId, CommentDtoRequest commentDtoRequest) {
        LocalDateTime ldtNow = LocalDateTime.now();
        Optional<User> user = userRepository.findById(userId);
        Optional<Item> item = itemRepository.findById(itemId);

        if (user.isEmpty())
            throw new NotFoundException(String.format("Пользователь с ID %s не найден", userId));
        if (item.isEmpty())
            throw new NotFoundException(String.format("Предмет с ID %s не найден", itemId));

        Optional<Comment> commentOpt = Optional.of(CommentMapper.toComment(commentDtoRequest, item.get(), user.get(), ldtNow));
        Comment comment;

        if (commentOpt.get().getText().isEmpty()) {
            throw new NotFoundException("Комментарий не найден");
        } else {
            comment = commentOpt.get();
        }

        List<Booking> booking = bookingDbStorage.findByBookerIdStatePast(comment.getUser().getId(),
                ldtNow);

        if (booking.isEmpty()) {
            throw new ValidationException("Не найдено брони у этого пользователя");
        }

        comment.setCreated(ldtNow);
        commentDbStorage.save(comment);
        log.info("Создан комментарий с ID {}", comment.getId());

        return CommentMapper.toCommentDto(comment);
    }

    public List<Comment> getCommentsByItemId(Item item) {
        return commentDbStorage.findByItemIdOrderByCreatedDesc(item.getId());
    }

    private static ItemDtoWithBooking addBookingsAndComments(Item item, List<Booking> bookings, List<Comment> comments) {
        LocalDateTime ldtNow = LocalDateTime.now();
        Booking lastBooking = bookings.stream()
                .filter(b -> !b.getStart().isAfter(ldtNow))
                .findFirst()
                .orElse(null);
        Booking nextBooking = bookings.stream()
                .filter(b -> !b.getStart().isBefore(ldtNow))
                .reduce((first, second) -> second).orElse(null);
        List<Comment> itemComments = comments.stream()
                .filter(comment -> comment.getItem().equals(item))
                .collect(Collectors.toList());

        return ItemMapper.toItemDtoWithBooking(itemComments, lastBooking, nextBooking, item);
    }
}
