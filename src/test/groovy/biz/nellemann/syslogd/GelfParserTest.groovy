package biz.nellemann.syslogd

import biz.nellemann.syslogd.msg.Facility
import biz.nellemann.syslogd.msg.Severity
import biz.nellemann.syslogd.msg.SyslogMessage
import biz.nellemann.syslogd.parser.GelfParser
import biz.nellemann.syslogd.parser.SyslogParser
import spock.lang.Specification


class GelfParserTest extends Specification {

    SyslogParser syslogParser;

    void setup() {
        syslogParser = new GelfParser();
    }

    void "uncompressed GELF message"() {

        setup:
        def input = '{"version":"1.1","host":"pop-os.localdomain","short_message":"main() - Starting VTD-Camera","full_message":"main() - Starting VTD-Camera\\n","timestamp":1670357783.694,"level":4,"_thread_name":"main","_logger_name":"vtd.camera.Application"}'

        when:
        SyslogMessage msg = syslogParser.parse(input)

        then:
        msg.version == "1.1"
        msg.message == "main() - Starting VTD-Camera"
        msg.hostname == "pop-os.localdomain"
        msg.application == "vtd.camera.Application"
        msg.processId == "main"
        msg.timestamp.toString() == "2022-12-06T20:16:23.694Z"
    }


    void "compressed GELF message"() {

        setup:
        byte[] input = [ 120, -100, -51, 81, 77, 107, -61, 48, 12, -3, 43, -63, -25, -38, -115, -77, -75, -51, 2, -127, -11, -80, -61, 96, -73, 94, 11, -63, 56, 90, -30, -59, 31, -63, -106, -53, -54, -40, 127, -97, 92, 86, -40, 79, -40, 69, -78, -97, -11, -12, -98, -28, 47, 118, -127, -104, 76, -16, -84, 99, 82, 72, -74, 97, 115, 72, 72, 23, -77, 114, 89, 115, 73, 65, -14, 102, 87, 11, -56, 92, -125, -57, -88, 44, -105, 66, 7, -73, 102, 4, 97, 60, 66, -12, -54, 18, 45, -51, 33, -30, -32, 32, 37, 53, 1, -15, -31, 66, -43, -3, 49, -29, 76, -39, 104, -123, -92, 113, -54, 90, 83, -63, 75, 121, -86, 114, 42, 84, 7, -67, 83, 113, 17, 30, -84, 5, -89, -68, 127, -98, -100, 50, -74, 40, 84, 8, 94, 81, -113, -90, -118, -32, 2, -62, 113, 28, 35, -79, 123, -39, -18, -124, 108, -91, 104, -102, 7, 33, -27, -95, 74, 4, 82, -13, -41, -79, -9, -39, 22, 43, -17, -108, -2, -127, -109, -77, 39, 47, 104, -56, 8, 42, -73, -78, 78, -18, 15, -11, -82, -106, -121, -89, 86, -44, -113, -5, 13, -77, 100, -52, -78, -114, 78, -125, 90, -41, 65, -121, 76, -21, -67, -110, -31, 113, 97, -65, 88, 113, 69, -128, -93, 61, -57, -126, -39, 48, 77, 16, -17, -80, -6, 8, 57, -118, 99, -119, 39, -48, 57, 26, -68, -2, -99, -21, -51, 36, -14, 13, 55, 34, 77, 72, -1, 60, -28, 72, -126, 108, 70, 92, 83, 119, -34, -98, -73, -29, 34, 110, -67, 5, -119, -35, 53, -63, 95, -88, 102, -115, 97, 44, 8, -50, 17, -44, 120, 87, 44, 76, -18, 77, -32, 109, -35, -42, 28, 62, 65, -13, -122, 125, -1, 0, -40, 60, -57, -72 ];

        when:
        SyslogMessage msg = syslogParser.parse(input)

        then:
        msg.version == "1.1"
        msg.facility == Facility.user;
        msg.severity == Severity.info;
        msg.message == "event=AuthenticationSuccessEvent username=mark.nellemann@gmail.com tenant=2 remoteAddress=185.181.223.117 sessionId=null"
        msg.hostname == "ip-10-1-101-250.eu-central-1.compute.internal"
        msg.application == "ajour.AjourSecuritySuccessEventListener"
        msg.processId == "http-nio-8080-exec-2"
        msg.timestamp.toString() == "2022-12-08T12:16:38.046Z"
    }


