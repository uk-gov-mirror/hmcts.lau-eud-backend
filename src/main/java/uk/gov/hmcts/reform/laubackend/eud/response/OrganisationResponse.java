package uk.gov.hmcts.reform.laubackend.eud.response;

import com.fasterxml.jackson.annotation.JsonAlias;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.util.List;

@Getter
@Setter
@Schema(description = "Organisational Response")
public class OrganisationResponse  implements Serializable {

    public static final long serialVersionUID = 432973378L;

    @Schema(description = "User Organisation Details")
    @JsonAlias("contactInformation")
    List<ContactInformationResponse> organisationalDetails;
}
