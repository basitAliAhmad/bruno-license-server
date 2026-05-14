package com.bebasit.brunolicenseserver.service;

import com.bebasit.brunolicenseserver.dto.request.ActivateLicenseRequestDto;
import com.bebasit.brunolicenseserver.dto.request.OtpActivationRequestDto;
import com.bebasit.brunolicenseserver.dto.response.ActivateLicenseResponseDto;
import com.bebasit.brunolicenseserver.dto.response.LicenseVerificationResponseDto;

import java.util.UUID;

public interface LicenseActivationService {
    ActivateLicenseResponseDto activateLicense(ActivateLicenseRequestDto request);

    String activateWithOtp(UUID activationId, OtpActivationRequestDto requestPayload);

    LicenseVerificationResponseDto verifyLicense();
}
