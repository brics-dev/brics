package gov.nih.tbi.api.query;

import java.sql.SQLException;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.beans.factory.config.PropertiesFactoryBean;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.web.servlet.support.SpringBootServletInitializer;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.client.loadbalancer.LoadBalanced;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.web.client.RestTemplate;

import gov.nih.tbi.constants.ApplicationConstants;

@SpringBootApplication(exclude = { SecurityAutoConfiguration.class })
@ComponentScan(basePackages = { "gov.nih.tbi.api.query", "gov.nih.tbi.service.impl", "gov.nih.tbi.store",
		"gov.nih.tbi.account.service.impl", "gov.nih.tbi.dao", "gov.nih.tbi.service", "gov.nih.tbi.account.service",
		"gov.nih.brics.downloadtool.security.jwt" })
@EnableDiscoveryClient
public class QueryApiApplication extends SpringBootServletInitializer {

	private static final int VIRTUOSO_FETCH_SIZE = 50000;

	/**
	 * Run as a JAR.
	 * 
	 * @param args
	 */
	public static void main(String[] args) throws SQLException {
		SpringApplication.run(QueryApiApplication.class, args);
	}

	/**
	 * Run as a WAR.
	 */
	@Override
	protected SpringApplicationBuilder configure(SpringApplicationBuilder application) {
		return application.sources(QueryApiApplication.class);
	}

	@Value("#{systemProperties['conf.dir']}")
	private String confDir;

	@Bean
	public PropertiesFactoryBean applicationProperties() {
		PropertiesFactoryBean applicationProperties = new PropertiesFactoryBean();
		return applicationProperties;
	}

	@Bean
	public ApplicationConstants applicationConstants() {
		ApplicationConstants applicationConstants = new ApplicationConstants();
		applicationConstants.setVirtuosoFetchSize(VIRTUOSO_FETCH_SIZE);
		return applicationConstants;
	}

	@LoadBalanced
	@Bean
	RestTemplate restTemplate() {
		return new RestTemplate();
	}
}
