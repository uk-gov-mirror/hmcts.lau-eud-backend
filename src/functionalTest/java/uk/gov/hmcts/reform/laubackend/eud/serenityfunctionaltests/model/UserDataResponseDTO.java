package uk.gov.hmcts.reform.laubackend.eud.serenityfunctionaltests.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import uk.gov.hmcts.reform.laubackend.eud.response.ContactInformationResponse;

import java.util.List;
import java.util.Map;

@JsonInclude(JsonInclude.Include.NON_NULL)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class UserDataResponseDTO {

    @JsonProperty("userId")
    private String userId;

    @JsonProperty("email")
    private String email;

    @JsonProperty("roles")
    private List<String> roles;

    @JsonProperty("accountStatus")
    private String accountStatus;

    @JsonProperty("accountCreationDate")
    private String accountCreationDate;

    @JsonProperty("organisationalAddress")
    List<ContactInformationResponse> organisationalAddress;

    @JsonProperty("meta")
    Map<String, Map<String, Integer>> meta;
}
