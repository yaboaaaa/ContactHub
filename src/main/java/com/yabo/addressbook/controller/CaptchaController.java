package com.yabo.addressbook.controller;

import com.yabo.addressbook.config.CaptchaProperties;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.util.List;
import java.util.Random;

@Controller
public class CaptchaController {

    @Value("${captcha.enabled:false}")
    private boolean captchaEnabled;

    private final CaptchaProperties captchaProperties;

    private static final Random RANDOM = new Random();

    public CaptchaController(CaptchaProperties captchaProperties) {
        this.captchaProperties = captchaProperties;
    }

    @GetMapping(value = "/captcha", produces = MediaType.IMAGE_PNG_VALUE)
    public void getCaptcha(HttpSession session, HttpServletResponse response) throws Exception {
        response.setContentType("image/png");
        response.setHeader("Cache-Control", "no-cache, no-store");
        response.setHeader("Pragma", "no-cache");

        if (!captchaEnabled) {
            // Return 1x1 transparent PNG
            BufferedImage img = new BufferedImage(1, 1, BufferedImage.TYPE_INT_ARGB);
            ImageIO.write(img, "png", response.getOutputStream());
            return;
        }

        int op = RANDOM.nextInt(4);
        int answer;
        String expression;
        boolean isIntegral = false;
        String integralLower = null, integralUpper = null, integralText = null;

        if (op == 3) {
            isIntegral = true;
            List<CaptchaProperties.IntegralProblem> problems = captchaProperties.getIntegrals();
            if (problems == null || problems.isEmpty()) {
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
            expression = null;
        } else {
            int a, b;
            switch (op) {
                case 0:
                    a = RANDOM.nextInt(90) + 10;
                    b = RANDOM.nextInt(90) + 10;
                    answer = a + b;
                    expression = a + "+" + b + "=?";
                    break;
                case 1:
                    a = RANDOM.nextInt(80) + 20;
                    b = RANDOM.nextInt(a - 1) + 1;
                    answer = a - b;
                    expression = a + "-" + b + "=?";
                    break;
                default:
                    a = RANDOM.nextInt(8) + 2;
                    b = RANDOM.nextInt(8) + 2;
                    answer = a * b;
                    expression = a + "\u00d7" + b + "=?";
                    break;
            }
        }

        int width = isIntegral ? 260 : 130;
        int height = 48;
        BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = image.createGraphics();

        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g.setColor(Color.WHITE);
        g.fillRect(0, 0, width, height);

        g.setColor(new Color(220, 220, 220));
        for (int i = 0; i < 8; i++) {
            g.drawLine(RANDOM.nextInt(width), RANDOM.nextInt(height), RANDOM.nextInt(width), RANDOM.nextInt(height));
        }

        if (isIntegral) {
            drawIntegral(g, integralLower, integralUpper, integralText, width, height);
        } else {
            drawExpression(g, expression, width, height);
        }

        g.dispose();

        session.setAttribute("captcha", String.valueOf(answer));
        ImageIO.write(image, "png", response.getOutputStream());
    }

    private void drawIntegral(Graphics2D g, String lowerText, String upperText, String funcText,
                              int width, int height) {
        Font integralFont = new Font("Serif", Font.BOLD, 30);
        Font limitFont = new Font("Arial", Font.BOLD, 14);
        Font funcFont = new Font("Arial", Font.BOLD, 20);
        Color textColor = new Color(RANDOM.nextInt(80), RANDOM.nextInt(80), RANDOM.nextInt(80) + 80);
        int centerY = height / 2;

        g.setFont(integralFont);
        g.setColor(textColor);
        FontMetrics fm = g.getFontMetrics();
        int integralX = 10;
        g.drawString("\u222b", integralX, centerY + fm.getAscent() / 2 + 2);

        g.setFont(limitFont);
        int limitX = integralX + fm.stringWidth("\u222b") - 4;
        g.drawString(upperText, limitX, centerY - 8);
        g.drawString(lowerText, limitX, centerY + 18);

        g.setFont(funcFont);
        int funcX = limitX + 34;
        g.drawString(funcText, funcX, centerY + 8);

        FontMetrics funcFm = g.getFontMetrics();
        g.drawString("dx", funcX + funcFm.stringWidth(funcText) + 5, centerY + 8);
    }

    private void drawExpression(Graphics2D g, String expression, int width, int height) {
        Font font = new Font("Arial", Font.BOLD, 22);
        g.setFont(font);
        FontMetrics fm = g.getFontMetrics();
        int totalWidth = fm.stringWidth(expression);
        int x = (width - totalWidth) / 2;
        int y = (height + fm.getAscent()) / 2 - 2;

        for (int i = 0; i < expression.length(); i++) {
            char ch = expression.charAt(i);
            g.setColor(new Color(RANDOM.nextInt(80), RANDOM.nextInt(80), RANDOM.nextInt(80) + 80));
            AffineTransform old = g.getTransform();
            g.rotate(Math.toRadians(RANDOM.nextInt(20) - 10), x + fm.charWidth(ch) / 2.0, y);
            g.drawString(String.valueOf(ch), x, y);
            g.setTransform(old);
            x += fm.charWidth(ch);
        }
    }
}
