package it.fastweb.service;

import com.jcraft.jsch.ChannelSftp;
import it.fastweb.model.SimboxTimestampIdx;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.*;
import java.util.Date;
import java.util.List;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;


public class SimboxFailureService {

    private static final String QUERY_MAX_DATE = "SELECT max(date) date FROM sales.simbox_timestamp_idx s WHERE s.folder = 'failure_csv';";
    private static final String INSERT_FILE = "INSERT INTO sales.simbox_timestamps_idx (date, folder, filename, dl) VALUES (?,?,?,?)";

    public SimboxFailureService() {
    	
    }
    public Date queryDate(Connection connection) {

        Statement stmt = null;
        Date date = null;

        try {
            stmt = connection.createStatement();
            ResultSet rs = stmt.executeQuery(QUERY_MAX_DATE);
            while (rs.next()) {
              date = rs.getTimestamp("date");
            }
            stmt.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return date;
    }

    public boolean readFile(ChannelSftp.LsEntry ticketSingolo, ChannelSftp channelSftp) {

        File tmp = null;
        String splitName = ticketSingolo.getFilename();
        String csvFile = "";
        boolean esitoResponse = false;

        try {
            tmp = File.createTempFile(splitName, ".tmp");

            InputStream in = null;
            OutputStream out = null;

            in = channelSftp.get("/home/rco/inventia_w/failure_csv/" + ticketSingolo.getFilename());
// codice precedente            
//            out = new FileOutputStream(tmp);
//
//            byte[] buf = new byte[1024];
//            int len;
//
//            while ((len = in.read(buf)) > 0) {
//                out.write(buf, 0, len);
//            }
//            in.close();
//            out.close();
//
//            Path p = Paths.get(tmp.getAbsolutePath());
//            File file = new File(String.valueOf(p));
//
//            csvFile = FileUtils.readFileToString(file, StandardCharsets.UTF_8);

            csvFile = IOUtils.toString(in, StandardCharsets.UTF_8.name());

            in.close();

            SimboxHttp simboxHttp = new SimboxHttp();
            esitoResponse = simboxHttp.sendTicket(csvFile);

        } catch (Exception e) {
            e.printStackTrace();
        }
        return esitoResponse;
    }

    public void insertDb(List<SimboxTimestampIdx> simboxTimestampIdxList, Connection connection) {

        simboxTimestampIdxList.forEach(simboxTimestampIdx -> {

            try {
                PreparedStatement ps = connection.prepareStatement(INSERT_FILE);

                ps.setTimestamp(1, new java.sql.Timestamp(simboxTimestampIdx.getDate().getTime()));
                ps.setString(2, simboxTimestampIdx.getFolder());
                ps.setString(3, simboxTimestampIdx.getFilename());
                ps.setString(4, simboxTimestampIdx.getDl());

                ps.execute();

            } catch (SQLException e) {
                e.printStackTrace();
            }
        });
    }
}
