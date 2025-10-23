package uk.gov.hmcts.reform.laubackend.eud.authorization;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.servlet.HandlerInterceptor;

@Slf4j
public class RestApiPreInvokeInterceptor implements HandlerInterceptor {

    @Autowired
    private ServiceAuthorizationAuthenticator serviceAuthorizationAuthenticator;

    @Override
    public boolean preHandle(final HttpServletRequest request,
                             final HttpServletResponse response,
                             final Object handler) {
        serviceAuthorizationAuthenticator.authorizeServiceToken(request);
        return true;
    }
}
