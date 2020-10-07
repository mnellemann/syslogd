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
        description = "Basic syslog server.",
        versionProvider = biz.nellemann.syslogd.VersionProvider.class)
public class SyslogServer implements Callable<Integer>, LogListener {

    private final static Logger log = LoggerFactory.getLogger(SyslogServer.class);

    @CommandLine.Option(names = {"-p", "--port"}, description = "Listening port [default: 514].")
    private int port = 514;

    @CommandLine.Option(names = "--no-udp", negatable = true, description = "Listen on UDP [default: true].")
    boolean udpServer = true;

    @CommandLine.Option(names = "--no-tcp", negatable = true, description = "Listen on TCP [default: true].")
    boolean tcpServer = true;

    @CommandLine.Option(names = "--no-ansi", negatable = true, description = "Output ANSI colors [default: true].")
    boolean ansiOutput = true;

    @CommandLine.Option(names = "--rfc5424", description = "Parse RFC-5424 messages [default: RFC-3164].")
    boolean rfc5424 = false;


    public static void main(String... args) {
        int exitCode = new CommandLine(new SyslogServer()).execute(args);
        System.exit(exitCode);
    }


    @Override
    public Integer call() throws IOException {

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
            log.error("Problem parsing message: ", e);
        }

        if(msg != null) {
            if(ansiOutput) {
                System.out.println(msg.toAnsiString());
            } else {
                System.out.println(msg);
            }
        }

    }

}
