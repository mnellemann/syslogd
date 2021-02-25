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
package biz.nellemann.syslogd;

import biz.nellemann.syslogd.msg.SyslogMessage;
import biz.nellemann.syslogd.net.TcpServer;
import biz.nellemann.syslogd.net.UdpClient;
import biz.nellemann.syslogd.net.UdpServer;
import biz.nellemann.syslogd.parser.SyslogParser;
import biz.nellemann.syslogd.parser.SyslogParserRfc3164;
import biz.nellemann.syslogd.parser.SyslogParserRfc5424;
import org.slf4j.impl.SimpleLogger;

import picocli.CommandLine;
import picocli.CommandLine.Command;

import java.io.IOException;
import java.util.concurrent.Callable;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Command(name = "syslogd",
        mixinStandardHelpOptions = true,
        versionProvider = biz.nellemann.syslogd.VersionProvider.class)
public class Application implements Callable<Integer>, LogListener {

    private boolean doForward = false;
    private SyslogParser syslogParser;
    private UdpClient udpClient;


    @CommandLine.Option(names = {"-p", "--port"}, description = "Listening port [default: 514].", defaultValue = "514")
    private int port;

    @CommandLine.Option(names = "--no-udp", negatable = true, description = "Listen on UDP [default: true].", defaultValue = "true")
    private boolean udpServer;

    @CommandLine.Option(names = "--no-tcp", negatable = true, description = "Listen on TCP [default: true].", defaultValue = "true")
    private boolean tcpServer;

    @CommandLine.Option(names = "--no-ansi", negatable = true, description = "Output ANSI colors [default: true].", defaultValue = "true")
    private boolean ansiOutput;

    @CommandLine.Option(names = "--no-stdout", negatable = true, description = "Output messages to stdout [default: true].", defaultValue = "true")
    private boolean stdout;

    @CommandLine.Option(names = "--rfc5424", description = "Parse RFC-5424 messages [default: RFC-3164].", defaultValue = "false")
    private boolean rfc5424;

    @CommandLine.Option(names = { "-f", "--forward"}, description = "Forward to UDP host[:port] (RFC-5424).", paramLabel = "<host>")
    private String forward;

    @CommandLine.Option(names = { "-g", "--gelf"}, description = "Forward in Graylog (GELF) JSON format.", defaultValue = "false")
    private boolean gelf;

    @CommandLine.Option(names = { "-d", "--debug" }, description = "Enable debugging [default: 'false'].")
    private boolean enableDebug = false;


    @Override
    public Integer call() throws IOException {

        if(enableDebug) {
            System.setProperty(SimpleLogger.DEFAULT_LOG_LEVEL_KEY, "DEBUG");
        }

        if(rfc5424) {
            syslogParser = new SyslogParserRfc5424();
        } else {
            syslogParser = new SyslogParserRfc3164();
        }

        if(forward != null && !forward.isEmpty()) {
            String fHost, fPort;
            Pattern pattern = Pattern.compile("^([^:]+)(?::([0-9]+))?$", Pattern.CASE_INSENSITIVE);
            Matcher matcher = pattern.matcher(forward);
            if(matcher.find()) {
                fHost = matcher.group(1);
                if(matcher.groupCount() == 2 && matcher.group(2) != null) {
                    fPort = matcher.group(2);
                } else {
                    fPort = "514";
                }
            } else {
                fHost = "localhost";
                fPort = "514";
            }

            udpClient = new UdpClient(fHost, Integer.parseInt(fPort));
            doForward = true;
        }

        if(udpServer) {
            UdpServer udpServer = new UdpServer(port);
            udpServer.addEventListener(this);
            udpServer.start();
        }

        if(tcpServer) {
            TcpServer tcpServer = new TcpServer(port);
            tcpServer.addEventListener(this);
            tcpServer.start();
        }

        return 0;
    }


    @Override
    public void onLogEvent(LogEvent event) {

        // Parse message
        String message = event.getMessage();
        SyslogMessage msg = null;
        try {
            msg = syslogParser.parse(message);
        } catch(Exception e) {
            e.printStackTrace();
        }

        if(msg != null) {

            if(stdout) {
                if(ansiOutput) {
                    System.out.println(SyslogPrinter.toAnsiString(msg));
                } else {
                    System.out.println(SyslogPrinter.toString(msg));
                }
            }

            if(doForward) {
                try {
                    if(gelf) {
                        udpClient.send(SyslogPrinter.toGelf(msg));
                    } else {
                        udpClient.send(SyslogPrinter.toRfc5424(msg));
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }

    }


    public static void main(String... args) {
        int exitCode = new CommandLine(new Application()).execute(args);
        System.exit(exitCode);
    }

}
