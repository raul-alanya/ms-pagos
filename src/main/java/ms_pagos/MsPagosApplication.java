package com.pagos;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.web.servlet.support.SpringBootServletInitializer;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@SpringBootApplication
@ComponentScan(basePackages = {"com.pagos", "ms_pagos"})
@EnableJpaRepositories(basePackages = {"ms_pagos.repository"})
@EntityScan(basePackages = {"ms_pagos.entity"})
public class MsPagosApplication extends SpringBootServletInitializer {

    @Override
    protected SpringApplicationBuilder configure(SpringApplicationBuilder application) {
        return application.sources(MsPagosApplication.class);
    }

    public static void main(String[] args) {
        SpringApplication.run(MsPagosApplication.class, args);
    }
}