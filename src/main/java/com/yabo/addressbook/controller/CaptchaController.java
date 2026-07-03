package com.yabo.addressbook.controller;

import com.wf.captcha.SpecCaptcha;
import com.wf.captcha.base.Captcha;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.HashMap;
import java.util.Map;

@Controller
public class CaptchaController {

    @Value("${captcha.enabled:false}")
    private boolean captchaEnabled;

    @GetMapping("/captcha")
    @ResponseBody
    public Object getCaptcha(HttpSession session) {
        if (!captchaEnabled) {
            Map<String, Object> result = new HashMap<>();
            result.put("enabled", false);
            return result;
        }

        SpecCaptcha captcha = new SpecCaptcha(130, 48, 4);
        captcha.setCharType(Captcha.TYPE_DEFAULT);
        
        // Store captcha code in session
        session.setAttribute("captcha", captcha.text().toLowerCase());
        
        // Return base64 image
        Map<String, Object> result = new HashMap<>();
        result.put("enabled", true);
        result.put("image", captcha.toBase64());
        return result;
    }
}