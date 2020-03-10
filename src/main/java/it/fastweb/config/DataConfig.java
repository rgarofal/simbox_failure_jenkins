package it.fastweb.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.sql.*;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

public class DataConfig {

    private String database;
    private String username;
    private String password;
    private static final String QUERY_FIND_CONFIG_FOR_SESSION = "select * from rco_m0.api_configuration";
    private static boolean config_test = false;

    private static final Logger log = LoggerFactory.getLogger(DataConfig.class);

    public DataConfig() {
    	
    }
    
    
    public Connection getConnectionFromDb() {

        Properties properties = new Properties();
        InputStream inputStream = null;
        Connection conn = null;

        try {
        	if(config_test) {
        		log.info("TEST configurazione database");
        		inputStream = ClassLoader.class.getResourceAsStream("/configtest.properties");
        	} else {
        		log.info("Configurazione database di produzione");
        		inputStream = ClassLoader.class.getResourceAsStream("/config.properties");
        	}
            
            properties.load(inputStream);

            database = properties.getProperty("database");
            username = properties.getProperty("username");
            password = properties.getProperty("password");
            
            log.info("Database = " + database);
            log.info("Username = " + username);

            conn = DriverManager.getConnection(database, username, password);
            log.info("Connessione al Db effettuata");

        } catch (SQLException | IOException e) {
            e.printStackTrace();
        } finally {
            try {
                if (inputStream != null) {
                    inputStream.close();
                } else log.error("File di configurazione non trovato, impossibile connettersi al Db");
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return conn;
    }

    public Properties getConfigurationForSession(Connection conn) {

        Properties properties = new Properties();
        ResultSet resultSet = null;
        Statement stmt = null;

        try {
            stmt = conn.createStatement();
            resultSet = stmt.executeQuery(QUERY_FIND_CONFIG_FOR_SESSION);

            Map<String, String> values = new HashMap<String, String>();

            while (resultSet.next()) {

                String key = resultSet.getString("chiave");
                String value = resultSet.getString("valore");

                values.put(key, value);
            }
            stmt.close();

            properties.setProperty("userSession", values.get("simbox_user"));
            properties.setProperty("portSession", values.get("simbox_port"));
            properties.setProperty("hostSession", values.get("simbox_host"));

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return properties;
    }
}
