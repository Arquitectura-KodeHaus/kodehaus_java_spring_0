package com.kodehaus.plaza.repository;

import com.kodehaus.plaza.entity.Permission;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.Set;

/**
 * Repository interface for Permission entity
 */
@Repository
public interface PermissionRepository extends JpaRepository<Permission, Long> {
    
    /**
     * Find permission by name
     */
    Optional<Permission> findByName(String name);
    
    /**
     * Find permission by resource and action
     */
    Optional<Permission> findByResourceAndAction(String resource, String action);
    
    /**
     * Find all active permissions
     */
    List<Permission> findByIsActiveTrue();
    
    /**
     * Find permissions by resource
     */
    List<Permission> findByResource(String resource);
    
    /**
     * Check if permission name exists
     */
    boolean existsByName(String name);
    
    /**
     * Check if permission exists by resource and action
     */
    boolean existsByResourceAndAction(String resource, String action);
    
    /**
     * Check if permission name exists excluding specific permission
     */
    boolean existsByNameAndIdNot(String name, Long id);
    
    /**
     * Find permissions by IDs
     */
    Set<Permission> findByIdInAndIsActiveTrue(Set<Long> ids);
}
