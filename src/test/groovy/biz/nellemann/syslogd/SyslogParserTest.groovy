package biz.nellemann.syslogd

import spock.lang.Ignore
import spock.lang.Specification
import java.time.Instant
import java.time.OffsetDateTime;

class SyslogParserTest extends Specification {

    void "test rfc5424 message"() {

        setup:
        def input = '<13>1 2020-09-23T08:57:30.950699+02:00 xps13 mark - - [exampleSDID@32473 iut="3" eventSource="Application" eventID="1011"] adfdfdf3432434565656'

        when:
        SyslogMessage msg = SyslogParser.parseRfc5424(input)

        then:
        msg.message == "adfdfdf3432434565656"
        msg.processId == "-"
    }

    void "test rfc5424 example message"() {

        setup:
        def input = "<34>1 2003-10-11T22:14:15.003Z mymachine.example.com su - ID47 - BOM'su root' failed for lonvick on /dev/pts/8"

        when:
        SyslogMessage msg = SyslogParser.parseRfc5424(input)

        then:
        msg.hostname == "mymachine.example.com"
        msg.application == "su"
        msg.processId == "-"
        msg.messageId == "ID47"
        msg.structuredData == "-"
    }

    void "test rfc5424 example2 message"() {

        setup:
        def input = "<165>1 2003-08-24T05:14:15.000003-07:00 192.0.2.1 myproc 8710 - - %% It's time to make the do-nuts."

        when:
        SyslogMessage msg = SyslogParser.parseRfc5424(input)

        then:
        msg.hostname == "192.0.2.1"
        msg.application == "myproc"
        msg.processId == "8710"
        msg.messageId == "-"
        msg.structuredData == "-"
    }


    void "test rfc3164 aix/vios message"() {

        setup:
        def input = "<13>Sep 23 08:37:09 Message forwarded from p924vio1: padmin: test"

        when:
        SyslogMessage msg = SyslogParser.parseRfc3164(input)

        then:
        msg.message == "test"
        msg.hostname == "p924vio1"
        msg.application == "padmin"
    }

    void "test another rfc3164 aix/vios message"() {

        setup:
        def input = "<13>Dec 18 10:09:22 Message forwarded from p924vio1: root: [errnotify] seq: 24266 - AA8AB241 1218100920 T O OPERATOR OPERATOR NOTIFICATION"

        when:
        SyslogMessage msg = SyslogParser.parseRfc3164(input)

        then:
        msg.message == "[errnotify] seq: 24266 - AA8AB241 1218100920 T O OPERATOR OPERATOR NOTIFICATION"
        msg.hostname == "p924vio1"
        msg.application == "root"
    }

    void "test rfc3164 normal message"() {

        setup:
        def input = "<13>Sep 23 08:53:28 xps13 mark: adfdfdf3432434"

        when:
        SyslogMessage msg = SyslogParser.parseRfc3164(input)

        then:
        msg.message == "adfdfdf3432434"
        msg.hostname == "xps13"
        msg.application == "mark"
    }

    void "test rsyslogd sudo message"() {
        setup:
        String input = "<85>Oct  5 17:13:41 xps13 sudo:     mark : TTY=pts/1 ; PWD=/etc/rsyslog.d ; USER=root ; COMMAND=/usr/sbin/service rsyslog restart"

        when:
        SyslogMessage msg = SyslogParser.parseRfc3164(input)

        then:
        msg.application == "sudo"
        msg.message == "mark : TTY=pts/1 ; PWD=/etc/rsyslog.d ; USER=root ; COMMAND=/usr/sbin/service rsyslog restart"
    }

    void "test gdm-session message"() {
        setup:
        String input = "<12>Oct  5 18:31:01 xps13 /usr/lib/gdm3/gdm-x-session[1921]: (EE) event5  - CUST0001:00 06CB:76AF Touchpad: kernel bug: Touch jump detected and discarded."

        when:
        SyslogMessage msg = SyslogParser.parseRfc3164(input)

        then:
        msg.application == "/usr/lib/gdm3/gdm-x-session[1921]"
        msg.message == "(EE) event5  - CUST0001:00 06CB:76AF Touchpad: kernel bug: Touch jump detected and discarded."
    }

    void "test intellij message"() {
        setup:
        String input = "<14>Oct  6 05:10:26 xps13 com.jetbrains.IntelliJ-IDEA-Ulti git4idea.commands.GitStandardProgressAnalyzer\$1.onLineAvailable(GitStandardProgressAnalyzer.java:45)"

        when:
        SyslogMessage msg = SyslogParser.parseRfc3164(input)

        then:
        msg.application == "com.jetbrains.IntelliJ-IDEA-Ulti"
        msg.message == "git4idea.commands.GitStandardProgressAnalyzer\$1.onLineAvailable(GitStandardProgressAnalyzer.java:45)"
    }

    void "test parseRfc3164Timestamp"() {

        setup:
        OffsetDateTime odt = OffsetDateTime.now()
        String dateString = "Sep 12 20:50:13"

        when:
        Instant inst = SyslogParser.parseRfc3164Timestamp(dateString)

        then:
        inst.toString() == "${odt.getYear()}-09-12T20:50:13Z"
    }

    void "test parseRfc5424Timestamp ex1"() {
        setup:
        String dateString = "1985-04-12T23:20:50.52Z"

        when:
        Instant inst = SyslogParser.parseRfc5424Timestamp(dateString)

        then:
        inst.toString() == "1985-04-12T21:20:50.052Z"
        inst.toEpochMilli() == 482188850052
    }

    void "test parseRfc5424Timestamp ex2"() {
        setup:
        String dateString = "1985-04-12T19:20:50.52-04:00"

        when:
        Instant inst = SyslogParser.parseRfc5424Timestamp(dateString)

        then:
        inst.toString() == "1985-04-12T23:20:50.052Z"
        inst.toEpochMilli() == 482196050052
    }

    void "test parseRfc5424Timestamp ex3"() {
        setup:
        String dateString = "2003-10-11T22:14:15.003Z"

        when:
        Instant inst = SyslogParser.parseRfc5424Timestamp(dateString)

        then:
        inst.toString() == "2003-10-11T20:14:15.003Z"
        inst.toEpochMilli() == 1065903255003
    }

    void "test parseRfc5424Timestamp ex4"() {
        setup:
        String dateString = "2003-08-24T05:14:15.000003-07:00"

        when:
        Instant inst = SyslogParser.parseRfc5424Timestamp(dateString)

        then:
        inst.toString() == "2003-08-24T12:14:15.003Z"
        inst.toEpochMilli() == 1061727255003
    }

    void "test parseRfc5424Timestamp ex5"() {
        setup:
        String dateString = "2003-08-24T05:14:15.000000003-07:00"

        when:
        Instant inst = SyslogParser.parseRfc5424Timestamp(dateString)

        then:
        inst.toString() == "2003-08-24T12:14:15.003Z"
        inst.toEpochMilli() == 1061727255003
    }



    @Ignore
    void "test parseRfc5424Timestamp"() {

        setup:
        String dateString = "2020-09-22T20:10:30.925438+02:00"

        when:
        Instant inst = SyslogParser.parseRfc5424Timestamp(dateString)

        then:
        inst.toString() == "2020-09-22T18:10:30.925438Z"
        inst.toEpochMilli() == 1600798230925l
    }

}


