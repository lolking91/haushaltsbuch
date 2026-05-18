package de.haushaltsbuch.backend.config;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
public class SpaController {

    @RequestMapping(value = {"/", "/{path:[^\\.]*}", "/{path:[^\\.]*}/**"})
    public String forward(HttpServletRequest request) {
        String path = request.getRequestURI();
        // Exclude API routes regardless of any servlet context-path prefix (e.g. /banking/api/...)
        if (path.contains("/api/")) {
            return null;
        }
        return "forward:/index.html";
    }
}
