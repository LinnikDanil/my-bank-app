package ru.practicum.account.web.logging;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * MVC-конфигурация логирования входящих HTTP-запросов.
 */
@Configuration
@RequiredArgsConstructor
public class WebLoggingConfig implements WebMvcConfigurer {

    private final ControllerLoggingInterceptor controllerLoggingInterceptor;

    /**
     * Подключает interceptor для всех endpoint сервиса.
     */
    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(controllerLoggingInterceptor).addPathPatterns("/**");
    }
}
