package it.fastweb;

import com.jcraft.jsch.ChannelSftp;
import com.jcraft.jsch.Session;
import it.fastweb.config.DataConfig;
import it.fastweb.config.SessionConfig;
import it.fastweb.model.SimboxTimestampIdx;
import it.fastweb.service.SimboxFailureService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.text.SimpleDateFormat;
import java.util.*;

public class SimboxFailureApplication {

    private static ChannelSftp channelSftp;
    private static Session session;
    private static Connection connection;
    private static DataConfig dataConfig;
    private static SessionConfig sessionConfig;
    private static Vector<ChannelSftp.LsEntry> fileList;
    private static Date currentFileDate;
    private static SimboxFailureService simboxFailureService;

    private static final Logger log = LoggerFactory.getLogger(SimboxFailureApplication.class);

    public static void main(String[] args) {

        try {
        	dataConfig = new DataConfig();
        	sessionConfig = new SessionConfig(dataConfig);
            connection = dataConfig.getConnectionFromDb();
            session = sessionConfig.openSession(connection);
            channelSftp = sessionConfig.openChannel(session);

            channelSftp.cd("/home/rco/inventia_w/failure_csv");
            fileList = channelSftp.ls("*.csv");

            List<SimboxTimestampIdx> newFileList = new ArrayList<>();
            final boolean[] esitoResponse = {false};

            Date maxDate = simboxFailureService.queryDate(connection);
            log.info("Ultimi dati inseriti a Db in data: " + maxDate);


            if (fileList.capacity() == 0) {
                log.info("Cartella vuota, file non trovati");
            } else {
                log.info("Totale files presenti nella cartella : " + fileList.size());

                fileList.forEach(ticketSingolo -> {

                    String dateFile = ticketSingolo.getAttrs().getMtimeString();

                    try {
                        currentFileDate = new SimpleDateFormat("EEE MMM dd HH:mm:ss zzz yyyy", Locale.US).parse(dateFile);
                    } catch (java.text.ParseException e) {
                        e.printStackTrace();
                    }

                    SimboxTimestampIdx simboxTimestampIdx = new SimboxTimestampIdx();

                    if (maxDate == null || maxDate.compareTo(currentFileDate) < 0) {
                        simboxTimestampIdx.setDate(currentFileDate);
                        simboxTimestampIdx.setFolder("failure_csv");
                        simboxTimestampIdx.setFilename(ticketSingolo.getFilename());
                        simboxTimestampIdx.setDl("CSB.SimShelf@fastweb.it");

                        newFileList.add(simboxTimestampIdx);
                        esitoResponse[0] = simboxFailureService.readFile(ticketSingolo, channelSftp); //legge il file e lo invia al TMT
                    }
                });

                if (newFileList.size() != 0 && esitoResponse[0]) {
                    log.info("Totale file da caricare " + newFileList.size());
                    simboxFailureService.insertDb(newFileList, connection);
                } else log.info("Non ci sono file da inserire a Db");

                session.disconnect();
                channelSftp.exit();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
