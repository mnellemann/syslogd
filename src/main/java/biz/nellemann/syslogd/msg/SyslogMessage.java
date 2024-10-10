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
package biz.nellemann.syslogd.msg;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.lang.reflect.Member;
import java.time.Instant;

@JsonIgnoreProperties(ignoreUnknown = true)
public class SyslogMessage implements Comparable<SyslogMessage> {

    @JsonIgnore
    public Facility facility = Facility.user;

    @JsonProperty("level")
    public Severity severity = Severity.info;

    // The VERSION field denotes the version of the syslog protocol specification.
    public String version;

    // The TIMESTAMP field is a formalized timestamp derived from [RFC3339].
    @JsonProperty("timestamp")  // 1670357783.694 - in GELF: seconds since UNIX epoch with optional decimal places for milliseconds
    public Instant timestamp = Instant.now();

    // The HOSTNAME field identifies the machine that originally sent the syslog message.
    @JsonProperty("host")
    public String hostname;

    // The APP-NAME field SHOULD identify the device or application that originated the message.
    @JsonProperty("_logger_name")
    public String application;

    // The PROCID field is often used to provide the process name or process ID associated with a syslog system.
    @JsonProperty("_thread_name")
    public String processId;

    // The MSGID SHOULD identify the type of message.
    @JsonIgnore
    public String messageId;

    // STRUCTURED-DATA provides a mechanism to express information in a well defined, easily parseable and interpretable data format.
    @JsonProperty("full_message")
    public String structuredData;

    // The MSG part contains a free-form message that provides information about the event.
    @JsonProperty("short_message")
    public String message;

    @JsonCreator
    public SyslogMessage(@JsonProperty("short_message") final String message) {
        this.message = message;
    }

    public int length() {
        if(message == null) return 0;
        return message.length();
    }

    @Override
    public int compareTo(SyslogMessage other) {
        if(this.timestamp.isBefore(other.timestamp)) {
            return 1;
        }
        if(this.timestamp.isAfter(other.timestamp)) {
            return -1;
        }
        return 0;
    }


}
