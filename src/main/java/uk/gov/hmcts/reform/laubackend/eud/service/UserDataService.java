package uk.gov.hmcts.reform.laubackend.eud.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.laubackend.eud.dto.UserDataGetRequestParams;
import uk.gov.hmcts.reform.laubackend.eud.response.OrganisationResponse;
import uk.gov.hmcts.reform.laubackend.eud.response.IdamUserResponse;
import uk.gov.hmcts.reform.laubackend.eud.response.UserDataResponse;
import uk.gov.hmcts.reform.laubackend.eud.service.remote.client.IdamClient;
import uk.gov.hmcts.reform.laubackend.eud.service.remote.client.RefDataClient;
import uk.gov.hmcts.reform.laubackend.eud.utils.IdamTokenGenerator;
import uk.gov.hmcts.reform.laubackend.eud.utils.ServiceTokenGenerator;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.function.Supplier;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserDataService {

    private final IdamClient idamClient;
    private final IdamTokenGenerator idamTokenGenerator;
    private final ServiceTokenGenerator serviceTokenGenerator;
    private final RefDataClient refDataClient;
    @Qualifier("userDataExecutor")
    private final Executor executor;

    static final String IDAM = "idam";
    static final String REF_DATA = "refdata";

    public UserDataResponse getUserData(final UserDataGetRequestParams params) {
        boolean hasUserId = params.getUserId() != null;

        String idamToken = idamTokenGenerator.generateIdamToken();
        String refDataToken = idamTokenGenerator.generateRefDataToken();
        String serviceToken = serviceTokenGenerator.generateServiceToken();

        CompletableFuture<CallResult<IdamUserResponse>> idamF;
        CompletableFuture<CallResult<OrganisationResponse>> refDataF;

        if (hasUserId) {
            final String userId = params.getUserId();
            idamF    = callAsync(IDAM, () -> idamClient.getUserDataByUserId(idamToken, userId), executor);
            refDataF = callAsync(REF_DATA,() -> refDataClient.getOrganisationDetailsByUserId(
                refDataToken, serviceToken, userId), executor);

        } else {
            idamF = callAsync(IDAM,    () -> idamClient.getUserDataByEmail(idamToken, params.getEmail()), executor);

            refDataF = idamF.thenCompose(idam -> {
                var body = idam.body;
                var userId = body != null ? body.userId() : null;

                if (userId != null && !userId.isBlank()) {
                    var trimmed = userId.trim();
                    return callAsync(REF_DATA,
                        () -> refDataClient.getOrganisationDetailsByUserId(refDataToken, serviceToken, trimmed),
                                     executor);
                }
                return CompletableFuture.completedFuture(new CallResult<>(REF_DATA, 404, null));
            });

        }

        CallResult<IdamUserResponse> idam = idamF.join();
        CallResult<OrganisationResponse> ref = refDataF.join();

        Map<String, Map<String, Integer>> meta = new LinkedHashMap<>();
        meta.put(IDAM, Map.of("responseCode", idam.responseCode));
        meta.put(REF_DATA, Map.of("responseCode", ref.responseCode));

        // Build final response + minimal meta and return
        return aggregateResponses(
            idam.body != null ? idam.body : IdamUserResponse.empty(),
            ref.body != null ? ref.body : new OrganisationResponse(null),meta);
    }

    private static <T> CompletableFuture<CallResult<T>> callAsync(
        String source, Supplier<ResponseEntity<T>> call, Executor ex) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                ResponseEntity<T> resp = call.get();
                int code = (resp != null) ? resp.getStatusCode().value() : 500;
                T body = (resp != null) ? resp.getBody() : null;
                return new CallResult<>(source, code, body);
            } catch (feign.FeignException fe) {
                int status = fe.status() > 0 ? fe.status() : 500;
                log.warn("[{}] FeignException status={} msg={}", source, status, fe.getMessage());
                return new CallResult<>(source, status, null);
            } catch (Exception e) {
                log.warn("[{}] Exception caught: {}", source, e.getMessage(),e);
                return new CallResult<>(source, 500, null);
            }
        }, ex);
    }

    private record CallResult<T>(String source, int responseCode, T body) {}

    private static UserDataResponse aggregateResponses(IdamUserResponse idamUserData,
                                                       OrganisationResponse organisation,
                                                       Map<String, Map<String, Integer>> meta) {
        return new UserDataResponse(
            idamUserData.userId(),
            idamUserData.email(),
            idamUserData.accountStatus(),
            idamUserData.accountCreationDate(),
            idamUserData.roles(),
            organisation.organisationalAddress(),
            meta
        );
    }
}

