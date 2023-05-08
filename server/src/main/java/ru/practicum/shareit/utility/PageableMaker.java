package ru.practicum.shareit.utility;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import ru.practicum.shareit.exceptions.ValidationException;

public class PageableMaker {
    public static Pageable makePageable(Integer from, Integer size, Sort sort) {
        if (from == null || size == null) {
            return null;
        }

        if (from < 0 || size <= 0) {
            throw new ValidationException("Неправильно указанны параметры для просмотра!");
        }

        return PageRequest.of(from / size, size, sort);
    }
}