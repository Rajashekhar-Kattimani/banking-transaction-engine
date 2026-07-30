package com.bank.auth.user.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.bank.auth.role.entity.Role;

public interface RoleRepository extends JpaRepository<Role, String> {

Optional<Role> findByName(String name);

}
