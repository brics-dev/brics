package gov.nih.tbi.api.query.configuration;

import java.util.Properties;

import javax.sql.DataSource;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.orm.jpa.vendor.HibernateJpaVendorAdapter;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;

@Configuration
@EnableTransactionManagement
@EnableJpaRepositories(basePackages = {"gov.nih.tbi.api.query.data.repository"})
public class MetaDbConfiguration {

	@Autowired
	private Environment env;

	@Autowired
	@Qualifier("metaConnection")
	private DataSource dataSource;
	
	@Bean(name = "entityManagerFactory")
	public LocalContainerEntityManagerFactoryBean metaEntityManagerFactory() {
		String[] entityPackages = new String[] {"gov.nih.tbi.account", "gov.nih.tbi.commons", "gov.nih.tbi.repository",
				"gov.nih.tbi.file.model.hibernate"};
 
		// Setup the Hibernate JPA adapter object.
		HibernateJpaVendorAdapter vendorAdapter = new HibernateJpaVendorAdapter();

		// Setup some additional Hibernate properties.
		Properties moreHibernateProps = new Properties();
		moreHibernateProps.put("hibernate.dialect", env.getProperty("spring.jpa.properties.hibernate.dialect"));
		moreHibernateProps.put("hibernate.jdbc.lob.non_contextual_creation", env.getProperty("spring.jpa.properties.hibernate.jdbc.lob.non_contextual_creation"));
		moreHibernateProps.put("hibernate.temp.use_jdbc_metadata_defaults", env.getProperty("spring.jpa.properties.hibernate.temp.use_jdbc_metadata_defaults"));
		moreHibernateProps.setProperty("hibernate.show_sql", "false");
		moreHibernateProps.setProperty("hibernate.format_sql", "false");
		moreHibernateProps.setProperty("hibernate.use_sql_comments", "false");

		// Setup the entity manager object.
		LocalContainerEntityManagerFactoryBean em = new LocalContainerEntityManagerFactoryBean();
		em.setDataSource(dataSource);
		em.setPackagesToScan(entityPackages);
		em.setJpaVendorAdapter(vendorAdapter);
		em.setJpaProperties(moreHibernateProps);

		return em;
	}

	@Bean(name = "metaTransactionManager")
	public PlatformTransactionManager metaTransactionManager() {
		return new JpaTransactionManager(metaEntityManagerFactory().getObject());
	}
}