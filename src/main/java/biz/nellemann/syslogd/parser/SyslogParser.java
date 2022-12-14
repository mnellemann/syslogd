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
package biz.nellemann.syslogd.parser;

import biz.nellemann.syslogd.msg.SyslogMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.zip.DataFormatException;
import java.util.zip.Inflater;

public abstract class SyslogParser {

    private final static Logger log = LoggerFactory.getLogger(SyslogParser.class);

    public abstract SyslogMessage parse(final String input);
    public abstract SyslogMessage parse(final byte[] input);

    public abstract Instant parseTimestamp(final String dateString);


    /**
     * Converts syslog PRI field into Facility.
     *
     * @param pri
     * @return
     */
    public int getFacility(String pri) {
        int priority = Integer.parseInt(pri);
        int facility = priority >> 3;
        return facility;
    }


    /**
     * Converts syslog PRI field into Severity.
     *
     * @param pri
     * @return
     */
    public int getSeverity(String pri) {
        int priority = Integer.parseInt(pri);
        int severity = priority & 0x07;
        return severity;
    }


    protected String byteArrayToString(byte[] input) {
        return new String(input, 0, input.length, StandardCharsets.UTF_8);
    }


    protected byte[] decompress(byte[] data) {

        byte[] result = new byte[data.length * 2];
        try {
            // Decompress the bytes
            Inflater decompressor = new Inflater();
            decompressor.setInput(data, 0, data.length);
            //byte[] result = new byte[data.length * 2];
            int resultLength = decompressor.inflate(result);
            decompressor.end();

            // Decode the bytes into a String
            //uncompressed = new String(result, 0, resultLength, StandardCharsets.UTF_8);
        } catch (DataFormatException e) {
            log.error("decompress() - error: {}", e.getMessage());
        }

        return result;
    }

}
