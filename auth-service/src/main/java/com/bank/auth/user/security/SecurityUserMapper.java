package com.bank.auth.user.security;

import java.util.Collections;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.stereotype.Component;

import com.bank.auth.user.entity.User;
import com.bank.security.authorization.Permission;
import com.bank.security.authorization.Role;
import com.bank.security.user.SecurityUser;

@Component
public class SecurityUserMapper {

    public SecurityUser toSecurityUser(User user) {

        Set<Role> roles =
                user.getRoles() == null
                        ? Collections.emptySet()
                        : user.getRoles()
                                .stream()
                                .map(role ->
                                        Role.valueOf(role.getName()))
                                .collect(Collectors.toUnmodifiableSet());

        Set<Permission> permissions =
                user.getRoles() == null
                        ? Collections.emptySet()
                        : user.getRoles()
                                .stream()
                                .filter(role -> role.getPermissions() != null)
                                .flatMap(role -> role.getPermissions().stream())
                                .map(permission ->
                                        Permission.valueOf(permission.getName()))
                                .collect(Collectors.toUnmodifiableSet());

        return new SecurityUser(
                user.getId(),
                user.getUsername(),
                user.getEmail(),
                user.getPassword(),
                roles,
                permissions,
                null,
                null,
                user.isEnabled(),
                user.isAccountNonLocked(),
                user.isAccountNonExpired(),
                user.isCredentialsNonExpired()
        );
    }
}