package ru.practicum.shareit.item.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import ru.practicum.shareit.booking.enums.BookingStatus;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.repository.BookingRepository;
import ru.practicum.shareit.comment.dto.CommentDtoRequest;
import ru.practicum.shareit.comment.dto.CommentDtoResponse;
import ru.practicum.shareit.exceptions.NotFoundException;
import ru.practicum.shareit.exceptions.ValidationException;
import ru.practicum.shareit.item.dto.*;
import ru.practicum.shareit.comment.model.Comment;
import ru.practicum.shareit.item.mapper.ItemMapper;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.comment.repository.CommentRepository;
import ru.practicum.shareit.item.repository.ItemRepository;
import ru.practicum.shareit.request.model.ItemRequest;
import ru.practicum.shareit.request.repository.ItemRequestRepository;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.mapper.UserMapper;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.repository.UserRepository;
import ru.practicum.shareit.utility.PageableMaker;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.assertThrows;

@ExtendWith(MockitoExtension.class)
class ItemServiceTest {
    @Mock
    private ItemRepository itemRepository;
    @Mock
    private UserRepository userRepository;
    @Mock
    private ItemRequestRepository requestRepository;
    @Mock
    private BookingRepository bookingRepository;
    @Mock
    private CommentRepository commentRepository;
    @Captor
    private ArgumentCaptor<Item> itemArgumentCaptor;
    @Captor
    private ArgumentCaptor<Comment> commentArgumentCaptor;
    @InjectMocks
    private ItemServiceImpl itemService;
    private ItemDtoResponse itemDto;
    private UserDto userDto;
    private CommentDtoResponse commentDto;
    private ItemRequest itemRequest;
    private Item item;
    private User user;
    private Comment comment;
    private Booking booking1;
    private Booking booking2;

    @BeforeEach
    void setUp() {
        userDto = UserDto.builder()
                .id(1L)
                .name("userName")
                .email("name@mail.com")
                .build();
        itemDto = ItemDtoResponse.builder()
                .id(1L)
                .name("item name")
                .description("item description")
                .available(false)
                .build();
        user = UserMapper.fromDtoToUser(userDto);
        item = ItemMapper.toItem(itemDto);
        commentDto = CommentDtoResponse.builder()
                .id(1L)
                .text("comment text")
                .authorName("Vasya")
                .build();
        itemRequest = ItemRequest.builder()
                .id(1L)
                .description("request description")
                .build();
        comment = Comment.builder()
                .id(1L)
                .text("asdasd")
                .item(item)
                .user(user)
                .created(LocalDateTime.now())
                .build();
        booking1 = Booking.builder()
                .id(1L)
                .start(LocalDateTime.now().plusDays(1))
                .end(LocalDateTime.now().plusDays(2))
                .item(item)
                .booker(user)
                .status(BookingStatus.WAITING)
                .build();
        booking2 = Booking.builder()
                .id(2L)
                .start(LocalDateTime.now().minusDays(2))
                .end(LocalDateTime.now().minusDays(1))
                .item(item)
                .booker(user)
                .status(BookingStatus.WAITING)
                .build();
    }

    @Test
    void save_whenUserNotFound_thenNotFoundExceptionThrown() {
        long userId = 1L;
        Mockito.when(userRepository.findById(userId))
                .thenReturn(Optional.empty());


        NotFoundException exception = assertThrows(NotFoundException.class,
                () -> itemService.createItem(userId, ItemMapper.toItemDtoReq(ItemMapper.toItem(itemDto))));
        assertThat(exception.getMessage(), equalTo("Пользователь с ID 1 не найден"));
    }

    @Test
    void save_whenRequestNotFound_thenNotFoundExceptionThrown() {
        long userId = 1L;
        User testUser = User.builder()
                .name("TestName")
                .build();
        itemDto.setRequestId(1L);
        Mockito.when(userRepository.findById(userId))
                .thenReturn(Optional.of(testUser));
        Mockito.when(requestRepository.findById(itemDto.getRequestId()))
                .thenReturn(Optional.empty());


        NotFoundException exception = assertThrows(NotFoundException.class,
                () -> itemService.createItem(userId, ItemMapper.toItemDtoReq(ItemMapper.toItem(itemDto))));
        assertThat(exception.getMessage(), equalTo("Запрос не существует!"));
    }

    @Test
    void getById_whenItemNotFound_thenNotFoundExceptionThrown() {
        long userId = 1L;
        long itemId = 1L;
        User user = UserMapper.fromDtoToUser(userDto);
        Item item = ItemMapper.toItem(itemDto);
        item.setOwner(user);
        Mockito.when(itemRepository.findById(userId))
                .thenReturn(Optional.empty());

        NotFoundException exception = assertThrows(NotFoundException.class,
                () -> itemService.getItemById(itemId, userId));
        assertThat(exception.getMessage(), equalTo("Вещь с ID 1 не найдена"));
    }

