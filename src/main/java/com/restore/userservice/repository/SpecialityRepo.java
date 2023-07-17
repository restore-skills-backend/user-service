package com.restore.userservice.repository;

import com.restore.core.entity.SpecialityEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SpecialityRepo extends JpaRepository<SpecialityEntity,Long> {
    SpecialityEntity findByName(String name);
}