    void "chunked GELF message"() {

        setup:
        byte[] chunk1 = [30, 15, -103, 51, -96, -10, -51, -31, 39, -41, 0, 2, 123, 34, 118, 101, 114, 115, 105, 111, 110, 34, 58, 34, 49, 46, 49, 34, 44, 34, 104, 111, 115, 116, 34, 58, 34, 105, 112, 45, 49, 48, 45, 49, 45, 49, 48, 49, 45, 49, 52, 46, 101, 117, 45, 99, 101, 110, 116, 114, 97, 108, 45, 49, 46, 99, 111, 109, 112, 117, 116, 101, 46, 105, 110, 116, 101, 114, 110, 97, 108, 34, 44, 34, 115, 104, 111, 114, 116, 95, 109, 101, 115, 115, 97, 103, 101, 34, 58, 34, 101, 118, 101, 110, 116, 61, 65, 117, 116, 104, 101, 110, 116, 105, 99, 97, 116, 105, 111, 110, 83, 117, 99, 99, 101, 115, 115, 69, 118, 101, 110, 116, 32, 117, 115, 101, 114, 110, 97, 109, 101, 61, 109, 97, 114, 107, 46, 110, 101, 108, 108, 101, 109, 97, 110, 110, 64, 103, 109, 97, 105, 108, 46, 99, 111, 109, 32, 116, 101, 110, 97, 110, 116, 61, 50, 32, 114, 101, 109, 111, 116, 101, 65, 100, 100, 114, 101, 115, 115, 61, 49, 56, 53, 46, 49, 56, 49, 46, 50, 50, 51, 46, 49, 49, 55, 32, 115, 101, 115, 115, 105, 111, 110, 73, 100, 61, 110, 117, 108, 108, 34, 44, 34, 102, 117, 108, 108, 95, 109, 101, 115, 115, 97, 103, 101, 34, 58, 34, 101, 118, 101, 110, 116, 61, 65, 117, 116, 104, 101, 110, 116, 105, 99, 97, 116, 105, 111, 110, 83, 117, 99, 99, 101, 115, 115, 69, 118, 101, 110, 116, 32, 117, 115, 101, 114, 110, 97, 109, 101, 61, 109, 97, 114, 107, 46, 110, 101, 108, 108, 101, 109, 97, 110, 110, 64, 103, 109, 97, 105, 108, 46, 99, 111, 109, 32, 116, 101, 110, 97, 110, 116, 61, 50, 32, 114, 101, 109, 111, 116, 101, 65, 100, 100, 114, 101, 115, 115, 61, 49, 56, 53, 46, 49, 56, 49, 46, 50, 50, 51, 46, 49, 49, 55, 32, 115, 101, 115, 115, 105, 111, 110, 73, 100, 61, 110, 117, 108, 108, 92, 110, 34, 44, 34, 116, 105, 109, 101, 115, 116, 97, 109, 112, 34, 58, 49, 54, 55, 49, 48, 48, 48, 53, 53, 54, 46, 56, 48, 50, 44, 34, 108, 101, 118, 101, 108, 34, 58, 54, 44, 34, 95, 97, 112, 112, 95, 99, 111, 117, 110, 116, 114, 121, 34, 58, 34, 100, 107, 34, 44, 34, 95, 97, 112, 112, 95, 110, 97, 109, 101, 34, 58, 34, 109, 105, 110, 116, 114, 34, 44, 34, 95, 108, 111, 103, 103, 101, 114, 95, 110, 97, 109, 101, 34, 58, 34, 97, 106, 111, 117, 114, 46, 65, 106, 111, 117, 114, 83, 101, 99, 117, 114, 105, 116, 121, 83, 117, 99, 99, 101, 115, 115, 69, 118, 101, 110, 116, 76, 105, 115, 116, 101, 110, 101, 114, 34, 44, 34, 95, 115, 101, 114, 118, 101, 114, 95, 117, 114, 108]
        byte[] chunk2 = [30, 15, -103, 51, -96, -10, -51, -31, 39, -41, 1, 2, 34, 58, 34, 104, 116, 116, 112, 115, 58, 92, 47, 92, 47, 100, 107, 46, 109, 105, 110, 116, 114, 46, 97, 112, 112, 34, 44, 34, 95, 97, 112, 112, 95, 101, 110, 118, 34, 58, 34, 112, 114, 111, 100, 34, 44, 34, 95, 116, 104, 114, 101, 97, 100, 95, 110, 97, 109, 101, 34, 58, 34, 104, 116, 116, 112, 45, 110, 105, 111, 45, 56, 48, 56, 48, 45, 101, 120, 101, 99, 45, 52, 34, 125]

        when:
        syslogParser.parse(chunk1)
        SyslogMessage msg = syslogParser.parse(chunk2)

        then:
        msg.message == "event=AuthenticationSuccessEvent username=mark.nellemann@gmail.com tenant=2 remoteAddress=185.181.223.117 sessionId=null"

    }


