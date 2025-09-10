package com.webkorps.sync_db.repository.link;

import com.webkorps.sync_db.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface LinkUserRepository extends JpaRepository<User, Long> {
}

