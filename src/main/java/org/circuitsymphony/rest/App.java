package org.circuitsymphony.rest;

import org.circuitsymphony.rest.swagger.SwaggerConfig;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

@SpringBootApplication
@ComponentScan("org.circuitsymphony.rest")
@Import({SwaggerConfig.class})
@Configuration
public class App {
    public static void main(String[] args) {
        SpringApplication.run(App.class, args);
//        SpringApplicationBuilder builder = new SpringApplicationBuilder(App.class);
//        builder.headless(false).run(args);
    }

    /*@Bean
    public static PropertySourcesPlaceholderConfigurer swaggerProperties() {
        PropertySourcesPlaceholderConfigurer properties = new PropertySourcesPlaceholderConfigurer();
        return properties;
    }*/
}
