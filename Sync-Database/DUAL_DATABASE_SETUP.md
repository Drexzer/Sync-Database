# Dual Database Synchronization with Automatic Failover

## Overview
This implementation provides automatic synchronization between MySQL (primary) and PostgreSQL (secondary) databases with seamless failover capabilities. When MySQL is unavailable, the application automatically switches to PostgreSQL without interrupting performance.

## Key Features
- **Automatic Failover**: Seamless switching between MySQL and PostgreSQL
- **Real-time Synchronization**: Bidirectional sync every 30 seconds
- **Health Monitoring**: Database health checks every 10 seconds
- **Zero Downtime**: No application interruption during database failures
- **Automatic Recovery**: Immediate reconnection when MySQL becomes available

## Architecture Components

### 1. DatabaseHealthService
- Monitors MySQL and PostgreSQL health status
- Provides real-time database availability information
- Handles connection validation and timeout management

### 2. DynamicDataSourceRouter
- Routes database connections based on health status
- Automatically switches to PostgreSQL when MySQL fails
- Returns to MySQL when it becomes available

### 3. RealTimeSyncService
- Performs bidirectional synchronization between databases
- Handles MySQL-to-PostgreSQL and PostgreSQL-to-MySQL sync
- Manages catch-up synchronization when databases reconnect

### 4. FailoverDataService
- Provides database operations with automatic failover
- Handles CRUD operations with transparent database switching
- Ensures data consistency across both databases

### 5. ScheduledTaskService
- Performs automatic health checks every 10 seconds
- Triggers synchronization every 30 seconds
- Provides health status reports every 5 minutes

## API Endpoints

### New Failover Endpoints (Recommended)
- `GET /api/v2/users` - Get all users with failover support
- `GET /api/v2/users/{id}` - Get user by ID with failover
- `POST /api/v2/users` - Create user with automatic sync
- `PUT /api/v2/users/{id}` - Update user with automatic sync
- `DELETE /api/v2/users/{id}` - Delete user with automatic sync
- `GET /api/v2/database-status` - Get current database status
- `POST /api/v2/trigger-sync` - Manually trigger synchronization

### Legacy Endpoints (Still Available)
- `GET /api/sync-data` - Manual synchronization trigger
- `GET /api/get-data` - Get data from current primary database

## Setup Instructions

### 1. Update Application Properties
Replace your current `application.properties` with the content from `application-updated.properties`:

```properties
# Copy the content from application-updated.properties
# This includes health check configurations and optimized settings
```

### 2. Database Setup
Ensure both MySQL and PostgreSQL databases are running:

**MySQL (Primary)**
- Host: localhost:3306
- Database: link_db
- Username: root
- Password: root

**PostgreSQL (Secondary)**
- Host: localhost:5432
- Database: postgres
- Username: postgres
- Password: root

### 3. Testing the Implementation

#### Test Failover Scenario
1. Start the application with both databases running
2. Create some users using: `POST /api/v2/users`
3. Stop MySQL service
4. Verify application continues working with PostgreSQL
5. Create more users (they'll be saved to PostgreSQL)
6. Restart MySQL
7. Check logs for automatic reconnection and sync

#### Monitor Health Status
- Check: `GET /api/v2/database-status`
- Watch application logs for health reports every 5 minutes

## Configuration Options

### Health Check Intervals
- Health checks: Every 10 seconds
- Synchronization: Every 30 seconds
- Status reports: Every 5 minutes

### Connection Timeouts
- Connection timeout: 5 seconds
- Validation timeout: 3 seconds
- Leak detection: 60 seconds

## Logging
The application provides detailed logging for:
- Database health status changes
- Failover events
- Synchronization operations
- Connection issues and recovery

Log levels:
- INFO: General operations and status
- DEBUG: Detailed database operations
- ERROR: Failures and exceptions

## Troubleshooting

### Common Issues
1. **Both databases down**: Application will throw exceptions until at least one database is available
2. **Sync conflicts**: The system uses email as a unique identifier for conflict resolution
3. **Connection timeouts**: Adjust timeout values in application.properties if needed

### Monitoring Commands
```bash
# Check application logs
tail -f logs/application.log

# Monitor database connections
# MySQL: SHOW PROCESSLIST;
# PostgreSQL: SELECT * FROM pg_stat_activity;
```

## Performance Considerations
- Async operations prevent blocking during sync
- Connection pooling optimizes database connections
- Batch operations improve sync performance
- Health checks are lightweight and non-blocking

## Security Notes
- Use environment variables for database credentials in production
- Configure SSL connections for both databases
- Implement proper authentication and authorization
- Monitor database access logs

This implementation ensures your application maintains high availability with automatic failover and seamless database synchronization.
