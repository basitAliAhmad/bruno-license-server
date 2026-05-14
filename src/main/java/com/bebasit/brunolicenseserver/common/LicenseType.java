package com.bebasit.brunolicenseserver.common;

import com.fasterxml.jackson.annotation.JsonValue;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Getter(onMethod = @__(@JsonValue))
public enum LicenseType {
    PERSONAL("personal"), TEAM("team"), ENTERPRISE("enterprise");

    private final String licenseType;
}
