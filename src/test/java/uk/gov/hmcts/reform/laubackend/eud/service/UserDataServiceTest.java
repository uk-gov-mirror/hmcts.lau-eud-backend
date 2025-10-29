package uk.gov.hmcts.reform.laubackend.eud.service;

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

import java.util.ArrayList;
import java.util.Arrays;
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

class UserDataServiceTest {

    public static final String ACTIVE = "ACTIVE";
    static final String IDAM = "idam";
    static final String REF_DATA = "refdata";
    static final String RESPONSE_CODE = "responseCode";
    static final String IDAM_TOKEN = "mock-token";
    static final String REF_DATA_TOKEN = "mock-ref-token";
    static final String SERVICE_TOKEN = "mock-service-token";
    static final String ROLE_1 = "role1";
    static final String ROLE_2 = "role2";

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

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        doAnswer(invocation -> {
            Runnable r = invocation.getArgument(0);
            r.run();
            return null;
        }).when(executor).execute(any());
    }

    @Test
    void shouldReturnUserDataByUserId() {
        String userId = "12345";
        UserDataGetRequestParams params = mock(UserDataGetRequestParams.class);
        when(params.getUserId()).thenReturn(userId);
        when(params.getEmail()).thenReturn(null);

        List<ContactInformationResponse> contactInfoList = new ArrayList<>();
        ContactInformationResponse contactInfo = new ContactInformationResponse(
            "Org details",
            "",
            "",
            "",
            "",
            "",
            ""
        );
        contactInfoList.add(contactInfo);

        IdamUserResponse idamUserResponse = new IdamUserResponse(
            userId,
            "test@test.com",
            ACTIVE,
            null, // accountCreationDate if not needed
            List.of(ROLE_1, ROLE_2)
        );
        OrganisationResponse orgResponse = new OrganisationResponse(contactInfoList);

        when(idamTokenGenerator.generateIdamToken()).thenReturn(IDAM_TOKEN);
        when(idamTokenGenerator.generateRefDataToken()).thenReturn(REF_DATA_TOKEN);
        when(serviceTokenGenerator.generateServiceToken()).thenReturn(SERVICE_TOKEN);

        when(idamClient.getUserDataByUserId(IDAM_TOKEN, userId)).thenReturn(ResponseEntity.ok(idamUserResponse));
        when(refDataClient.getOrganisationDetailsByUserId(REF_DATA_TOKEN, SERVICE_TOKEN, userId))
            .thenReturn(ResponseEntity.ok(orgResponse));

        UserDataResponse actualResponse = userDataService.getUserData(params);

        UserDataResponse expectedAggregated = new UserDataResponse(
            userId,
            "test@test.com",
            ACTIVE,
            null,
            new ArrayList<String>(Arrays.asList(ROLE_1, ROLE_2)),
            contactInfoList,
            null
        );

        assertNotNull(actualResponse);
        assertUserDataResponseEquals(expectedAggregated, actualResponse);
        Map<String, Map<String, Integer>> meta = actualResponse.meta();
        assertNotNull(meta);
        assertEquals(200, meta.get(IDAM).get(RESPONSE_CODE));
        assertEquals(200, meta.get(REF_DATA).get(RESPONSE_CODE));
        verify(idamTokenGenerator, times(1)).generateIdamToken();
        verify(idamTokenGenerator, times(1)).generateRefDataToken();
        verify(serviceTokenGenerator, times(1)).generateServiceToken();
        verify(idamClient, times(1)).getUserDataByUserId(IDAM_TOKEN, userId);
        verify(idamClient, never()).getUserDataByEmail(anyString(), anyString());
        verify(refDataClient, times(1)).getOrganisationDetailsByUserId(REF_DATA_TOKEN,SERVICE_TOKEN, userId);
    }

    @Test
    void shouldReturnUserDataByEmail() {
        String email = "test@example.com";
        UserDataGetRequestParams params = mock(UserDataGetRequestParams.class);
        when(params.getUserId()).thenReturn(null);
        when(params.getEmail()).thenReturn(email);

        List<ContactInformationResponse> contactInfoList = new ArrayList<>();
        ContactInformationResponse contactInfo = new ContactInformationResponse(
            "Org details",
            "",
            "",
            "",
            "",
            "",
            ""
        );
        contactInfoList.add(contactInfo);

        IdamUserResponse idamUserResponse = new IdamUserResponse(
            "14567",
            "test@example.com",
            ACTIVE,
            null, // accountCreationDate if not needed
            List.of(ROLE_1, ROLE_2)
        );
        OrganisationResponse orgResponse = new OrganisationResponse(contactInfoList);

        when(idamTokenGenerator.generateIdamToken()).thenReturn(IDAM_TOKEN);
        when(idamTokenGenerator.generateRefDataToken()).thenReturn(REF_DATA_TOKEN);
        when(serviceTokenGenerator.generateServiceToken()).thenReturn(SERVICE_TOKEN);

        when(idamClient.getUserDataByEmail(IDAM_TOKEN, email)).thenReturn(ResponseEntity.ok(idamUserResponse));
        when(refDataClient.getOrganisationDetailsByUserId(REF_DATA_TOKEN, SERVICE_TOKEN, idamUserResponse.userId()))
            .thenReturn(ResponseEntity.ok(orgResponse));

        UserDataResponse actualResponse = userDataService.getUserData(params);

        UserDataResponse expectedAggregated = new UserDataResponse(
            "14567",
            "test@example.com",
            ACTIVE,
            null,
            new ArrayList<String>(Arrays.asList(ROLE_1, ROLE_2)),
            contactInfoList,
            null
        );

        assertNotNull(actualResponse);
        assertUserDataResponseEquals(expectedAggregated, actualResponse);

        Map<String, Map<String, Integer>> meta = actualResponse.meta();
        assertNotNull(meta);
        assertEquals(200, meta.get(IDAM).get(RESPONSE_CODE));
        assertEquals(200, meta.get(REF_DATA).get(RESPONSE_CODE));

        verify(idamTokenGenerator, times(1)).generateIdamToken();
        verify(idamClient, times(1)).getUserDataByEmail(IDAM_TOKEN, email);
        verify(idamClient, never()).getUserDataByUserId(anyString(), anyString());
    }

    @Test
    void shouldHandleNullBodyAndReturnEmptyResponseWithMeta() {
        String userId = "99999";
        UserDataGetRequestParams params = mock(UserDataGetRequestParams.class);
        when(params.getUserId()).thenReturn(userId);
        when(params.getEmail()).thenReturn(null);

        when(idamTokenGenerator.generateIdamToken()).thenReturn(IDAM_TOKEN);
        when(idamTokenGenerator.generateRefDataToken()).thenReturn(REF_DATA_TOKEN);
        when(serviceTokenGenerator.generateServiceToken()).thenReturn(SERVICE_TOKEN);

        when(idamClient.getUserDataByUserId(IDAM_TOKEN, userId)).thenReturn(ResponseEntity.ok(null));
        when(refDataClient.getOrganisationDetailsByUserId(REF_DATA_TOKEN, SERVICE_TOKEN, userId))
            .thenReturn(ResponseEntity.ok(null));

        UserDataResponse actualResponse = userDataService.getUserData(params);

        assertNotNull(actualResponse);
        assertNull(actualResponse.userId());
        assertNull(actualResponse.email());
        assertNull(actualResponse.accountStatus());
        assertNull(actualResponse.roles());
        assertNull(actualResponse.organisationalAddress());

        Map<String, Map<String, Integer>> meta = actualResponse.meta();
        assertNotNull(meta);
        assertEquals(200, meta.get(IDAM).get(RESPONSE_CODE));
        assertEquals(200, meta.get(REF_DATA).get(RESPONSE_CODE));
    }

    @Test
    void shouldHandleExceptionGracefully() {
        String userId = "12345";
        UserDataGetRequestParams params = mock(UserDataGetRequestParams.class);
        when(params.getUserId()).thenReturn(userId);
        when(params.getEmail()).thenReturn(null);

        when(idamTokenGenerator.generateIdamToken()).thenReturn(IDAM_TOKEN);
        when(idamTokenGenerator.generateRefDataToken()).thenReturn(REF_DATA_TOKEN);
        when(serviceTokenGenerator.generateServiceToken()).thenReturn(SERVICE_TOKEN);

        when(idamClient.getUserDataByUserId(IDAM_TOKEN, userId)).thenThrow(new RuntimeException("Service error"));
        when(refDataClient.getOrganisationDetailsByUserId(REF_DATA_TOKEN, SERVICE_TOKEN, userId))
            .thenThrow(new RuntimeException("Service error"));

        UserDataResponse actualResponse = userDataService.getUserData(params);


        assertNotNull(actualResponse);
        assertNull(actualResponse.userId());
        assertNull(actualResponse.email());
        assertNull(actualResponse.accountStatus());
        assertNull(actualResponse.roles());
        assertNull(actualResponse.organisationalAddress());

        Map<String, Map<String, Integer>> meta = actualResponse.meta();
        assertNotNull(meta);
        assertEquals(500, meta.get(IDAM).get(RESPONSE_CODE));
        assertEquals(500, meta.get(REF_DATA).get(RESPONSE_CODE));

    }

    private void assertUserDataResponseEquals(UserDataResponse expected, UserDataResponse actual) {
        assertEquals(expected.userId(), actual.userId());
        assertEquals(expected.email(), actual.email());
        assertEquals(expected.roles(), actual.roles());
        assertEquals(expected.accountStatus(), actual.accountStatus());
        assertEquals(expected.organisationalAddress(), actual.organisationalAddress());
    }
}
