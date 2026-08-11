package com.bank.persistence.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

import com.bank.persistence.repository.BaseRepositoryImpl;

@Configuration
@EnableJpaRepositories(
        repositoryBaseClass = BaseRepositoryImpl.class)
public class RepositoryConfig {

}