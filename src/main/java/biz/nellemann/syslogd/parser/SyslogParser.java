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

import biz.nellemann.syslogd.msg.SyslogMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Instant;

public abstract class SyslogParser {

    private final static Logger log = LoggerFactory.getLogger(SyslogParser.class);


    public abstract SyslogMessage parse(final String input);

    public abstract Instant parseTimestamp(final String dateString);


    /**
     * Converts syslog PRI field into Facility.
     *
     * @param pri
     * @return
     */
    public int getFacility(String pri) {

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
    public int getSeverity(String pri) {

        int priority = Integer.parseInt(pri);
        int severity = priority & 0x07;

        //log.debug("getSeverity() - " + pri + " => " + severity);
        return severity;
    }


}
