package com.kodehaus.plaza.repository;

import com.kodehaus.plaza.entity.Store;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository interface for Store entity
 */
@Repository
public interface StoreRepository extends JpaRepository<Store, Long> {
    
    /**
     * Find all stores by plaza ID
     */
    List<Store> findByPlazaId(Long plazaId);
    
    /**
     * Find all active stores by plaza ID
     */
    List<Store> findByPlazaIdAndIsActiveTrue(Long plazaId);
    
    /**
     * Find store by ID and plaza ID
     */
    Optional<Store> findByIdAndPlazaIdAndIsActiveTrue(Long id, Long plazaId);
    
    /**
     * Find store by external ID
     */
    Optional<Store> findByExternalId(String externalId);
    
    /**
     * Check if store exists by name and plaza
     */
    boolean existsByNameAndPlazaId(String name, Long plazaId);
    
    /**
     * Check if store exists by name and plaza excluding specific store
     */
    boolean existsByNameAndPlazaIdAndIdNot(String name, Long plazaId, Long id);
}

