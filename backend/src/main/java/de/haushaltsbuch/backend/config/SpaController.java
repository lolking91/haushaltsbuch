package de.haushaltsbuch.backend.config;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
public class SpaController {

    @RequestMapping(value = {"/", "/{path:[^\\.]*}", "/{path:[^\\.]*}/**"})
    public String forward(HttpServletRequest request) {
        String path = request.getRequestURI();
        // Exclude API routes and SvelteKit static assets from the SPA fallback
        if (path.contains("/api/") || path.contains("/_app/")) {
            return null;
        }
        return "forward:/index.html";
    }
}
