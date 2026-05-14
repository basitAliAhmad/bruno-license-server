package com.bebasit.brunolicenseserver.dto.response;

import com.bebasit.brunolicenseserver.common.LicensePlan;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serial;
import java.io.Serializable;

@NoArgsConstructor
@AllArgsConstructor
@Getter @Setter
public class LicenseActivationSubscriptionDto implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    private LicensePlan plan;
}
