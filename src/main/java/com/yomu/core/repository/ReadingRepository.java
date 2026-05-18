package com.yomu.core.repository;

import com.yomu.core.entity.Reading;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface ReadingRepository extends JpaRepository<Reading, UUID> {
    List<Reading> findByCategoryId(Integer categoryId);
}
