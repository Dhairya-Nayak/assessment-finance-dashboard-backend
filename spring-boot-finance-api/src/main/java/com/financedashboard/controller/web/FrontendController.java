package com.financedashboard.controller.web;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class FrontendController {

    @GetMapping("/")
    public String home() {
        return "redirect:/dashboard";
    }

    @GetMapping("/login")
    public String login(Model model) {
        model.addAttribute("page", "login");
        model.addAttribute("title", "Sign in");
        return "auth";
    }

    @GetMapping("/register")
    public String register(Model model) {
        model.addAttribute("page", "register");
        model.addAttribute("title", "Create account");
        return "auth";
    }

    @GetMapping("/dashboard")
    public String dashboard(Model model) {
        return app(model, "dashboard", "Dashboard");
    }

    @GetMapping("/analytics")
    public String analytics(Model model) {
        return app(model, "analytics", "Analytics");
    }

    @GetMapping("/records")
    public String records(Model model) {
        return app(model, "records", "Records");
    }

    @GetMapping("/categories")
    public String categories(Model model) {
        return app(model, "categories", "Categories");
    }

    @GetMapping("/users")
    public String users(Model model) {
        return app(model, "users", "Users");
    }

    @GetMapping("/audit")
    public String audit(Model model) {
        return app(model, "audit", "Audit");
    }

    @GetMapping("/profile")
    public String profile(Model model) {
        return app(model, "profile", "Profile");
    }

    private String app(Model model, String page, String title) {
        model.addAttribute("page", page);
        model.addAttribute("title", title);
        return "app";
    }
}
