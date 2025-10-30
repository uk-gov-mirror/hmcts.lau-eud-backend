package uk.gov.hmcts.reform.laubackend.eud.functionaltests.helper;

import io.restassured.RestAssured;
import io.restassured.response.Response;
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;

import static com.google.common.collect.ImmutableMap.of;
import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.http.HttpHeaders.CONTENT_TYPE;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
import static uk.gov.hmcts.reform.laubackend.eud.functionaltests.config.EnvConfig.IDAM_CLIENT_SECRET;
import static uk.gov.hmcts.reform.laubackend.eud.functionaltests.utils.TestConstants.CLIENT_ID;
import static uk.gov.hmcts.reform.laubackend.eud.functionaltests.utils.TestConstants.GRANT_TYPE;
import static uk.gov.hmcts.reform.laubackend.eud.functionaltests.utils.TestConstants.S2S_NAME;
import static uk.gov.hmcts.reform.laubackend.eud.functionaltests.utils.TestConstants.S2S_URL;
import static uk.gov.hmcts.reform.laubackend.eud.functionaltests.utils.TestConstants.TOKEN_URL;
import static uk.gov.hmcts.reform.laubackend.eud.functionaltests.utils.TestConstants.USER_SCOPE;

public class AuthorizationHeaderHelper {

    private static final Logger LOGGER =
        LoggerFactory.getLogger(AuthorizationHeaderHelper.class);

    public String getAuthorizationToken() throws JSONException {
        Response response = RestAssured
            .given()
            .contentType("application/x-www-form-urlencoded; charset=utf-8")
            .formParam("grant_type", GRANT_TYPE)
            .formParam("scope", USER_SCOPE)
            .formParam("client_id", CLIENT_ID)
            .formParam("client_secret", IDAM_CLIENT_SECRET)
            .when()
            .post(TOKEN_URL);

        return "Bearer " + new JSONObject(response.getBody().asString())
            .getString("access_token");

    }

    public String getServiceToken() {

        LOGGER.info("s2sUrl lease url: {}", S2S_URL + "/lease");
        final Map<String, Object> params = of(
            "microservice", S2S_NAME
        );

        final Response response = RestAssured
            .given()
            .relaxedHTTPSValidation()
            .baseUri(S2S_URL)
            .header(CONTENT_TYPE, APPLICATION_JSON_VALUE)
            .body(params)
            .when()
            .post("/lease")
            .andReturn();
        assertThat(response.getStatusCode()).isEqualTo(200);

        return "Bearer " + response
            .getBody()
            .asString();
    }



}
