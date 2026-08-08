package com.bank.security.user;

import java.io.Serial;
import java.util.Collection;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import com.bank.security.authorization.Permission;
import com.bank.security.authorization.Role;

public final class UserPrincipal implements UserDetails {

    @Serial
    private static final long serialVersionUID = 1L;

    private final SecurityUser securityUser;

    public UserPrincipal(SecurityUser securityUser) {
        this.securityUser = securityUser;
    }

    public SecurityUser getSecurityUser() {
        return securityUser;
    }

    public UUID getUserId() {
        return securityUser.userId();
    }

    public String getEmail() {
        return securityUser.email();
    }

    public String getBranchCode() {
        return securityUser.branchCode();
    }

    public String getTenantId() {
        return securityUser.tenantId();
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {

        Set<GrantedAuthority> roleAuthorities =
                securityUser.roles()
                        .stream()
                        .map(Role::name)
                        .map(SimpleGrantedAuthority::new)
                        .collect(Collectors.toSet());

        Set<GrantedAuthority> permissionAuthorities =
                securityUser.permissions()
                        .stream()
                        .map(Permission::name)
                        .map(SimpleGrantedAuthority::new)
                        .collect(Collectors.toSet());

        return Stream.concat(
                        roleAuthorities.stream(),
                        permissionAuthorities.stream())
                .collect(Collectors.toUnmodifiableSet());
    }

    @Override
    public String getPassword() {
        return securityUser.password();
    }

    @Override
    public String getUsername() {
        return securityUser.username();
    }

    @Override
    public boolean isAccountNonExpired() {
        return securityUser.accountNonExpired();
    }

    @Override
    public boolean isAccountNonLocked() {
        return securityUser.accountNonLocked();
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return securityUser.credentialsNonExpired();
    }

    @Override
    public boolean isEnabled() {
        return securityUser.enabled();
    }
}