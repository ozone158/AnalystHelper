package org.example.model

import java.io.File
import java.util.Date

/**
 * Represents an industry-specific file uploaded by a banker
 */
data class IndustryFile(
    val id: String,
    val industry: String,
    val fileName: String,
    val file: File,
    val uploadedAt: Date,
    val uploadedBy: String = "Bank Officer",
    val description: String? = null
)
