package com.studyhaja.repository;

import com.studyhaja.domain.Zone;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ZoneRepository extends JpaRepository<Zone,Long> {

    Zone findByLocalNameOfCity(String localNameOfCity);
}