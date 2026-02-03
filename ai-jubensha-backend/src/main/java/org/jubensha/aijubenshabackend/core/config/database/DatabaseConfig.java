package org.jubensha.aijubenshabackend.core.config.database;

import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.transaction.annotation.EnableTransactionManagement;

@Configuration
@EnableJpaRepositories(basePackages = "org.jubensha.aijubenshabackend.repository")
@EnableTransactionManagement
public class DatabaseConfig {
}