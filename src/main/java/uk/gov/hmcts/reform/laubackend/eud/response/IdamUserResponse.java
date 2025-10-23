package uk.gov.hmcts.reform.laubackend.eud.response;

import com.fasterxml.jackson.annotation.JsonAlias;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.util.List;

@Getter
@Setter
@Schema(description = "Idam User GET Response")
public class IdamUserResponse implements Serializable {

    public static final long serialVersionUID = 432973389L;

    @Schema(description = "IdAM ID of the user.")
    @JsonAlias("id")
    String userId;

    @Schema(description = "Email address/username of the user.")
    String email;

    @Schema(description = "Account Status of user")
    String accountStatus;

    @Schema(description = "User's account creation timestamp in iso-8601-date-and-time-format.")
    @JsonAlias("createDate")
    String accountCreationDate;

    @Schema(description = "User's roles.")
    @JsonAlias("roleNames")
    List<String> roles;

}
