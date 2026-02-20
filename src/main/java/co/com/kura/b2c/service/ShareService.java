package co.com.kura.b2c.service;

import co.com.kura.b2c.api.dto.ShareResponse;
import co.com.kura.b2c.domain.entity.PatientResult;
import co.com.kura.b2c.domain.entity.ShareLink;
import co.com.kura.b2c.domain.repository.PatientResultRepository;
import co.com.kura.b2c.domain.repository.ShareLinkRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class ShareService {

    private final ShareLinkRepository shareLinkRepository;
    private final PatientResultRepository patientResultRepository;
    private final JdbcTemplate jdbcTemplate;

    @Transactional
    public ShareResponse getSharedResult(String shareUuid) {
        ShareLink shareLink = shareLinkRepository.findByShareUuid(UUID.fromString(shareUuid))
            .orElseThrow(() -> new IllegalArgumentException("Share link not found"));

        // Check expiry
        if (shareLink.getExpiresAt().isBefore(OffsetDateTime.now())) {
            throw new IllegalArgumentException("Share link has expired");
        }

        // Increment access count
        shareLink.setAccessedCount(shareLink.getAccessedCount() + 1);
        shareLinkRepository.save(shareLink);

        // Get result
        PatientResult result = patientResultRepository.findById(shareLink.getResultId())
            .orElseThrow(() -> new IllegalArgumentException("Result not found"));

        // Get patient name via JDBC (User entity removed — owned by Enterprise API)
        String patientName = jdbcTemplate.query(
            "SELECT full_name FROM users WHERE id = ?",
            rs -> rs.next() ? rs.getString("full_name") : "Unknown",
            result.getPatientId()
        );

        return ShareResponse.builder()
            .patientName(patientName)
            .serviceName(null)
            .resultData(result.getResultData())
            .sharedAt(shareLink.getCreatedAt())
            .expiresAt(shareLink.getExpiresAt())
            .build();
    }
}
