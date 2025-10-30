package uk.gov.hmcts.reform.laubackend.eud.functionaltests.config;

public final class EnvConfig {

    public static final String API_URL = System.getenv("TEST_URL");
    public static final String IDAM_CLIENT_SECRET = System.getenv("IDAM_CLIENT_SECRET");

    private EnvConfig() {
    }
}
