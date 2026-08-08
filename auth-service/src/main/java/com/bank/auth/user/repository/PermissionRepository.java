package com.bank.auth.user.repository;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.bank.auth.permission.entity.Permission;

public interface PermissionRepository extends JpaRepository<Permission, UUID> {

Optional<Permission> findByName(String name);

}
