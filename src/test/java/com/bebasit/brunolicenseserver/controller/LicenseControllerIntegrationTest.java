package com.bebasit.brunolicenseserver.controller;

import com.bebasit.brunolicenseserver.dto.request.ActivateLicenseRequestDto;
import com.bebasit.brunolicenseserver.dto.request.OtpActivationRequestDto;
import com.bebasit.brunolicenseserver.service.LicenseActivationServiceImpl;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;
import tools.jackson.databind.ObjectMapper;

import java.lang.reflect.Field;
import java.util.Map;
import java.util.UUID;

import static org.hamcrest.Matchers.matchesPattern;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK)
class LicenseControllerIntegrationTest {

    @Autowired
    private WebApplicationContext context;

    @Autowired
    private ObjectMapper objectMapper;

    private MockMvc mockMvc;

    @BeforeEach
    void setup() {
        mockMvc = MockMvcBuilders.webAppContextSetup(context).build();
    }

    @AfterEach
    void clearPendingActivations() throws Exception {
        Field field = LicenseActivationServiceImpl.class.getDeclaredField("PENDING_ACTIVATIONS");
        field.setAccessible(true);
        ((Map<?, ?>) field.get(null)).clear();
    }

    // --- GET /status ---

    @Test
    void status_returns200() throws Exception {
        mockMvc.perform(get("/status"))
                .andExpect(status().isOk());
    }

    @Test
    void status_returnsServerIsRunningMessage() throws Exception {
        mockMvc.perform(get("/status"))
                .andExpect(content().string("Server is running"));
    }

    @Test
    void status_returnsPlainTextContentType() throws Exception {
        mockMvc.perform(get("/status"))
                .andExpect(content().contentTypeCompatibleWith(MediaType.TEXT_PLAIN));
    }

    // --- POST /v2/license/activate ---

    @Test
    void activateLicense_returns200() throws Exception {
        mockMvc.perform(post("/v2/license/activate")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(buildRequest())))
                .andExpect(status().isOk());
    }

    @Test
    void activateLicense_responseHasActivatedStatus() throws Exception {
        mockMvc.perform(post("/v2/license/activate")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(buildRequest())))
                .andExpect(jsonPath("$.status").value("activated"));
    }

    @Test
    void activateLicense_responseContainsActivationId() throws Exception {
        mockMvc.perform(post("/v2/license/activate")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(buildRequest())))
                .andExpect(jsonPath("$.activationId").isNotEmpty());
    }

    @Test
    void activateLicense_echoesDeviceAndEmailFields() throws Exception {
        ActivateLicenseRequestDto request = buildRequest();
        mockMvc.perform(post("/v2/license/activate")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(jsonPath("$.deviceId").value(request.getDeviceId()))
                .andExpect(jsonPath("$.deviceName").value(request.getDeviceName()))
                .andExpect(jsonPath("$.email").value(request.getEmail()));
    }

    // --- POST /v1/license/activate/{activationId} ---

    @Test
    void activateWithOtp_returns200() throws Exception {
        String activationId = extractActivationId();
        mockMvc.perform(post("/v1/license/activate/" + activationId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new OtpActivationRequestDto(123456))))
                .andExpect(status().isOk());
    }

    @Test
    void activateWithOtp_returnsLicenseToken() throws Exception {
        String activationId = extractActivationId();
        mockMvc.perform(post("/v1/license/activate/" + activationId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new OtpActivationRequestDto(123456))))
                .andExpect(jsonPath("$.licenseToken").isNotEmpty());
    }

    @Test
    void activateWithOtp_licenseTokenIsThreePartJwt() throws Exception {
        String activationId = extractActivationId();
        mockMvc.perform(post("/v1/license/activate/" + activationId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new OtpActivationRequestDto(123456))))
                .andExpect(jsonPath("$.licenseToken").value(matchesPattern("^[^.]+\\.[^.]+\\.[^.]+$")));
    }

    @Test
    void activateWithOtp_unknownActivationId_returns404() throws Exception {
        mockMvc.perform(post("/v1/license/activate/" + UUID.randomUUID())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new OtpActivationRequestDto(123456))))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("Invalid activationId"));
    }

    // --- POST /v2/license/verify ---

    @Test
    void verifyLicense_returns200() throws Exception {
        mockMvc.perform(post("/v2/license/verify"))
                .andExpect(status().isOk());
    }

    @Test
    void verifyLicense_responseHasVerifiedTrue() throws Exception {
        mockMvc.perform(post("/v2/license/verify"))
                .andExpect(jsonPath("$.verified").value(true));
    }

    @Test
    void verifyLicense_responseHasUltimateEditionPlan() throws Exception {
        mockMvc.perform(post("/v2/license/verify"))
                .andExpect(jsonPath("$.subscription.plan").value("ULTIMATE_EDITION"));
    }

    // --- helpers ---

    private String extractActivationId() throws Exception {
        MvcResult result = mockMvc.perform(post("/v2/license/activate")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(buildRequest())))
                .andReturn();
        Map<String, Object> body = objectMapper.readValue(
                result.getResponse().getContentAsString(),
                new tools.jackson.core.type.TypeReference<>() {});
        return (String) body.get("activationId");
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
