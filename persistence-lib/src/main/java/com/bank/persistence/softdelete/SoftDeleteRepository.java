package com.bank.persistence.softdelete;

import java.io.Serializable;
import java.util.List;

import org.springframework.data.repository.NoRepositoryBean;

import com.bank.persistence.repository.BaseRepository;

@NoRepositoryBean
public interface SoftDeleteRepository<T extends SoftDeleteEntity, ID extends Serializable>
        extends BaseRepository<T, ID> {

    List<T> findByDeletedFalse();

    List<T> findByDeletedTrue();

    long countByDeletedFalse();

    long countByDeletedTrue();

    boolean existsByIdAndDeletedFalse(ID id);

}