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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import picocli.CommandLine;
import picocli.CommandLine.Command;

import java.io.IOException;
import java.util.concurrent.Callable;

@Command(name = "syslogd",
        mixinStandardHelpOptions = true,
        description = "Simple Syslog Server",
        versionProvider = biz.nellemann.syslogd.VersionProvider.class)
public class Application implements Callable<Integer>, LogListener {

    private final static Logger log = LoggerFactory.getLogger(Application.class);

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

    @CommandLine.Option(names = { "-f", "--forward"}, description = "Forward messages (UDP RFC-3164) [default: false].", defaultValue = "false")
    private boolean forward;

    @CommandLine.Option(names = "--forward-host", description = "Forward to host [default: localhost].", paramLabel = "<hostname>", defaultValue = "localhost")
    private String forwardHost;

    @CommandLine.Option(names = "--forward-port", description = "Forward to port [default: 1514].", paramLabel = "<port>", defaultValue = "1514")
    private int forwardPort;

    private UdpClient udpClient;

    @Override
    public Integer call() throws IOException {

        if(forward) {
            udpClient = new UdpClient(forwardHost, forwardPort);
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
            if(rfc5424) {
                msg = SyslogParser.parseRfc5424(message);
            } else {
                msg = SyslogParser.parseRfc3164(message);
            }
        } catch(Exception e) {
            log.error("onLogEvent() - Error parsing message: ", e);
        }

        if(msg != null) {

            if(stdout) {
                if(ansiOutput) {
                    System.out.println(SyslogPrinter.toAnsiString(msg));
                } else {
                    System.out.println(SyslogPrinter.toString(msg));
                }
            }

            if(forward) {
                try {
                    udpClient.send(SyslogPrinter.toRfc3164(msg));
                } catch (Exception e) {
                    log.error("onLogEvent()", e);
                }
            }
        }

    }


    public static void main(String... args) {
        int exitCode = new CommandLine(new Application()).execute(args);
        System.exit(exitCode);
    }

}
