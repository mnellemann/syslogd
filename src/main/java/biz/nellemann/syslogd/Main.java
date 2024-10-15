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

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.URI;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.Callable;

import org.apache.commons.collections4.queue.CircularFifoQueue;

import biz.nellemann.syslogd.msg.SyslogMessage;
import biz.nellemann.syslogd.net.GelfClient;
import biz.nellemann.syslogd.net.LokiClient;
import biz.nellemann.syslogd.net.TcpServer;
import biz.nellemann.syslogd.net.UdpClient;
import biz.nellemann.syslogd.net.UdpServer;
import biz.nellemann.syslogd.net.WebServer;
import biz.nellemann.syslogd.parser.GelfParser;
import biz.nellemann.syslogd.parser.SyslogParser;
import biz.nellemann.syslogd.parser.SyslogParserRfc3164;
import biz.nellemann.syslogd.parser.SyslogParserRfc5424;
import picocli.CommandLine;
import picocli.CommandLine.Command;
import ro.pippo.core.Pippo;
import ro.pippo.core.PippoSettings;
import ro.pippo.core.RuntimeMode;

@Command(name = "syslogd",
        mixinStandardHelpOptions = true,
        versionProvider = biz.nellemann.syslogd.VersionProvider.class)
public class Main implements Callable<Integer>, LogReceiveListener {

    private CircularFifoQueue<SyslogMessage>    queue = new CircularFifoQueue<>(500);
    private final List<LogForwardListener> logForwardListeners = new ArrayList<>();
    private SyslogParser syslogParser;


    @CommandLine.Option(names = {"-p", "--port"}, description = "Listening port [default: ${DEFAULT-VALUE}].", defaultValue = "1514", paramLabel = "<num>")
    private int port;


    @CommandLine.Option(names = "--no-monitor", negatable = true, description = "Start Monitor UI on port 8514 [default: ${DEFAULT-VALUE}].", defaultValue = "true")
    private boolean monitor;

    @CommandLine.Option(names = "--monitor-port", description = "Monitor listening port [default: ${DEFAULT-VALUE}].", defaultValue = "8514", paramLabel = "<num>")
    private int monitorPort;

    @CommandLine.Option(names = "--monitor-path", description = "Monitor context path [default: ${DEFAULT-VALUE}].", defaultValue = "/", paramLabel = "<path>")
    private String monitorPath;


    @CommandLine.Option(names = "--no-udp", negatable = true, description = "Listen on UDP [default: ${DEFAULT-VALUE}].", defaultValue = "true")
    private boolean udpServer;

    @CommandLine.Option(names = "--no-tcp", negatable = true, description = "Listen on TCP [default: ${DEFAULT-VALUE}].", defaultValue = "true")
    private boolean tcpServer;


    @CommandLine.Option(names = "--no-ansi", negatable = true, description = "Output in ANSI colors [default: ${DEFAULT-VALUE}].", defaultValue = "true")
    private boolean ansiOutput;

    @CommandLine.Option(names = "--no-stdout", negatable = true, description = "Output messages to stdout [default: ${DEFAULT-VALUE}].", defaultValue = "true")
    private boolean stdout;

    @CommandLine.Option(names = "--no-stdin", negatable = true, description = "Forward messages from stdin [default: ${DEFAULT-VALUE}].", defaultValue = "true")
    private boolean stdin;


    @CommandLine.Option(names = {"-f", "--format"}, description = "Input format: ${COMPLETION-CANDIDATES} [default: ${DEFAULT-VALUE}].", defaultValue = "RFC3164", paramLabel = "<proto>")
    private InputProtocol protocol = InputProtocol.RFC3164;

    @CommandLine.Option(names = { "--to-syslog"}, description = "Forward to Syslog <udp://host:port> (RFC-5424).", paramLabel = "<uri>")
    private URI syslog;

    @CommandLine.Option(names = { "--to-gelf"}, description = "Forward to Graylog <udp://host:port>.", paramLabel = "<uri>")
    private URI gelf;

    @CommandLine.Option(names = { "--to-loki"}, description = "Forward to Grafana Loki <http://host:port>.", paramLabel = "<url>")
    private URL loki;


    @CommandLine.Option(names = { "-d", "--debug" }, description = "Enable debugging [default: ${DEFAULT-VALUE}].")
    private boolean enableDebug = false;


    @Override
    public Integer call() throws IOException, InterruptedException {

        if(enableDebug) {
            System.setProperty("org.slf4j.simpleLogger.defaultLogLevel", "DEBUG");
        }

        if(protocol == InputProtocol.GELF)
            syslogParser = new GelfParser();
        else if (protocol == InputProtocol.RFC5424) {
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

        if(stdin) {
            InputReader inputReader = new InputReader(System.in, protocol);
            inputReader.addEventListener(this);
            inputReader.start();
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

        if(monitor) {
            PippoSettings settings = new PippoSettings(RuntimeMode.PROD);
            settings.overrideSetting("server.port", monitorPort);
            settings.overrideSetting("server.contextPath", monitorPath);
            WebServer pippoApp = new WebServer(settings, queue);
            logForwardListeners.add(pippoApp.getLogSocketHandler());

            Pippo pippo = new Pippo(pippoApp);
            pippo.addPublicResourceRoute();
            pippo.start();
        }

        Thread.currentThread().join(); // sleep forever
        return 0;
    }


    @Override
    public void onLogEvent(LogReceiveEvent event) {

        // Parse message
        SyslogMessage msg = null;
        try {
            msg = syslogParser.parse(event.getBytes());
        } catch(Exception e) {
            e.printStackTrace();
        }

        if(msg != null) {

            if(!logForwardListeners.isEmpty()) {
                sendForwardEvent(msg);
                queue.add(msg);
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
        return new InetSocketAddress(input.getHost(), input.getPort());
    }


    public static void main(String... args) {
        Thread shutdownHook = new Thread(() -> keepRunning = false);
        Runtime.getRuntime().addShutdownHook(shutdownHook);

        int exitCode = new CommandLine(new Main()).execute(args);
        System.exit(exitCode);
    }

}
