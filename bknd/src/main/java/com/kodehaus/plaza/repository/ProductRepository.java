package com.kodehaus.plaza.repository;

import com.kodehaus.plaza.entity.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository interface for Product entity
 */
@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {
    
    /**
     * Find products by plaza
     */
    List<Product> findByPlazaId(Long plazaId);
    
    /**
     * Find active products by plaza
     */
    List<Product> findByPlazaIdAndIsActiveTrue(Long plazaId);
    
    /**
     * Find available products by plaza
     */
    List<Product> findByPlazaIdAndIsActiveTrueAndIsAvailableTrue(Long plazaId);
    
    /**
     * Find products by plaza and category
     */
    List<Product> findByPlazaIdAndCategoryAndIsActiveTrue(Long plazaId, String category);
    
    /**
     * Find products by plaza and availability
     */
    List<Product> findByPlazaIdAndIsAvailableAndIsActiveTrue(Long plazaId, Boolean isAvailable);
    
    /**
     * Find product by ID and plaza ID
     */
    Optional<Product> findByIdAndPlazaIdAndIsActiveTrue(Long id, Long plazaId);
    
    /**
     * Find products by name containing (case insensitive)
     */
    @Query("SELECT p FROM Product p WHERE p.plaza.id = :plazaId AND LOWER(p.name) LIKE LOWER(CONCAT('%', :name, '%')) AND p.isActive = true")
    List<Product> findByPlazaIdAndNameContainingIgnoreCase(@Param("plazaId") Long plazaId, @Param("name") String name);
    
    /**
     * Find all categories by plaza
     */
    @Query("SELECT DISTINCT p.category FROM Product p WHERE p.plaza.id = :plazaId AND p.isActive = true ORDER BY p.category")
    List<String> findDistinctCategoriesByPlazaId(@Param("plazaId") Long plazaId);
    
    /**
     * Find products ordered by category and name
     */
    @Query("SELECT p FROM Product p WHERE p.plaza.id = :plazaId AND p.isActive = true ORDER BY p.category, p.name")
    List<Product> findByPlazaIdOrderByCategoryAndName(@Param("plazaId") Long plazaId);
}
