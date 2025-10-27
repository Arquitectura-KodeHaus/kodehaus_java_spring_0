package com.kodehaus.plaza.repository;

import com.kodehaus.plaza.model.Plaza;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PlazaRepository extends JpaRepository<Plaza, Long> {
}
