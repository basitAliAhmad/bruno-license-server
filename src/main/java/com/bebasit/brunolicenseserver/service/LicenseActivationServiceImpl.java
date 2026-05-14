package com.bebasit.brunolicenseserver.service;

import com.bebasit.brunolicenseserver.common.LicenceActivationStatus;
import com.bebasit.brunolicenseserver.common.LicensePlan;
import com.bebasit.brunolicenseserver.common.LicenseType;
import com.bebasit.brunolicenseserver.dto.pojo.LicenseActivation;
import com.bebasit.brunolicenseserver.dto.pojo.LicenseActivationPayload;
import com.bebasit.brunolicenseserver.dto.request.ActivateLicenseRequestDto;
import com.bebasit.brunolicenseserver.dto.request.OtpActivationRequestDto;
import com.bebasit.brunolicenseserver.dto.response.ActivateLicenseResponseDto;
import com.bebasit.brunolicenseserver.dto.response.LicenseActivationSubscriptionDto;
import com.bebasit.brunolicenseserver.dto.response.LicenseVerificationResponseDto;
import com.bebasit.brunolicenseserver.exception.NoActivationExistException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import tools.jackson.databind.ObjectMapper;

import java.time.Instant;
import java.time.LocalDate;
import java.util.Base64;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
@Service
public class LicenseActivationServiceImpl implements LicenseActivationService {

    private final ObjectMapper objectMapper;

    /**
     * Base64 encoded JWT header segment as:
     * {@code {
     *     "alg": "HS256",
     *     "typ": "JWT"
     * }}
     */
    private final String JWT_HEADER = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9";
//    private final String JWT_SIGNATURE = "czhmSzl2TDJ4UTdtWXBSNE53M1VjVDZaYUIxSGRFNUpyMFdxWHlWdVA4TQo=";
    private final String JWT_SIGNATURE = "c2lnbmF0dXJl";

    private final static Map<UUID, LicenseActivation> PENDING_ACTIVATIONS = new ConcurrentHashMap<>();

    @Override
    public ActivateLicenseResponseDto activateLicense(ActivateLicenseRequestDto request) {
        log.info("Received the activation request as: {}", request);

        UUID activationId = UUID.randomUUID();
        Instant activatedAt = Instant.now();

        log.info(activationId.toString());

        PENDING_ACTIVATIONS.put(activationId, new LicenseActivation(
                request.getLicenseKey(),
                request.getDeviceId(),
                request.getDeviceName(),
                request.getEmail(),
                request.getLicenseServerUrl(),
                activatedAt
        ));

        return new ActivateLicenseResponseDto(
                LicenceActivationStatus.ACTIVATED,
                request.getLicenseKey(),
                request.getDeviceId(),
                request.getDeviceName(),
                request.getEmail(),
                activationId, activatedAt
        );
    }

    @Override
    public String activateWithOtp(UUID activationId, OtpActivationRequestDto requestPayload) {
        log.info("Received the activation request with activation ID: {}", activationId);
        log.info("Received the activation request body as: {}", requestPayload);

        LicenseActivation activation = PENDING_ACTIVATIONS.get(activationId);
        if (activation == null) {
            log.error("No activation exist for activationId: {}", activationId);
            throw new NoActivationExistException(activationId);
        }

        LicenseActivationPayload payload = new LicenseActivationPayload(
                activation.getLicenseKey(),
                activation.getEmail(),
                activation.getDeviceId(),
                activation.getDeviceName(),
                activation.getLicenseServerUrl(),
                LicensePlan.ULTIMATE_EDITION,
                LicenseType.PERSONAL,
                activation.getActivatedAt(),
                Instant.now(),
                false
        );

        PENDING_ACTIVATIONS.remove(activationId);

        return JWT_HEADER + "." + Base64.getEncoder().encodeToString(objectMapper.writeValueAsBytes(payload))
                + "." + JWT_SIGNATURE;
    }

    @Override
    public LicenseVerificationResponseDto verifyLicense() {
        return new LicenseVerificationResponseDto(Boolean.TRUE, new LicenseActivationSubscriptionDto(LicensePlan.ULTIMATE_EDITION));
    }

}
