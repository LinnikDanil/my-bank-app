package ru.practicum.notification.web.logging;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import ru.practicum.common.web.logging.ControllerLoggingInterceptor;

@Configuration
@RequiredArgsConstructor
public class WebLoggingConfig implements WebMvcConfigurer {

    private final ControllerLoggingInterceptor controllerLoggingInterceptor;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(controllerLoggingInterceptor).addPathPatterns("/**");
    }
}
