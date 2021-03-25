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

import java.util.HashMap;
import java.util.Map;

/*
    0       Emergency: system is unusable
    1       Alert: action must be taken immediately
    2       Critical: critical conditions
    3       Error: error conditions
    4       Warning: warning conditions
    5       Notice: normal but significant condition
    6       Informational: informational messages
    7       Debug: debug-level messages
 */
public enum Severity {

    emerg(0),
    alert(1),
    crit(2),
    error(3),
    warn(4),
    notice(5),
    info(6),
    debug(7);

    // Cache lookups
    private static final Map<Integer, Severity> BY_NUMBER = new HashMap<>();
    static {
        for (Severity s: values()) {
            BY_NUMBER.put(s.severityNumber, s);
        }
    }

    public static Severity getByNumber(Integer number) {
        return BY_NUMBER.get(number);
    }

    public Integer toNumber() {
        return this.severityNumber;
    }

    private final Integer severityNumber;
    Severity(int severityNumber) {
        this.severityNumber = severityNumber;
    }

}
