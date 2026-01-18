package org.example.service.database.filebased

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.SerializationFeature
import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import java.io.File

/**
 * Local file-based storage for database service
 * Stores data in JSON files in user's application data directory
 * Location: ~/.bmo-analyst-helper/
 */
object LocalFileStorage {
    private val objectMapper = ObjectMapper().apply {
        registerKotlinModule()
        configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, true) // Use timestamps for java.util.Date
        configure(SerializationFeature.INDENT_OUTPUT, true)
        configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
    }
    
    /**
     * Gets the local data directory for the application
     * Creates it if it doesn't exist
     */
    private fun getDataDirectory(): File {
        val userHome = System.getProperty("user.home")
        val dataDir = File(userHome, ".bmo-analyst-helper")
        
        if (!dataDir.exists()) {
            dataDir.mkdirs()
        }
        
        return dataDir
    }
    
    /**
     * Gets the submissions storage file
     */
    fun getSubmissionsFile(): File {
        return File(getDataDirectory(), "submissions.json")
    }
    
    /**
     * Gets the industry files metadata storage file
     */
    fun getIndustryFilesMetadataFile(): File {
        return File(getDataDirectory(), "industry_files.json")
    }
    
    /**
     * Gets the directory for storing uploaded industry files
     */
    fun getIndustryFilesDirectory(): File {
        val dir = File(getDataDirectory(), "industry_files")
        if (!dir.exists()) {
            dir.mkdirs()
        }
        return dir
    }
    
    /**
     * Gets the directory for storing criteria configurations
     */
    fun getCriteriaConfigDirectory(): File {
        val dir = File(getDataDirectory(), "criteria_configs")
        if (!dir.exists()) {
            dir.mkdirs()
        }
        return dir
    }
    
    /**
     * Saves submissions to JSON file
     */
    fun saveSubmissions(submissions: List<org.example.model.SubmissionReview>) {
        try {
            val file = getSubmissionsFile()
            objectMapper.writeValue(file, submissions)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
    
    /**
     * Loads submissions from JSON file
     */
    fun loadSubmissions(): List<org.example.model.SubmissionReview> {
        return try {
            val file = getSubmissionsFile()
            if (file.exists() && file.length() > 0) {
                objectMapper.readValue(file, object : com.fasterxml.jackson.core.type.TypeReference<List<org.example.model.SubmissionReview>>() {})
            } else {
                emptyList()
            }
        } catch (e: Exception) {
            e.printStackTrace()
            emptyList()
        }
    }
    
    /**
     * Data class for storing industry file metadata (without File object)
     */
    data class IndustryFileMetadata(
        val id: String,
        val industry: String,
        val fileName: String,
        val filePath: String, // Relative path from industry files directory
        val uploadedAt: Long, // Timestamp
        val uploadedBy: String = "Bank Officer",
        val description: String? = null
    )
    
    /**
     * Converts IndustryFile to metadata
     */
    fun toMetadata(file: org.example.model.IndustryFile): IndustryFileMetadata {
        val storageDir = getIndustryFilesDirectory()
        val storedFile = File(storageDir, "${file.id}_${file.fileName}")
        return IndustryFileMetadata(
            id = file.id,
            industry = file.industry,
            fileName = file.fileName,
            filePath = storedFile.name,
            uploadedAt = file.uploadedAt.time,
            uploadedBy = file.uploadedBy,
            description = file.description
        )
    }
    
    /**
     * Converts metadata back to IndustryFile
     */
    fun fromMetadata(metadata: IndustryFileMetadata): org.example.model.IndustryFile? {
        return try {
            val storageDir = getIndustryFilesDirectory()
            val storedFile = File(storageDir, metadata.filePath)
            if (storedFile.exists()) {
                org.example.model.IndustryFile(
                    id = metadata.id,
                    industry = metadata.industry,
                    fileName = metadata.fileName,
                    file = storedFile,
                    uploadedAt = java.util.Date(metadata.uploadedAt),
                    uploadedBy = metadata.uploadedBy,
                    description = metadata.description
                )
            } else {
                null
            }
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
    
    /**
     * Saves industry files metadata to JSON file
     */
    fun saveIndustryFilesMetadata(files: List<org.example.model.IndustryFile>) {
        try {
            val file = getIndustryFilesMetadataFile()
            val metadata = files.map { toMetadata(it) }
            objectMapper.writeValue(file, metadata)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
    
    /**
     * Loads industry files metadata from JSON file
     */
    fun loadIndustryFilesMetadata(): List<org.example.model.IndustryFile> {
        return try {
            val file = getIndustryFilesMetadataFile()
            if (file.exists() && file.length() > 0) {
                val metadataList = objectMapper.readValue(file, object : com.fasterxml.jackson.core.type.TypeReference<List<IndustryFileMetadata>>() {})
                metadataList.mapNotNull { fromMetadata(it) }
            } else {
                emptyList()
            }
        } catch (e: Exception) {
            e.printStackTrace()
            emptyList()
        }
    }
    
    /**
     * Copies a file to the industry files storage directory
     */
    fun storeIndustryFile(sourceFile: File, fileId: String, originalFileName: String): File {
        val storageDir = getIndustryFilesDirectory()
        val storedFile = File(storageDir, "${fileId}_${originalFileName}")
        sourceFile.copyTo(storedFile, overwrite = true)
        return storedFile
    }
    
    /**
     * Saves criteria configuration to JSON file
     */
    fun saveCriteriaConfig(industry: String, config: org.example.model.CriteriaConfig) {
        try {
            val dir = getCriteriaConfigDirectory()
            val file = File(dir, "${industry.lowercase()}.json")
            objectMapper.writeValue(file, config)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
    
    /**
     * Loads criteria configuration from JSON file
     */
    fun loadCriteriaConfig(industry: String): org.example.model.CriteriaConfig? {
        return try {
            val dir = getCriteriaConfigDirectory()
            val file = File(dir, "${industry.lowercase()}.json")
            if (file.exists() && file.length() > 0) {
                objectMapper.readValue(file, org.example.model.CriteriaConfig::class.java)
            } else {
                null
            }
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
    
    /**
     * Gets list of all industries with custom criteria configurations
     */
    fun getIndustriesWithCustomCriteria(): List<String> {
        return try {
            val dir = getCriteriaConfigDirectory()
            dir.listFiles { _, name -> name.endsWith(".json") }
                ?.map { it.nameWithoutExtension.replaceFirstChar { char -> char.uppercase() } }
                ?.sorted()
                ?: emptyList()
        } catch (e: Exception) {
            e.printStackTrace()
            emptyList()
        }
    }
}
