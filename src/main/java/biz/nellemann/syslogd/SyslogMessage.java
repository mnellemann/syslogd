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

import java.time.Instant;

public class SyslogMessage {

    protected Facility facility;
    protected Severity severity;

    // The VERSION field denotes the version of the syslog protocol specification.
    protected Integer version;

    // The TIMESTAMP field is a formalized timestamp derived from [RFC3339].
    protected Instant timestamp;

    // The HOSTNAME field identifies the machine that originally sent the syslog message.
    protected String hostname;

    // The APP-NAME field SHOULD identify the device or application that originated the message.
    protected String application;

    // The PROCID field is often used to provide the process name or process ID associated with a syslog system.
    protected String processId;

    // The MSGID SHOULD identify the type of message.
    protected String messageId;

    // STRUCTURED-DATA provides a mechanism to express information in a well defined, easily parseable and interpretable data format.
    protected String structuredData;

    // The MSG part contains a free-form message that provides information about the event.
    protected final String message;

    SyslogMessage(final String message) {
        this.message = message;
    }



    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(timestamp.toString());
        sb.append(String.format("  [%6.6s.%-6.6s] ", facility, severity));
        sb.append(String.format(" %-24.24s ", hostname));
        sb.append(String.format(" %-32.43s ", application));
        sb.append(message);
        return sb.toString();
    }


    public String toAnsiString() {
        StringBuilder sb = new StringBuilder();

        sb.append(timestamp.toString());

        if(severity.toNumber() < 3 ) {
            sb.append(Ansi.RED);
        } else if(severity.toNumber() < 5) {
            sb.append(Ansi.YELLOW);
        } else {
            sb.append(Ansi.GREEN);
        }

        sb.append(String.format("  [%6.6s.%-6.6s] ", facility, severity)).append(Ansi.RESET);
        sb.append(Ansi.BLUE).append(String.format(" %-24.24s ", hostname)).append(Ansi.RESET);
        sb.append(Ansi.CYAN).append(String.format(" %-32.32s ", application)).append(Ansi.RESET);
        sb.append(message);

        return sb.toString();
    }

}
