package com.workingbit.article.config;

import org.springframework.context.annotation.Bean;
import org.springframework.security.authorization.AuthorizationDecision;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.MapReactiveUserDetailsService;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.server.SecurityWebFilterChain;
import org.springframework.security.web.server.authorization.AuthorizationContext;
import reactor.core.publisher.Mono;

@EnableWebFluxSecurity
class SecurityConfig {

  @Bean
  SecurityWebFilterChain springWebFilterChain(ServerHttpSecurity http) {
    return http
        .authorizeExchange()
//                .pathMatchers(HttpMethod.GET, "/api/v1/**").permitAll()
//                .pathMatchers(HttpMethod.DELETE, "/posts/**").hasRole("ADMIN")
//                .pathMatchers("/posts/**").authenticated()
        //.pathMatchers("/users/{user}/**").access(this::currentUserMatchesPath)
        .pathMatchers("/**").permitAll()
        .anyExchange().permitAll()
        .and()
        .build();
  }

  private Mono<AuthorizationDecision> currentUserMatchesPath(Mono<Authentication> authentication, AuthorizationContext context) {
    return authentication
        .map(a -> context.getVariables().get("user").equals(a.getName()))
        .map(AuthorizationDecision::new);
  }

  @Bean
  public MapReactiveUserDetailsService userDetailsRepository() {
    UserDetails user = User.withDefaultPasswordEncoder().username("user").password("password").roles("USER").build();
    UserDetails admin = User.withDefaultPasswordEncoder().username("admin").password("password").roles("USER", "ADMIN").build();
    return new MapReactiveUserDetailsService(user, admin);
  }

}
