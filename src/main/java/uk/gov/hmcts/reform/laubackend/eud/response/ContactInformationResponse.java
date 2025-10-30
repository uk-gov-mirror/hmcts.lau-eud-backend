package uk.gov.hmcts.reform.laubackend.eud.response;

import io.swagger.v3.oas.annotations.media.Schema;

import java.io.Serial;
import java.io.Serializable;

@Schema(description = "Organisational Contact Information")
public record ContactInformationResponse(
    @Schema(description = "Address line 1 of the user's organisation.")
    String addressLine1,

    @Schema(description = "Address line 2 of the user's organisation.")
    String addressLine2,

    @Schema(description = "Address line 3 of the user's organisation.")
    String addressLine3,

    @Schema(description = "TownCity of the user's organisation.")
    String townCity,

    @Schema(description = "County of the user's organisation.")
    String county,

    @Schema(description = "Country of the user's organisation.")
    String country,

    @Schema(description = "Postcode of the user's organisation.")
    String postCode
) implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;
}
