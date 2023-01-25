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
package biz.nellemann.syslogd.parser;

import biz.nellemann.syslogd.msg.Facility;
import biz.nellemann.syslogd.msg.Severity;
import biz.nellemann.syslogd.msg.SyslogMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.*;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class SyslogParserRfc3164 extends SyslogParser {

    private final static Logger log = LoggerFactory.getLogger(SyslogParserRfc3164.class);

    private final Pattern pattern = Pattern.compile("^<(\\d{1,3})>(\\D{3}\\s+\\d{1,2} \\d{2}:\\d{2}:\\d{2})\\s+(Message forwarded from \\S+:|\\S+:?)\\s+([^\\s:]+):?\\s+(.*)", Pattern.CASE_INSENSITIVE);
    private final DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("yyyy MMM [ ]d HH:mm:ss").withZone(ZoneId.systemDefault());

    /**
     * Parses [rfc3164](https://tools.ietf.org/html/rfc3164) syslog messages.
     *
     * @param input
     * @return
     * @throws NumberFormatException
     */
    @Override
    public SyslogMessage parse(final String input) throws NumberFormatException {

        log.debug("parseRfc3164() " + input);

        Matcher matcher = pattern.matcher(input);
        if(!matcher.find()) {
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
        syslogMessage.timestamp = parseTimestamp(date);
        syslogMessage.hostname = hostname;
        syslogMessage.application = application;

        return syslogMessage;
    }

    @Override
    public SyslogMessage parse(byte[] input) {
        return parse(byteArrayToString(input));
    }


    /**
     * Parse rfc3164 TIMESTAMP field into Instant.
     *
     * @param dateString
     * @return
     */
    public Instant parseTimestamp(String dateString) {

        // We need to add current year to parse date correctly
        OffsetDateTime odt = OffsetDateTime.now();

        // Date: Mmm dd hh:mm:ss
        try {
            return Instant.from(dateTimeFormatter.parse(odt.getYear() + " " + dateString));
        } catch(DateTimeParseException e) {
            log.debug("parseTimestamp()", e);
        }

        log.warn("parseTimestamp() - Could not parse timestamp: " + dateString);
        return Instant.now();
    }

}
