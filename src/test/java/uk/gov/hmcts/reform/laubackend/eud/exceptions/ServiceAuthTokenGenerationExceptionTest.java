package uk.gov.hmcts.reform.laubackend.eud.exceptions;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class ServiceAuthTokenGenerationExceptionTest {

    private static final String TEST_MESSAGE = "Test message";

    @Test
    void shouldSetMessage() {
        ServiceAuthTokenGenerationException exception =
            new ServiceAuthTokenGenerationException(TEST_MESSAGE);

        assertEquals(TEST_MESSAGE, exception.getMessage());
        assertNull(exception.getCause());
    }

    @Test
    void shouldSetMessageAndCause() {
        Throwable cause = new RuntimeException("Cause");
        ServiceAuthTokenGenerationException exception =
            new ServiceAuthTokenGenerationException(TEST_MESSAGE, cause);

        assertEquals(TEST_MESSAGE, exception.getMessage());
        assertEquals(cause, exception.getCause());
    }
}
