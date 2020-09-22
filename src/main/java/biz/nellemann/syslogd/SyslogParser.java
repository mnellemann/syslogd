/*
   Copyright 2020 mark.nellemann@gmail.com

   Licensed under the Apache License, Version 2.0 (the "License");
   you may not use this file except in compliance with the License.
   You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.
 */
package biz.nellemann.syslogd;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.*;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/*
    https://tools.ietf.org/html/rfc5424
    https://tools.ietf.org/html/rfc3164
 */


public class SyslogParser {

    private final static Logger log = LoggerFactory.getLogger(SyslogParser.class);


    public static SyslogMessage parseRfc3164(String input) throws NumberFormatException {

        Pattern pattern = Pattern.compile("^<(\\d{1,3})>(\\D{3} \\d{2} \\d{2}:\\d{2}:\\d{2})\\s+(\\S+)\\s+(\\S+): (.*)", Pattern.CASE_INSENSITIVE);
        Matcher matcher = pattern.matcher(input);
        boolean matchFound = matcher.find();
        if(!matchFound) {
            log.warn("Match not found");
            return null;
        }

        String pri = matcher.group(1);
        String date = matcher.group(2);
        String hostname = matcher.group(3);
        String application = matcher.group(4);
        String message = matcher.group(5);

        log.debug("PRI: " + pri);
        log.debug("DATE: " + date);
        log.debug("HOST: " + hostname);
        log.debug("APP: " + application);
        log.debug("MSG: " + message);

        Integer facility = Integer.parseInt(pri.substring(0, pri.length()-1));
        Integer severity = Integer.parseInt(pri.substring(pri.length()-1));
        log.debug("facility: " + facility);
        log.debug("severity: " + severity);

        SyslogMessage syslogMessage = new SyslogMessage();
        syslogMessage.facility = facility;
        syslogMessage.severity = severity;
        syslogMessage.timestamp = parseRfc3164Timestamp(date);
        syslogMessage.hostname = hostname;
        syslogMessage.application = application;
        syslogMessage.message = message;

        return syslogMessage;
    }


    public static SyslogMessage parseRfc5424(String input) throws NumberFormatException {

        Pattern pattern = Pattern.compile("^<(\\d{1,3})>(\\d+)\\s+(\\S+)\\s+(\\S+)\\s+(\\S+)\\s+(\\S+)\\s+(\\S+)\\s+(\\[.*\\])\\s+(\\S+)", Pattern.CASE_INSENSITIVE);
        Matcher matcher = pattern.matcher(input);
        boolean matchFound = matcher.find();
        if(!matchFound) {
            log.warn("Match not found");
            return null;
        }

        String pri = matcher.group(1);
        String ver = matcher.group(2);
        String date = matcher.group(3);
        String host = matcher.group(4);
        String app = matcher.group(5);
        String procId = matcher.group(6);
        String msgId = matcher.group(7);
        String data = matcher.group(8);
        String msg = matcher.group(9);

        log.debug("PRI: " + pri);
        log.debug("VER: " + ver);
        log.debug("DATE: " + date);
        log.debug("HOST: " + host);
        log.debug("APP: " + app);
        log.debug("PROCID: " + procId);
        log.debug("MSGID: " + msgId);
        log.debug("DATA: " + data);
        log.debug("MSG: " + msg);

        Integer facility = Integer.parseInt(pri.substring(0, pri.length()-1));
        Integer severity = Integer.parseInt(pri.substring(pri.length()-1));
        log.debug("facility: " + facility);
        log.debug("severity: " + severity);

        SyslogMessage syslogMessage = new SyslogMessage();
        syslogMessage.facility = facility;
        syslogMessage.severity = severity;
        syslogMessage.version = Integer.parseInt(ver);
        syslogMessage.timestamp = parseRfc5424Timestamp(date);
        syslogMessage.hostname = host;
        if(app != null && !app.equals("-"))
            syslogMessage.application = app;
        if(procId != null && !procId.equals("-"))
            syslogMessage.processId = procId;
        if(msgId != null && !msgId.equals("-"))
            syslogMessage.messageId = msgId;
        syslogMessage.structuredData = data;
        syslogMessage.message = msg;

        return syslogMessage;
    }


    static protected Instant parseRfc3164Timestamp(String dateString) {

        // We need to add year to parse date correctly
        LocalDateTime dt = LocalDateTime.now();

        // Date: Mmm dd hh:mm:ss
        Instant instant = null;
        try {
            DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("yyyy MMM dd HH:mm:ss")
                .withLocale(Locale.getDefault())
                .withZone(ZoneId.systemDefault());
            LocalDateTime dateTime = LocalDateTime.parse(dt.getYear() + " " + dateString, dateTimeFormatter);
            instant = dateTime.toInstant(ZoneOffset.UTC);
        } catch(DateTimeParseException e) {
            log.error("parseDate()", e);
        }

        return instant;
    }


    static protected Instant parseRfc5424Timestamp(String dateString) {

        Instant instant = null;
        try {
            DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ISO_OFFSET_DATE_TIME;
            instant = Instant.from(dateTimeFormatter.parse(dateString));
        } catch(DateTimeParseException e) {
            log.error("parseTimestamp()", e);
        }

        return instant;
    }

}
