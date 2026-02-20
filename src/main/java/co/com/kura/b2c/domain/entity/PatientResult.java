package co.com.kura.b2c.domain.entity;

import jakarta.persistence.*;
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
@Entity
@Table(name = "patient_results")
public class PatientResult {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "order_id", nullable = false)
    private UUID orderId;

    @Column(name = "order_item_id")
    private UUID orderItemId;

    @Column(name = "patient_id")
    private UUID patientId;

    @Column(name = "pos_id", nullable = false)
    private UUID posId;

    @Column(name = "status", nullable = false)
    private String status;

    @Column(name = "result_data", columnDefinition = "TEXT")
    private String resultData;

    @Column(columnDefinition = "TEXT")
    private String notes;

    @Column(name = "sample_taken_at")
    private OffsetDateTime sampleTakenAt;

    @Column(name = "completed_at")
    private OffsetDateTime completedAt;

    @Column(name = "audio_url")
    private String audioUrl;

    @Column(name = "created_at", nullable = false, updatable = false)
    private OffsetDateTime createdAt;

    @Column(name = "updated_at")
    private OffsetDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = OffsetDateTime.now();
        updatedAt = OffsetDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = OffsetDateTime.now();
    }
}
