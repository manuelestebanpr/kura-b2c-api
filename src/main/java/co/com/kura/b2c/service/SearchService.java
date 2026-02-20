package co.com.kura.b2c.service;

import co.com.kura.b2c.api.dto.PosResponse;
import co.com.kura.b2c.api.dto.SearchResponse;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@Service
public class SearchService {

    private final JdbcTemplate jdbcTemplate;

    public SearchService(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Cacheable(value = "service-search", key = "#q + '-' + #posId + '-' + #limit")
    public List<SearchResponse> searchServices(String q, UUID posId, int limit) {
        String sql = """
                SELECT id, code, name, description, service_type, category, base_price
                FROM master_services
                WHERE deleted_at IS NULL AND name % ?
                ORDER BY similarity(name, ?) DESC
                LIMIT ?
                """;
        return jdbcTemplate.query(sql, (rs, rowNum) -> SearchResponse.builder()
                        .id(UUID.fromString(rs.getString("id")))
                        .code(rs.getString("code"))
                        .name(rs.getString("name"))
                        .description(rs.getString("description"))
                        .serviceType(rs.getString("service_type"))
                        .category(rs.getString("category"))
                        .basePrice(rs.getBigDecimal("base_price"))
                        .build(),
                q, q, limit);
    }

    @Cacheable(value = "service-detail", key = "#code")
    public SearchResponse getServiceByCode(String code) {
        String sql = "SELECT id, code, name, description, service_type, category, base_price FROM master_services WHERE code = ? AND deleted_at IS NULL";
        return jdbcTemplate.queryForObject(sql, (rs, rowNum) -> SearchResponse.builder()
                        .id(UUID.fromString(rs.getString("id")))
                        .code(rs.getString("code"))
                        .name(rs.getString("name"))
                        .description(rs.getString("description"))
                        .serviceType(rs.getString("service_type"))
                        .category(rs.getString("category"))
                        .basePrice(rs.getBigDecimal("base_price"))
                        .build(),
                code);
    }

    @Cacheable(value = "pos-locations")
    public List<PosResponse> getActivePointsOfService() {
        String sql = "SELECT id, name, address, city, department FROM points_of_service WHERE deleted_at IS NULL AND is_active = true ORDER BY city, name";
        return jdbcTemplate.query(sql, (rs, rowNum) -> PosResponse.builder()
                .id(UUID.fromString(rs.getString("id")))
                .name(rs.getString("name"))
                .address(rs.getString("address"))
                .city(rs.getString("city"))
                .department(rs.getString("department"))
                .build());
    }
}
