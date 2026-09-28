
package com.smartlog;

import java.nio.file.Files;
import java.nio.file.Path;

public class SmartLogDetector {

    public static void main(String[] args) throws Exception {

        LogAnalyzer analyzer = new LogAnalyzer();
        System.out.println("Program Started");

        Files.lines(Path.of("src/com/smartlog/application.log"))
             .forEach(line -> {
            	 System.out.println("READ: " +line);
            	 System.out.println("Calling analyzer...");
            	 String level = line.split(" ")[0];
                 LogEntry logEntry = new LogEntry(line, level);
                 analyzer.analyze(logEntry);
             });
        analyzer.resolveAnomaly(1);
        analyzer.generateReport();
    }
}