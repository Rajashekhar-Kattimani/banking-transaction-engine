package com.bank.security.user;

import java.io.Serializable;
import java.util.Set;
import java.util.UUID;

import com.bank.security.authorization.Permission;
import com.bank.security.authorization.Role;

public record SecurityUser(

        UUID userId,

        String username,

        String email,

        String password,

        Set<Role> roles,

        Set<Permission> permissions,

        String branchCode,

        String tenantId,

        boolean enabled,

        boolean accountNonLocked,

        boolean accountNonExpired,

        boolean credentialsNonExpired

) implements Serializable {
}