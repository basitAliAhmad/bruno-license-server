package com.bebasit.brunolicenseserver.dto.request;

import lombok.*;

import java.io.Serial;
import java.io.Serializable;

@NoArgsConstructor
@AllArgsConstructor
@Getter @Setter
@ToString
public class ActivateLicenseRequestDto implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    private String licenseKey;
    private String deviceId;
    private String deviceName;
    private String email;
    private String licenseServerUrl;
}

