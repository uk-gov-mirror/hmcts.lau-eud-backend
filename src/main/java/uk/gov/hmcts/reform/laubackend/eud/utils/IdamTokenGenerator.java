package uk.gov.hmcts.reform.laubackend.eud.utils;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.idam.client.models.TokenResponse;
import uk.gov.hmcts.reform.laubackend.eud.exceptions.IdamAuthTokenGenerationException;
import uk.gov.hmcts.reform.laubackend.eud.parameter.ParameterResolver;
import uk.gov.hmcts.reform.laubackend.eud.service.remote.client.IdamClient;

@Service
@Slf4j
@Getter
@RequiredArgsConstructor
public class IdamTokenGenerator {

    public static final String BEARER = "Bearer ";
    public static final String IDAM_GRANT_TYPE = "client_credentials";
    public static final String IDAM_SCOPE = "view-user";
    public static final String REF_DATA_GRANT_TYPE = "password";
    public static final String REF_DATA_SCOPE = "openid profile roles";

    private final IdamClient idamClient;
    private final ParameterResolver parameterResolver;

    private String idamClientToken = "idamToken";
    private String refDataToken = "refDataToken";

    public String generateIdamToken() {
        try {
            TokenResponse tokenResponse = idamClient.getToken(
                parameterResolver.getClientId(),
                parameterResolver.getClientSecret(),
                parameterResolver.getRedirectUrl(),
                IDAM_GRANT_TYPE,
                IDAM_SCOPE
            );
            idamClientToken = tokenResponse.accessToken;

        } catch (final Exception exception) {
            String msg = String.format("Unable to generate IDAM token due to error - %s", exception.getMessage());
            log.error(msg, exception);
            throw new IdamAuthTokenGenerationException(msg, exception);
        }
        return BEARER + idamClientToken;
    }

    public String generateRefDataToken() {
        try {
            TokenResponse tokenResponse = idamClient.getRefDataToken(
                parameterResolver.getClientId(),
                parameterResolver.getClientSecret(),
                parameterResolver.getRedirectUrl(),
                REF_DATA_GRANT_TYPE,
                REF_DATA_SCOPE,
                parameterResolver.getUsername(),
                parameterResolver.getPassword()
            );
            refDataToken = tokenResponse.accessToken;

        } catch (final Exception exception) {
            String msg = String.format("Unable to generate IDAM token due to error - %s", exception.getMessage());
            log.error(msg, exception);
            throw new IdamAuthTokenGenerationException(msg, exception);
        }
        return BEARER + refDataToken;
    }
}
