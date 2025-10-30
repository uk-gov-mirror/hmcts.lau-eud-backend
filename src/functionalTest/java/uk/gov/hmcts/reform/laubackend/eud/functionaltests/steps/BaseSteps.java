package uk.gov.hmcts.reform.laubackend.eud.functionaltests.steps;

import io.restassured.builder.RequestSpecBuilder;
import io.restassured.config.EncoderConfig;
import io.restassured.config.RestAssuredConfig;
import io.restassured.http.Header;
import io.restassured.response.ExtractableResponse;
import io.restassured.response.Response;
import io.restassured.specification.RequestSpecification;
import net.serenitybdd.annotations.Step;
import net.serenitybdd.rest.SerenityRest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import uk.gov.hmcts.reform.laubackend.eud.functionaltests.model.UserDataResponse;
import uk.gov.hmcts.reform.laubackend.eud.functionaltests.helper.AuthorizationHeaderHelper;
import uk.gov.hmcts.reform.laubackend.eud.functionaltests.config.EnvConfig;
import uk.gov.hmcts.reform.laubackend.eud.functionaltests.helper.DatabaseHelper;

import java.nio.charset.StandardCharsets;
import java.util.Map;

public class BaseSteps {

    private static final RequestSpecification REQSPEC;
    private static final Logger LOGGER =
        LoggerFactory.getLogger(BaseSteps.class);
    protected final AuthorizationHeaderHelper authorizationHeaderHelper = new AuthorizationHeaderHelper();
    protected final DatabaseHelper databaseHelper = new DatabaseHelper();

    static {
        final String proxyHost = System.getProperty("http.proxyHost");
        final Integer proxyPort = proxyHost == null ? null : Integer.parseInt(System.getProperty("http.proxyPort"));

        final RestAssuredConfig config = RestAssuredConfig.newConfig()
            .encoderConfig(EncoderConfig.encoderConfig().defaultContentCharset(StandardCharsets.UTF_8));

        final RequestSpecBuilder specBuilder = new RequestSpecBuilder()
            .setConfig(config)
            .setBaseUri(EnvConfig.API_URL)
            .setRelaxedHTTPSValidation();

        LOGGER.info("Using base API URL: {}", EnvConfig.API_URL);
        if (proxyHost != null) {
            specBuilder.setProxy(proxyHost, proxyPort);
        }

        REQSPEC = specBuilder.build();
    }

    public RequestSpecification rest() {
        return SerenityRest.given(REQSPEC);
    }

    public ResponseEntity<UserDataResponse> performGetOperation(String endpoint,
                                                                Map<String, String> headers,
                                                                Map<String, String> queryParams,
                                                 String authServiceToken) {

        RequestSpecification requestSpecification = rest().urlEncodingEnabled(false)
            .given()
            .header("ServiceAuthorization", authServiceToken)
            .header("Content-Type", "application/json");


        if (null != headers && !headers.isEmpty()) {
            for (Map.Entry<String, String> entry : headers.entrySet()) {
                requestSpecification.header(createHeader(entry.getKey(), entry.getValue()));
            }
        }

        if (null != queryParams && !queryParams.isEmpty()) {
            for (Map.Entry<String, String> entry : queryParams.entrySet()) {
                requestSpecification.queryParam(entry.getKey(), entry.getValue());
            }
        }

        Response response = requestSpecification.get(endpoint)
            .then()
            .extract().response();
        UserDataResponse userDataResponse = response.as(UserDataResponse.class);
        return new ResponseEntity<>(userDataResponse, HttpStatus.valueOf(response.getStatusCode()));
    }

    public Response performGetOperationWithInvalidParams(String endpoint,
                                                                              Map<String, String> headers,
                                                                              Map<String, String> queryParams,
                                                                              String authServiceToken) {

        RequestSpecification requestSpecification = rest().urlEncodingEnabled(false)
            .given()
            .header("ServiceAuthorization", authServiceToken)
            .header("Content-Type", "application/json");


        if (null != headers && !headers.isEmpty()) {
            for (Map.Entry<String, String> entry : headers.entrySet()) {
                requestSpecification.header(createHeader(entry.getKey(), entry.getValue()));
            }
        }

        if (null != queryParams && !queryParams.isEmpty()) {
            for (Map.Entry<String, String> entry : queryParams.entrySet()) {
                requestSpecification.queryParam(entry.getKey(), entry.getValue());
            }
        }

        return requestSpecification.get(endpoint)
            .then()
            .extract().response();
    }

    public Header createHeader(String headerKey, String headerValue) {
        return new Header(headerKey, headerValue);
    }

    public Response performPostOperation(String endpoint, String bodyJson, String serviceToken) {
        return performPostOperation(endpoint, null, null, bodyJson, serviceToken);
    }

    public Response performPostOperation(String endpoint,
                                         Map<String, String> headers,
                                         Map<String, String> queryParams,
                                         String bodyAsJsonString,
                                         String authServiceToken
    ) {

        RequestSpecification requestSpecification = rest()
            .given().header("ServiceAuthorization", authServiceToken)
            .header("Content-Type", "application/json");
        if (null != headers && !headers.isEmpty()) {
            for (Map.Entry<String, String> entry: headers.entrySet()) {
                requestSpecification.header(createHeader(entry.getKey(), entry.getValue()));
            }
        }
        if (null != queryParams && !queryParams.isEmpty()) {
            for (Map.Entry<String, String> entry: queryParams.entrySet()) {
                requestSpecification.param(entry.getKey(), entry.getValue());
            }
        }

        return requestSpecification.urlEncodingEnabled(true).body(bodyAsJsonString).post(endpoint)
            .then()
            .extract().response();
    }


    @Step("Given a valid service token is generated")
    public String givenAValidServiceTokenIsGenerated() {
        return authorizationHeaderHelper.getServiceToken();
    }

    protected String createUser() {
        ExtractableResponse<Response> res =  databaseHelper.createUser();
        return res.path("id");
    }

    protected int deleteUser() {
        ExtractableResponse<Response> res =  databaseHelper.deleteUser();
        return res.statusCode();
    }
}
