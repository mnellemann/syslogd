package biz.nellemann.syslogd

import spock.lang.Specification
import java.time.Instant
import java.time.LocalDateTime;

class SyslogParserTest extends Specification {

    void "test parseRfc3164Timestamp"() {

        setup:
        LocalDateTime dt = LocalDateTime.now()
        String dateString = "Sep 12 22:50:13"

        when:
        Instant inst = SyslogParser.parseRfc3164Timestamp(dateString)

        then:
        inst.toString() == "${dt.getYear()}-09-12T20:50:13Z"
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
