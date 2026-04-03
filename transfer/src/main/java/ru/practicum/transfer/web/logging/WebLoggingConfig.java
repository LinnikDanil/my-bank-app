package ru.practicum.transfer.web.logging;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import ru.practicum.common.web.logging.ControllerLoggingInterceptor;

@Configuration
@RequiredArgsConstructor
public class WebLoggingConfig implements WebMvcConfigurer {

    private final ObjectProvider<ControllerLoggingInterceptor> controllerLoggingInterceptorProvider;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        controllerLoggingInterceptorProvider.ifAvailable(
                interceptor -> registry.addInterceptor(interceptor).addPathPatterns("/**")
        );
    }
}
