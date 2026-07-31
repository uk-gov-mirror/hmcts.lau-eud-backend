package uk.gov.hmcts.reform.laubackend.eud.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.laubackend.eud.dto.UserDataGetRequestParams;
import uk.gov.hmcts.reform.laubackend.eud.response.IdamUserResponse;
import uk.gov.hmcts.reform.laubackend.eud.response.OrganisationResponse;
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
public class UserDataService {

    private final IdamClient idamClient;
    private final IdamTokenGenerator idamTokenGenerator;
    private final ServiceTokenGenerator serviceTokenGenerator;
    private final RefDataClient refDataClient;

    @Qualifier("userDataExecutor")
    private final Executor userDataExecutor;

    public UserDataService(
        IdamClient idamClient,
        IdamTokenGenerator idamTokenGenerator,
        ServiceTokenGenerator serviceTokenGenerator,
        RefDataClient refDataClient,
        @Qualifier("userDataExecutor") Executor userDataExecutor
    ) {
        this.idamClient = idamClient;
        this.idamTokenGenerator = idamTokenGenerator;
        this.serviceTokenGenerator = serviceTokenGenerator;
        this.refDataClient = refDataClient;
        this.userDataExecutor = userDataExecutor;
    }

    public static final String IDAM = "idam";
    public static final String REF_DATA = "refdata";
    public static final String RESPONSE_CODE_STR = "responseCode";

    public UserDataResponse getUserData(final UserDataGetRequestParams params) {
        boolean hasUserId = params.getUserId() != null;

        String idamToken = idamTokenGenerator.generateIdamToken();
        String refDataToken = idamTokenGenerator.generateRefDataToken();
        String serviceToken = serviceTokenGenerator.generateServiceToken();

        CompletableFuture<CallResult<IdamUserResponse>> idamF;
        CompletableFuture<CallResult<OrganisationResponse>> refDataF;

        if (hasUserId) {
            final String userId = params.getUserId();
            idamF    = callAsync(IDAM, () -> idamClient.getUserDataByUserId(idamToken, userId), userDataExecutor);
            refDataF = callAsync(REF_DATA,() -> refDataClient.getOrganisationDetailsByUserId(
                refDataToken, serviceToken, userId), userDataExecutor);

        } else {
            idamF = callAsync(
                IDAM,
                () -> idamClient.getUserDataByEmail(idamToken, params.getEmail()),
                userDataExecutor
            );

            refDataF = idamF.thenCompose(idam -> {
                String userId = idam.body != null ? idam.body.userId() : null;

                if (userId != null && !userId.isBlank()) {
                    String trimmed = userId.trim();
                    return callAsync(REF_DATA,
                        () -> refDataClient.getOrganisationDetailsByUserId(refDataToken, serviceToken, trimmed),
                                     userDataExecutor);
                }
                return CompletableFuture.completedFuture(new CallResult<>(REF_DATA, 404, null));
            });

        }

        CallResult<IdamUserResponse> idam = idamF.join();
        CallResult<OrganisationResponse> ref = refDataF.join();

        Map<String, Map<String, Integer>> meta = new LinkedHashMap<>();
        meta.put(IDAM, Map.of(RESPONSE_CODE_STR, idam.responseCode));
        meta.put(REF_DATA, Map.of(RESPONSE_CODE_STR, ref.responseCode));

        // Build final response + minimal meta and return
        return aggregateResponses(
            idam.body != null ? idam.body : IdamUserResponse.empty(),
            ref.body != null ? ref.body : new OrganisationResponse(null),meta);
    }

    @SuppressWarnings("PMD.AvoidCatchingGenericException")
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
                log.warn("[{}] FeignException status={} ", source, status);
                return new CallResult<>(source, status, null);
            } catch (Exception e) {
                log.warn("[{}] Exception caught: {}", source, e.getMessage(), e);
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
            idamUserData.forename(),
            idamUserData.surname(),
            idamUserData.accountStatus(),
            idamUserData.recordType(),
            idamUserData.accountCreationDate(),
            idamUserData.roles(),
            organisation.organisationalAddress(),
            meta
        );
    }
}
