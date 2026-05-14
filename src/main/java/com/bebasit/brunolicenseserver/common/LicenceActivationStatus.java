package com.bebasit.brunolicenseserver.common;

import com.fasterxml.jackson.annotation.JsonValue;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Getter(onMethod = @__(@JsonValue))
public enum LicenceActivationStatus {
    ACTIVATED("activated");

    private final String representation;
}
