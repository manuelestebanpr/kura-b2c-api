package co.com.kura.b2c.api.controller;

import co.com.kura.b2c.api.dto.PosResponse;
import co.com.kura.b2c.api.dto.SearchResponse;
import co.com.kura.b2c.config.GlobalExceptionHandler;
import co.com.kura.b2c.service.SearchService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class SearchControllerTest {

    private MockMvc mockMvc;

    @Mock
    private SearchService searchService;

    @InjectMocks
    private SearchController searchController;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(searchController)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    private SearchResponse sampleService() {
        return SearchResponse.builder()
                .id(UUID.randomUUID())
                .code("HEM001")
                .name("Hemograma Completo")
                .description("Complete blood count")
                .serviceType("SINGLE")
                .category("HEMATOLOGY")
                .basePrice(new BigDecimal("45000.00"))
                .build();
    }

    @Test
    @DisplayName("GET /search/services?q=hemograma — returns 200 with results")
    void searchServices_withResults() throws Exception {
        when(searchService.searchServices(eq("hemograma"), any(), eq(10)))
                .thenReturn(List.of(sampleService()));

        mockMvc.perform(get("/api/v1/search/services")
                        .param("q", "hemograma")
                        .param("limit", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].code").value("HEM001"))
                .andExpect(jsonPath("$[0].name").value("Hemograma Completo"));
    }

    @Test
    @DisplayName("GET /search/services?q=hemograma — returns 200 with empty list")
    void searchServices_emptyResults() throws Exception {
        when(searchService.searchServices(eq("hemograma"), any(), eq(10)))
                .thenReturn(Collections.emptyList());

        mockMvc.perform(get("/api/v1/search/services")
                        .param("q", "hemograma")
                        .param("limit", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(0)));
    }

    @Test
    @DisplayName("GET /search/services/{code} — returns service by code")
    void getServiceByCode_found() throws Exception {
        SearchResponse service = sampleService();
        when(searchService.getServiceByCode("HEM001")).thenReturn(service);

        mockMvc.perform(get("/api/v1/search/services/HEM001"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("HEM001"))
                .andExpect(jsonPath("$.basePrice").value(45000.00));
    }

    @Test
    @DisplayName("GET /search/services/{code} — unknown code returns 500")
    void getServiceByCode_notFound() throws Exception {
        when(searchService.getServiceByCode("UNKNOWN"))
                .thenThrow(new EmptyResultDataAccessException(1));

        mockMvc.perform(get("/api/v1/search/services/UNKNOWN"))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.status").value(500));
    }

    @Test
    @DisplayName("GET /search/pos — returns active points of service")
    void getPointsOfService() throws Exception {
        PosResponse pos = PosResponse.builder()
                .id(UUID.randomUUID())
                .name("Sede Norte")
                .address("Calle 100 #15-20")
                .city("Bogotá")
                .department("Cundinamarca")
                .build();

        when(searchService.getActivePointsOfService()).thenReturn(List.of(pos));

        mockMvc.perform(get("/api/v1/search/pos"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].name").value("Sede Norte"))
                .andExpect(jsonPath("$[0].city").value("Bogotá"));
    }
}
