package io.github.upinmcse.reader.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import tools.jackson.databind.ObjectMapper;

@Configuration
public class BeanConfig {

    @Bean
    ObjectMapper newObjectMapper(){
        return new ObjectMapper();
    }
}
