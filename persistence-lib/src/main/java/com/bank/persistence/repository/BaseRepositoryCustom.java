package com.bank.persistence.repository;

import java.io.Serializable;
import java.util.Optional;

import com.bank.common.enums.ErrorCode;

public interface BaseRepositoryCustom<T, ID extends Serializable> {

    Optional<T> findOptional(ID id);

    T findByIdOrThrow(ID id, ErrorCode errorCode);

    T saveAndRefresh(T entity);

    void refresh(T entity);

    void detach(T entity);

    void flushAndClear();

}