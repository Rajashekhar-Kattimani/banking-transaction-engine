package com.bank.persistence.util;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.bank.common.enums.ErrorCode;
import com.bank.common.exception.ResourceNotFoundException;

import lombok.experimental.UtilityClass;

@UtilityClass
public class JpaUtils {

    public static <T, ID> T findByIdOrThrow(
            JpaRepository<T, ID> repository,
            ID id,
            ErrorCode errorCode) {

        return repository.findById(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException(errorCode));
    }

    public static <T> T requireNonNull(
            T entity,
            ErrorCode errorCode) {

        if (entity == null) {
            throw new ResourceNotFoundException(errorCode);
        }

        return entity;
    }

    public static <T> Optional<T> optional(T entity) {
        return Optional.ofNullable(entity);
    }

}