package org.example.model

import org.example.model.StartupSubmissionData
import java.util.Date

/**
 * Submission review model for bank officers
 */
data class SubmissionReview(
    val id: String,
    val submissionData: StartupSubmissionData,
    val analysisResult: AnalysisResult,
    val status: ReviewStatus,
    val notes: List<ReviewNote>,
    val createdAt: Date,
    val updatedAt: Date
)

enum class ReviewStatus {
    PENDING,    // Awaiting review
    IN_REVIEW,  // Currently being reviewed
    APPROVED,   // Approved for funding
    PARTIAL,    // Partial approval
    DECLINED    // Declined
}

data class ReviewNote(
    val id: String,
    val content: String,
    val createdAt: Date,
    val createdBy: String = "Bank Officer"
)
