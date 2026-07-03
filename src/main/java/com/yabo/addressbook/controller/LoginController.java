package com.yabo.addressbook.controller;

import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class LoginController {

    @GetMapping("/login")
    public String login(HttpSession session, Model model) {
        // Read and clear the login error message set by CustomAuthenticationFailureHandler
        String errorMsg = (String) session.getAttribute("loginErrorMsg");
        if (errorMsg != null) {
            session.removeAttribute("loginErrorMsg");
            model.addAttribute("loginErrorMsg", errorMsg);
        }
        return "login";
    }

    @GetMapping("/")
    public String root() {
        return "redirect:/contacts";
    }
}