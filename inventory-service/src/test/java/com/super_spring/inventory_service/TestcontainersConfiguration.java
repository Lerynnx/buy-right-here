package com.super_spring.inventory_service;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.context.annotation.Bean;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.postgresql.PostgreSQLContainer;
import org.testcontainers.pulsar.PulsarContainer;
import org.testcontainers.utility.DockerImageName;

@TestConfiguration(proxyBeanMethods = false)
class TestcontainersConfiguration {

	@Bean
	@ServiceConnection
	PostgreSQLContainer postgresContainer() {
		return new PostgreSQLContainer(DockerImageName.parse("postgres:latest"));
	}

	@Bean
	@ServiceConnection
	PulsarContainer pulsarContainer() {
		return new PulsarContainer(DockerImageName.parse("apachepulsar/pulsar:latest"));
	}

	@Bean
	@ServiceConnection(name = "openzipkin/zipkin")
	GenericContainer<?> zipkinContainer() {
		return new GenericContainer<>(DockerImageName.parse("openzipkin/zipkin:latest")).withExposedPorts(9411);
	}

}
