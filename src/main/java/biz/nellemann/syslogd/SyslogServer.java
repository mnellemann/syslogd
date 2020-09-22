package biz.nellemann.syslogd;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import picocli.CommandLine;
import picocli.CommandLine.Command;
import java.util.concurrent.Callable;

@Command(name = "syslogd", mixinStandardHelpOptions = true, version = "syslogd 1.0",
        description = "Simple syslog server that prints messages to stdout.")
public class SyslogServer implements Callable<Integer>, LogListener {

    private final static Logger log = LoggerFactory.getLogger(SyslogServer.class);

    @CommandLine.Option(names = {"-p", "--port"}, description = "Listening port, 514 by default.")
    private int port = 8080;

    @CommandLine.Option(names = "--no-udp", negatable = true, description = "Listen on UDP, true by default.")
    boolean udpServer = true;

    @CommandLine.Option(names = "--no-tcp", negatable = true, description = "Listen on TCP, true by default.")
    boolean tcpServer = true;

    @CommandLine.Option(names = "--rfc3164", negatable = true, description = "Parse rfc3164 syslog message, false by default.")
    boolean rfc3164 = false;

    @CommandLine.Option(names = "--no-rfc5424", negatable = true, description = "Parse rfc5424 syslog message, true by default.")
    boolean rfc5424 = true;


    public static void main(String... args) {
        int exitCode = new CommandLine(new SyslogServer()).execute(args);
        System.exit(exitCode);
    }


    @Override
    public Integer call() throws Exception {

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

        String message = event.getMessage();
        SyslogMessage msg = null;
        try {
            if(rfc5424) {
                msg = SyslogParser.parseRfc5424(message);
            } else if(rfc3164) {
                msg = SyslogParser.parseRfc3164(message);
            }
        } catch(Exception e) {
            log.error("Problem parsing message: ", e);
        }

        if(msg != null) {
            System.out.println(msg);
        }

    }

}
