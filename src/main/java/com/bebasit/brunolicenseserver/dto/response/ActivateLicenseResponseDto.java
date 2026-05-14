package com.bebasit.brunolicenseserver.dto.response;

import com.bebasit.brunolicenseserver.common.LicenceActivationStatus;
import lombok.*;

import java.io.Serial;
import java.io.Serializable;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

@NoArgsConstructor
@AllArgsConstructor
@Getter @Setter
@ToString
public class ActivateLicenseResponseDto implements Serializable {
    @Serial
    private final static long serialVersionUID = 1L;

    private LicenceActivationStatus status;
    private String licenseKey;
    private String deviceId;
    private String deviceName;
    private String email;
    private UUID activationId;
    private Instant activatedAt;
}
