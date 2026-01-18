# Database Service Package

This package contains the database service interface and implementations.

## Structure

```
service/database/
├── DatabaseService.kt       # Interface and DatabaseResult sealed class
├── filebased/               # File-based implementation (fake database)
│   ├── FileBasedDatabaseService.kt  # Main implementation using JSON files
│   └── LocalFileStorage.kt          # Utility for file I/O operations
└── README.md               # This file
```

## Interface

- **`DatabaseService.kt`**: Defines the interface that all database implementations must follow
- **`DatabaseResult`**: Sealed class for operation results (Success/Error)

## Current Implementation

### File-Based Database (`filebased/`)
- **Location**: `service/database/filebased/`
- **Storage**: JSON files in `~/.bmo-analyst-helper/`
- **Purpose**: Development/testing database that persists data locally
- **Files**:
  - `FileBasedDatabaseService.kt`: Implements `DatabaseService` interface
  - `LocalFileStorage.kt`: Handles JSON file I/O operations

### Data Storage Structure
```
~/.bmo-analyst-helper/
├── submissions.json              # All submission reviews
├── industry_files.json           # Metadata for uploaded industry files
├── industry_files/               # Directory for actual industry files
│   ├── {fileId}_filename.csv
│   └── {fileId}_filename.txt
└── criteria_configs/             # Custom criteria configurations
    ├── tech.json
    └── energy.json
```

## Adding a Real Database Implementation

To implement a real database (SQL, NoSQL, REST API, etc.):

1. Create a new subfolder under `database/`, e.g.:
   - `database/sql/` for SQL database
   - `database/rest/` for REST API
   - `database/mongodb/` for MongoDB, etc.

2. Implement the `DatabaseService` interface in your package

3. Update `Main.kt` to use your implementation:
   ```kotlin
   // Instead of:
   val databaseService: DatabaseService = remember { FileBasedDatabaseService() }
   
   // Use:
   val databaseService: DatabaseService = remember { SqlDatabaseService(connectionString) }
   ```

4. All existing code will work without changes since they use the `DatabaseService` interface

## Example: Adding a SQL Implementation

```kotlin
package org.example.service.database.sql

import org.example.service.database.DatabaseService
import org.example.service.database.DatabaseResult
// ... other imports

class SqlDatabaseService(
    private val connectionString: String
) : DatabaseService {
    // Implement all interface methods using SQL queries
    // ...
}
```

## Notes

- The file-based implementation automatically loads data on startup and persists on every modification
- All implementations should follow the same interface contract
- The interface is designed to be implementation-agnostic
