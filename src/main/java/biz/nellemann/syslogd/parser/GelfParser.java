package biz.nellemann.syslogd.parser;

import biz.nellemann.syslogd.msg.SyslogMessage;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.UnsupportedEncodingException;
import java.time.Instant;
import java.util.zip.DataFormatException;

public class GelfParser extends SyslogParser {

    private final static Logger log = LoggerFactory.getLogger(GelfParser.class);

    private final ObjectMapper objectMapper;

    public GelfParser() {
        objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
    }


    @Override
    public SyslogMessage parse(String input) {
        SyslogMessage message = null;
        try {
            message = objectMapper.readValue(input, SyslogMessage.class);
        } catch (JsonProcessingException e) {
            log.warn("parse() - error: {}", e.getMessage());
        }
        return message;
    }


    /*
        https://go2docs.graylog.org/5-0/getting_in_log_data/gelf.html

        zlib signatures at offset 0
        78 01 : No Compression (no preset dictionary)
        78 5E : Best speed (no preset dictionary)
        78 9C : Default Compression (no preset dictionary)
        78 DA : Best Compression (no preset dictionary)
        78 20 : No Compression (with preset dictionary)
        78 7D : Best speed (with preset dictionary)
        78 BB : Default Compression (with preset dictionary)
        78 F9 : Best Compression (with preset dictionary)

        gzip signature at offset 0
        1F 8B : GZIP compressed
    */

    @Override
    public SyslogMessage parse(byte[] input) {
        String text = null;
        if(input[0] == (byte)0x78 && input[1] == (byte)0x9c) {  // Compressed data - TODO: better detection ?
            try {
                text = decompress(input);
            } catch (DataFormatException | UnsupportedEncodingException e) {
                log.error("parse() - error: {}", e.getMessage());
            }
        } else {
            text = byteArrayToString(input);
        }

        return parse(text);
    }


    @Override
    public Instant parseTimestamp(String dateString) {
        return null;
    }

}
