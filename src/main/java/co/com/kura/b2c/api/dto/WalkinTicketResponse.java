package co.com.kura.b2c.api.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.OffsetDateTime;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WalkinTicketResponse {

    private UUID id;
    private String ticketCode;
    private OffsetDateTime expiresAt;
    private OffsetDateTime redeemedAt;
}
