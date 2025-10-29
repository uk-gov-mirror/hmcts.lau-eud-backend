package uk.gov.hmcts.reform.laubackend.eud.response;

import io.swagger.v3.oas.annotations.media.Schema;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

public record UserDataResponse(
    @Schema(description = "IdAM ID of the user.")
    String userId,

    @Schema(description = "Email address/username of the user.")
    String email,

    @Schema(description = "Account Status of user")
    String accountStatus,

    @Schema(description = "User's account creation timestamp in iso-8601-date-and-time-format.")
    String accountCreationDate,

    @Schema(description = "User's roles.")
    List<String> roles,

    @Schema(description = "User Organisation Address")
    List<ContactInformationResponse> organisationalAddress,

    @Schema(description = "Metadata related to the user data response.")
    Map<String, Map<String, Integer>> meta
) implements Serializable {}

