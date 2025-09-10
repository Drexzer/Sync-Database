package com.webkorps.sync_db.repository.dlink;

import com.webkorps.sync_db.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface DLinkUserRepository extends JpaRepository<User, Long> {
}

