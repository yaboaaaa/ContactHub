package com.yabo.addressbook.controller;

import com.yabo.addressbook.config.CaptchaProperties;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.util.Base64;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

@Controller
@RequestMapping("/api/v1")
public class CaptchaController {

    @Value("${captcha.enabled:false}")
    private boolean captchaEnabled;

    private final CaptchaProperties captchaProperties;

    private static final Random RANDOM = new Random();

    public CaptchaController(CaptchaProperties captchaProperties) {
        this.captchaProperties = captchaProperties;
    }

    @GetMapping("/captcha")
    @ResponseBody
    public Object getCaptcha(HttpSession session) {
        if (!captchaEnabled) {
            Map<String, Object> result = new HashMap<>();
            result.put("enabled", false);
            return result;
        }

        int op = RANDOM.nextInt(4); // 0=addition, 1=subtraction, 2=multiplication, 3=integral
        int answer;
        String expression;
        boolean isIntegral = false;
        String integralLower = null, integralUpper = null, integralText = null;

        if (op == 3) {
            isIntegral = true;
            // Load integrals from config; fallback to hardcoded defaults
            List<CaptchaProperties.IntegralProblem> problems = captchaProperties.getIntegrals();
            if (problems == null || problems.isEmpty()) {
                // Fallback default problems
                answer = 2;
                integralLower = "0";
                integralUpper = "2";
                integralText = "x";
            } else {
                CaptchaProperties.IntegralProblem problem = problems.get(RANDOM.nextInt(problems.size()));
                answer = problem.getAnswer();
                integralLower = problem.getLowerText();
                integralUpper = problem.getUpperText();
                integralText = problem.getText();
            }
            expression = null; // not used for integral rendering
        } else {
            int a, b;
            switch (op) {
                case 0: // Addition
                    a = RANDOM.nextInt(90) + 10;
                    b = RANDOM.nextInt(90) + 10;
                    answer = a + b;
                    expression = a + "+" + b + "=?";
                    break;
                case 1: // Subtraction
                    a = RANDOM.nextInt(80) + 20;
                    b = RANDOM.nextInt(a - 1) + 1;
                    answer = a - b;
                    expression = a + "-" + b + "=?";
                    break;
                default: // Multiplication
                    a = RANDOM.nextInt(8) + 2;
                    b = RANDOM.nextInt(8) + 2;
                    answer = a * b;
                    expression = a + "×" + b + "=?";
                    break;
            }
        }

        // Generate image
        int width = isIntegral ? 260 : 130;
        int height = 48;
        BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = image.createGraphics();

        // Anti-aliasing for better quality
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        // Background
        g.setColor(Color.WHITE);
        g.fillRect(0, 0, width, height);

        // Draw noise lines
        g.setColor(new Color(220, 220, 220));
        for (int i = 0; i < 8; i++) {
            int x1 = RANDOM.nextInt(width);
            int y1 = RANDOM.nextInt(height);
            int x2 = RANDOM.nextInt(width);
            int y2 = RANDOM.nextInt(height);
            g.drawLine(x1, y1, x2, y2);
        }

        if (isIntegral) {
            drawIntegral(g, integralLower, integralUpper, integralText, width, height);
        } else {
            drawExpression(g, expression, width, height);
        }

        g.dispose();

        // Convert to base64
        String base64Image;
        try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            ImageIO.write(image, "png", baos);
            base64Image = "data:image/png;base64," + Base64.getEncoder().encodeToString(baos.toByteArray());
        } catch (Exception e) {
            Map<String, Object> result = new HashMap<>();
            result.put("enabled", false);
            return result;
        }

        // Store captcha answer in session
        session.setAttribute("captcha", String.valueOf(answer));

        // Return base64 image
        Map<String, Object> result = new HashMap<>();
        result.put("enabled", true);
        result.put("image", base64Image);
        return result;
    }

    /** Draw integral notation: ∫ with limits, then function text, then dx */
    private void drawIntegral(Graphics2D g, String lowerText, String upperText, String funcText,
                              int width, int height) {
        // Fonts
        Font integralFont = new Font("Serif", Font.BOLD, 30);
        Font limitFont = new Font("Arial", Font.BOLD, 14);
        Font funcFont = new Font("Arial", Font.BOLD, 20);

        // Colors
        Color textColor = new Color(
            RANDOM.nextInt(80),
            RANDOM.nextInt(80),
            RANDOM.nextInt(80) + 80
        );

        // Center of the image
        int centerY = height / 2;

        // Draw integral sign
        g.setFont(integralFont);
        g.setColor(textColor);
        FontMetrics fm = g.getFontMetrics();
        int integralX = 10;
        int integralY = centerY + fm.getAscent() / 2 + 2;
        g.drawString("\u222B", integralX, integralY); // ∫

        // Draw upper/lower limits beside the integral sign
        g.setFont(limitFont);
        g.setColor(textColor);
        int limitX = integralX + fm.stringWidth("\u222B") - 4;
        g.drawString(upperText, limitX, centerY - 8);
        g.drawString(lowerText, limitX, centerY + 18);

        // Draw the function to the right
        g.setFont(funcFont);
        int funcX = limitX + 34;
        int funcY = centerY + 8;
        g.drawString(funcText, funcX, funcY);

        // Draw "dx"
        FontMetrics funcFm = g.getFontMetrics();
        int dxX = funcX + funcFm.stringWidth(funcText) + 5;
        g.drawString("dx", dxX, funcY);
    }

    /** Draw a regular arithmetic expression */
    private void drawExpression(Graphics2D g, String expression, int width, int height) {
        Font font = new Font("Arial", Font.BOLD, 22);
        g.setFont(font);
        FontMetrics fm = g.getFontMetrics();
        int totalWidth = fm.stringWidth(expression);
        int x = (width - totalWidth) / 2;
        int y = (height + fm.getAscent()) / 2 - 2;

        for (int i = 0; i < expression.length(); i++) {
            String ch = String.valueOf(expression.charAt(i));
            g.setColor(new Color(
                    RANDOM.nextInt(80),
                    RANDOM.nextInt(80),
                    RANDOM.nextInt(80) + 80
            ));
            AffineTransform old = g.getTransform();
            g.rotate(Math.toRadians(RANDOM.nextInt(20) - 10), x + fm.charWidth(expression.charAt(i)) / 2.0, y);
            g.drawString(ch, x, y);
            g.setTransform(old);
            x += fm.charWidth(expression.charAt(i));
        }
    }
}