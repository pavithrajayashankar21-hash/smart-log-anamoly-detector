package com.smartlog;

import java.util.HashMap;
import java.util.Map;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;

public class LogAnalyzer {

    private RuleManager ruleManager;

    private Map<String, Integer> errorCount = new HashMap<>();
    private Map<String, Integer> patternCount = new HashMap<>();

    private String url = "jdbc:mysql://host.docker.internal:3306/jdbcforme?useSSL=false&allowPublicKeyRetrieval=true";
;
    private String username = "root";
    private String password = 

    public LogAnalyzer() {

        ruleManager = new RuleManager();

        System.out.println(
                "Rules loaded: " + ruleManager.getRules().size()
        );
    }

    // Converts changing values into common placeholders
    private String normalizeMessage(String message) {

        // Remove IP addresses
        message = message.replaceAll(
                "\\b(?:\\d{1,3}\\.){3}\\d{1,3}\\b",
                "<IP>"
        );

        // Remove user IDs
        message = message.replaceAll(
                "(?i)(user\\s+)(\\d+)",
                "$1<ID>"
        );

        // Remove request IDs
        message = message.replaceAll(
                "(?i)(request[-_ ]?id[:= ]+)([a-zA-Z0-9-]+)",
                "$1<ID>"
        );

        // Remove long numeric IDs
        message = message.replaceAll(
                "\\b\\d{4,}\\b",
                "<ID>"
        );

        return message;
    }

    private void saveAnomaly(
            String message,
            String level,
            String problem,
            String severity,
            int occurrences) {

        String sql = "INSERT INTO log_anomalies "
                + "(message, level, problem, severity, occurrences, status) "
                + "VALUES (?, ?, ?, ?, ?, ?)";

        try (
                Connection con = DriverManager.getConnection(
                        url,
                        username,
                        password
                );

                PreparedStatement ps = con.prepareStatement(sql)
        ) {

            ps.setString(1, message);
            ps.setString(2, level);
            ps.setString(3, problem);
            ps.setString(4, severity);
            ps.setInt(5, occurrences);
            ps.setString(6, "PENDING");

            ps.executeUpdate();

            System.out.println("Anomaly saved to database");

        } catch (Exception e) {

            System.out.println("Database connection failed");

            e.printStackTrace();
        }
    }

    public void analyze(LogEntry logEntry) {

        System.out.println("ANALYZE STARTED");

        System.out.println("BEFORE GET MESSAGE");

        String message = logEntry.getMessage();

        System.out.println("AFTER GET MESSAGE");

        String level = logEntry.getLevel();

        System.out.println("AFTER GET LEVEL");

        System.out.println("BEFORE NORMALIZE");

        // Create normalized version for smart pattern detection
        String normalizedMessage = normalizeMessage(message);

        System.out.println("AFTER NORMALIZE");

        System.out.println("BEFORE RULE CHECK");

        System.out.println(
                "RULE COUNT: " + ruleManager.getRules().size()
        );

        for (ErrorRule rule : ruleManager.getRules()) {

            System.out.println(
                    "CHECKING RULE: " + rule.getProblem()
            );
        }

        // Exact message count
        errorCount.put(
                message,
                errorCount.getOrDefault(message, 0) + 1
        );

        // -----------------------------------------
        // FIND MATCHING RULE
        // -----------------------------------------

        boolean matched = false;

        String matchedPattern = null;

        ErrorRule matchedRule = null;

        System.out.println(
                "RULE COUNT IN ANALYZE: "
                        + ruleManager.getRules().size()
        );

        // Find matching rule
        for (ErrorRule rule : ruleManager.getRules()) {

            System.out.println(
                    "CHECKING RULE: " + rule.getProblem()
            );

            System.out.println("CALLING MATCHES");

            if (rule.matches(normalizedMessage)) {

                System.out.println(
                        "RULE MATCHED: " + rule.getProblem()
                );

                matched = true;

                matchedRule = rule;

                matchedPattern = rule.getProblem();

                break;
            }
        }

        // -----------------------------------------
        // COUNT SIMILAR PROBLEMS
        // -----------------------------------------

        if (matchedPattern != null) {

            patternCount.put(
                    matchedPattern,
                    patternCount.getOrDefault(
                            matchedPattern,
                            0
                    ) + 1
            );
        }

        // -----------------------------------------
        // DISPLAY DETECTED PROBLEM
        // -----------------------------------------

        if (matched) {

            System.out.println("Level: " + level);

            System.out.println(
                    "Problem: " + matchedRule.getProblem()
            );

            System.out.println(
                    "Likely Cause: " + matchedRule.getCause()
            );

            System.out.println(
                    "Suggested Action: " + matchedRule.getAction()
            );

            System.out.println(
                    "Severity: " + matchedRule.getSeverity()
            );

            int occurrences = patternCount.get(
                    matchedPattern
            );

            // Detect anomaly based on common pattern
            if (occurrences >= 3) {

                System.out.println(
                        "ANOMALY: Repeated pattern detected"
                );

                System.out.println(
                        "Pattern: " + matchedPattern
                );

                System.out.println(
                        "Occurrences: " + occurrences
                );

                saveAnomaly(
                        message,
                        level,
                        matchedRule.getProblem(),
                        matchedRule.getSeverity(),
                        occurrences
                );
            }

        } else {

            System.out.println("Normal log");
        }

        System.out.println("-----------------------------");
    }

    public void resolveAnomaly(int id) {

        String sql = "UPDATE log_anomalies "
                + "SET status = 'RESOLVED' "
                + "WHERE id = ?";

        try (
                Connection con = DriverManager.getConnection(
                        url,
                        username,
                        password
                );

                PreparedStatement ps = con.prepareStatement(sql)
        ) {

            ps.setInt(1, id);

            ps.executeUpdate();

            System.out.println(
                    "Anomaly resolved successfully"
            );

        } catch (Exception e) {

            e.printStackTrace();
        }
    }
    public void generateReport() {

        String sql = "SELECT COUNT(*) AS total, " +
                     "SUM(severity = 'HIGH') AS high_count, " +
                     "SUM(severity = 'MEDIUM') AS medium_count, " +
                     "SUM(severity = 'LOW') AS low_count " +
                     "FROM log_anomalies";

        try (Connection con = DriverManager.getConnection(url, username, password);
             PreparedStatement ps = con.prepareStatement(sql);
             java.sql.ResultSet rs = ps.executeQuery()) {

            if (rs.next()) {

                System.out.println("===== FINAL REPORT =====");
                System.out.println("Total Anomalies: " + rs.getInt("total"));
                System.out.println("HIGH: " + rs.getInt("high_count"));
                System.out.println("MEDIUM: " + rs.getInt("medium_count"));
                System.out.println("LOW: " + rs.getInt("low_count"));
                System.out.println("========================");
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
