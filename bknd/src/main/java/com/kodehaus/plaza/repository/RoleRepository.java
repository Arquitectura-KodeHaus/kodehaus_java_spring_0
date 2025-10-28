package com.kodehaus.plaza.repository;

import com.kodehaus.plaza.entity.Role;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.Set;

/**
 * Repository interface for Role entity
 */
@Repository
public interface RoleRepository extends JpaRepository<Role, Long> {
    
    /**
     * Find role by name
     */
    Optional<Role> findByName(String name);
    
    /**
     * Find all active roles
     */
    List<Role> findByIsActiveTrue();
    
    /**
     * Check if role name exists
     */
    boolean existsByName(String name);
    
    /**
     * Check if role name exists excluding specific role
     */
    boolean existsByNameAndIdNot(String name, Long id);
    
    /**
     * Find roles by permission
     */
    @Query("SELECT r FROM Role r JOIN r.permissions p WHERE p.name = :permissionName")
    List<Role> findByPermissionName(@Param("permissionName") String permissionName);
    
    /**
     * Find roles by IDs
     */
    Set<Role> findByIdInAndIsActiveTrue(Set<Long> ids);
}
