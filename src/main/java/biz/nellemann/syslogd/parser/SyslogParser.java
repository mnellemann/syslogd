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
import java.time.Instant;

public abstract class SyslogParser {

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
        return severity;
    }

}
