package com.jkrocha.shoplab.order.api;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

/**
 * Provides a stable, human-friendly entry point for the static load launcher.
 */
@Controller
public class LoadGeneratorPageController {

    @GetMapping({"/load-generator", "/load-generator/"})
    public String loadGenerator() {
        return "forward:/load-generator/index.html";
    }
}
