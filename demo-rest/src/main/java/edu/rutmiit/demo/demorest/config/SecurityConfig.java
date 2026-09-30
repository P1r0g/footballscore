package edu.rutmiit.demo.demorest.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableMethodSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http)
            throws Exception {

        http
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/login", "/csrf", "/error")
                        .permitAll()

                        .requestMatchers(HttpMethod.GET, "/api", "/api/**")
                        .hasAnyRole("READER", "EDITOR")

                        .requestMatchers("/api", "/api/**")
                        .hasRole("EDITOR")

                        .requestMatchers("/graphql", "/graphql/**")
                        .hasAnyRole("READER", "EDITOR")

                        .anyRequest()
                        .authenticated()
                )

                .formLogin(form -> form
                        .defaultSuccessUrl("/api/session/me", true)
                        .permitAll()
                )

                .sessionManagement(session -> session
                        .sessionCreationPolicy(SessionCreationPolicy.IF_REQUIRED)
                        .sessionFixation(fixation -> fixation.changeSessionId())
                )

                .csrf(Customizer.withDefaults())

                .logout(logout -> logout
                        .logoutUrl("/logout")
                        .logoutSuccessUrl("/login?logout")
                        .invalidateHttpSession(true)
                        .clearAuthentication(true)
                        .deleteCookies("JSESSIONID")
                );

        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public UserDetailsService userDetailsService(
            PasswordEncoder passwordEncoder) {

        var reader = User.withUsername("reader")
                .password(passwordEncoder.encode("reader"))
                .roles("READER")
                .build();

        var editor = User.withUsername("editor")
                .password(passwordEncoder.encode("editor"))
                .roles("EDITOR")
                .build();

        return new InMemoryUserDetailsManager(reader, editor);
    }
}