package ru.practicum.shareit.utility;

import org.junit.jupiter.api.Test;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import ru.practicum.shareit.exceptions.ValidationException;

import static org.hamcrest.MatcherAssert.*;
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.assertThrows;

class PageableMakerTest {

    @Test
    void createMakerTest() {
        PageableMaker pageableMaker = new PageableMaker();
        assertThat(pageableMaker, notNullValue());
    }

    @Test
    void makePageable_whenInvoked_thenReturnPageable() {
        Integer from = 0;
        Integer size = 1;
        Sort sort = Sort.by(Sort.Direction.ASC, "id");

        Pageable pageable = PageableMaker.makePageable(from, size, sort);
        assertThat(pageable, notNullValue());
    }

    @Test
    void makePageable_whenFromIsNull_thenReturnNull() {
        Integer from = null;
        Integer size = 1;
        Sort sort = Sort.by(Sort.Direction.ASC, "id");

        Pageable pageable = PageableMaker.makePageable(from, size, sort);
        assertThat(pageable, nullValue());
    }

    @Test
    void makePageable_whenSizeIsZero_thenValidationExceptionThrown() {
        Integer from = 0;
        Integer size = 0;
        Sort sort = Sort.by(Sort.Direction.ASC, "id");

        ValidationException validationException = assertThrows(ValidationException.class,
                () -> PageableMaker.makePageable(from, size, sort));
        assertThat(validationException.getMessage(), equalTo("Неправильно указанны параметры для просмотра!"));
    }

    @Test
    void makePageable_whenFromIsNegative_thenValidationExceptionThrown() {
        Integer from = -2;
        Integer size = 4;
        Sort sort = Sort.by(Sort.Direction.ASC, "id");

        ValidationException validationException = assertThrows(ValidationException.class,
                () -> PageableMaker.makePageable(from, size, sort));
        assertThat(validationException.getMessage(), equalTo("Неправильно указанны параметры для просмотра!"));
    }
}
