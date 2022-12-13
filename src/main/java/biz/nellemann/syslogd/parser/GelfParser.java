package biz.nellemann.syslogd.parser;

import biz.nellemann.syslogd.msg.SyslogMessage;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.apache.commons.collections4.map.PassiveExpiringMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Instant;
import java.util.*;

public class GelfParser extends SyslogParser {

    private final static Logger log = LoggerFactory.getLogger(GelfParser.class);

    private final ObjectMapper objectMapper;

    private final int expiryInMills = 10_000;
    private final PassiveExpiringMap<Integer, TreeMap<Integer, byte[]>> expiringMap = new PassiveExpiringMap<>(expiryInMills);


    public GelfParser() {
        objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
    }


    /*
        Magic Bytes - 2 bytes: 0x1e 0x0f
        Message ID - 8 bytes: Must be the same for every chunk of this message.
            Identifies the whole message and is used to reassemble the chunks later.
            Generate from millisecond timestamp + hostname, for example.
        Sequence number - 1 byte: The sequence number of this chunk starts at 0 and is always less than the sequence count.
        Sequence count - 1 byte: Total number of chunks this message has.

        All chunks MUST arrive within 5 seconds or the server will discard all chunks that have arrived or are in the process of arriving. A message MUST NOT consist of more than 128 chunks.
    */
    private SyslogMessage parseChunked(byte[] input) {

        if(input.length < 12) return null;

        byte[] messageId = { input[2], input[3], input[4], input[5], input[6], input[7], input[8], input[9] };
        byte seqNumber = input[10];
        byte seqTotal = input[11];
        byte[] payload = Arrays.copyOfRange(input, 12, input.length);
        log.debug("parseChunked() - msgId: {}, seqNo: {}, seqTot: {}, payload: {}", messageId, seqNumber, seqTotal, byteArrayToString(payload));

        // messageId byte[] to int
        int id = 0;
        for (byte b : messageId) {
            id = (id << 8) + (b & 0xFF);
        }

        TreeMap<Integer, byte[]> integerTreeMap;
        if(expiringMap.containsKey(id)) {
            integerTreeMap = expiringMap.get(id);
        } else {
            integerTreeMap = new TreeMap<>();
        }
        integerTreeMap.put((int)seqNumber, payload);
        expiringMap.put(id, integerTreeMap);

        if(seqNumber+1 >= seqTotal) {
            StringBuilder sb = new StringBuilder();
            integerTreeMap.forEach( (i, p) -> {
                sb.append(byteArrayToString(p));
            });
            return parse(sb.toString());
        }

        return null;
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


        for(byte b : input) {
            if(b > 0x0) {
                System.out.printf("%d, ", (b & 0xff));
            }
        }
        System.out.println();


        if(input.length < 8) return null;   // TODO: Find proper minimum input length ?

        // Compressed data: 0x78 0x9c
        if(input[0] == (byte)0x78 && input[1] == (byte)0x9c) {
            input = decompress(input);
        }

        // Magic Bytes: 0x1e 0x0f
        if(input[0] == (byte)0x1e && input[1] == (byte)0x0f) {
            return parseChunked(input);
        }

        return parse(byteArrayToString(input));
    }


    @Override
    public Instant parseTimestamp(String dateString) {
        return null;
    }

}
