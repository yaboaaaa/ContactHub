package com.example.addressbook.dto;

import java.util.ArrayList;
import java.util.List;

public class ImportResult {

    private int successCount;
    private int failCount;
    private List<String> failures;

    public ImportResult() {
        this.failures = new ArrayList<>();
    }

    public ImportResult(int successCount, int failCount, List<String> failures) {
        this.successCount = successCount;
        this.failCount = failCount;
        this.failures = failures != null ? failures : new ArrayList<>();
    }

    public void addFailure(String reason) {
        this.failures.add(reason);
        this.failCount++;
    }

    public int getSuccessCount() {
        return successCount;
    }

    public void setSuccessCount(int successCount) {
        this.successCount = successCount;
    }

    public int getFailCount() {
        return failCount;
    }

    public void setFailCount(int failCount) {
        this.failCount = failCount;
    }

    public List<String> getFailures() {
        return failures;
    }

    public void setFailures(List<String> failures) {
        this.failures = failures;
    }
}