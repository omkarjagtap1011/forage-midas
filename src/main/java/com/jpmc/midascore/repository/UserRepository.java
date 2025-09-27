package com.jpmc.midascore.repository;

import com.jpmc.midascore.entity.UserRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.Optional;

public interface UserRepository extends JpaRepository<UserRecord, Long> {
    @Query("SELECT u FROM UserRecord u WHERE u.id = :id")
    Optional<UserRecord> findByUserId(@Param("id") long userId);
}
