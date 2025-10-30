package uk.gov.hmcts.reform.laubackend.eud.functionaltests.utils;

@SuppressWarnings("PMD.TestClassWithoutTestCases")
public final class TestConstants {

    public static final String S2S_NAME = "lau_frontend";
    public static final String S2S_URL = "http://rpe-service-auth-provider-aat.service.core-compute-aat.internal/testing-support";

    // Authorization constants
    public static final String GRANT_TYPE = "client_credentials";
    public static final String CLIENT_ID = "lau";
    public static final String TOKEN_URL = "https://idam-api.aat.platform.hmcts.net/o/token";
    public static final String USER_SCOPE = "roles profile";
    public static final String TESTING_SUPPORT_ACCOUNTS_URL = "https://idam-api.aat.platform.hmcts.net/testing-support/accounts";


    /*endPoint*/
    public static final String USER_DATA_ENDPOINT = "/audit/userData";


    public static final String SUCCESS = "Success";

    private TestConstants() {

    }
}
