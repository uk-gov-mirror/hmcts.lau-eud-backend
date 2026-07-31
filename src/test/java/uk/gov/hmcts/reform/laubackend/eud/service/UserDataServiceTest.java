package uk.gov.hmcts.reform.laubackend.eud.service;

import feign.FeignException;
import feign.Request;
import feign.RequestTemplate;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.ResponseEntity;
import uk.gov.hmcts.reform.laubackend.eud.dto.UserDataGetRequestParams;
import uk.gov.hmcts.reform.laubackend.eud.response.ContactInformationResponse;
import uk.gov.hmcts.reform.laubackend.eud.response.IdamUserResponse;
import uk.gov.hmcts.reform.laubackend.eud.response.OrganisationResponse;
import uk.gov.hmcts.reform.laubackend.eud.response.UserDataResponse;
import uk.gov.hmcts.reform.laubackend.eud.service.remote.client.IdamClient;
import uk.gov.hmcts.reform.laubackend.eud.service.remote.client.RefDataClient;
import uk.gov.hmcts.reform.laubackend.eud.utils.IdamTokenGenerator;
import uk.gov.hmcts.reform.laubackend.eud.utils.ServiceTokenGenerator;

import java.util.List;
import java.util.Map;
import java.util.concurrent.Executor;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.laubackend.eud.service.UserDataService.IDAM;
import static uk.gov.hmcts.reform.laubackend.eud.service.UserDataService.REF_DATA;
import static uk.gov.hmcts.reform.laubackend.eud.service.UserDataService.RESPONSE_CODE_STR;

class UserDataServiceTest {

    private static final String ACTIVE = "ACTIVE";
    private static final String IDAM_TOKEN = "mock-token";
    private static final String REF_DATA_TOKEN = "mock-ref-token";
    private static final String SERVICE_TOKEN = "mock-service-token";
    private static final String ROLE_1 = "role1";
    private static final String ROLE_2 = "role2";
    private static final String RECORD_TYPE = "LIVE";

    @Mock
    private Executor executor;

    @Mock
    private IdamClient idamClient;

    @Mock
    private RefDataClient refDataClient;

    @Mock
    private IdamTokenGenerator idamTokenGenerator;

    @Mock
    private ServiceTokenGenerator serviceTokenGenerator;

    @InjectMocks
    private UserDataService userDataService;

    private String userId = "99999";
    private String email = "test@example.com";
    private String forename = "Test";
    private String surname = "User";
    private UserDataGetRequestParams params;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        doAnswer(invocation -> {
            Runnable r = invocation.getArgument(0);
            r.run();
            return null;
        }).when(executor).execute(any());
        params = mock(UserDataGetRequestParams.class);
        when(params.getUserId()).thenReturn(userId);
        when(params.getEmail()).thenReturn(null);

