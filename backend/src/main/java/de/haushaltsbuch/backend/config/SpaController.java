package de.haushaltsbuch.backend.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.springframework.web.servlet.resource.PathResourceResolver;

import java.io.IOException;

/**
 * Configures SPA (Single Page Application) fallback routing for the SvelteKit frontend.
 *
 * All requests are first resolved against the static classpath resources. If a file
 * is found (JS, CSS, images, etc.) it is served directly. If no file matches, the
 * request is a SvelteKit client-side route and index.html is returned as fallback
 * so SvelteKit's router can take over.
 *
 * This approach avoids controller-level pattern matching and the issues that come
 * with it (greedy wildcards, forward loops, MIME-type mismatches).
 */
@Configuration
public class SpaController implements WebMvcConfigurer {

    private static final Resource INDEX_HTML = new ClassPathResource("static/index.html");

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        registry.addResourceHandler("/**")
                .addResourceLocations("classpath:/static/")
                .resourceChain(true)
                .addResolver(new PathResourceResolver() {
                    @Override
                    protected Resource getResource(String resourcePath, Resource location) throws IOException {
                        Resource requested = location.createRelative(resourcePath);
                        // Serve the actual file if it exists, otherwise fall back to index.html
                        return (requested.exists() && requested.isReadable()) ? requested : INDEX_HTML;
                    }
                });
    }
}
