package com.bebasit.brunolicenseserver.dto.pojo;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;
import java.time.LocalDate;

@NoArgsConstructor
@AllArgsConstructor
@Getter @Setter
public class LicenseActivation {
    private String licenseKey;
    private String deviceId;
    private String deviceName;
    private String email;
    private String licenseServerUrl;
    private Instant activatedAt;
}
