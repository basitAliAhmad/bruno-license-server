package com.bebasit.brunolicenseserver.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.io.Serial;
import java.io.Serializable;

@NoArgsConstructor
@AllArgsConstructor
@Getter @Setter
@ToString
public class OtpActivationRequestDto implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    @NotNull
    private Integer otp;
}