    void "chunked GELF unordered message"() {

        setup:
        byte[] chunk1 = [30, 15, -103, 51, -96, -10, -51, -31, 39, -41, 0, 2, 123, 34, 118, 101, 114, 115, 105, 111, 110, 34, 58, 34, 49, 46, 49, 34, 44, 34, 104, 111, 115, 116, 34, 58, 34, 105, 112, 45, 49, 48, 45, 49, 45, 49, 48, 49, 45, 49, 52, 46, 101, 117, 45, 99, 101, 110, 116, 114, 97, 108, 45, 49, 46, 99, 111, 109, 112, 117, 116, 101, 46, 105, 110, 116, 101, 114, 110, 97, 108, 34, 44, 34, 115, 104, 111, 114, 116, 95, 109, 101, 115, 115, 97, 103, 101, 34, 58, 34, 101, 118, 101, 110, 116, 61, 65, 117, 116, 104, 101, 110, 116, 105, 99, 97, 116, 105, 111, 110, 83, 117, 99, 99, 101, 115, 115, 69, 118, 101, 110, 116, 32, 117, 115, 101, 114, 110, 97, 109, 101, 61, 109, 97, 114, 107, 46, 110, 101, 108, 108, 101, 109, 97, 110, 110, 64, 103, 109, 97, 105, 108, 46, 99, 111, 109, 32, 116, 101, 110, 97, 110, 116, 61, 50, 32, 114, 101, 109, 111, 116, 101, 65, 100, 100, 114, 101, 115, 115, 61, 49, 56, 53, 46, 49, 56, 49, 46, 50, 50, 51, 46, 49, 49, 55, 32, 115, 101, 115, 115, 105, 111, 110, 73, 100, 61, 110, 117, 108, 108, 34, 44, 34, 102, 117, 108, 108, 95, 109, 101, 115, 115, 97, 103, 101, 34, 58, 34, 101, 118, 101, 110, 116, 61, 65, 117, 116, 104, 101, 110, 116, 105, 99, 97, 116, 105, 111, 110, 83, 117, 99, 99, 101, 115, 115, 69, 118, 101, 110, 116, 32, 117, 115, 101, 114, 110, 97, 109, 101, 61, 109, 97, 114, 107, 46, 110, 101, 108, 108, 101, 109, 97, 110, 110, 64, 103, 109, 97, 105, 108, 46, 99, 111, 109, 32, 116, 101, 110, 97, 110, 116, 61, 50, 32, 114, 101, 109, 111, 116, 101, 65, 100, 100, 114, 101, 115, 115, 61, 49, 56, 53, 46, 49, 56, 49, 46, 50, 50, 51, 46, 49, 49, 55, 32, 115, 101, 115, 115, 105, 111, 110, 73, 100, 61, 110, 117, 108, 108, 92, 110, 34, 44, 34, 116, 105, 109, 101, 115, 116, 97, 109, 112, 34, 58, 49, 54, 55, 49, 48, 48, 48, 53, 53, 54, 46, 56, 48, 50, 44, 34, 108, 101, 118, 101, 108, 34, 58, 54, 44, 34, 95, 97, 112, 112, 95, 99, 111, 117, 110, 116, 114, 121, 34, 58, 34, 100, 107, 34, 44, 34, 95, 97, 112, 112, 95, 110, 97, 109, 101, 34, 58, 34, 109, 105, 110, 116, 114, 34, 44, 34, 95, 108, 111, 103, 103, 101, 114, 95, 110, 97, 109, 101, 34, 58, 34, 97, 106, 111, 117, 114, 46, 65, 106, 111, 117, 114, 83, 101, 99, 117, 114, 105, 116, 121, 83, 117, 99, 99, 101, 115, 115, 69, 118, 101, 110, 116, 76, 105, 115, 116, 101, 110, 101, 114, 34, 44, 34, 95, 115, 101, 114, 118, 101, 114, 95, 117, 114, 108]
        byte[] chunk2 = [30, 15, -103, 51, -96, -10, -51, -31, 39, -41, 1, 2, 34, 58, 34, 104, 116, 116, 112, 115, 58, 92, 47, 92, 47, 100, 107, 46, 109, 105, 110, 116, 114, 46, 97, 112, 112, 34, 44, 34, 95, 97, 112, 112, 95, 101, 110, 118, 34, 58, 34, 112, 114, 111, 100, 34, 44, 34, 95, 116, 104, 114, 101, 97, 100, 95, 110, 97, 109, 101, 34, 58, 34, 104, 116, 116, 112, 45, 110, 105, 111, 45, 56, 48, 56, 48, 45, 101, 120, 101, 99, 45, 52, 34, 125]

        when:
        syslogParser.parse(chunk2)
        SyslogMessage msg = syslogParser.parse(chunk1)

        then:
        msg.message == "event=AuthenticationSuccessEvent username=mark.nellemann@gmail.com tenant=2 remoteAddress=185.181.223.117 sessionId=null"

    }

    void "junk GET request"() {

        setup:
        def input = 'GET /'

        when:
        SyslogMessage msg = syslogParser.parse(input)

        then:
        msg == null
    }

}
