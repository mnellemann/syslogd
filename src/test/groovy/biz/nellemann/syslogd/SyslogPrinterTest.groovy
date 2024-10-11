package biz.nellemann.syslogd

import biz.nellemann.syslogd.msg.SyslogMessage
import biz.nellemann.syslogd.parser.SyslogParser
import biz.nellemann.syslogd.parser.SyslogParserRfc5424
import spock.lang.Specification

class SyslogPrinterTest extends Specification {

    void setup() {
    }

    void "to plain"() {
        setup:
        SyslogParser syslogParser = new SyslogParserRfc5424();
        String input = '<13>1 2020-09-23T08:57:30.950699+02:00 xps13 mark - - [exampleSDID@32473 iut="3" eventSource="Application" eventID="1011"] adfdfdf3432434565656 abcdefghijklmnopqrstuvwxyz'
        SyslogMessage msg = syslogParser.parse(input)

        when:
        String output = SyslogPrinter.toString(msg)

        then:
        output.endsWith("abcdefghijklmnopqrstuvwxyz")
    }

    void "test toGelf"() {
        setup:
        SyslogParser syslogParser = new SyslogParserRfc5424();
        String input = '<13>1 2020-09-23T08:57:30.950699+02:00 xps13 mark - - [exampleSDID@32473 iut="3" eventSource="Application" eventID="1011"] adfdfdf3432434565656 abcdefghijklmnopqrstuvwxyz'
        SyslogMessage msg = syslogParser.parse(input)

        when:
        String output = SyslogPrinter.toGelf(msg)

        then:
        output.contains("_structured-data")
    }

    void "test toLoki"() {
        setup:
        SyslogParser syslogParser = new SyslogParserRfc5424();
        String input = '<13>1 2020-09-23T08:57:30.950699+02:00 xps13 mark - - [exampleSDID@32473 iut="3" eventSource="Application" eventID="1011"] adfdfdf3432434565656 abcdefghijklmnopqrstuvwxyz'
        SyslogMessage msg = syslogParser.parse(input)

        when:
        String output = SyslogPrinter.toLoki(msg)

        then:
        output == '{ "streams": [ { "stream": { "hostname": "xps13", "facility": "user", "level": "notice", "application": "mark"}, "values": [ [ "1600844250000000000", "[user.notice] xps13 mark adfdfdf3432434565656 abcdefghijklmnopqrstuvwxyz" ] ] } ] }'
    }

}


