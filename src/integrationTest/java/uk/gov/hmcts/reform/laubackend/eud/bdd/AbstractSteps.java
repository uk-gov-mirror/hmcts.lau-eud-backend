package uk.gov.hmcts.reform.laubackend.eud.bdd;

import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.client.WireMock;
import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.web.server.LocalServerPort;
import uk.gov.hmcts.reform.laubackend.eud.helper.RestHelper;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.containing;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.jsonResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.post;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathEqualTo;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathMatching;
import static feign.form.ContentProcessor.CONTENT_TYPE_HEADER;

import static uk.gov.hmcts.reform.laubackend.eud.constants.CommonConstants.REF_DATA_ORGANISATION_DETAILS_PATH;
import static uk.gov.hmcts.reform.laubackend.eud.constants.CommonConstants.SERVICE_AUTHORISATION_HEADER;
import static uk.gov.hmcts.reform.laubackend.eud.constants.CommonConstants.USER_DATA_BY_EMAIL_PATH;
import static uk.gov.hmcts.reform.laubackend.eud.constants.CommonConstants.USER_DATA_BY_USERID_PATH;
import static uk.gov.hmcts.reform.laubackend.eud.helper.RestConstants.BAD_S2S_TOKEN;
import static uk.gov.hmcts.reform.laubackend.eud.helper.RestConstants.GOOD_TOKEN;

@Getter
public class AbstractSteps {

    private static final String JSON_RESPONSE = "application/json;charset=UTF-8";
    private static final String AUTHORISATION_HEADER = "Authorization";
    private static final String BEARER = "Bearer ";
    protected final RestHelper restHelper = new RestHelper();

    @LocalServerPort
    private int port;

    @Value("${dummy-jwt}")
    String dummyJwtToken;

    public String baseUrl() {
        return "http://localhost:" + port;
    }

    public void setupStub() {
        WireMockServer server = WireMockInstantiator.getWireMockInstance();
        server.stubFor(get(urlPathMatching("/details"))
                           .withHeader(AUTHORISATION_HEADER, containing(GOOD_TOKEN))
                           .willReturn(aResponse()
                                           .withHeader(CONTENT_TYPE_HEADER, JSON_RESPONSE)
                                           .withStatus(200)
                                           .withBody("lau_frontend")));

        server.stubFor(get(urlPathMatching("/details"))
                           .withHeader(AUTHORISATION_HEADER, containing(BAD_S2S_TOKEN))
                           .willReturn(aResponse()
                                           .withHeader(CONTENT_TYPE_HEADER, JSON_RESPONSE)
                                           .withStatus(401)));

        server.stubFor(WireMock.post(WireMock.urlPathEqualTo("/lease"))
                           .willReturn(WireMock.aResponse().withBody(dummyJwtToken)));

        server.stubFor(get(urlPathMatching(USER_DATA_BY_USERID_PATH
                                               + "[0-9a-f\\-]{36}"))
                           .withHeader(AUTHORISATION_HEADER, containing(BEARER + GOOD_TOKEN))
                           .willReturn(aResponse()
                                           .withHeader(CONTENT_TYPE_HEADER, JSON_RESPONSE)
                                           .withStatus(200)
                                           .withBodyFile("UserData.json")));

        server.stubFor(get(urlPathMatching(USER_DATA_BY_EMAIL_PATH
               + "(?:[A-Za-z0-9._%+-]|%[0-9A-Fa-f]{2})+"
               + "(?:@|%40)"
               + "(?:[A-Za-z0-9-]+\\.)+"
               + "[A-Za-z]{2,63}$"))
                           .withHeader(AUTHORISATION_HEADER, containing(BEARER + GOOD_TOKEN))
                           .willReturn(aResponse()
                                           .withHeader(CONTENT_TYPE_HEADER, JSON_RESPONSE)
                                           .withStatus(200)
                                           .withBodyFile("UserData.json")));

        server.stubFor(get(urlPathMatching(REF_DATA_ORGANISATION_DETAILS_PATH
                                               + "[0-9a-f\\-]{36}"))
                           .withHeader(AUTHORISATION_HEADER, containing(BEARER + GOOD_TOKEN))
                           .withHeader(SERVICE_AUTHORISATION_HEADER, containing(BEARER + dummyJwtToken))
                           .willReturn(aResponse()
                                           .withHeader(CONTENT_TYPE_HEADER, JSON_RESPONSE)
                                           .withStatus(200)
                                           .withBodyFile("OrganisationalAddress.json")));

        String jsonBody = "{ \"access_token\": \"" + GOOD_TOKEN + "\" }";

        server.stubFor(post(urlPathEqualTo("/o/token"))
                .willReturn(jsonResponse(jsonBody, 200))
        );
    }

}
