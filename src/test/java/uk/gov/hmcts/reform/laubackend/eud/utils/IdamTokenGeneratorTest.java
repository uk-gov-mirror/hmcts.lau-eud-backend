package uk.gov.hmcts.reform.laubackend.eud.utils;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import uk.gov.hmcts.reform.idam.client.models.TokenResponse;
import uk.gov.hmcts.reform.laubackend.eud.exceptions.IdamAuthTokenGenerationException;
import uk.gov.hmcts.reform.laubackend.eud.parameter.ParameterResolver;
import uk.gov.hmcts.reform.laubackend.eud.service.remote.client.IdamClient;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;


class IdamTokenGeneratorTest {

    private static final String TEST_CLIENT_SECRET = "test-client-secret";
    private static final String TEST_CLIENT_ID = "test-client-id";
    private static final String CLIENT_CREDENTIALS = "client_credentials";
    private static final String VIEW_USER = "view-user";
    private static final String TEST_USER = "test-user";
    private static final String TEST_PASS = "test-pass";

    @Mock
    private IdamClient idamClient;

    @Mock
    private ParameterResolver parameterResolver;

    @InjectMocks
    private IdamTokenGenerator idamTokenGenerator;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void shouldGenerateIdamTokenSuccessfully() {
        String accessToken = "mock-access-token";
        TokenResponse tokenResponse = new TokenResponse(
            accessToken,
            null,
            null,
            null,
            null,
            null
        );

        when(parameterResolver.getClientId()).thenReturn(TEST_CLIENT_ID);
        when(parameterResolver.getClientSecret()).thenReturn(TEST_CLIENT_SECRET);
        when(idamClient.getToken(TEST_CLIENT_ID, TEST_CLIENT_SECRET, null,
                                 CLIENT_CREDENTIALS, VIEW_USER))
            .thenReturn(tokenResponse);

        String token = idamTokenGenerator.generateIdamToken();

        assertNotNull(token);
        assertEquals("Bearer " + accessToken, token);
        verify(parameterResolver, times(1)).getClientId();
        verify(parameterResolver, times(1)).getClientSecret();
        verify(idamClient, times(1)).getToken(TEST_CLIENT_ID, TEST_CLIENT_SECRET,
            null, CLIENT_CREDENTIALS, VIEW_USER);
    }

    @Test
    void shouldThrowExceptionWhenTokenGenerationFails() {

        when(parameterResolver.getClientId()).thenReturn(TEST_CLIENT_ID);
        when(parameterResolver.getClientSecret()).thenReturn(TEST_CLIENT_SECRET);
        when(idamClient.getToken(TEST_CLIENT_ID, TEST_CLIENT_SECRET, null,
            CLIENT_CREDENTIALS, VIEW_USER))
            .thenThrow(new RuntimeException("IDAM service error"));

        IdamAuthTokenGenerationException exception = assertThrows(
            IdamAuthTokenGenerationException.class,
            () -> idamTokenGenerator.generateIdamToken()
        );

        assertTrue(exception.getMessage().contains("Unable to generate IDAM token"));
        verify(parameterResolver, times(1)).getClientId();
        verify(parameterResolver, times(1)).getClientSecret();
        verify(idamClient, times(1)).getToken(TEST_CLIENT_ID, TEST_CLIENT_SECRET,
            null, CLIENT_CREDENTIALS, VIEW_USER);
    }

    @Test
    void shouldGenerateRefDataTokenSuccessfully() {
        String accessToken = "mock-refdata-token";
        TokenResponse tokenResponse = new TokenResponse(
            accessToken,
            null,
            null,
            null,
            null,
            null
        );

        when(parameterResolver.getClientId()).thenReturn(TEST_CLIENT_ID);
        when(parameterResolver.getClientSecret()).thenReturn(TEST_CLIENT_SECRET);
        when(parameterResolver.getRedirectUrl()).thenReturn(null);
        when(parameterResolver.getUsername()).thenReturn(TEST_USER);
        when(parameterResolver.getPassword()).thenReturn(TEST_PASS);
        when(idamClient.getRefDataToken(
            TEST_CLIENT_ID,
            TEST_CLIENT_SECRET,
            null,
            IdamTokenGenerator.REF_DATA_GRANT_TYPE,
            IdamTokenGenerator.REF_DATA_SCOPE,
            TEST_USER,
            TEST_PASS
        )).thenReturn(tokenResponse);

        String token = idamTokenGenerator.generateRefDataToken();

        assertNotNull(token);
        assertEquals("Bearer " + accessToken, token);
        verify(parameterResolver).getClientId();
        verify(parameterResolver).getClientSecret();
        verify(parameterResolver).getRedirectUrl();
        verify(parameterResolver).getUsername();
        verify(parameterResolver).getPassword();
        verify(idamClient).getRefDataToken(
            TEST_CLIENT_ID,
            TEST_CLIENT_SECRET,
            null,
            IdamTokenGenerator.REF_DATA_GRANT_TYPE,
            IdamTokenGenerator.REF_DATA_SCOPE,
            TEST_USER,
            TEST_PASS
        );
    }

    @Test
    void shouldThrowExceptionWhenRefDataTokenGenerationFails() {
        when(parameterResolver.getClientId()).thenReturn(TEST_CLIENT_ID);
        when(parameterResolver.getClientSecret()).thenReturn(TEST_CLIENT_SECRET);
        when(parameterResolver.getRedirectUrl()).thenReturn(null);
        when(parameterResolver.getUsername()).thenReturn(TEST_USER);
        when(parameterResolver.getPassword()).thenReturn(TEST_PASS);
        when(idamClient.getRefDataToken(
            TEST_CLIENT_ID,
            TEST_CLIENT_SECRET,
            null,
            IdamTokenGenerator.REF_DATA_GRANT_TYPE,
            IdamTokenGenerator.REF_DATA_SCOPE,
            TEST_USER,
            TEST_PASS
        )).thenThrow(new RuntimeException("RefData service error"));

        IdamAuthTokenGenerationException exception = assertThrows(
            IdamAuthTokenGenerationException.class,
            () -> idamTokenGenerator.generateRefDataToken()
        );

        assertTrue(exception.getMessage().contains("Unable to generate IDAM token"));
        verify(parameterResolver).getClientId();
        verify(parameterResolver).getClientSecret();
        verify(parameterResolver).getRedirectUrl();
        verify(parameterResolver).getUsername();
        verify(parameterResolver).getPassword();
        verify(idamClient).getRefDataToken(
            TEST_CLIENT_ID,
            TEST_CLIENT_SECRET,
            null,
            IdamTokenGenerator.REF_DATA_GRANT_TYPE,
            IdamTokenGenerator.REF_DATA_SCOPE,
            TEST_USER,
            TEST_PASS
        );
    }

}
