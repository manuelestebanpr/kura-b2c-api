package co.com.kura.b2c.service;

import co.com.kura.b2c.api.dto.ShareResponse;
import co.com.kura.b2c.domain.entity.PatientResult;
import co.com.kura.b2c.domain.entity.ShareLink;
import co.com.kura.b2c.domain.entity.User;
import co.com.kura.b2c.domain.repository.PatientResultRepository;
import co.com.kura.b2c.domain.repository.ShareLinkRepository;
import co.com.kura.b2c.domain.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;

@Slf4j
@Service
@RequiredArgsConstructor
public class ShareService {

    private final ShareLinkRepository shareLinkRepository;
    private final PatientResultRepository patientResultRepository;
    private final UserRepository userRepository;

    @Transactional
    public ShareResponse getSharedResult(String shareUuid) {
        ShareLink shareLink = shareLinkRepository.findByShareUuid(shareUuid)
            .orElseThrow(() -> new IllegalArgumentException("Share link not found"));

        // Check expiry
        if (shareLink.getExpiresAt().isBefore(OffsetDateTime.now())) {
            throw new IllegalArgumentException("Share link has expired");
        }

        // Increment access count
        shareLink.setAccessCount(shareLink.getAccessCount() + 1);
        shareLinkRepository.save(shareLink);

        // Get result
        PatientResult result = patientResultRepository.findById(shareLink.getResultId())
            .orElseThrow(() -> new IllegalArgumentException("Result not found"));

        // Get patient info
        String patientName = userRepository.findById(result.getPatientId())
            .map(User::getFullName)
            .orElse("Unknown");

        return ShareResponse.builder()
            .patientName(patientName)
            .serviceName(null) // Could be fetched from OrderItem if needed
            .resultData(result.getResultData())
            .sharedAt(shareLink.getCreatedAt())
            .expiresAt(shareLink.getExpiresAt())
            .build();
    }
}
