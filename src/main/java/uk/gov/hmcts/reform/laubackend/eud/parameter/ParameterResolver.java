package uk.gov.hmcts.reform.laubackend.eud.parameter;

import lombok.Getter;
import lombok.Setter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
@Getter
@Setter
public class ParameterResolver {

    @Value("${idam.api.url}")
    private String idamHost;

    @Value("${idam.client.id}")
    private String clientId;

    @Value("${idam.client.secret}")
    private String clientSecret;

    @Value("${idam.client.username}")
    private String username;

    @Value("${idam.client.password}")
    private String password;

    @Value("${idam.client.redirect_uri}")
    private String redirectUrl;

}
