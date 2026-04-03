package ru.practicum.common.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.context.annotation.Bean;
import ru.practicum.common.web.logging.ControllerLoggingInterceptor;

@AutoConfiguration
@ConditionalOnClass(name = "org.springframework.web.servlet.HandlerInterceptor")
@ConditionalOnWebApplication(type = ConditionalOnWebApplication.Type.SERVLET)
public class CommonWebLoggingAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean
    public ControllerLoggingInterceptor controllerLoggingInterceptor(ObjectMapper objectMapper) {
        return new ControllerLoggingInterceptor(objectMapper);
    }
}
