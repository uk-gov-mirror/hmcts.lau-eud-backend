package uk.gov.hmcts.reform.laubackend.eud.constants;

import lombok.NoArgsConstructor;

@NoArgsConstructor
public final class CommonConstants {
    public static final String AUTHORISATION_HEADER = "Authorization";
    public static final String SERVICE_AUTHORISATION_HEADER = "ServiceAuthorization";
    public static final String USER_DATA_BY_USERID_PATH = "/api/v2/users/";
    public static final String USER_DATA_BY_EMAIL_PATH = "/api/v2/users-by-email/";
    public static final String REF_DATA_ORGANISATION_DETAILS_PATH = "/refdata/internal/v1/organisations/orgDetails/";
}
