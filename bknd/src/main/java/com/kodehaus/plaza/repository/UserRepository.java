package com.kodehaus.plaza.repository;

import com.kodehaus.plaza.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository interface for User entity
 */
@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    
    /**
     * Find user by username
     */
    Optional<User> findByUsername(String username);
    
    /**
     * Find user by email
     */
    Optional<User> findByEmail(String email);
    
    /**
     * Find all users by plaza
     */
    List<User> findByPlazaId(Long plazaId);
    
    /**
     * Find all active users by plaza
     */
    List<User> findByPlazaIdAndIsActiveTrue(Long plazaId);
    
    /**
     * Find users by role name
     */
    @Query("SELECT u FROM User u JOIN u.roles r WHERE r.name = :roleName")
    List<User> findByRoleName(@Param("roleName") String roleName);
    
    /**
     * Find users by plaza and role
     */
    @Query("SELECT u FROM User u JOIN u.roles r WHERE u.plaza.id = :plazaId AND r.name = :roleName")
    List<User> findByPlazaIdAndRoleName(@Param("plazaId") Long plazaId, @Param("roleName") String roleName);
    
    /**
     * Check if username exists
     */
    boolean existsByUsername(String username);
    
    /**
     * Check if email exists
     */
    boolean existsByEmail(String email);
    
    /**
     * Check if username exists excluding specific user
     */
    boolean existsByUsernameAndIdNot(String username, Long id);
    
    /**
     * Check if email exists excluding specific user
     */
    boolean existsByEmailAndIdNot(String email, Long id);
    
    /**
     * Find user by ID and plaza ID
     */
    Optional<User> findByIdAndPlazaIdAndIsActiveTrue(Long id, Long plazaId);
    
    /**
     * Check if plaza exists and is active
     */
    @Query("SELECT COUNT(p) > 0 FROM Plaza p WHERE p.id = :plazaId AND p.isActive = true")
    boolean existsByIdAndIsActiveTrue(@Param("plazaId") Long plazaId);
}
