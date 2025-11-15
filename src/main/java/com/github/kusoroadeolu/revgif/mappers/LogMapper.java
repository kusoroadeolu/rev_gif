package com.github.kusoroadeolu.revgif.mappers;

import com.github.kusoroadeolu.revgif.dtos.LogDump;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Component
public class LogMapper {

    private final DateTimeFormatter dateTimeFormatter;

    public LogMapper(@Value("${date-time-pattern}") String dateTimePattern) {
        this.dateTimeFormatter = DateTimeFormatter.ofPattern(dateTimePattern);
    }

    public String getLog(String className, String logMessage) {
        String timestamp = this.dateTimeFormatter.format(LocalDateTime.now());
        return new LogDump(timestamp, className, logMessage).toString();
    }
}
