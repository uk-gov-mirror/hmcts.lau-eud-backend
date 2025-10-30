package uk.gov.hmcts.reform.laubackend.eud.utils;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.InjectMocks;
import org.mockito.MockitoAnnotations;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.reform.laubackend.eud.exceptions.ServiceAuthTokenGenerationException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class ServiceTokenGeneratorTest {

    @Mock
    private AuthTokenGenerator authTokenGenerator;

    @InjectMocks
    private ServiceTokenGenerator serviceTokenGenerator;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void shouldGenerateServiceTokenSuccessfully() {
        String expectedToken = "service-token";
        when(authTokenGenerator.generate()).thenReturn(expectedToken);

        String token = serviceTokenGenerator.generateServiceToken();

        assertEquals(expectedToken, token);
        verify(authTokenGenerator, times(1)).generate();
    }

    @Test
    void shouldThrowExceptionWhenTokenGenerationFails() {
        when(authTokenGenerator.generate()).thenThrow(new RuntimeException("Auth error"));

        ServiceAuthTokenGenerationException exception = assertThrows(
            ServiceAuthTokenGenerationException.class,
            () -> serviceTokenGenerator.generateServiceToken()
        );

        assertTrue(exception.getMessage().contains("Unable to generate service auth token"));
        verify(authTokenGenerator, times(1)).generate();
    }
}

