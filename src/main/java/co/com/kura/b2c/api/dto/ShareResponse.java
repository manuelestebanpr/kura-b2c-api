package co.com.kura.b2c.api.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.OffsetDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ShareResponse {

    private String patientName;
    private String serviceName;
    private String resultData;
    private OffsetDateTime sharedAt;
    private OffsetDateTime expiresAt;
}
