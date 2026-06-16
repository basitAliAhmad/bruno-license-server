package com.bebasit.brunolicenseserver.controller;

import com.bebasit.brunolicenseserver.dto.request.ActivateLicenseRequestDto;
import com.bebasit.brunolicenseserver.dto.request.OtpActivationRequestDto;
import com.bebasit.brunolicenseserver.dto.response.ActivateLicenseResponseDto;
import com.bebasit.brunolicenseserver.dto.response.LicenseVerificationResponseDto;
import com.bebasit.brunolicenseserver.service.LicenseActivationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.Collections;
import java.util.Map;
import java.util.UUID;

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
import static org.springframework.http.MediaType.TEXT_PLAIN_VALUE;

@Slf4j
@RestController
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class LicenseController {
    private final LicenseActivationService licenseActivationService;

    @GetMapping(value = "status", produces = TEXT_PLAIN_VALUE)
    public ResponseEntity<String> getStatus() {
        return ResponseEntity.ok("Server is running");
    }

    @PostMapping(value = "/v2/license/activate", consumes = APPLICATION_JSON_VALUE, produces = APPLICATION_JSON_VALUE)
    public ResponseEntity<ActivateLicenseResponseDto> activateLicense(@RequestBody ActivateLicenseRequestDto request) {
        return ResponseEntity.ok(licenseActivationService.activateLicense(request));
    }

    @PostMapping(value = "/v1/license/activate/{activationId}", produces = APPLICATION_JSON_VALUE)
    public ResponseEntity<Map<String, String>> activateLicense(@PathVariable UUID activationId, @RequestBody OtpActivationRequestDto request) {
        String jwt = licenseActivationService.activateWithOtp(activationId, request);
        log.info("Generated the activation JWT: {}", jwt);
        return ResponseEntity.ok(Collections.singletonMap("licenseToken", jwt));
    }

    @PostMapping(value = "/v2/license/verify", produces = APPLICATION_JSON_VALUE)
    public ResponseEntity<LicenseVerificationResponseDto> verifyLicense() {
        return ResponseEntity.ok(licenseActivationService.verifyLicense());
    }
}
