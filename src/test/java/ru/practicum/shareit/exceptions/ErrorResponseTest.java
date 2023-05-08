package ru.practicum.shareit.exceptions;

import org.junit.jupiter.api.Test;
import static org.hamcrest.MatcherAssert.*;
import static org.hamcrest.Matchers.*;

class ErrorResponseTest {

    @Test
    void createErrorResponse_errorAsString() {
        String error = "test";
        ErrorResponse eR = new ErrorResponse(error);
        assertThat(eR.getError(), equalTo(error));
    }

}