        when(idamTokenGenerator.generateIdamToken()).thenReturn(IDAM_TOKEN);
        when(idamTokenGenerator.generateRefDataToken()).thenReturn(REF_DATA_TOKEN);
        when(serviceTokenGenerator.generateServiceToken()).thenReturn(SERVICE_TOKEN);
    }

    @Test
    void shouldReturnUserDataByUserId() {
        IdamUserResponse idamUserResponse = createIdamUserResponse(userId);
        ContactInformationResponse contactInfo = new ContactInformationResponse("Org details", "", "", "", "", "", "");
        OrganisationResponse orgResponse = new OrganisationResponse(List.of(contactInfo));

        when(idamClient.getUserDataByUserId(IDAM_TOKEN, userId)).thenReturn(ResponseEntity.ok(idamUserResponse));
        when(refDataClient.getOrganisationDetailsByUserId(REF_DATA_TOKEN, SERVICE_TOKEN, userId))
            .thenReturn(ResponseEntity.ok(orgResponse));

        UserDataResponse actualResponse = userDataService.getUserData(params);

        UserDataResponse expectedAggregated = new UserDataResponse(
            userId,
            email,
            forename,
            surname,
            ACTIVE,
            RECORD_TYPE,
            null,
            List.of(ROLE_1, ROLE_2),
            List.of(contactInfo),
            null
        );

        assertNotNull(actualResponse);
        assertUserDataResponseEquals(expectedAggregated, actualResponse);
        assertMetaDetails(actualResponse, 200, 200);
        verify(idamTokenGenerator, times(1)).generateIdamToken();
        verify(idamTokenGenerator, times(1)).generateRefDataToken();
        verify(serviceTokenGenerator, times(1)).generateServiceToken();
        verify(idamClient, times(1)).getUserDataByUserId(IDAM_TOKEN, userId);
        verify(idamClient, never()).getUserDataByEmail(anyString(), anyString());
        verify(refDataClient, times(1)).getOrganisationDetailsByUserId(REF_DATA_TOKEN,SERVICE_TOKEN, userId);
    }

    @Test
    void shouldReturnUserDataByEmail() {
        when(params.getUserId()).thenReturn(null);
        when(params.getEmail()).thenReturn(email);

        ContactInformationResponse contactInfo = new ContactInformationResponse(
            "Org details",
            "",
            "",
            "",
            "",
            "",
            ""
        );

        IdamUserResponse idamUserResponse = createIdamUserResponse("14567");
        OrganisationResponse orgResponse = new OrganisationResponse(List.of(contactInfo));

        when(idamClient.getUserDataByEmail(IDAM_TOKEN, email)).thenReturn(ResponseEntity.ok(idamUserResponse));
        when(refDataClient.getOrganisationDetailsByUserId(REF_DATA_TOKEN, SERVICE_TOKEN, idamUserResponse.userId()))
            .thenReturn(ResponseEntity.ok(orgResponse));

        UserDataResponse actualResponse = userDataService.getUserData(params);

        UserDataResponse expectedAggregated = new UserDataResponse(
            "14567",
            email,
            forename,
            surname,
            ACTIVE,
            RECORD_TYPE,
            null,
            List.of(ROLE_1, ROLE_2),
            List.of(contactInfo),
            null
        );

        assertNotNull(actualResponse);
        assertUserDataResponseEquals(expectedAggregated, actualResponse);
        assertMetaDetails(actualResponse, 200, 200);
        verify(idamTokenGenerator, times(1)).generateIdamToken();
        verify(idamClient, times(1)).getUserDataByEmail(IDAM_TOKEN, email);
        verify(idamClient, never()).getUserDataByUserId(anyString(), anyString());
    }

    @Test
    void shouldHandleNullBodyAndReturnEmptyResponseWithMeta() {
        when(idamClient.getUserDataByUserId(IDAM_TOKEN, userId)).thenReturn(ResponseEntity.ok(null));
        when(refDataClient.getOrganisationDetailsByUserId(REF_DATA_TOKEN, SERVICE_TOKEN, userId))
            .thenReturn(ResponseEntity.ok(null));

        UserDataResponse actualResponse = userDataService.getUserData(params);

        assertResponseBodyNull(actualResponse);
        assertMetaDetails(actualResponse, 200, 200);
    }

    @Test
    void shouldHandleExceptionGracefully() {
        when(idamClient.getUserDataByUserId(IDAM_TOKEN, userId)).thenThrow(new RuntimeException("Service error"));
        when(refDataClient.getOrganisationDetailsByUserId(REF_DATA_TOKEN, SERVICE_TOKEN, userId))
            .thenThrow(new RuntimeException("Service error"));

        UserDataResponse actualResponse = userDataService.getUserData(params);

        assertResponseBodyNull(actualResponse);
        assertMetaDetails(actualResponse, 500, 500);
    }

    @Test
    void shouldHandleFeignExceptionGracefully() {
        Request request = Request.create(
            Request.HttpMethod.GET,
            "http://mockurl",
            Map.of(),
            null,
            new RequestTemplate()
        );
        FeignException notFound = new FeignException.NotFound("User not found", request, null, null);
        FeignException feignUnknown = new FeignException.FeignClientException(-1, "Unknown error", request, null, null);

        when(idamClient.getUserDataByUserId(IDAM_TOKEN, userId)).thenThrow(notFound);
        when(refDataClient.getOrganisationDetailsByUserId(REF_DATA_TOKEN, SERVICE_TOKEN, userId))
            .thenThrow(feignUnknown);

        UserDataResponse actualResponse = userDataService.getUserData(params);

        assertResponseBodyNull(actualResponse);
        assertMetaDetails(actualResponse, 404, 500);
    }

    @Test
    void shouldNotCallRefDataIfIdamEmailResponds404() {
        when(params.getUserId()).thenReturn(null);
        when(params.getEmail()).thenReturn(email);

        Request request = Request.create(
            Request.HttpMethod.GET,
            "http://mockurl",
            Map.of(),
            null,
            new RequestTemplate()
        );
        FeignException notFound = new FeignException.NotFound("User not found", request, null, null);
        when(idamClient.getUserDataByEmail(IDAM_TOKEN, email)).thenThrow(notFound);

        UserDataResponse actualResponse = userDataService.getUserData(params);

        assertResponseBodyNull(actualResponse);

        assertMetaDetails(actualResponse, 404, 404);
        verify(refDataClient, never()).getOrganisationDetailsByUserId(anyString(), anyString(), anyString());
    }

    @Test
    void shouldNotCallRefDataIfIdamUserRespondsWithMissingUserId() {

        when(params.getUserId()).thenReturn(null);
        when(params.getEmail()).thenReturn(email);

        IdamUserResponse idamUserResponse = createIdamUserResponse(null);

        when(idamClient.getUserDataByEmail(IDAM_TOKEN, email)).thenReturn(ResponseEntity.ok(idamUserResponse));

        UserDataResponse actualResponse = userDataService.getUserData(params);

        assertMetaDetails(actualResponse, 200, 404);
        verify(refDataClient, never()).getOrganisationDetailsByUserId(anyString(), anyString(), anyString());
    }

    private void assertMetaDetails(UserDataResponse actualResponse, int idamResp, int refdataResp) {
        Map<String, Map<String, Integer>> meta = actualResponse.meta();
        assertNotNull(meta);
        assertEquals(idamResp, meta.get(IDAM).get(RESPONSE_CODE_STR));
        assertEquals(refdataResp, meta.get(REF_DATA).get(RESPONSE_CODE_STR));
    }

    private void assertResponseBodyNull(final UserDataResponse actualResponse) {
        assertNotNull(actualResponse);
        assertNull(actualResponse.userId());
        assertNull(actualResponse.email());
        assertNull(actualResponse.accountStatus());
        assertNull(actualResponse.roles());
        assertNull(actualResponse.organisationalAddress());
    }

    private void assertUserDataResponseEquals(UserDataResponse expected, UserDataResponse actual) {
        assertEquals(expected.userId(), actual.userId());
        assertEquals(expected.email(), actual.email());
        assertEquals(expected.roles(), actual.roles());
        assertEquals(expected.accountStatus(), actual.accountStatus());
        assertEquals(expected.organisationalAddress(), actual.organisationalAddress());
    }

    private IdamUserResponse createIdamUserResponse(String userId) {
        return new IdamUserResponse(
            userId,
            email,
            ACTIVE,
            RECORD_TYPE,
            null,
            List.of(ROLE_1, ROLE_2),
            null,
            null,
            null,
            null,
            null,
            null,
            null,
            null
        );
    }
}
