package ru.practicum.common.config;

import io.micrometer.observation.ObservationPredicate;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.http.server.observation.ServerRequestObservationContext;

@AutoConfiguration
@ConditionalOnClass(ServerRequestObservationContext.class)
public class CommonTracingAutoConfiguration {

    @org.springframework.context.annotation.Bean
    @ConditionalOnMissingBean(name = "skipActuatorObservationPredicate")
    public ObservationPredicate skipActuatorObservationPredicate() {
        return (observationName, context) -> {
            if (context instanceof ServerRequestObservationContext serverContext) {
                String path = serverContext.getCarrier().getRequestURI();
                return path == null || !path.startsWith("/actuator");
            }
            return true;
        };
    }
}
