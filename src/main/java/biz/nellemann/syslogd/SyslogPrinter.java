/*
   Copyright 2021 mark.nellemann@gmail.com

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

import biz.nellemann.syslogd.msg.Facility;
import biz.nellemann.syslogd.msg.Severity;
import biz.nellemann.syslogd.msg.SyslogMessage;
import biz.nellemann.syslogd.parser.JsonUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SyslogPrinter {

    private final static Logger log = LoggerFactory.getLogger(SyslogPrinter.class);

    private final static char SPACE = ' ';


    public static String toString(SyslogMessage msg) {
        StringBuilder sb = new StringBuilder(msg.timestamp.toString());
        sb.append(String.format("  [%8.8s.%-6.6s] ", msg.facility, msg.severity));
        sb.append(String.format(" %-16.16s ", msg.hostname));
        sb.append(String.format(" %-16.16s  ", msg.application));
        sb.append(msg.message);
        return sb.toString();
    }


    public static String toAnsiString(SyslogMessage msg) {
        StringBuilder sb = new StringBuilder(msg.timestamp.toString());

        if (msg.severity.toNumber() < 3) {
            sb.append(Ansi.RED);
        } else if (msg.severity.toNumber() < 5) {
            sb.append(Ansi.YELLOW);
        } else {
            sb.append(Ansi.GREEN);
        }

        sb.append(String.format("  [%8.8s.%-6.6s] ", msg.facility, msg.severity)).append(Ansi.RESET);
        sb.append(Ansi.BLUE).append(String.format(" %-16.16s ", msg.hostname)).append(Ansi.RESET);
        sb.append(Ansi.CYAN).append(String.format(" %-16.16s  ", msg.application)).append(Ansi.RESET);
        sb.append(msg.message);

        return sb.toString();
    }


    public static String toHtml(SyslogMessage msg) {
        String colorClass;
        if (msg.severity.toNumber() < 3) {
            colorClass = "danger";
        } else if (msg.severity.toNumber() < 5) {
            colorClass = "warning";
        } else {
            colorClass = "success";
        }

        StringBuilder sb = new StringBuilder();
        sb.append(String.format("<tr><td>%s</td>", msg.timestamp))   ;
        sb.append(String.format("<td class=\"has-text-%s\">%s.%s</td>", colorClass, msg.facility, msg.severity));
        sb.append(String.format("<td class=\"has-text-link\">%s</td>", msg.hostname));
        sb.append(String.format("<td class=\"has-text-info\">%s</td>", msg.application));
        sb.append(String.format("<td>%s</td></tr>", msg.message));
        return sb.toString();
    }


    /**
     * Return a RFC-3164 formatted string of the SyslogMessage.
     * @param msg
     * @return
     */
    public static String toRfc3164(SyslogMessage msg) {
        StringBuilder sb = new StringBuilder();
        sb.append(getPri(msg.facility, msg.severity));
        sb.append(new java.text.SimpleDateFormat("MMM dd HH:mm:ss").format(new java.util.Date(msg.timestamp.toEpochMilli())));
        sb.append(SPACE).append(msg.hostname);
        sb.append(SPACE).append(msg.application);
        sb.append(":").append(SPACE).append(msg.message);
        log.debug(sb.toString());
        return sb.toString();
    }


    /**
     * Return a RFC-5424 formatted string of the SyslogMessage.
     * @param msg
     * @return
     */
    public static String toRfc5424(SyslogMessage msg) {
        StringBuilder sb = new StringBuilder();
        sb.append(getPri(msg.facility, msg.severity)).append("1");
        sb.append(SPACE).append(new java.text.SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'").format(new java.util.Date(msg.timestamp.toEpochMilli())));
        sb.append(SPACE).append(msg.hostname);
        sb.append(SPACE).append(msg.application);
        sb.append(SPACE).append(msg.processId != null ? msg.processId : "-");
        sb.append(SPACE).append(msg.messageId != null ? msg.messageId : "-");
        sb.append(SPACE).append(msg.structuredData != null ? msg.structuredData : "-");
        sb.append(SPACE).append(msg.message);
        log.debug(sb.toString());
        return sb.toString();
    }


    /**
     * Return a GELF JSON formatted string of the SyslogMessage.
     * https://www.graylog.org/features/gelf
     * @param msg
     * @return
     */
    public static String toGelf(SyslogMessage msg) {
        StringBuilder sb = new StringBuilder("{ \"version\": \"1.1\"");
        sb.append(String.format(", \"host\": \"%s\"", msg.hostname));
        sb.append(String.format(", \"short_message\": \"%s\"", JsonUtil.encode(msg.message)));
        sb.append(String.format(", \"full_message\": \"%s\"", JsonUtil.encode(msg.structuredData)));
        sb.append(String.format(", \"timestamp\": %d", msg.timestamp.getEpochSecond()));
        sb.append(String.format(", \"level\": %d", msg.severity.toNumber()));
        sb.append(String.format(", \"_facility\": \"%s\"", msg.facility));
        sb.append(String.format(", \"_severity\": \"%s\"", msg.severity));
        sb.append(String.format(", \"_application\": \"%s\"", msg.application));
        if(msg.processId != null) { sb.append(String.format(", \"_process-id\": \"%s\"", msg.processId)); }
        if(msg.messageId != null) { sb.append(String.format(", \"_message-id\": \"%s\"", msg.messageId)); }
        if(msg.structuredData != null) { sb.append(String.format(", \"_structured-data\": \"%s\"", JsonUtil.encode(msg.structuredData))); }
        sb.append("}");
        return sb.toString();
    }


    /**
     * Return a Loki JSON formatted string of the SyslogMessage.
     * https://grafana.com/docs/loki/latest/api/
     * @param msg
     * @return
     */
    public static String toLoki(SyslogMessage msg) {
        StringBuilder sb = new StringBuilder("{ \"streams\": [ { \"stream\": {");
        sb.append(String.format(" \"hostname\": \"%s\",", msg.hostname));
        sb.append(String.format(" \"facility\": \"%s\",", msg.facility));
        sb.append(String.format(" \"level\": \"%s\",", msg.severity));
        sb.append(String.format(" \"application\": \"%s\"", msg.application));
        sb.append("}, \"values\": [ ");
        sb.append(String.format("[ \"%d\", \"%s\" ]", msg.timestamp.getEpochSecond() * 1000000000L, getMessageLine(msg)));
        sb.append(" ] } ] }");
        log.debug(sb.toString());
        return sb.toString();
    }


    private static String getMessageLine(SyslogMessage msg) {
        StringBuilder sb = new StringBuilder();
        sb.append(String.format("[%s.%s] ", msg.facility, msg.severity));
        sb.append(String.format("%s ", msg.hostname));
        sb.append(String.format("%s ", msg.application));
        sb.append(JsonUtil.encode(msg.message));
        return sb.toString();
    }


    static private String getPri(Facility facility, Severity severity) {
        int pri = (facility.toNumber() * 8) + severity.toNumber();
        return String.format("%c%d%c", '<', pri, '>');
    }

}
