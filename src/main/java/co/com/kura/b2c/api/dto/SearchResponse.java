package co.com.kura.b2c.api.dto;

import lombok.*;
import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@Data @Builder @NoArgsConstructor @AllArgsConstructor
public class SearchResponse {
    private UUID id;
    private String code;
    private String name;
    private String description;
    private String serviceType;
    private String category;
    private BigDecimal basePrice;
    private List<BundleDetail> bundleItems;

    @Data @Builder @NoArgsConstructor @AllArgsConstructor
    public static class BundleDetail {
        private String serviceCode;
        private String serviceName;
        private int quantity;
    }
}
