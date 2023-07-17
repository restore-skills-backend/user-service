//package com.restore.userservice.config;
//
//import jakarta.persistence.EntityManagerFactory;
////import liquibase.integration.spring.SpringLiquibase;
//import org.hibernate.boot.model.naming.CamelCaseToUnderscoresNamingStrategy;
//import org.springframework.beans.factory.annotation.Qualifier;
//import org.springframework.beans.factory.annotation.Value;
//import org.springframework.boot.autoconfigure.liquibase.LiquibaseProperties;
//import org.springframework.boot.context.properties.ConfigurationProperties;
//import org.springframework.boot.jdbc.DataSourceBuilder;
//import org.springframework.boot.orm.jpa.EntityManagerFactoryBuilder;
//import org.springframework.boot.orm.jpa.hibernate.SpringImplicitNamingStrategy;
//import org.springframework.context.annotation.Bean;
//import org.springframework.context.annotation.Configuration;
//import org.springframework.context.annotation.Primary;
//import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
//import org.springframework.orm.jpa.JpaTransactionManager;
//import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
//import org.springframework.transaction.PlatformTransactionManager;
//import org.springframework.transaction.annotation.EnableTransactionManagement;
//
//import javax.sql.DataSource;
//import java.util.HashMap;
//import java.util.Map;
//
//@Configuration
//@EnableTransactionManagement(proxyTargetClass = true)
//@EnableJpaRepositories(basePackages = {"com.restore.userservice.repository"})
//public class DataSourceConfig {
//
//    @Value("${spring.datasource.driver-class-name}")
//    private String driver;
//    @Value("${spring.datasource.url}")
//    private String url;
//    @Value("${spring.datasource.username}")
//    private String username;
//    @Value("${spring.datasource.password}")
//    private String password;
//
//    @Primary
//    @Bean(name = "dataSource")
//    @ConfigurationProperties(prefix = "spring.datasource.hikari")
//    public DataSource dataSource() {
//        return DataSourceBuilder.create()
//                .driverClassName(driver)
//                .url(url)
//                .username(username)
//                .password(password)
//                .build();
//    }
//
//    @Primary
//    @Bean(name = "entityManagerFactory")
//    public LocalContainerEntityManagerFactoryBean entityManagerFactory(EntityManagerFactoryBuilder builder,
//                                                                       @Qualifier("dataSource") DataSource dataSource) {
//        return builder.dataSource(dataSource).packages("com.restore.userservice.entity", "com.restore.core.entity").persistenceUnit("main")
//                .properties(jpaProperties()).build();
//    }
//
//    @Primary
//    @Bean(name = "transactionManager")
//    public PlatformTransactionManager transactionManager(
//            @Qualifier("entityManagerFactory") EntityManagerFactory entityManagerFactory) {
//        return new JpaTransactionManager(entityManagerFactory);
//    }
//
//    // ToDo - Refactor This
//    protected Map<String, Object> jpaProperties() {
//        Map<String, Object> props = new HashMap<>();
//        props.put("hibernate.physical_naming_strategy", CamelCaseToUnderscoresNamingStrategy.class.getName());
//        props.put("hibernate.implicit_naming_strategy", SpringImplicitNamingStrategy.class.getName());
//        return props;
//    }
//
//}
