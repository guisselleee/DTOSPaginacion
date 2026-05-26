package com.example.weill.attempt;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

import java.time.OffsetDateTime;

@Entity
@Table(
    name = "weill_item_responses",
    uniqueConstraints = @UniqueConstraint(
        name = "uq_weill_response_item",
        columnNames = {"attempt_id", "page_number", "row_number", "item_number"}
    )
)
public class ItemResponse {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "attempt_id", nullable = false)
    private WeillTestAttempt attempt;

    @Column(name = "page_number", nullable = false)
    private short pageNumber;

    @Column(name = "row_number", nullable = false)
    private short rowNumber;

    @Column(name = "item_number", nullable = false)
    private short itemNumber;

    @Column(name = "selected_option", nullable = false)
    private short selectedOption;

    @Column(name = "correct_option", nullable = false)
    private short correctOption;

    @Column(name = "is_correct", nullable = false)
    private boolean correct;

    @Column(nullable = false)
    private short score;

    @Column(name = "answered_at", nullable = false)
    private OffsetDateTime answeredAt = OffsetDateTime.now();

    protected ItemResponse() {
    }

    public ItemResponse(WeillTestAttempt attempt, int pageNumber, int rowNumber, int itemNumber) {
        this.attempt = attempt;
        this.pageNumber = (short) pageNumber;
        this.rowNumber = (short) rowNumber;
        this.itemNumber = (short) itemNumber;
    }

    public Long getId() {
        return id;
    }

    public WeillTestAttempt getAttempt() {
        return attempt;
    }

    public int getPageNumber() {
        return pageNumber;
    }

    public int getRowNumber() {
        return rowNumber;
    }

    public int getItemNumber() {
        return itemNumber;
    }

    public int getSelectedOption() {
        return selectedOption;
    }

    public int getCorrectOption() {
        return correctOption;
    }

    public boolean isCorrect() {
        return correct;
    }

    public int getScore() {
        return score;
    }

    public OffsetDateTime getAnsweredAt() {
        return answeredAt;
    }

    public void answer(int selectedOption, int correctOption) {
        this.selectedOption = (short) selectedOption;
        this.correctOption = (short) correctOption;
        this.correct = selectedOption == correctOption;
        this.score = (short) (correct ? 1 : 0);
        this.answeredAt = OffsetDateTime.now();
    }
}
