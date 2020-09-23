package biz.nellemann.syslogd

import spock.lang.Specification
import java.time.Instant
import java.time.OffsetDateTime;

class SyslogParserTest extends Specification {

    void "test rfc5424 message"() {

        setup:
        def input = "<13>1 2020-09-23T08:57:30.950699+02:00 xps13 mark - - [timeQuality tzKnown=\"1\" isSynced=\"1\" syncAccuracy=\"125500\"] adfdfdf3432434565656"

        when:
        SyslogMessage msg = SyslogParser.parseRfc5424(input)

        then:
        msg.message == "adfdfdf3432434565656"
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

    void "test parseRfc3164Timestamp"() {

        setup:
        OffsetDateTime odt = OffsetDateTime.now()
        String dateString = "Sep 12 20:50:13"

        when:
        Instant inst = SyslogParser.parseRfc3164Timestamp(dateString)

        then:
        inst.toString() == "${odt.getYear()}-09-12T20:50:13Z"
    }

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
