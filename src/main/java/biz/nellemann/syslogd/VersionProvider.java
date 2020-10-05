package biz.nellemann.syslogd;

import picocli.CommandLine;

import java.io.IOException;
import java.net.URL;
import java.util.Properties;

class VersionProvider implements CommandLine.IVersionProvider {

    public String[] getVersion() throws IOException {

        URL url = getClass().getResource("/version.properties");
        if (url == null) {
            return new String[] { "No version.txt file found in the classpath." };
        }
        Properties properties = new Properties();
        properties.load(url.openStream());
        return new String[] { "${COMMAND-FULL-NAME} " + properties.getProperty("VERSION_GRADLE") + "-" + properties.getProperty("VERSION_BUILD") };
    }

}
