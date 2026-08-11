package com.bank.persistence.repository;

import java.io.Serializable;
import java.util.Optional;

import org.springframework.data.jpa.repository.support.JpaEntityInformation;
import org.springframework.data.jpa.repository.support.SimpleJpaRepository;

import com.bank.common.enums.ErrorCode;
import com.bank.common.exception.ResourceNotFoundException;

import jakarta.persistence.EntityManager;

public class BaseRepositoryImpl<T, ID extends Serializable>
        extends SimpleJpaRepository<T, ID>
        implements BaseRepositoryCustom<T, ID> {

    private final EntityManager entityManager;

    public BaseRepositoryImpl(
            JpaEntityInformation<T, ?> entityInformation,
            EntityManager entityManager) {

        super(entityInformation, entityManager);
        this.entityManager = entityManager;
    }

    @Override
    public Optional<T> findOptional(ID id) {
        return super.findById(id);
    }

    @Override
    public T findByIdOrThrow(
            ID id,
            ErrorCode errorCode) {

        return super.findById(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException(errorCode));
    }

    @Override
    public T saveAndRefresh(T entity) {

        T saved = super.save(entity);

        entityManager.flush();

        entityManager.refresh(saved);

        return saved;
    }

    @Override
    public void refresh(T entity) {
        entityManager.refresh(entity);
    }

    @Override
    public void detach(T entity) {
        entityManager.detach(entity);
    }

    @Override
    public void flushAndClear() {

        entityManager.flush();

        entityManager.clear();
    }

}