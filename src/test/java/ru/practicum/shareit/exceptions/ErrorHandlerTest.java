package ru.practicum.shareit.exceptions;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.practicum.shareit.user.service.UserService;



import static org.hamcrest.MatcherAssert.*;
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.assertThrows;

@ExtendWith(MockitoExtension.class)
class ErrorHandlerTest {

    @Mock
    ErrorHandler errorHandler;

    @Mock
    ErrorResponse errorResponse;

    @Mock
    private UserService userService;

    @Test
    void getUserNotFound_throwNotFoundException() {
        Long userId = 100L;
        Mockito
                .when(userService.getUserById(Mockito.anyLong()))
                .thenThrow(new NotFoundException("Пользователь не найден"));

        final NotFoundException exception = assertThrows(NotFoundException.class, () -> {
            userService.getUserById(userId);
        });

        assertThat(exception.getMessage(), equalTo("Пользователь не найден"));
    }

    @Test
    void tryPostUser_getUserExists_throwConflictExc() {
        Mockito
                .when(userService.createUser(Mockito.any()))
                .thenThrow(new ConflictException("Пользователь уже существует"));

        final ConflictException exception = assertThrows(ConflictException.class, () -> {
            userService.createUser(Mockito.any());
        });

        assertThat(exception.getMessage(), equalTo("Пользователь уже существует"));
    }

    @Test
    void tryPostUser_getInvalidFields_throwValidationException() {
        Mockito
                .when(userService.createUser(Mockito.any()))
                .thenThrow(new ValidationException("Параметры не прошли проверку валидации"));

        final ValidationException exception = assertThrows(ValidationException.class, () -> {
            userService.createUser(Mockito.any());
        });

        assertThat(exception.getMessage(), equalTo("Параметры не прошли проверку валидации"));
    }

    @Test
    void direct_NotFoundCheck() {
        final NotFoundException notFoundException = new NotFoundException("Пользователь не найден");
        Mockito
                .when(errorHandler.handleNotFoundException(notFoundException))
                .thenReturn(new ErrorResponse(notFoundException.getMessage()));

        assertThat(errorHandler.handleNotFoundException(notFoundException).getError(), equalTo("Пользователь не найден"));
    }

    @Test
    void direct_ConflictCheck() {
        final ConflictException conflictException = new ConflictException("Конфликт");
        Mockito
                .when(errorHandler.handleConflictException(conflictException))
                .thenReturn(new ErrorResponse(conflictException.getMessage()));

        assertThat(errorHandler.handleConflictException(conflictException).getError(), equalTo("Конфликт"));
    }

    @Test
    void direct_UnhandledCheck() {
        final Throwable t = new Throwable("Внезапное исключение");
        Mockito
                .when(errorHandler.handleUnhandledExceptions(t))
                .thenReturn(new ErrorResponse(t.getMessage()));

        assertThat(errorHandler.handleUnhandledExceptions(t).getError(), equalTo("Внезапное исключение"));
    }

    @Test
    void direct_ValidationCheck() {
        final ValidationException v = new ValidationException("Не прошла валидация");
        Mockito
                .when(errorHandler.handleValidationException(v))
                .thenReturn(new ErrorResponse(v.getMessage()));

        assertThat(errorHandler.handleValidationException(v).getError(), equalTo("Не прошла валидация"));
    }

    @Test
    void direct_NotFoundException_ErrorResponse() {
        errorResponse = new ErrorResponse("Тест");
        NotFoundException notFoundException = new NotFoundException("Тест");
        ErrorResponse errorResponse1 = new ErrorResponse(notFoundException.getMessage());
        assertThat(errorResponse.getError(), equalTo("Тест"));
        assertThat(errorResponse1.getError(), equalTo("Тест"));
        assertThat(notFoundException.getMessage(), equalTo("Тест"));
    }

}
