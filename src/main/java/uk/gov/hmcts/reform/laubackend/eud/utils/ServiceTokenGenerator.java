package uk.gov.hmcts.reform.laubackend.eud.utils;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.reform.laubackend.eud.exceptions.ServiceAuthTokenGenerationException;

@Service
@Slf4j
@RequiredArgsConstructor
@Getter
public class ServiceTokenGenerator {

    private final AuthTokenGenerator authTokenGenerator;

    private String serviceAuthToken = "dummy token";

    public String generateServiceToken() {
        try {
            serviceAuthToken = authTokenGenerator.generate();
        } catch (final Exception exception) {
            String msg = String.format(
                "Unable to generate service auth token due to error - %s", exception.getMessage());
            log.error(msg, exception);
            throw new ServiceAuthTokenGenerationException(msg, exception);
        }
        return serviceAuthToken;
    }
}
