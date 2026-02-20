package co.com.kura.b2c.api.controller;

import co.com.kura.b2c.api.dto.ShareResponse;
import co.com.kura.b2c.config.GlobalExceptionHandler;
import co.com.kura.b2c.service.ShareService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.time.OffsetDateTime;
import java.util.UUID;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class ShareControllerTest {

    private MockMvc mockMvc;

    @Mock
    private ShareService shareService;

    @InjectMocks
    private ShareController shareController;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(shareController)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    private static final String VALID_UUID = "a1b2c3d4-e5f6-7890-abcd-ef1234567890";

    @Test
    @DisplayName("GET /share/{uuid} — valid link returns 200 with ShareResponse")
    void getSharedResult_valid() throws Exception {
        OffsetDateTime now = OffsetDateTime.now();
        ShareResponse response = ShareResponse.builder()
                .patientName("Juan Pérez")
                .serviceName("Hemograma Completo")
                .resultData("{\"wbc\": 7500}")
                .sharedAt(now.minusHours(1))
                .expiresAt(now.plusHours(47))
                .build();

        when(shareService.getSharedResult(VALID_UUID)).thenReturn(response);

        mockMvc.perform(get("/api/v1/share/" + VALID_UUID))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.patientName").value("Juan Pérez"))
                .andExpect(jsonPath("$.resultData").value("{\"wbc\": 7500}"));
    }

    @Test
    @DisplayName("GET /share/{uuid} — expired link returns 400")
    void getSharedResult_expired() throws Exception {
        when(shareService.getSharedResult(VALID_UUID))
                .thenThrow(new IllegalArgumentException("Share link has expired"));

        mockMvc.perform(get("/api/v1/share/" + VALID_UUID))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Share link has expired"));
    }

    @Test
    @DisplayName("GET /share/{uuid} — unknown UUID returns 400")
    void getSharedResult_notFound() throws Exception {
        String unknownUuid = UUID.randomUUID().toString();
        when(shareService.getSharedResult(unknownUuid))
                .thenThrow(new IllegalArgumentException("Share link not found"));

        mockMvc.perform(get("/api/v1/share/" + unknownUuid))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Share link not found"));
    }
}
