package ms_pagos.config;

import jakarta.persistence.EntityManagerFactory;
import org.springframework.boot.autoconfigure.orm.jpa.JpaProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.orm.jpa.vendor.HibernateJpaVendorAdapter;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import javax.sql.DataSource;
import java.util.HashMap;
import java.util.Map;

/**
 * Configuración JPA explícita para Payara 7.
 * Payara provee su propio Hibernate que expone SessionFactory en lugar de
 * EntityManagerFactory. Al llamar setEntityManagerFactoryInterface(EntityManagerFactory.class)
 * se fuerza a Spring a usar la interfaz estándar de Jakarta JPA.
 */
@Configuration
@EnableTransactionManagement
@EnableConfigurationProperties(JpaProperties.class)
public class JpaConfig {

    @Bean
    @Primary
    public LocalContainerEntityManagerFactoryBean entityManagerFactory(
            DataSource dataSource, JpaProperties jpaProperties) {

        LocalContainerEntityManagerFactoryBean em = new LocalContainerEntityManagerFactoryBean();
        em.setDataSource(dataSource);
        em.setPackagesToScan("ms_pagos.entity");

        HibernateJpaVendorAdapter vendorAdapter = new HibernateJpaVendorAdapter();
        em.setJpaVendorAdapter(vendorAdapter);

        // CLAVE: forzar interfaz estándar Jakarta JPA
        // evita el conflicto con SessionFactory que Payara expone via delegate=true
        em.setEntityManagerFactoryInterface(EntityManagerFactory.class);

        Map<String, Object> props = new HashMap<>(jpaProperties.getProperties());
        props.put("hibernate.hbm2ddl.auto", "update");
        props.put("hibernate.show_sql", "false");
        props.put("hibernate.dialect", "org.hibernate.dialect.MySQLDialect");
        em.setJpaProperties(toProperties(props));

        return em;
    }

    @Bean
    public PlatformTransactionManager transactionManager(
            EntityManagerFactory entityManagerFactory) {
        return new JpaTransactionManager(entityManagerFactory);
    }

    private java.util.Properties toProperties(Map<String, Object> map) {
        java.util.Properties props = new java.util.Properties();
        map.forEach((k, v) -> props.setProperty(k, String.valueOf(v)));
        return props;
    }
}
