package biz.nellemann.syslogd

import biz.nellemann.syslogd.msg.SyslogMessage
import biz.nellemann.syslogd.parser.SyslogParser
import biz.nellemann.syslogd.parser.SyslogParserRfc3164
import spock.lang.Specification

import java.text.DateFormat
import java.time.Instant
import java.time.OffsetDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter;

class SyslogParserRfc3164Test extends Specification {

    SyslogParser syslogParser;

    void setup() {
        syslogParser = new SyslogParserRfc3164();
    }

    void "test rfc3164 aix/vios message"() {

        setup:
        def input = "<13>Sep 23 08:37:09 Message forwarded from p924vio1: padmin: test"

        when:
        SyslogMessage msg = syslogParser.parse(input)

        then:
        msg.message == "test"
        msg.hostname == "p924vio1"
        msg.application == "padmin"
    }

    void "test another rfc3164 aix/vios message"() {

        setup:
        def input = "<13>Dec 18 10:09:22 Message forwarded from p924vio1: root: [errnotify] seq: 24266 - AA8AB241 1218100920 T O OPERATOR OPERATOR NOTIFICATION"

        when:
        SyslogMessage msg = syslogParser.parse(input)

        then:
        msg.message == "[errnotify] seq: 24266 - AA8AB241 1218100920 T O OPERATOR OPERATOR NOTIFICATION"
        msg.hostname == "p924vio1"
        msg.application == "root"
    }

    void "test rfc3164 normal message"() {

        setup:
        def input = "<13>Sep 23 08:53:28 xps13 mark: adfdfdf3432434"

        when:
        SyslogMessage msg = syslogParser.parse(input)

        then:
        msg.message == "adfdfdf3432434"
        msg.hostname == "xps13"
        msg.application == "mark"
    }

    void "test rsyslogd sudo message"() {
        setup:
        String input = "<85>Oct  5 17:13:41 xps13 sudo:     mark : TTY=pts/1 ; PWD=/etc/rsyslog.d ; USER=root ; COMMAND=/usr/sbin/service rsyslog restart"

        when:
        SyslogMessage msg = syslogParser.parse(input)

        then:
        msg.application == "sudo"
        msg.message == "mark : TTY=pts/1 ; PWD=/etc/rsyslog.d ; USER=root ; COMMAND=/usr/sbin/service rsyslog restart"
    }

    void "test gdm-session message"() {
        setup:
        String input = "<12>Oct  5 18:31:01 xps13 /usr/lib/gdm3/gdm-x-session[1921]: (EE) event5  - CUST0001:00 06CB:76AF Touchpad: kernel bug: Touch jump detected and discarded."

        when:
        SyslogMessage msg = syslogParser.parse(input)

        then:
        msg.application == "/usr/lib/gdm3/gdm-x-session[1921]"
        msg.message == "(EE) event5  - CUST0001:00 06CB:76AF Touchpad: kernel bug: Touch jump detected and discarded."
    }

    void "test intellij message"() {
        setup:
        String input = "<14>Oct  6 05:10:26 xps13 com.jetbrains.IntelliJ-IDEA-Ulti git4idea.commands.GitStandardProgressAnalyzer\$1.onLineAvailable(GitStandardProgressAnalyzer.java:45)"

        when:
        SyslogMessage msg = syslogParser.parse(input)

        then:
        msg.application == "com.jetbrains.IntelliJ-IDEA-Ulti"
        msg.message == "git4idea.commands.GitStandardProgressAnalyzer\$1.onLineAvailable(GitStandardProgressAnalyzer.java:45)"
    }

    void "test parseRfc3164Timestamp"() {

        setup:
        OffsetDateTime odt = OffsetDateTime.now();
        String dateString = "Sep 12 20:50:13"

        when:
        Instant instant = syslogParser.parseTimestamp(dateString)
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("YYYY MMM dd HH:mm:ss").withZone(ZoneId.systemDefault());

        then:
        assert formatter.format(instant).equals(odt.getYear() + " " + dateString);
    }

}


