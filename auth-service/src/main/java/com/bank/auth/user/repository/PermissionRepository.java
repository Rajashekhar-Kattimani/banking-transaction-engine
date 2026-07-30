package com.bank.auth.user.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.bank.auth.permission.entity.Permission;

public interface PermissionRepository extends JpaRepository<Permission, String> {

Optional<Permission> findByName(String name);

}
