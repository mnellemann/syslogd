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
import biz.nellemann.syslogd.net.*;
import biz.nellemann.syslogd.parser.SyslogParser;
import biz.nellemann.syslogd.parser.SyslogParserRfc3164;
import biz.nellemann.syslogd.parser.SyslogParserRfc5424;
import org.slf4j.impl.SimpleLogger;

import picocli.CommandLine;
import picocli.CommandLine.Command;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.URI;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.Callable;

@Command(name = "syslogd",
        mixinStandardHelpOptions = true,
        versionProvider = biz.nellemann.syslogd.VersionProvider.class)
public class Application implements Callable<Integer>, LogReceiveListener {

    private final List<LogForwardListener> logForwardListeners = new ArrayList<>();
    private SyslogParser syslogParser;


    @CommandLine.Option(names = {"-p", "--port"}, description = "Listening port [default: 514].", defaultValue = "514", paramLabel = "<num>")
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

    @CommandLine.Option(names = { "-s", "--syslog"}, description = "Forward to Syslog <udp://host:port> (RFC-5424).", paramLabel = "<uri>")
    private URI syslog;

    @CommandLine.Option(names = { "-g", "--gelf"}, description = "Forward to Graylog <udp://host:port>.", paramLabel = "<uri>")
    private URI gelf;

    @CommandLine.Option(names = { "-l", "--loki"}, description = "Forward to Grafana Loki <http://host:port>.", paramLabel = "<url>")
    private URL loki;

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

        if(syslog != null) {
            if(syslog.getScheme().toLowerCase(Locale.ROOT).equals("udp")) {
                UdpClient udpClient = new UdpClient(getInetSocketAddress(syslog));
                logForwardListeners.add(udpClient);
            } else {
                throw new UnsupportedOperationException("Forward protocol not implemented: " + syslog.getScheme());
            }
        }

        if(gelf != null) {
            if(gelf.getScheme().toLowerCase(Locale.ROOT).equals("udp")) {
                GelfClient gelfClient = new GelfClient(getInetSocketAddress(gelf));
                logForwardListeners.add(gelfClient);
            } else {
                throw new UnsupportedOperationException("Forward protocol not implemented: " + gelf.getScheme());
            }
        }

        if(loki != null) {
            LokiClient lokiClient = new LokiClient(loki);
            logForwardListeners.add(lokiClient);
            Thread t = new Thread(lokiClient);
            t.start();
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
    public void onLogEvent(LogReceiveEvent event) {

        // Parse message
        String message = event.getMessage();
        SyslogMessage msg = null;
        try {
            msg = syslogParser.parse(message);
        } catch(Exception e) {
            e.printStackTrace();
        }

        if(msg != null) {

            if(logForwardListeners.size() > 0) {
                sendForwardEvent(msg);
            }

            if(stdout) {
                if(ansiOutput) {
                    System.out.println(SyslogPrinter.toAnsiString(msg));
                } else {
                    System.out.println(SyslogPrinter.toString(msg));
                }
            }

        }

    }


    private void sendForwardEvent(SyslogMessage message) {
        LogForwardEvent event = new LogForwardEvent( this, message);
        for (LogForwardListener listener : logForwardListeners) {
            listener.onForwardEvent(event);
        }
    }


    private InetSocketAddress getInetSocketAddress(URI input) {
        InetSocketAddress inetSocketAddress = new InetSocketAddress(input.getHost(), input.getPort());
        return inetSocketAddress;
    }


    public static void main(String... args) {
        int exitCode = new CommandLine(new Application()).execute(args);
        System.exit(exitCode);
    }

}
