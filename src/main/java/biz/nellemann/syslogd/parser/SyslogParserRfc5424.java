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

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import biz.nellemann.syslogd.msg.Facility;
import biz.nellemann.syslogd.msg.Severity;
import biz.nellemann.syslogd.msg.SyslogMessage;

public class SyslogParserRfc5424 extends SyslogParser {

    private final static Logger log = LoggerFactory.getLogger(SyslogParserRfc5424.class);

    private final Pattern pattern = Pattern.compile("^<(\\d{1,3})>(\\d+)\\s+(\\S+)\\s+(\\S+)\\s+(\\S+)\\s+(\\S+)\\s+(\\S+)\\s+(\\[.*\\]|-)\\s+(.*)", Pattern.CASE_INSENSITIVE);

    /**
     * Parses [rfc5424](https://tools.ietf.org/html/rfc5424) syslog messages.
     *
     * @param input
     * @return
     * @throws NumberFormatException
     */
    @Override
    public SyslogMessage parse(final String input) throws NumberFormatException {

        log.debug("parseRfc5424() " + input);

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
        syslogMessage.version = ver;
        syslogMessage.timestamp = parseTimestamp(date);
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

    @Override
    public SyslogMessage parse(byte[] input) {
        return parse(byteArrayToString(input));
    }


    /**
     * Parse rfc5424 TIMESTAMP field into Instant.
     *
     * @param dateString
     * @return
     */
    @Override
    public Instant parseTimestamp(String dateString) {

        /*
        https://docs.oracle.com/javase/8/docs/api/java/time/format/DateTimeFormatter.html

        ex1: 1985-04-12T23:20:50.52Z
        ex2: 1985-04-12T19:20:50.52-04:00
        ex3: 2003-10-11T22:14:15.003Z
        ex4: 2003-08-24T05:14:15.000003-07:00
        ex5: 2003-08-24T05:14:15.000000003-07:00
         */

        List<String> formatStrings = Arrays.asList(
            //"yyyy-MM-dd'T'HH:mm:ss.SS'X'",
            "yyyy-MM-dd'T'HH:mm:ss.SSX",
            "yyyy-MM-dd'T'HH:mm:ss.SSSX",
            "yyyy-MM-dd'T'HH:mm:ss.SSSSSSX",
            "yyyy-MM-dd'T'HH:mm:ss.SSSSSSSSSX"
        );

        for(String formatString : formatStrings)
        {
            try {
                return new SimpleDateFormat(formatString).parse(dateString).toInstant();
            }
            catch (ParseException e) {
                log.debug("parseTimestamp() " + e.getMessage());
            }
        }

        log.warn("parseTimestamp() - Could not parse timestamp: " + dateString);
        return Instant.now();
    }

}
