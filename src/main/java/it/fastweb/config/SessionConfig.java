package it.fastweb.config;


import com.jcraft.jsch.Channel;
import com.jcraft.jsch.ChannelSftp;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.Session;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.sql.Connection;
import java.util.Properties;

public class SessionConfig {

    private DataConfig dataConfig;
    private Session session;
    private URL resource;
    private ChannelSftp channelSftp;

    private static final Logger log = LoggerFactory.getLogger(SessionConfig.class);
    
    public SessionConfig(DataConfig dataconfig) {
    	this.dataConfig = dataconfig;
    }

    public  Session openSession(Connection conn) {

    	
        Session session = null;
        Properties prop = dataConfig.getConfigurationForSession(conn);

        try {
            String user = prop.getProperty("userSession");
            int port = Integer.parseInt(prop.getProperty("portSession"));
            String host = prop.getProperty("hostSession");
            log.debug("Session sftp - User = " + user);
            log.debug("Session sftp - Port = " + port);
            log.debug("Session sftp - Host = " + host);
            
            JSch jsch = new JSch();

            String path_identity = writeResourceToFile("rco-sftp.ppk");
            jsch.addIdentity(path_identity);
            session = jsch.getSession(user, host, port);
            java.util.Properties config = new java.util.Properties();
            config.put("StrictHostKeyChecking", "no");
            session.setConfig(config);
            session.connect();
            log.info("Sessione aperta: " + session.isConnected());

        } catch (Exception e) {
            e.printStackTrace();
        }
        return session;
    }

    private String writeResourceToFile(String resourceName) throws URISyntaxException, IOException {

        ClassLoader CLDR = getClass().getClassLoader();
        log.debug("Class loader = " + CLDR.toString());
        log.debug("Get class Name = " + getClass().getName());
        log.debug("Resource name = " + resourceName );
        resource = CLDR.getResource(resourceName);
        log.debug("Resource URI  = " + resource.toURI());
        log.debug("Reading path without using URI = " + resource.getPath());
        log.debug("Reading path using URI = " + resource.toURI().getPath() );

        InputStream configStream = CLDR.getResourceAsStream(resourceName);
        Path pth =  Paths.get(resourceName);
        Files.deleteIfExists(pth);
        Files.copy(configStream, Files.createFile(pth), StandardCopyOption.REPLACE_EXISTING);
        return pth.toAbsolutePath().toString();
    }

    public ChannelSftp openChannel(Session session) {

        try {
            Channel channel = session.openChannel("sftp");
            channel.setInputStream(System.in);
            channel.setOutputStream(System.out);
            channel.connect();

            channelSftp = (ChannelSftp) channel;
            log.info("Channel aperto: " + channelSftp.isConnected());
        } catch (Exception e) {
            e.printStackTrace();
        }
        return channelSftp;
    }
}
