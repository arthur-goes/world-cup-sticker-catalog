package com.arthurgoes.world_cup_sticker_catalog.configuration;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.context.annotation.Bean;
import org.testcontainers.postgresql.PostgreSQLContainer;
import org.testcontainers.utility.DockerImageName;

@TestConfiguration(proxyBeanMethods = false)
public class TestcontainersConfig {

    private final PostgreSQLContainer fallbackContainer = new PostgreSQLContainer(DockerImageName.parse("postgres:18"));

    @Bean
    @ServiceConnection
    PostgreSQLContainer postgreSQLContainer(){
        String postgresImage = System.getenv("POSTGRES_DOCKER_IMAGE");

        if (postgresImage == null){
            return fallbackContainer;
        }

        return new PostgreSQLContainer(DockerImageName.parse("postgres:18.3"));
    }
}
