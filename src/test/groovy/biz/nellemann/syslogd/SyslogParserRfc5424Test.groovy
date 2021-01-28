package biz.nellemann.syslogd

import biz.nellemann.syslogd.msg.SyslogMessage
import biz.nellemann.syslogd.parser.SyslogParser
import biz.nellemann.syslogd.parser.SyslogParserRfc5424
import spock.lang.Specification
import java.time.Instant

class SyslogParserRfc5424Test extends Specification {

    SyslogParser syslogParser;

    void setup() {
        syslogParser = new SyslogParserRfc5424();
    }

    void "test rfc5424 message"() {

        setup:
        def input = '<13>1 2020-09-23T08:57:30.950699+02:00 xps13 mark - - [exampleSDID@32473 iut="3" eventSource="Application" eventID="1011"] adfdfdf3432434565656'

        when:
        SyslogMessage msg = syslogParser.parse(input)

        then:
        msg.message == "adfdfdf3432434565656"
        msg.processId == "-"
    }

    void "test rfc5424 example message"() {

        setup:
        def input = "<34>1 2003-10-11T22:14:15.003Z mymachine.example.com su - ID47 - BOM'su root' failed for lonvick on /dev/pts/8"

        when:
        SyslogMessage msg = syslogParser.parse(input)

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
        SyslogMessage msg = syslogParser.parse(input)

        then:
        msg.hostname == "192.0.2.1"
        msg.application == "myproc"
        msg.processId == "8710"
        msg.messageId == "-"
        msg.structuredData == "-"
    }

    void "test parseRfc5424Timestamp ex1"() {
        setup:
        String dateString = "1985-04-12T23:20:50.52Z"

        when:
        Instant inst = syslogParser.parseTimestamp(dateString)

        then:
        inst.toEpochMilli() == 482196050052
        inst.toString() == "1985-04-12T23:20:50.052Z"
    }

    void "test parseRfc5424Timestamp ex2"() {
        setup:
        String dateString = "1985-04-12T19:20:50.52-04:00"

        when:
        Instant inst = syslogParser.parseTimestamp(dateString)

        then:
        inst.toEpochMilli() == 482196050052
        inst.toString() == "1985-04-12T23:20:50.052Z"
    }

    void "test parseRfc5424Timestamp ex3"() {
        setup:
        String dateString = "2003-10-11T22:14:15.003Z"

        when:
        Instant inst = syslogParser.parseTimestamp(dateString)

        then:
        inst.toEpochMilli() == 1065910455003
        inst.toString() == "2003-10-11T22:14:15.003Z"
    }

    void "test parseRfc5424Timestamp ex4"() {
        setup:
        String dateString = "2003-08-24T05:14:15.000003-07:00"

        when:
        Instant inst = syslogParser.parseTimestamp(dateString)

        then:
        inst.toEpochMilli() == 1061727255003
        inst.toString() == "2003-08-24T12:14:15.003Z"
    }

    void "test parseRfc5424Timestamp ex5"() {
        setup:
        String dateString = "2003-08-24T05:14:15.000000003-07:00"

        when:
        Instant inst = syslogParser.parseTimestamp(dateString)

        then:
        inst.toEpochMilli() == 1061727255003
        inst.toString() == "2003-08-24T12:14:15.003Z"
    }

}


