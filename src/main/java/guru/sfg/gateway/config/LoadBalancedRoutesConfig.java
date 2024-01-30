package guru.sfg.gateway.config;

import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.GatewayFilterSpec;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.cloud.gateway.route.builder.UriSpec;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

import java.util.function.Function;

@Profile("local-discovery")
@Configuration
public class LoadBalancedRoutesConfig {

    @Bean
    public RouteLocator loadBalancedRoutes(RouteLocatorBuilder builder) {
        return builder.routes()
                .route(r -> r.path("/api/v1/beer*", "/api/v1/beer/*", "/api/v1/beer/upc/*")
                        .uri("lb://beer-service"))
                .route(r -> r.path("/api/v1/customers/**")
                        .uri("lb://order-service"))
                .route(r -> r.path("/api/v1/beer/*/inventory")
                        .filters(this.filterInventoryFailoverCircuitBreaker())
                        .uri("lb://inventory-service"))
                .route(r -> r.path("/inventory-failover/**")
                        .uri("lb://inventory-failover"))
                .build();
    }

    Function<GatewayFilterSpec, UriSpec> filterInventoryFailoverCircuitBreaker() {

        return f -> f.circuitBreaker(config ->
                config
                        .setName("inventoryCB")
                        .setFallbackUri("forward:/inventory-failover")
                        .setRouteId("inv-failover"));
    }
}
