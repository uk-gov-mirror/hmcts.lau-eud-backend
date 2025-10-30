package uk.gov.hmcts.reform.laubackend.eud.functionaltests.helper;

import io.restassured.RestAssured;
import io.restassured.response.ExtractableResponse;
import io.restassured.response.Response;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static uk.gov.hmcts.reform.laubackend.eud.functionaltests.utils.TestConstants.TESTING_SUPPORT_ACCOUNTS_URL;

public class DatabaseHelper extends AuthorizationHeaderHelper {

    private static final Logger LOGGER =
        LoggerFactory.getLogger(DatabaseHelper.class);

    public ExtractableResponse<Response> createUser() {
        try {
            return RestAssured.given()
                .header("Authorization", getAuthorizationToken())
                .header("Content-Type", "application/json")
                .body(makeUser())
                .when()
                .post(TESTING_SUPPORT_ACCOUNTS_URL)
                .then()
                .statusCode(201)
                .extract();
        } catch (JSONException e) {
            throw new RuntimeException(e);
        }
    }

    private String makeUser() {
        JSONObject user = new JSONObject();
        try {
            user.put("password","testEudLau123!");
            user.put("email", "testEUD@test.com");
            user.put("forename", "TestEUD");
            user.put("surname", "EUDTest");
            JSONArray roles = new JSONArray();
            JSONObject role = new JSONObject();
            role.put("code","citizen");
            roles.put(role);
            role.put("code","caseworker");
            roles.put(role);
            user.put("roles", roles);
        } catch (JSONException je) {
            LOGGER.error(je.getMessage(), je);
        }

        return user.toString();
    }

    public ExtractableResponse<Response> deleteUser() {
        try {
            return RestAssured.given()
                .header("Authorization", getAuthorizationToken())
                .header("Content-Type", "application/json")
                .when()
                .delete(TESTING_SUPPORT_ACCOUNTS_URL + "/testEUD@test.com")
                .then()
                .statusCode(204)
                .extract();
        } catch (JSONException e) {
            throw new RuntimeException(e);
        }
    }
}
