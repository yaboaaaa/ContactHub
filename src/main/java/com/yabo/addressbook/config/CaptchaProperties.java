package com.yabo.addressbook.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.ArrayList;
import java.util.List;

@ConfigurationProperties(prefix = "captcha")
public class CaptchaProperties {

    private boolean enabled;
    private List<IntegralProblem> integrals = new ArrayList<>();

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public List<IntegralProblem> getIntegrals() {
        return integrals;
    }

    public void setIntegrals(List<IntegralProblem> integrals) {
        this.integrals = integrals;
    }

    public static class IntegralProblem {
        /** Lower limit text, e.g. "0", "π/2" */
        private String lowerText = "0";
        /** Upper limit text, e.g. "2", "π" */
        private String upperText = "2";
        /** Function text to display, e.g. "3x²", "sin(x)" */
        private String text = "x";
        /** The correct answer (integer) */
        private int answer = 0;

        public String getLowerText() {
            return lowerText;
        }

        public void setLowerText(String lowerText) {
            this.lowerText = lowerText;
        }

        public String getUpperText() {
            return upperText;
        }

        public void setUpperText(String upperText) {
            this.upperText = upperText;
        }

        public String getText() {
            return text;
        }

        public void setText(String text) {
            this.text = text;
        }

        public int getAnswer() {
            return answer;
        }

        public void setAnswer(int answer) {
            this.answer = answer;
        }
    }
}