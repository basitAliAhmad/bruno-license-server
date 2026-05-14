package com.bebasit.brunolicenseserver.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serial;
import java.io.Serializable;

@NoArgsConstructor
@AllArgsConstructor
@Getter @Setter
public class LicenseVerificationResponseDto implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    private Boolean verified;
    private LicenseActivationSubscriptionDto subscription;

}
