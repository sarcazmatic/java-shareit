package ru.practicum.shareit.request.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Sort;
import ru.practicum.shareit.booking.dto.BookingDtoResponse;
import ru.practicum.shareit.booking.enums.BookingStatus;
import ru.practicum.shareit.booking.mapper.BookingMapper;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.comment.dto.CommentDtoResponse;
import ru.practicum.shareit.exceptions.NotFoundException;
import ru.practicum.shareit.item.dto.ItemDtoResponse;
import ru.practicum.shareit.item.mapper.ItemMapper;
import ru.practicum.shareit.comment.model.Comment;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.repository.ItemRepository;
import ru.practicum.shareit.request.dto.ItemRequestDtoRequest;
import ru.practicum.shareit.request.dto.ItemRequestDtoResponse;
import ru.practicum.shareit.request.model.ItemRequest;
import ru.practicum.shareit.request.repository.ItemRequestRepository;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.mapper.UserMapper;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.repository.UserRepository;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasSize;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ItemRequestServiceTest {
    @Mock
    private ItemRequestRepository requestRepository;
    @Mock
    private UserRepository userRepository;
    @Mock
    private ItemRepository itemRepository;
    @Captor
    ArgumentCaptor<ItemRequest> itemRequestArgumentCaptor;
    @InjectMocks
    private ItemRequestServiceImpl itemRequestService;
    private ItemDtoResponse itemDto;
    private UserDto userDto;
    private CommentDtoResponse commentDto;
    private ItemRequest itemRequest;
    private ItemRequestDtoResponse itemRequestDto;
    private ItemRequestDtoRequest itemRequestDtoRequest;

    private Item item;
    private User user;
    private Comment comment;
    private Booking booking;
    private BookingDtoResponse bookingDto;

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
        comment = Comment.builder()
                .id(1L)
                .text("asdasd")
                .item(item)
                .user(user)
                .created(LocalDateTime.now())
                .build();
        booking = Booking.builder()
                .id(1L)
                .start(LocalDateTime.now().plusDays(1))
                .end(LocalDateTime.now().plusDays(2))
                .item(item)
                .booker(user)
                .status(BookingStatus.WAITING)
                .build();
        bookingDto = BookingMapper.toBookingDtoResponse(booking);
        itemRequest = ItemRequest.builder()
                .id(1L)
                .description("request description")
                .build();
        itemRequestDto = ItemRequestDtoResponse.builder()
                .id(1L)
                .description("itemRequest description")
                .created(LocalDateTime.now().plusDays(1))
                .build();
        itemRequestDtoRequest = ItemRequestDtoRequest.builder()
                .id(1L)
                .description("itemRequest description")
                .created(LocalDateTime.now().plusDays(1))
                .build();
    }

    @Test
    void save_whenInvoked_thenSaveItemRequest() {
        long userId = 1L;
        when(userRepository.findById(userId))
                .thenReturn(Optional.of(user));
        itemRequestService.createItemRequest(userId, itemRequestDtoRequest);

        verify(requestRepository).save(itemRequestArgumentCaptor.capture());
        ItemRequest value = itemRequestArgumentCaptor.getValue();
        assertThat(value.getRequester(), equalTo(user));
        assertThat(value.getRequester().getId(), equalTo(1L));
        assertThat(value.getRequester().getName(), equalTo("userName"));
        assertThat(value.getRequester().getEmail(), equalTo("name@mail.com"));
    }

    @Test
    void save_whenItemNotFound_thenNotFoundExceptionThrown() {
        long userId = 1L;
        when(userRepository.findById(userId))
                .thenThrow(new NotFoundException("Пользователь не существует!"));


        NotFoundException exception = assertThrows(NotFoundException.class,
                () -> itemRequestService.createItemRequest(userId, itemRequestDtoRequest));
        assertThat(exception.getMessage(), equalTo("Пользователь не существует!"));
    }

    @Test
    void findAllByRequestorId_whenInvoked_thenGetListOfRequestDto() {
        long userId = 1L;
        ItemRequest itemRequest1 = ItemRequest.builder()
                .id(10L)
                .requester(user)
                .build();
        ItemRequest itemRequest2 = ItemRequest.builder()
                .id(20L)
                .requester(user)
                .build();
        User user3 = User.builder()
                .id(3L)
                .name("userName")
                .email("name@mail.com")
                .build();
        Item item1 = Item.builder()
                .id(2L)
                .owner(user3)
                .request(itemRequest1)
                .build();
        Item item2 = Item.builder()
                .id(3L)
                .owner(user3)
                .request(itemRequest2)
                .build();
        item.setOwner(user3);
        Mockito
                .when(userRepository.findById(userId))
                .thenReturn(Optional.of(user));
        Mockito
                .when(requestRepository.findAllByRequester_IdOrderByCreatedAsc(userId))
                .thenReturn(Arrays.asList(itemRequest1, itemRequest2));



        List<ItemRequestDtoResponse> allByRequesterId = itemRequestService.getAllMyItemRequest(userId);
        assertThat(allByRequesterId, hasSize(2));
        assertThat(allByRequesterId.get(0).getId(), equalTo(10L));
        assertThat(allByRequesterId.get(1).getId(), equalTo(20L));
    }

    @Test
    void findAllByRequestorId_whenUserNotFound_thenNotFoundExceptionThrown() {
        long userId = 1L;
        when(userRepository.findById(userId))
                .thenThrow(new NotFoundException("Пользователь не существует!"));


        NotFoundException exception = assertThrows(NotFoundException.class,
                () -> itemRequestService.getAllMyItemRequest(userId));
        assertThat(exception.getMessage(), equalTo("Пользователь не существует!"));
    }

    @Test
    void findAllByParams_whenInvoked_thenGetListOfRequestDto() {
        long userId = 1L;
        int from = 0;
        int size = 5;
        ItemRequest itemRequest1 = ItemRequest.builder()
                .id(10L)
                .requester(user)
                .build();
        ItemRequest itemRequest2 = ItemRequest.builder()
                .id(20L)
                .requester(user)
                .build();
        User user3 = User.builder()
                .id(3L)
                .name("userName")
                .email("name@mail.com")
                .build();
        Item item1 = Item.builder()
                .id(2L)
                .owner(user3)
                .request(itemRequest1)
                .build();
        Item item2 = Item.builder()
                .id(3L)
                .owner(user3)
                .request(itemRequest2)
                .build();
        item.setOwner(user3);
        List<ItemRequestDtoResponse> allByParams = itemRequestService.findAll(userId, from, size);
        assertThat(allByParams, hasSize(0));
    }

    @Test
    void findAllByParams_whenUserNotFound_thenNotFoundExceptionThrown() {
        long userId = 1000L;
        when(userRepository.findById(userId))
                .thenThrow(new NotFoundException("Пользователь не существует!"));

        NotFoundException exception = assertThrows(NotFoundException.class,
                () -> userRepository.findById(userId));
        assertThat(exception.getMessage(), equalTo("Пользователь не существует!"));
    }

    @Test
    void findById() {
        long userId = 1L;
        long requestId = 1L;
        when(requestRepository.findById(requestId))
                .thenReturn(Optional.of(itemRequest));
        when(userRepository.findById(userId))
                .thenReturn(Optional.of(user));
        when(itemRepository.findAllByRequest_Id(requestId, Sort.by("id").descending()))
                .thenReturn(Collections.singletonList(item));

        ItemRequestDtoResponse result = itemRequestService.getItemRequestById(userId, requestId);
        assertThat(result.getItems(), hasSize(1));
        assertThat(result.getItems().get(0).getDescription(), equalTo("item description"));
        assertThat(result.getItems().get(0).getId(), equalTo(1L));
    }

    @Test
    void findById_whenRequestNotFound_thenNotFoundExceptionThrown() {
        long requestId = 1L;
        when(requestRepository.findById(anyLong()))
                .thenThrow(new NotFoundException("Запрос не существует!"));

        NotFoundException exception = assertThrows(NotFoundException.class,
                () -> requestRepository.findById(requestId));
        assertThat(exception.getMessage(), equalTo("Запрос не существует!"));
    }

    @Test
    void findById_whenUserNotFound_thenNotFoundExceptionThrown() {
        long userId = 1L;
        long requestId = 1L;
        when(userRepository.findById(userId))
                .thenReturn(Optional.empty());

        NotFoundException exception = assertThrows(NotFoundException.class,
                () -> itemRequestService.getItemRequestById(userId, requestId));
        assertThat(exception.getMessage(), equalTo("Пользователь с таким ID не найден"));
    }
}
