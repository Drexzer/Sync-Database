# Data Synchronization Project (sync-db)

## What is Data Synchronization?

Data synchronization is the process of ensuring that data in multiple databases or systems remains consistent and up-to-date. In this project, we demonstrate **unidirectional data synchronization** between two different database systems:

- **Source Database**: MySQL (link_db) - Contains the master data
- **Target Database**: PostgreSQL (dlink_db) - Receives synchronized data

### Why Data Synchronization?

1. **Data Consistency**: Ensures data remains consistent across multiple systems
2. **Backup & Redundancy**: Creates data copies for disaster recovery
3. **Cross-Platform Integration**: Enables different systems to work with the same data
4. **Performance Optimization**: Distributes data load across multiple databases
5. **Migration Support**: Facilitates database migration processes

## Project Overview

This Spring Boot application provides a REST API to synchronize user data from a MySQL database to a PostgreSQL database. The synchronization process reads all users from the MySQL database and creates corresponding records in the PostgreSQL database.

### Key Features

- **Dual Database Configuration**: Supports multiple datasources with separate transaction managers
- **REST API**: Simple endpoint to trigger data synchronization
- **Transaction Management**: Ensures data integrity during synchronization
- **Logging**: Detailed console output for monitoring sync progress
- **Entity Mapping**: Uses JPA entities for database operations

## Technology Stack

- **Framework**: Spring Boot 3.5.5
- **Java Version**: 17
- **Databases**: MySQL 8.x, PostgreSQL 13+
- **ORM**: Spring Data JPA with Hibernate
- **Build Tool**: Maven
- **Additional Libraries**: Lombok for boilerplate code reduction

## Project Structure

```
sync-db/
├── src/main/java/com/webkorps/sync_db/
│   ├── SyncDbApplication.java          # Main Spring Boot application
│   ├── config/
│   │   ├── DatabaseConfig.java         # Dual datasource configuration
│   │   ├── LinkDatabaseConfig.java     # MySQL repository configuration
│   │   └── DLinkDatabaseConfig.java    # PostgreSQL repository configuration
│   ├── controller/
│   │   └── DataSyncController.java     # REST API controller
│   ├── entity/
│   │   └── User.java                   # JPA entity for user data
│   ├── repository/
│   │   ├── link/
│   │   │   └── LinkUserRepository.java # MySQL repository
│   │   └── dlink/
│   │       └── DLinkUserRepository.java # PostgreSQL repository
│   └── service/
│       └── DataSyncService.java        # Business logic for synchronization
├── src/main/resources/
│   └── application.properties          # Database configurations
├── setup-mysql.sql                     # MySQL database setup script
├── setup-postgresql.sql                # PostgreSQL database setup script
└── pom.xml                            # Maven dependencies
```

## Prerequisites

Before running this project, ensure you have the following installed:

1. **Java 17** or higher
2. **Maven 3.6+**
3. **MySQL 8.0+** running on localhost:3306
4. **PostgreSQL 13+** running on localhost:5432

## Database Setup

### 1. MySQL Setup

1. Start MySQL server
2. Run the MySQL setup script:

```sql
-- Create MySQL database for link_db
CREATE DATABASE IF NOT EXISTS link_db;
USE link_db;

-- Create users table
CREATE TABLE IF NOT EXISTS users (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(255),
    email VARCHAR(255)
);

-- Insert sample data
INSERT INTO users (name, email) VALUES 
('John Doe', 'john.doe@example.com'),
('Jane Smith', 'jane.smith@example.com'),
('Bob Johnson', 'bob.johnson@example.com');
```

### 2. PostgreSQL Setup

1. Start PostgreSQL server
2. Run the PostgreSQL setup script:

```sql
-- Create PostgreSQL database for dlink_db
CREATE DATABASE dlink_db;

-- Connect to the database
\c dlink_db;

-- Create user table
CREATE TABLE IF NOT EXISTS "user" (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(255),
    email VARCHAR(255)
);
```

### 3. Database Credentials

Ensure your database credentials match those in `application.properties`:

- **MySQL**: username=`root`, password=`root`
- **PostgreSQL**: username=`postgres`, password=`root`

## Installation & Setup

### 1. Clone the Repository

```bash
git clone <repository-url>
cd sync-db
```

### 2. Configure Database Connections

Update `src/main/resources/application.properties` if your database credentials differ:

