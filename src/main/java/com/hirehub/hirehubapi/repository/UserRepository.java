package com.hirehub.hirehubapi.repository;

import com.hirehub.hirehubapi.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;


public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByEmail(String email); //bcs it is unique;

    boolean existsByEmail(String email);

    @Modifying
    @Query("UPDATE User u SET u.isActive = false WHERE u.id = :userId")
    void deactivateUser(@Param("userId") Long userId);

    long countByRole(com.hirehub.hirehubapi.model.Role role);

}
