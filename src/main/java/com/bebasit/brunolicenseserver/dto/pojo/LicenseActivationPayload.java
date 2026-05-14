package com.bebasit.brunolicenseserver.dto.pojo;

import com.bebasit.brunolicenseserver.common.LicensePlan;
import com.bebasit.brunolicenseserver.common.LicenseType;
import lombok.*;

import java.io.Serial;
import java.io.Serializable;
import java.time.Instant;
import java.time.LocalDate;

@NoArgsConstructor
@AllArgsConstructor
@Getter @Setter
@ToString
public class LicenseActivationPayload implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    private String licenseKey;
    private String email;
    private String deviceId;
    private String deviceName;
    private String licenseServerUrl;
    private LicensePlan plan;
    private LicenseType type;
    private Instant createdAt;
    private Instant updatedAt;
    private Boolean trialActive;
}
