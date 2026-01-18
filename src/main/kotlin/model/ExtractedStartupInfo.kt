package org.example.model

/**
 * Information extracted from a business plan document
 */
data class ExtractedStartupInfo(
    val startupName: String? = null,
    val problemStatement: String? = null,
    val proposedSolution: String? = null
)
