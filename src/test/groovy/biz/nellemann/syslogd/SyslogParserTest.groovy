package biz.nellemann.syslogd

import biz.nellemann.syslogd.msg.Facility
import biz.nellemann.syslogd.msg.Severity
import biz.nellemann.syslogd.parser.SyslogParser
import biz.nellemann.syslogd.parser.SyslogParserRfc5424
import spock.lang.Specification

class SyslogParserTest extends Specification {

    SyslogParser syslogParser;

    void setup() {
        syslogParser = new SyslogParserRfc5424();
    }

    void "test facility LOCAL0"() {
        when:
        int code = syslogParser.getFacility("132")

        then:
        code == Facility.local0.toNumber()
    }

    void "test severity WARN"() {
        when:
        int code = syslogParser.getSeverity("132")

        then:
        code == Severity.warn.toNumber()
    }

}


