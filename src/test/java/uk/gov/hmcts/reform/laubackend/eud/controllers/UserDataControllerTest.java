package uk.gov.hmcts.reform.laubackend.eud.controllers;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.ResponseEntity;
import uk.gov.hmcts.reform.laubackend.eud.dto.UserDataGetRequestParams;
import uk.gov.hmcts.reform.laubackend.eud.response.UserDataResponse;
import uk.gov.hmcts.reform.laubackend.eud.service.UserDataService;

import java.util.ArrayList;
import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class UserDataControllerTest {

    @Mock
    private UserDataService userDataService;

    @InjectMocks
    private UserDataController userDataController;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void shouldReturnUserDataWhenRequestIsValid() {
        UserDataResponse expectedResponse = new UserDataResponse(
            "1234",
            "test@test.com",
            "ACTIVE",
            null,
            new ArrayList<String>(Arrays.asList("role1", "role2")),
            null,
            null
        );

        UserDataGetRequestParams requestParams = new UserDataGetRequestParams("1234","test@test.com");
        String authToken = "Bearer valid-token";

        when(userDataService.getUserData(requestParams)).thenReturn(expectedResponse);

        ResponseEntity<UserDataResponse> response = userDataController.getUserData(authToken, requestParams);

        assertNotNull(response);
        assertEquals(200, response.getStatusCode().value());
        assertEquals(expectedResponse, response.getBody());
        verify(userDataService, times(1)).getUserData(requestParams);
    }
}
