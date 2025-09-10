package com.webkorps.sync_db.scheduler;

import com.webkorps.sync_db.service.UserFailoverService;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class DatabaseSyncScheduler {

    private final UserFailoverService userFailoverService;

    public DatabaseSyncScheduler(UserFailoverService service) {
        this.userFailoverService = service;
    }

    @Scheduled(fixedRate = 2000)
    public void scheduledSync() {
        userFailoverService.syncDatabases();
    }
}

