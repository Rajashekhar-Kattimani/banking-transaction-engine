package com.bank.persistence.softdelete;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.MappedSuperclass;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import com.bank.persistence.entity.AuditableEntity;

@Getter
@Setter
@NoArgsConstructor
@ToString(callSuper = true)
@MappedSuperclass
public abstract class SoftDeleteEntity extends AuditableEntity {

    @Column(name = "deleted", nullable = false)
    private Boolean deleted = false;

    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;

    @Column(name = "deleted_by")
    private String deletedBy;

}