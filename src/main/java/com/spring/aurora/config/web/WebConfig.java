package com.spring.aurora.config.web;

import java.io.IOException;
import java.util.Locale;
import java.util.Set;

import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.MessageSource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.servlet.View;
import org.springframework.web.servlet.ViewResolver;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import tools.jackson.databind.json.JsonMapper;

/**
 * The browser UI is the React app in frontend/, built into static/ (index.html plus assets/).
 *
 * - A page load (a GET that accepts HTML, such as /aurora/orders/list) gets index.html, and React
 *   shows the screen for that address.
 * - React then asks the same address for its data with "Accept: application/json"; the controller
 *   runs as before and its model comes back as JSON through {@link ModelJsonView}.
 * - Form saves are ordinary form POSTs. A controller's "redirect:" is followed by the browser and
 *   answers with the next screen's JSON, including any flash message such as "msg".
 */
@Configuration
public class WebConfig {

    @Bean
    public ViewResolver modelJsonViewResolver(JsonMapper jsonMapper, MessageSource messageSource) {
        return new JsonViewResolver(jsonMapper, messageSource);
    }

    @Bean
    public FilterRegistrationBean<SpaPageFilter> spaPageFilter() {
        FilterRegistrationBean<SpaPageFilter> registration = new FilterRegistrationBean<>(new SpaPageFilter());
        // After Spring Security, so a page load still needs a login (React shows the login screen).
        registration.setOrder(Ordered.LOWEST_PRECEDENCE - 10);
        return registration;
    }

    /** Every view name is the JSON model; "redirect:" and "forward:" go to Spring's usual resolver. */
    static class JsonViewResolver implements ViewResolver, Ordered {
        private final JsonMapper jsonMapper;
        private final MessageSource messageSource;

        JsonViewResolver(JsonMapper jsonMapper, MessageSource messageSource) {
            this.jsonMapper = jsonMapper;
            this.messageSource = messageSource;
        }

        @Override
        public View resolveViewName(String viewName, Locale locale) {
            if (viewName.startsWith("redirect:") || viewName.startsWith("forward:")) {
                return null;
            }
            return new ModelJsonView(viewName, this.jsonMapper, this.messageSource, locale);
        }

        @Override
        public int getOrder() {
            return Ordered.HIGHEST_PRECEDENCE;
        }
    }

    /** Sends index.html for page loads, so every React address works when opened or refreshed. */
    static class SpaPageFilter extends OncePerRequestFilter {
        private static final Set<String> STATIC_PREFIXES = Set.of("/assets/", "/error");

        @Override
        protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
                throws ServletException, IOException {
            if (isPageLoad(request)) {
                request.getRequestDispatcher("/index.html").forward(request, response);
                return;
            }
            chain.doFilter(request, response);
        }

        static boolean isPageLoad(HttpServletRequest request) {
            if (!"GET".equals(request.getMethod())) {
                return false;
            }
            String accept = request.getHeader("Accept");
            if (accept == null || !accept.contains("text/html") || accept.contains("application/json")) {
                return false;
            }
            String path = request.getRequestURI().substring(request.getContextPath().length());
            if (path.equals("/index.html") || STATIC_PREFIXES.stream().anyMatch(path::startsWith)) {
                return false;
            }
            // Files such as /favicon.svg are served as they are.
            return !path.substring(path.lastIndexOf('/') + 1).contains(".");
        }
    }
}
