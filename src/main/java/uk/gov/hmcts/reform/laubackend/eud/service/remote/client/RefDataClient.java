package uk.gov.hmcts.reform.laubackend.eud.service.remote.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.cloud.openfeign.FeignClientProperties;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;
import uk.gov.hmcts.reform.laubackend.eud.response.OrganisationResponse;

import static uk.gov.hmcts.reform.laubackend.eud.constants.CommonConstants.AUTHORISATION_HEADER;
import static uk.gov.hmcts.reform.laubackend.eud.constants.CommonConstants.REF_DATA_ORGANISATION_DETAILS_PATH;
import static uk.gov.hmcts.reform.laubackend.eud.constants.CommonConstants.SERVICE_AUTHORISATION_HEADER;

@FeignClient(
    name = "rd-professional-api",
    url = "${rd.professional.url}",
    configuration = FeignClientProperties.FeignClientConfiguration.class
)
public interface RefDataClient {

    @GetMapping(REF_DATA_ORGANISATION_DETAILS_PATH + "{userId}")
    ResponseEntity<OrganisationResponse> getOrganisationDetailsByUserId(
        @RequestHeader(AUTHORISATION_HEADER) String authorisation,
        @RequestHeader(SERVICE_AUTHORISATION_HEADER) String serviceAuthorization,
        @PathVariable(name = "userId") String userId
    );
}
