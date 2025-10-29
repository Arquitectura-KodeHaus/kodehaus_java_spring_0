package com.kodehaus.plaza.repository;

import com.kodehaus.plaza.entity.Plaza;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository interface for Plaza entity
 */
@Repository
public interface PlazaRepository extends JpaRepository<Plaza, Long> {
    
    /**
     * Find plaza by name
     */
    Optional<Plaza> findByName(String name);
    
    /**
     * Find all active plazas
     */
    List<Plaza> findByIsActiveTrue();
    
    /**
     * Find plazas by name containing (case insensitive)
     */
    @Query("SELECT p FROM Plaza p WHERE LOWER(p.name) LIKE LOWER(CONCAT('%', :name, '%'))")
    List<Plaza> findByNameContainingIgnoreCase(@Param("name") String name);
    
    /**
     * Check if plaza name exists
     */
    boolean existsByName(String name);
    
    /**
     * Check if plaza name exists excluding specific plaza
     */
    boolean existsByNameAndIdNot(String name, Long id);
    
    /**
     * Check if plaza exists by ID and is active
     */
    @Query("SELECT COUNT(p) > 0 FROM Plaza p WHERE p.id = :id AND p.isActive = true")
    boolean existsByIdAndIsActiveTrue(@Param("id") Long id);
}