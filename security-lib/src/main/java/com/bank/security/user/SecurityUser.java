package com.bank.security.user;

import java.io.Serializable;
import java.util.Set;

import com.bank.security.authorization.Permission;
import com.bank.security.authorization.Role;

public record SecurityUser(

        Long userId,

        String username,

        String email,

        Set<Role> roles,

        Set<Permission> permissions,

        String branchCode,

        String tenantId,

        boolean authenticated

) implements Serializable {

    private static final long serialVersionUID = 1L;

}