package com.bebasit.brunolicenseserver.service;

import com.bebasit.brunolicenseserver.common.LicenceActivationStatus;
import com.bebasit.brunolicenseserver.common.LicensePlan;
import com.bebasit.brunolicenseserver.dto.request.ActivateLicenseRequestDto;
import com.bebasit.brunolicenseserver.dto.request.OtpActivationRequestDto;
import com.bebasit.brunolicenseserver.dto.response.ActivateLicenseResponseDto;
import com.bebasit.brunolicenseserver.dto.response.LicenseVerificationResponseDto;
import com.bebasit.brunolicenseserver.exception.NoActivationExistException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.lang.reflect.Field;
import java.util.Base64;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
class LicenseActivationServiceTest {

    @Autowired
    private LicenseActivationService service;

    @AfterEach
    void clearPendingActivations() throws Exception {
        Field field = LicenseActivationServiceImpl.class.getDeclaredField("PENDING_ACTIVATIONS");
        field.setAccessible(true);
        ((Map<?, ?>) field.get(null)).clear();
    }

    // --- activateLicense ---

    @Test
    void activateLicense_returnsActivatedStatus() {
        ActivateLicenseResponseDto response = service.activateLicense(buildRequest());
        assertThat(response.getStatus()).isEqualTo(LicenceActivationStatus.ACTIVATED);
    }

    @Test
    void activateLicense_echoesRequestFields() {
        ActivateLicenseRequestDto request = buildRequest();
        ActivateLicenseResponseDto response = service.activateLicense(request);

        assertThat(response.getLicenseKey()).isEqualTo(request.getLicenseKey());
        assertThat(response.getDeviceId()).isEqualTo(request.getDeviceId());
        assertThat(response.getDeviceName()).isEqualTo(request.getDeviceName());
        assertThat(response.getEmail()).isEqualTo(request.getEmail());
    }

    @Test
    void activateLicense_generatesUniqueActivationIds() {
        UUID first = service.activateLicense(buildRequest()).getActivationId();
        UUID second = service.activateLicense(buildRequest()).getActivationId();
        assertThat(first).isNotEqualTo(second);
    }

    @Test
    void activateLicense_setsActivatedAt() {
        ActivateLicenseResponseDto response = service.activateLicense(buildRequest());
        assertThat(response.getActivatedAt()).isNotNull();
    }

    // --- activateWithOtp ---

    @Test
    void activateWithOtp_returnsThreePartJwt() {
        UUID activationId = service.activateLicense(buildRequest()).getActivationId();
        String jwt = service.activateWithOtp(activationId, new OtpActivationRequestDto(123456));
        assertThat(jwt.split("\\.")).hasSize(3);
    }

    @Test
    void activateWithOtp_jwtHeaderIsHs256() {
        UUID activationId = service.activateLicense(buildRequest()).getActivationId();
        String jwt = service.activateWithOtp(activationId, new OtpActivationRequestDto(123456));
        assertThat(jwt.split("\\.")[0]).isEqualTo("eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9");
    }

    @Test
    void activateWithOtp_jwtPayloadContainsLicenseKey() {
        ActivateLicenseRequestDto request = buildRequest();
        UUID activationId = service.activateLicense(request).getActivationId();
        String jwt = service.activateWithOtp(activationId, new OtpActivationRequestDto(123456));

        String payload = new String(Base64.getDecoder().decode(jwt.split("\\.")[1]));
        assertThat(payload).contains(request.getLicenseKey());
    }

    @Test
    void activateWithOtp_jwtPayloadContainsUltimateEditionPlan() {
        UUID activationId = service.activateLicense(buildRequest()).getActivationId();
        String jwt = service.activateWithOtp(activationId, new OtpActivationRequestDto(123456));

        String payload = new String(Base64.getDecoder().decode(jwt.split("\\.")[1]));
        assertThat(payload).contains("ULTIMATE_EDITION");
    }

    @Test
    void activateWithOtp_consumesActivation() {
        UUID activationId = service.activateLicense(buildRequest()).getActivationId();
        service.activateWithOtp(activationId, new OtpActivationRequestDto(123456));

        assertThatThrownBy(() -> service.activateWithOtp(activationId, new OtpActivationRequestDto(123456)))
                .isInstanceOf(NoActivationExistException.class);
    }

    @Test
    void activateWithOtp_unknownId_throwsNoActivationExistException() {
        UUID unknownId = UUID.randomUUID();
        assertThatThrownBy(() -> service.activateWithOtp(unknownId, new OtpActivationRequestDto(123456)))
                .isInstanceOf(NoActivationExistException.class)
                .hasMessageContaining(unknownId.toString());
    }

    // --- verifyLicense ---

    @Test
    void verifyLicense_returnsVerifiedTrue() {
        LicenseVerificationResponseDto response = service.verifyLicense();
        assertThat(response.getVerified()).isTrue();
    }

    @Test
    void verifyLicense_returnsUltimateEditionPlan() {
        LicenseVerificationResponseDto response = service.verifyLicense();
        assertThat(response.getSubscription().getPlan()).isEqualTo(LicensePlan.ULTIMATE_EDITION);
    }

    @Test
    void verifyLicense_subscriptionIsNotNull() {
        LicenseVerificationResponseDto response = service.verifyLicense();
        assertThat(response.getSubscription()).isNotNull();
    }

    private ActivateLicenseRequestDto buildRequest() {
        return new ActivateLicenseRequestDto(
                "TEST-LICENSE-KEY-001",
                "device-001",
                "Test Machine",
                "test@example.com",
                "http://localhost:9090"
        );
    }
}