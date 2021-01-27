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

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class SyslogParser {

    private final static Logger log = LoggerFactory.getLogger(SyslogParser.class);


    /**
     * Parses [rfc3164](https://tools.ietf.org/html/rfc3164) syslog messages.
     *
     * @param input
     * @return
     * @throws NumberFormatException
     */
    public static SyslogMessage parseRfc3164(final String input) throws NumberFormatException {

        Pattern pattern = Pattern.compile("^<(\\d{1,3})>(\\D{3}\\s+\\d{1,2} \\d{2}:\\d{2}:\\d{2})\\s+(Message forwarded from \\S+:|\\S+)\\s+([^\\s:]+):?\\s+(.*)", Pattern.CASE_INSENSITIVE);
        Matcher matcher = pattern.matcher(input);
        boolean matchFound = matcher.find();
        if(!matchFound) {
            //log.warn("parseRfc3164() - Match not found in: ");
            System.err.println("!" + input);
            return null;
        }

        String pri = matcher.group(1);
        String date = matcher.group(2);
        String hostname = matcher.group(3);
        String application = matcher.group(4);
        String msg = matcher.group(5);

        if(hostname.endsWith(":")) {
            String[] tmp = hostname.split(" ");
            hostname = tmp[tmp.length-1];
            hostname = hostname.substring(0, hostname.length()-1);
        }

        Integer facility = getFacility(pri);
        Integer severity = getSeverity(pri);

        SyslogMessage syslogMessage = new SyslogMessage(msg.trim());
        syslogMessage.facility = Facility.getByNumber(facility);
        syslogMessage.severity = Severity.getByNumber(severity);
        syslogMessage.timestamp = parseRfc3164Timestamp(date);
        syslogMessage.hostname = hostname;
        syslogMessage.application = application;

        return syslogMessage;
    }


    /**
     * Parses [rfc5424](https://tools.ietf.org/html/rfc5424) syslog messages.
     *
     * @param input
     * @return
     * @throws NumberFormatException
     */
    public static SyslogMessage parseRfc5424(final String input) throws NumberFormatException {

        log.warn("parseRfc5424() " + input);

        // "<165>1 2003-08-24T05:14:15.000003-07:00 192.0.2.1 myproc 8710 - - %% It's time to make the do-nuts."
        // '<13>1 2020-09-23T08:57:30.950699+02:00 xps13 mark - - [exampleSDID@32473 iut="3" eventSource="Application" eventID="1011"] adfdfdf3432434565656'
        Pattern pattern = Pattern.compile("^<(\\d{1,3})>(\\d+)\\s+(\\S+)\\s+(\\S+)\\s+(\\S+)\\s+(\\S+)\\s+(\\S+)\\s+(\\[.*\\]|-)\\s+(\\S+)", Pattern.CASE_INSENSITIVE);
        Matcher matcher = pattern.matcher(input);
        boolean matchFound = matcher.find();
        if(!matchFound) {
            log.debug("parseRfc5424() - Match not found in: " + input);
            System.err.println("!" + input);
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

        Integer facility = getFacility(pri);
        Integer severity = getSeverity(pri);

        SyslogMessage syslogMessage = new SyslogMessage(msg.trim());
        syslogMessage.facility = Facility.getByNumber(facility);
        syslogMessage.severity = Severity.getByNumber(severity);
        syslogMessage.version = Integer.parseInt(ver);
        syslogMessage.timestamp = parseRfc5424Timestamp(date);
        syslogMessage.hostname = host;
        if(app != null && !app.equals("-"))
            syslogMessage.application = app;
        if(procId != null && !procId.equals("-"))
            syslogMessage.processId = procId;
        if(msgId != null && !msgId.equals("-"))
            syslogMessage.messageId = msgId;
        if(data != null && !data.equals("-"))
            syslogMessage.structuredData = data;

        return syslogMessage;
    }


    /**
     * Parse rfc3164 TIMESTAMP field into Instant.
     *
     * @param dateString
     * @return
     */
    static protected Instant parseRfc3164Timestamp(String dateString) {

        // We need to add year to parse date correctly
        OffsetDateTime odt = OffsetDateTime.now();

        // Date: Mmm dd hh:mm:ss
        Instant instant = null;
        try {
            DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("yyyy MMM [ ]d HH:mm:ss").withZone(ZoneOffset.UTC);
            instant = Instant.from(dateTimeFormatter.parse(odt.getYear() + " " + dateString));
        } catch(DateTimeParseException e) {
            log.error("parseDate()", e);
        }

        return instant;
    }


    /**
     * Parse rfc5424 TIMESTAMP field into Instant.
     *
     * @param dateString
     * @return
     */
    static protected Instant parseRfc5424Timestamp(String dateString) {

        /*
        https://docs.oracle.com/javase/8/docs/api/java/time/format/DateTimeFormatter.html

        ex1: 1985-04-12T23:20:50.52Z
        ex2: 1985-04-12T19:20:50.52-04:00
        ex3: 2003-10-11T22:14:15.003Z
        ex4: 2003-08-24T05:14:15.000003-07:00
        ex5: 2003-08-24T05:14:15.000000003-07:00
         */

        List<String> formatStrings = Arrays.asList(
            "yyyy-MM-dd'T'HH:mm:ss.SS'Z'",
            "yyyy-MM-dd'T'HH:mm:ss.SSXXX",
            "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'",
            "yyyy-MM-dd'T'HH:mm:ss.SSSSSSXXX",
            "yyyy-MM-dd'T'HH:mm:ss.SSSSSSSSSXXX"
        );

        for(String formatString : formatStrings)
        {
            try {
                return new SimpleDateFormat(formatString).parse(dateString).toInstant();
            }
            catch (ParseException e) {}
        }
        /*
        try {
            DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ISO_OFFSET_DATE_TIME;
            instant = Instant.from(dateTimeFormatter.parse(dateString));
        } catch(DateTimeParseException e) {
            log.error("parseTimestamp()", e);
        }
        return instant;*/
        return null;
    }


    /**
     * Converts syslog PRI field into Facility.
     *
     * @param pri
     * @return
     */
    static protected int getFacility(String pri) {

        int priority = Integer.parseInt(pri);
        int facility = priority >> 3;

        //log.debug("getFacility() - " + pri + " => " + facility);
        return facility;
    }


    /**
     * Converts syslog PRI field into Severity.
     *
     * @param pri
     * @return
     */
    static protected int getSeverity(String pri) {

        int priority = Integer.parseInt(pri);
        int severity = priority & 0x07;

        //log.debug("getSeverity() - " + pri + " => " + severity);
        return severity;
    }


}
