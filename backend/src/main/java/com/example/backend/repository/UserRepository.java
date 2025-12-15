package com.example.backend.repository;

import com.example.backend.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByUsername(String username);
    boolean existsByUsername(String username);
    Optional<User> findByActiveToken(String activeToken);

    @Modifying
    @Transactional
    @Query("update User u set u.activeToken = :token, u.loggedIn = true, u.lastLoginTime = :loginTime where u.id = :id and (u.loggedIn = false or u.loggedIn is null)")
    int markLoggedIn(@Param("id") Long id, @Param("token") String token, @Param("loginTime") LocalDateTime loginTime);

    @Modifying
    @Transactional
    @Query("update User u set u.activeToken = null, u.loggedIn = false where u.activeToken = :token")
    int clearLoginStateByToken(@Param("token") String token);
}
