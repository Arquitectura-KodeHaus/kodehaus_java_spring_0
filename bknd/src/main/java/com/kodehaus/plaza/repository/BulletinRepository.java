package com.kodehaus.plaza.repository;

import com.kodehaus.plaza.entity.Bulletin;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * Repository interface for Bulletin entity
 */
@Repository
public interface BulletinRepository extends JpaRepository<Bulletin, Long> {
    
    /**
     * Find bulletins by plaza
     */
    List<Bulletin> findByPlazaId(Long plazaId);
    
    /**
     * Find active bulletins by plaza
     */
    List<Bulletin> findByPlazaIdAndIsActiveTrue(Long plazaId);
    
    /**
     * Find bulletins by plaza and publication date
     */
    List<Bulletin> findByPlazaIdAndPublicationDate(Long plazaId, LocalDate publicationDate);
    
    /**
     * Find bulletins by plaza and date range
     */
    @Query("SELECT b FROM Bulletin b WHERE b.plaza.id = :plazaId AND b.publicationDate BETWEEN :startDate AND :endDate ORDER BY b.publicationDate DESC")
    List<Bulletin> findByPlazaIdAndPublicationDateBetween(
        @Param("plazaId") Long plazaId, 
        @Param("startDate") LocalDate startDate, 
        @Param("endDate") LocalDate endDate
    );
    
    /**
     * Find latest bulletins by plaza
     */
    @Query("SELECT b FROM Bulletin b WHERE b.plaza.id = :plazaId AND b.isActive = true ORDER BY b.publicationDate DESC, b.createdAt DESC")
    List<Bulletin> findLatestByPlazaId(@Param("plazaId") Long plazaId);
    
    /**
     * Find bulletins by creator
     */
    List<Bulletin> findByCreatedById(Long createdById);
    
    /**
     * Find today's bulletins by plaza
     */
    @Query("SELECT b FROM Bulletin b WHERE b.plaza.id = :plazaId AND b.publicationDate = CURRENT_DATE AND b.isActive = true")
    List<Bulletin> findTodaysBulletinsByPlazaId(@Param("plazaId") Long plazaId);
    
    /**
     * Find bulletin by ID and plaza ID
     */
    Optional<Bulletin> findByIdAndPlazaIdAndIsActiveTrue(Long id, Long plazaId);
}
