package org.example.service

import org.apache.pdfbox.Loader
import org.apache.pdfbox.pdmodel.PDDocument
import org.apache.pdfbox.text.PDFTextStripper
import java.io.File

/**
 * Utility for processing business plan documents
 * Extracts text from various document formats
 */
object DocumentProcessor {
    /**
     * Extracts text from a document file
     * @param file The document file to process
     * @return The extracted text, or null if extraction fails
     */
    fun extractText(file: File): String? {
        return try {
            when (file.extension.lowercase()) {
                "pdf" -> extractTextFromPDF(file)
                "txt", "text" -> extractTextFromTXT(file)
                "doc", "docx" -> {
                    // For DOC/DOCX, we would need Apache POI
                    // For now, return a message indicating the format is not yet supported
                    // In a production environment, you could add:
                    // implementation("org.apache.poi:poi:5.2.4")
                    // implementation("org.apache.poi:poi-ooxml:5.2.4")
                    null // Not supported yet without additional dependencies
                }
                else -> null
            }
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
    
    /**
     * Extracts text from a PDF file
     */
    private fun extractTextFromPDF(file: File): String {
        val document = Loader.loadPDF(file)
        try {
            val stripper = PDFTextStripper()
            stripper.startPage = 1
            stripper.endPage = document.numberOfPages
            return stripper.getText(document)
        } finally {
            document.close()
        }
    }
    
    /**
     * Extracts text from a TXT file
     */
    private fun extractTextFromTXT(file: File): String {
        return file.readText()
    }
    
    /**
     * Validates if a file is a supported business plan format
     */
    fun isSupportedFormat(file: File): Boolean {
        val extension = file.extension.lowercase()
        return extension in listOf("pdf", "txt", "text") // Add "doc", "docx" when Apache POI is added
    }
    
    /**
     * Gets the list of supported file extensions
     */
    fun getSupportedExtensions(): List<String> {
        return listOf("pdf", "txt", "text") // Add "doc", "docx" when Apache POI is added
    }
}
