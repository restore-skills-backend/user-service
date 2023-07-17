package com.restore.userservice.repository;

import com.restore.core.entity.ProviderGroupEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ProviderGroupRepo extends JpaRepository<ProviderGroupEntity,Long> {
}
