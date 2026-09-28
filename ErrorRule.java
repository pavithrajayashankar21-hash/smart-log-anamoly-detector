package com.smartlog;

public class ErrorRule {

    private String pattern;
    private String problem;
    private String cause;
    private String action;
    private String severity;

    public ErrorRule(String pattern, String problem, String cause,
                     String action, String severity) {

        this.pattern = pattern;
        this.problem = problem;
        this.cause = cause;
        this.action = action;
        this.severity = severity;
    }

    public boolean matches(String message) {
    	System.out.println("MESSAGE = " + message);
        System.out.println("PATTERN = " + pattern);
        System.out.println("MATCH RESULT = " + message.contains(pattern));
        return message.contains(pattern);
    }

    public String getProblem() {
        return problem;
    }

    public String getCause() {
        return cause;
    }

    public String getAction() {
        return action;
    }

    public String getSeverity() {
        return severity;
    }
}



