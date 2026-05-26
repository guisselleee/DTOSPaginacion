package com.example.weill.attempt;

import com.example.weill.user.WeillUser;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

import java.time.OffsetDateTime;

@Entity
@Table(name = "weill_test_attempts")
public class WeillTestAttempt {

    public static final int MAX_SCORE = 60;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private WeillUser user;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private AttemptStatus status = AttemptStatus.IN_PROGRESS;

    @Column(name = "started_at", nullable = false, insertable = false, updatable = false)
    private OffsetDateTime startedAt;

    @Column(name = "completed_at")
    private OffsetDateTime completedAt;

    @Column(name = "total_score", nullable = false)
    private int totalScore;

    @Column(name = "max_score", nullable = false)
    private int maxScore = MAX_SCORE;

    protected WeillTestAttempt() {
    }

    public WeillTestAttempt(WeillUser user) {
        this.user = user;
    }

    public Long getId() {
        return id;
    }

    public WeillUser getUser() {
        return user;
    }

    public AttemptStatus getStatus() {
        return status;
    }

    public OffsetDateTime getStartedAt() {
        return startedAt;
    }

    public OffsetDateTime getCompletedAt() {
        return completedAt;
    }

    public int getTotalScore() {
        return totalScore;
    }

    public int getMaxScore() {
        return maxScore;
    }

    public boolean isCompleted() {
        return status == AttemptStatus.COMPLETED;
    }

    public void complete(int totalScore) {
        this.totalScore = totalScore;
        this.completedAt = OffsetDateTime.now();
        this.status = AttemptStatus.COMPLETED;
    }
}
