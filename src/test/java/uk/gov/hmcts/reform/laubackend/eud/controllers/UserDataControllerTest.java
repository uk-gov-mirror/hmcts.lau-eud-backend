package uk.gov.hmcts.reform.laubackend.eud.controllers;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.http.ResponseEntity;
import uk.gov.hmcts.reform.laubackend.eud.domain.EventType;
import uk.gov.hmcts.reform.laubackend.eud.dto.UserDataGetRequestParams;
import uk.gov.hmcts.reform.laubackend.eud.dto.UserUpdate;
import uk.gov.hmcts.reform.laubackend.eud.response.UserDataResponse;
import uk.gov.hmcts.reform.laubackend.eud.response.UserUpdatesResponse;
import uk.gov.hmcts.reform.laubackend.eud.service.UserDataService;
import uk.gov.hmcts.reform.laubackend.eud.service.UserUpdatesService;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class UserDataControllerTest {

    private static final String USER_ID = "1234";

    @Mock
    private UserDataService userDataService;

    @Mock
    UserUpdatesService userUpdatesService;

    @InjectMocks
    private UserDataController userDataController;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void shouldReturnUserDataWhenRequestIsValid() {
        UserDataResponse expectedResponse = new UserDataResponse(
            USER_ID,
            "test@test.com",
            "Test",
            "User",
            "ACTIVE",
            null,
            null,
            new ArrayList<String>(Arrays.asList("role1", "role2")),
            null,
            null
        );

        UserDataGetRequestParams requestParams = new UserDataGetRequestParams(USER_ID, "test@test.com");
        String authToken = "Bearer valid-token";

        when(userDataService.getUserData(requestParams)).thenReturn(expectedResponse);

        ResponseEntity<UserDataResponse> response = userDataController.getUserData(authToken, requestParams);

        assertNotNull(response);
        assertEquals(200, response.getStatusCode().value());
        assertEquals(expectedResponse, response.getBody());
        verify(userDataService, times(1)).getUserData(requestParams);
    }

    @Test
    void shouldCallUserUpdatesService() {
        UserUpdate update = new UserUpdate("event", EventType.ADD, "val", null, "principal", "prevVal");
        Page<UserUpdate> page = new PageImpl<>(List.of(update));
        UserDataGetRequestParams requestParams = new UserDataGetRequestParams(USER_ID, null);
        when(userUpdatesService.getUserUpdates(USER_ID, null)).thenReturn(page);

        ResponseEntity<UserUpdatesResponse> response = userDataController.getUserAccountUpdates(
            "",
            requestParams,
            null
        );

        verify(userUpdatesService, times(1)).getUserUpdates(requestParams.getUserId(), null);
        assertThat(response.getStatusCode().value()).isEqualTo(200);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().content()).hasSize(1);
    }

    @Test
    void shouldReturn500OnError_whenUserUpdatesServiceThrowsException() {
        UserDataGetRequestParams requestParams = new UserDataGetRequestParams(USER_ID, null);
        when(userUpdatesService.getUserUpdates(requestParams.getUserId(), null))
            .thenThrow(new UnsupportedOperationException());

        ResponseEntity<UserUpdatesResponse> response = userDataController.getUserAccountUpdates(
            "",
            requestParams,
            null
        );
        assertThat(response.getStatusCode().value()).isEqualTo(500);
    }
}
