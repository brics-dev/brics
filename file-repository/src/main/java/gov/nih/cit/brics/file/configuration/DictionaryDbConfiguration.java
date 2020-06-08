package gov.nih.cit.brics.file.configuration;

import java.util.Properties;

import javax.persistence.EntityManagerFactory;
import javax.sql.DataSource;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.dao.annotation.PersistenceExceptionTranslationPostProcessor;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.orm.jpa.vendor.Database;
import org.springframework.orm.jpa.vendor.HibernateJpaVendorAdapter;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import gov.nih.cit.brics.file.util.FileDataSourceUtil;

@Configuration
@EnableTransactionManagement
@EnableJpaRepositories(basePackages = "gov.nih.cit.brics.file.data.repository.dictionary", entityManagerFactoryRef = "dictionaryEntityManagerFactory", transactionManagerRef = "dictionaryTransactionManager")
public class DictionaryDbConfiguration {
	private static final String DICT_CONN_POOL_NAME_FORMAT = "dictionaryHikariCP-%1$s";
	private static final String DICTIONARY_FILENAME = "dictionary.properties";

	@Autowired
	private Environment env;

	@Autowired
	private FileDataSourceUtil fileDataSourceUtil;

	@Bean("dictionaryDataSource")
	public DataSource dictionaryDataSource() {
		return fileDataSourceUtil.createMultiTenantDataSource(env.getProperty("conf.dir"), DICTIONARY_FILENAME,
				DICT_CONN_POOL_NAME_FORMAT);
	}

	@Bean("dictionaryEntityManagerFactory")
	public LocalContainerEntityManagerFactoryBean dictionaryEntityManagerFactory(
			@Qualifier("dictionaryDataSource") DataSource dictionaryDataSource) {
		String[] entityPackages = new String[] {"gov.nih.cit.brics.file.data.entity.dictionary"};

		// Setup the Hibernate JPA adapter object.
		HibernateJpaVendorAdapter vendorAdapter = new HibernateJpaVendorAdapter();

		vendorAdapter.setDatabase(Database.POSTGRESQL);
		vendorAdapter.setGenerateDdl(false);

		// Setup some additional Hibernate properties.
		Properties moreHibernateProps = new Properties();

		moreHibernateProps.put("hibernate.hbm2ddl.auto", env.getProperty("spring.jpa.hibernate.ddl-auto"));
		moreHibernateProps.put("hibernate.dialect", env.getProperty("spring.jpa.properties.hibernate.dialect"));
		moreHibernateProps.put("hibernate.current_session_context_class",
				env.getProperty("spring.jpa.properties.hibernate.current_session_context_class"));
		moreHibernateProps.put("hibernate.show_sql", env.getProperty("spring.jpa.show-sql"));
		moreHibernateProps.put("hibernate.format_sql", env.getProperty("spring.jpa.properties.hibernate.format_sql"));
		moreHibernateProps.put("hibernate.jdbc.lob.non_contextual_creation",
				env.getProperty("spring.jpa.properties.hibernate.jdbc.lob.non_contextual_creation"));

		// Setup the entity manager object.
		LocalContainerEntityManagerFactoryBean em = new LocalContainerEntityManagerFactoryBean();

		em.setDataSource(dictionaryDataSource);
		em.setPackagesToScan(entityPackages);
		em.setJpaVendorAdapter(vendorAdapter);
		em.setJpaProperties(moreHibernateProps);

		return em;
	}

	@Bean("dictionaryTransactionManager")
	public PlatformTransactionManager dictionaryTransactionManager(
			@Qualifier("dictionaryEntityManagerFactory") EntityManagerFactory dictionaryEntityManager) {
		JpaTransactionManager transactionManager = new JpaTransactionManager();
		transactionManager.setEntityManagerFactory(dictionaryEntityManager);

		return transactionManager;
	}

	@Bean("dictionaryExceptionTranslation")
	public PersistenceExceptionTranslationPostProcessor dictionaryExceptionTranslation() {
		return new PersistenceExceptionTranslationPostProcessor();
	}
}
