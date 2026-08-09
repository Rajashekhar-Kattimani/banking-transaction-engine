package com.bank.auth.config;

import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.bank.auth.role.entity.Role;
import com.bank.auth.role.repository.RoleRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
public class RoleInitializer {

    private final RoleRepository roleRepository;

    @EventListener(ApplicationReadyEvent.class)
    @Transactional
    public void initializeRoles() {
        log.info("Initializing default roles...");

        // Create ROLE_USER if it doesn't exist
        if (!roleRepository.existsByName("ROLE_USER")) {
            Role userRole = Role.builder()
                .name("ROLE_USER")
                .description("Default user role")
                .build();
            roleRepository.save(userRole);
            log.info("Created ROLE_USER");
        } else {
            log.info("ROLE_USER already exists");
        }

        // Create ROLE_ADMIN if it doesn't exist (optional, for future use)
        if (!roleRepository.existsByName("ROLE_ADMIN")) {
            Role adminRole = Role.builder()
                .name("ROLE_ADMIN")
                .description("Administrator role")
                .build();
            roleRepository.save(adminRole);
            log.info("Created ROLE_ADMIN");
        } else {
            log.info("ROLE_ADMIN already exists");
        }

        log.info("Role initialization completed");
    }
}