    @Test
    void getAll_whenInvoked_thenReturnListOfOneItemDtoResponse() {
        long ownerId = 1L;
        int from = 0;
        int size = 5;
        Pageable pageable = PageableMaker.makePageable(from, size, Sort.by(Sort.Direction.ASC, "id"));
        Mockito.when(itemRepository.findAllByOwnerId(Mockito.anyLong(), Mockito.any(Pageable.class)))
                .thenReturn(Collections.singletonList(item));

        List<ItemDtoWithBooking> returnedList = itemService.getListItemByUserId(ownerId, pageable);

        assertThat(returnedList, hasSize(1));
        assertThat(returnedList.get(0).getId(), is(1L));
    }

    @Test
    void update_whenItemNotFound_thenNotFoundExceptionThrown() {
        long userId = 1L;
        long itemId = 1L;
        itemDto.setName("BeforeUpdate");
        itemDto.setDescription("BeforeUpdate");
        ItemDtoResponse itemWithUpdates = ItemDtoResponse.builder()
                .name("UpdatedName")
                .description("UpdatedDescription")
                .build();
        Mockito.when(itemRepository.findById(itemId))
                .thenReturn(Optional.empty());


        NotFoundException exception = assertThrows(NotFoundException.class,
                () -> itemService.updateItem(userId, itemId, itemWithUpdates));
        assertThat(exception.getMessage(), equalTo("Предмет с ID 1 не найден"));
    }

    @Test
    void update_whenItemDtoWithWrongOwner_thenNotFoundExceptionThrown() {
        long userId = 10L;
        long itemId = 1L;
        item.setName("BeforeUpdate");
        item.setDescription("BeforeUpdate");
        item.setOwner(user);
        ItemDtoResponse itemWithUpdates = ItemDtoResponse.builder()
                .name("UpdatedName")
                .description("UpdatedDescription")
                .build();

        NotFoundException exception = assertThrows(NotFoundException.class,
                () -> itemService.updateItem(userId, itemId, itemWithUpdates));
        assertThat(exception.getMessage(), equalTo("Предмет с ID 1 не найден"));
    }

    @Test
    void getSearchResults_whenInvoked_thenReturnListWithOneItem() {
        String text = "aSd";
        int from = 0;
        int size = 5;
        Pageable pageable = PageableMaker.makePageable(from, size, Sort.by(Sort.Direction.ASC, "id"));
        Mockito.when(itemRepository.searchItem(Mockito.anyString(), Mockito.any(Pageable.class)))
                .thenReturn(Collections.singletonList(item));

        List<ItemDtoResponse> searchResults = itemService.searchItem(text, pageable);
        assertThat(searchResults, hasSize(1));
    }

    @Test
    void postComment_whenStatusWrong_thenValidationExceptionThrown() {
        long userId = 1L;
        long itemId = 1L;
        commentDto.setText("testText");
        CommentDtoRequest cdr = CommentDtoRequest.builder().text(comment.getText()).build();
        Mockito.when(userRepository.findById(userId))
                .thenReturn(Optional.of(user));
        Mockito.when(itemRepository.findById(itemId))
                .thenReturn(Optional.of(item));


        ValidationException exception = assertThrows(ValidationException.class,
                () -> itemService.addComment(userId, itemId, cdr));
        assertThat(exception.getMessage(), equalTo("Не найдено брони у этого пользователя"));
    }

    @Test
    void postComment_whenUserNotFound_thenNotFoundExceptionThrown() {
        long userId = 1L;
        long itemId = 1L;
        commentDto.setText("testText");
        CommentDtoRequest cdr = CommentDtoRequest.builder().text(comment.getText()).build();
        Mockito.when(userRepository.findById(userId))
                .thenReturn(Optional.empty());


        NotFoundException exception = assertThrows(NotFoundException.class,
                () -> itemService.addComment(userId, itemId, cdr));
        assertThat(exception.getMessage(), equalTo("Пользователь с ID 1 не найден"));
    }

    @Test
    void postComment_whenItemNotFound_thenNotFoundExceptionThrown() {
        long userId = 1L;
        long itemId = 1L;
        commentDto.setText("testText");
        CommentDtoRequest cdr = CommentDtoRequest.builder().text(comment.getText()).build();
        Mockito.when(userRepository.findById(userId))
                .thenReturn(Optional.of(user));
        Mockito.when(itemRepository.findById(itemId))
                .thenReturn(Optional.empty());


        NotFoundException exception = assertThrows(NotFoundException.class,
                () -> itemService.addComment(userId, itemId, cdr));
        assertThat(exception.getMessage(), equalTo("Предмет с ID 1 не найден"));
    }

    @Test
    void postComment_whenBookingNotFound_thenNotFoundExceptionThrown() {
        long itemId = 1L;
        commentDto.setText("testText");
        CommentDtoRequest comment = CommentDtoRequest.builder()
                .text("testText")
                .build();
        Mockito.when(userRepository.findById(Mockito.anyLong()))
                .thenReturn(Optional.of(user));


        NotFoundException exception = assertThrows(NotFoundException.class,
                () -> itemService.addComment(Mockito.anyLong(), itemId, comment));
        assertThat(exception.getMessage(), equalTo("Предмет с ID 0 не найден"));
    }
}
