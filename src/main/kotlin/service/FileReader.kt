package org.example.service

import org.example.model.IndustryFile
import java.io.File

/**
 * Utility for reading industry files (CSV, TXT) for AI analysis
 */
object FileReader {
    /**
     * Reads the content of an industry file
     * @param file The industry file to read
     * @return The file content as a string, or null if reading fails
     */
    fun readFileContent(file: IndustryFile): String? {
        return try {
            when (file.file.extension.lowercase()) {
                "csv" -> readCSVFile(file.file)
                "txt" -> readTXTFile(file.file)
                else -> null
            }
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
    
    /**
     * Reads a CSV file and formats it for AI analysis
     */
    private fun readCSVFile(file: File): String {
        val lines = file.readLines()
        if (lines.isEmpty()) return "Empty CSV file"
        
        // Return CSV content with header information
        val header = if (lines.isNotEmpty()) lines[0] else ""
        val dataRows = if (lines.size > 1) lines.subList(1, minOf(lines.size, 101)) else emptyList() // Limit to 100 rows
        
        val sb = StringBuilder()
        sb.appendLine("CSV File: ${file.name}")
        sb.appendLine("Header: $header")
        sb.appendLine("Data rows (showing up to 100):")
        dataRows.forEach { row ->
            sb.appendLine(row)
        }
        if (lines.size > 101) {
            sb.appendLine("... (${lines.size - 101} more rows)")
        }
        
        return sb.toString()
    }
    
    /**
     * Reads a TXT file
     */
    private fun readTXTFile(file: File): String {
        val content = file.readText()
        return "TXT File: ${file.name}\n\n$content"
    }
    
    /**
     * Formats multiple industry files for AI prompt
     */
    fun formatIndustryFilesForPrompt(files: List<IndustryFile>): String {
        if (files.isEmpty()) return ""
        
        val sb = StringBuilder()
        sb.appendLine("\n=== INDUSTRY STATISTICS AND DATA FILES ===")
        sb.appendLine("The following industry-specific data files have been provided by bank officers.")
        sb.appendLine("Use this data to inform your analysis, especially for market opportunity, competitive analysis, and industry benchmarks.")
        sb.appendLine()
        
        files.forEachIndexed { index, file ->
            val content = readFileContent(file)
            if (content != null) {
                sb.appendLine("--- File ${index + 1}: ${file.fileName} ---")
                if (file.description != null) {
                    sb.appendLine("Description: ${file.description}")
                }
                sb.appendLine(content)
                sb.appendLine()
            }
        }
        
        return sb.toString()
    }
}