```properties
# MySQL (Link) Database Configuration
spring.datasource.link.url=jdbc:mysql://localhost:3306/link_db
spring.datasource.link.username=root
spring.datasource.link.password=root

# PostgreSQL (DLink) Database Configuration
spring.datasource.dlink.url=jdbc:postgresql://localhost:5432/dlink_db
spring.datasource.dlink.username=postgres
spring.datasource.dlink.password=root
```

### 3. Build the Project

```bash
mvn clean install
```

### 4. Run the Application

```bash
mvn spring-boot:run
```

The application will start on port `8082`.

## Usage

### Trigger Data Synchronization

Once the application is running, you can trigger data synchronization using the REST API:

**Endpoint**: `GET http://localhost:8082/sync-data`

**Using curl**:
```bash
curl http://localhost:8082/sync-data
```

**Using browser**: Navigate to `http://localhost:8082/sync-data`

### Expected Response

```
Data synced successfully!
```

### Console Output

The application provides detailed logging during synchronization:

```
Found 3 users in MySQL database:
User: John Doe - john.doe@example.com
User: Jane Smith - jane.smith@example.com
User: Bob Johnson - bob.johnson@example.com
Syncing data to PostgreSQL database...
Synced user: John Doe to PostgreSQL
Synced user: Jane Smith to PostgreSQL
Synced user: Bob Johnson to PostgreSQL
Data sync completed successfully!
```

## How It Works

### 1. Dual Database Configuration

The application uses Spring Boot's multiple datasource configuration:

- **Primary DataSource**: MySQL (link_db)
- **Secondary DataSource**: PostgreSQL (dlink_db)

Each datasource has its own:
- EntityManagerFactory
- TransactionManager
- Repository package

### 2. Synchronization Process

1. **Data Retrieval**: Fetch all users from MySQL database using `LinkUserRepository`
2. **Data Transformation**: Create new User objects for PostgreSQL (to avoid ID conflicts)
3. **Data Insertion**: Save users to PostgreSQL database using `DLinkUserRepository`
4. **Logging**: Output progress information to console

### 3. Transaction Management

The synchronization uses the MySQL transaction manager (`@Transactional("linkTransactionManager")`) to ensure data consistency during the read operation.

## Architecture Components

### Entity Layer
- **User.java**: JPA entity representing user data with id, name, and email fields

### Repository Layer
- **LinkUserRepository**: JPA repository for MySQL operations
- **DLinkUserRepository**: JPA repository for PostgreSQL operations

### Service Layer
- **DataSyncService**: Contains business logic for data synchronization

### Controller Layer
- **DataSyncController**: REST API endpoint for triggering synchronization

### Configuration Layer
- **DatabaseConfig**: Configures dual datasources and transaction managers
- **LinkDatabaseConfig**: Configures MySQL repository scanning
- **DLinkDatabaseConfig**: Configures PostgreSQL repository scanning

## Troubleshooting

### Common Issues

1. **Database Connection Errors**
   - Verify MySQL and PostgreSQL servers are running
   - Check database credentials in `application.properties`
   - Ensure databases `link_db` and `dlink_db` exist

2. **Port Conflicts**
   - Change server port in `application.properties`: `server.port=8083`

3. **Permission Issues**
   - Ensure database users have appropriate permissions
   - Grant CREATE, INSERT, SELECT permissions

### Error Messages

- **"Unknown database 'link_db'"**: Create the MySQL database using setup script
- **"database 'dlink_db' does not exist"**: Create the PostgreSQL database using setup script
- **Connection refused**: Verify database servers are running on correct ports

## Future Enhancements

1. **Bidirectional Sync**: Support synchronization in both directions
2. **Incremental Sync**: Only sync changed data since last synchronization
3. **Conflict Resolution**: Handle data conflicts during synchronization
4. **Scheduling**: Add automatic periodic synchronization
5. **Data Validation**: Implement data validation before synchronization
6. **Error Handling**: Improve error handling and rollback mechanisms
7. **Monitoring**: Add metrics and health checks
8. **Configuration UI**: Web interface for managing synchronization settings

## Contributing

1. Fork the repository
2. Create a feature branch
3. Make your changes
4. Add tests if applicable
5. Submit a pull request

## License

This project is licensed under the MIT License.

## Support

For questions or issues, please create an issue in the repository or contact the development team.
