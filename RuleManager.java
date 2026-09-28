package com.smartlog;

import java.util.ArrayList;
import java.util.List;

public class RuleManager {

    private List<ErrorRule> rules = new ArrayList<>();

    public RuleManager() {
         
    	rules.add(new ErrorRule(
            "Database connection failed",
            "Database connection failure",
            "Database service may be unavailable",
            "Check database service and connection settings",
            "HIGH"
        ));

        rules.add(new ErrorRule(
            "Access denied",
            "Database authentication failure",
            "Invalid username or password",
            "Check database credentials",
            "HIGH"
        ));

        rules.add(new ErrorRule(
            "ERROR",
            "Unknown error detected",
            "The application reported an error",
            "Investigate the error message",
            "MEDIUM"
        ));

        rules.add(new ErrorRule(
            "WARNING",
            "Warning detected",
            "The application detected a possible issue",
            "Monitor this issue",
            "LOW"
        ));
    }
    public List<ErrorRule> getRules() {
        return rules;
    }
}

