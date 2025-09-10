package com.webkorps.sync_db.service;

import com.webkorps.sync_db.entity.User;
import com.webkorps.sync_db.repository.dlink.DLinkUserRepository;
import com.webkorps.sync_db.repository.link.LinkUserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

@Service
public class UserFailoverService {

    private static final Logger logger = LoggerFactory.getLogger(UserFailoverService.class);

    @Autowired
    private LinkUserRepository linkUserRepository;

    @Autowired
    private DLinkUserRepository dlinkUserRepository;

    @Autowired
    @Qualifier("mysqlDataSource")
    private DataSource mysqlDataSource;

    @Autowired
    @Qualifier("postgresDataSource")
    private DataSource postgresDataSource;


    private boolean isMysqlHealthy() {
        try (Connection conn = mysqlDataSource.getConnection()) {
            return conn.isValid(2);
        } catch (SQLException e) {
            logger.warn("MySQL health check failed: {}", e.getMessage());
            return false;
        }
    }


    private boolean isPostgresHealthy() {
        try (Connection conn = postgresDataSource.getConnection()) {
            return conn.isValid(2);
        } catch (SQLException e) {
            logger.warn("PostgreSQL health check failed: {}", e.getMessage());
            return false;
        }
    }


    public User saveUser(User user) {
        User savedUser = null;

        try {
            if (isMysqlHealthy()) {
                // Primary save to MySQL
                savedUser = linkUserRepository.save(user);
                logger.info("User saved to MySQL: {}", savedUser.getEmail());

                // Try to sync to PostgreSQL if healthy
                if (isPostgresHealthy()) {
                    try {
                        // Simply try to save - if duplicate, catch the exception
//                        User syncUser = new User(user.getName(), user.getEmail());
                        dlinkUserRepository.save(user);
                        logger.info("User synced to PostgreSQL: {}", user.getEmail());
                    } catch (Exception syncException) {
                        // Check if it's a duplicate entry error
                        if (syncException.getMessage() != null && 
                            syncException.getMessage().contains("Duplicate entry")) {
                            logger.info("User already exists in PostgreSQL, skipping sync: {}", user.getEmail());
                        } else {
                            logger.warn("Failed to sync to PostgreSQL: {}", syncException.getMessage());
                        }
                    }
                }

            } else if (isPostgresHealthy()) {
                // MySQL down → save to PostgreSQL
                savedUser = dlinkUserRepository.save(user);
                logger.info("MySQL down. User saved to PostgreSQL: {}", savedUser.getEmail());
            } else {
                throw new RuntimeException("No healthy database available for saving user");
            }

            return savedUser;

        } catch (Exception e) {
            logger.error("Error saving user {}: {}", user.getEmail(), e.getMessage());
            throw new RuntimeException("Failed to save user: " + e.getMessage());
        }
    }

    /** Get all users (failover support) */
    public List<User> getAllUsers() {
        if (isMysqlHealthy()) {
            return linkUserRepository.findAll();
        } else if (isPostgresHealthy()) {
            logger.info("MySQL down. Fetching from PostgreSQL");
            return dlinkUserRepository.findAll();
        } else {
            return Collections.emptyList();
        }
    }

    /** Find user by ID (failover support) */
    public Optional<User> getUserById(Long id) {
        if (isMysqlHealthy()) {
            return linkUserRepository.findById(id);
        } else if (isPostgresHealthy()) {
            return dlinkUserRepository.findById(id);
        } else {
            return Optional.empty();
        }
    }

    /** Find user by ID with failover support (alternative method name for controller) */
    public Optional<User> findUserById(Long id) {
        return getUserById(id);
    }

    /** Delete user with failover and sync */
    public void deleteUser(Long id) {
        try {
            if (isMysqlHealthy()) {
                linkUserRepository.deleteById(id);
                logger.info("User deleted from MySQL: ID {}", id);
                
                // Try to sync delete to PostgreSQL if healthy
                if (isPostgresHealthy()) {
                    try {
                        dlinkUserRepository.deleteById(id);
                        logger.info("Delete synced to PostgreSQL: ID {}", id);
                    } catch (Exception e) {
                        logger.warn("Failed to sync delete to PostgreSQL: {}", e.getMessage());
                    }
                }
            } else if (isPostgresHealthy()) {
                dlinkUserRepository.deleteById(id);
                logger.info("MySQL down. User deleted from PostgreSQL: ID {}", id);
            } else {
                throw new RuntimeException("No healthy database available for deleting user");
            }
        } catch (Exception e) {
            logger.error("Error deleting user ID {}: {}", id, e.getMessage());
            throw new RuntimeException("Failed to delete user: " + e.getMessage());
        }
    }

    /** Manual bidirectional sync with duplicate handling */
    public void syncDatabases() {
        if (!isMysqlHealthy() || !isPostgresHealthy()) {
            logger.warn("Cannot sync. One or both DBs are unhealthy.");
            return;
        }

        try {
            logger.info("Starting bidirectional synchronization with duplicate handling");

            // Fetch all users from both DBs
            List<User> mysqlUsers = linkUserRepository.findAll();
            List<User> postgresUsers = dlinkUserRepository.findAll();

            // Sync missing users MySQL → PostgreSQL
            for (User mysqlUser : mysqlUsers) {
                boolean exists = postgresUsers.stream()
                        .anyMatch(p -> p.getEmail().equals(mysqlUser.getEmail()));

                if (!exists) {
                    try {
                        User newUser = new User(mysqlUser.getName(), mysqlUser.getEmail());
                        dlinkUserRepository.save(newUser);
                        logger.info("Synced user to PostgreSQL: {}", newUser.getEmail());
                    } catch (Exception e) {
                        logger.warn("Failed to sync user {} to PostgreSQL: {}",
                                mysqlUser.getEmail(), e.getMessage());
                    }
                }
            }

            // Sync missing users PostgreSQL → MySQL
            for (User postgresUser : postgresUsers) {
                boolean exists = mysqlUsers.stream()
                        .anyMatch(m -> m.getEmail().equals(postgresUser.getEmail()));

                if (!exists) {
                    try {
                        User newUser = new User(postgresUser.getName(), postgresUser.getEmail());
                        linkUserRepository.save(newUser);
                        logger.info("Synced user to MySQL: {}", newUser.getEmail());
                    } catch (Exception e) {
                        logger.warn("Failed to sync user {} to MySQL: {}",
                                postgresUser.getEmail(), e.getMessage());
                    }
                }
            }

            logger.info("Bidirectional synchronization completed successfully");

        } catch (Exception e) {
            logger.error("Bidirectional synchronization failed: {}", e.getMessage(), e);
        }
    }
}
