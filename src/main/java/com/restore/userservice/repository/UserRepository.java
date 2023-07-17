package com.restore.userservice.repository;

import com.restore.core.entity.UserEntity;
import jakarta.persistence.EntityManager;
import jakarta.persistence.NoResultException;
import jakarta.persistence.Query;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface UserRepository extends JpaRepository<UserEntity, Long> {

    Optional<UserEntity> findByEmail(String email);

    Optional<UserEntity> findByUuid(UUID uuid);

    Optional<UserEntity> findByIamId(String iamId);

    default Optional<UserEntity> findByIdWithDynamicSchema(EntityManager entityManager, String schema, UUID uuid) {
        String nativeQuery = "SELECT * FROM " + schema + ".users WHERE uuid = :uuid";
        jakarta.persistence.Query query = entityManager.createNativeQuery(nativeQuery, UserEntity.class);
        query.setParameter("uuid", uuid);
        try {
            return Optional.ofNullable((UserEntity) query.getSingleResult());
        } catch (NoResultException e) {
            return Optional.empty();
        }
    }

    default Optional<UserEntity> findByEmailWithDynamicSchema(EntityManager entityManager, String schema, String email) {
        String nativeQuery = "SELECT * FROM " + schema + ".users WHERE email = :email";
        Query query = entityManager.createNativeQuery(nativeQuery, UserEntity.class);
        query.setParameter("email", email);
        try {
            return Optional.ofNullable((UserEntity) query.getSingleResult());
        } catch (NoResultException e) {
            return Optional.empty();
        }
    }

    Page<UserEntity> findAllByArchiveIsFalse(Pageable pageable);

}
