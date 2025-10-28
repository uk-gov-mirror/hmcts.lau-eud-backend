package uk.gov.hmcts.reform.laubackend.eud.response;

public record ContactInformationResponse(
    String addressLine1,
    String addressLine2,
    String addressLine3,
    String townCity,
    String county,
    String country,
    String postCode
) {}
