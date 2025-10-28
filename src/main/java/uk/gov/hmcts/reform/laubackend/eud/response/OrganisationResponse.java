package uk.gov.hmcts.reform.laubackend.eud.response;

import com.fasterxml.jackson.annotation.JsonAlias;
import io.swagger.v3.oas.annotations.media.Schema;

import java.io.Serializable;
import java.util.List;

public record OrganisationResponse(
    @Schema(description = "User Organisation Details")
    @JsonAlias("contactInformation")
    List<ContactInformationResponse> organisationalAddress
) implements Serializable {
    private static final long serialVersionUID = 432973378L;
}
