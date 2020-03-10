package it.fastweb.model;

import java.util.Date;

public class SimboxTimestampIdx {

    private Integer id;
    private Date date;
    private String folder;
    private String filename;
    private String dl;

    public SimboxTimestampIdx() {
    }

    public SimboxTimestampIdx(Date date, String folder, String filename, String dl) {
        this.date = date;
        this.folder = folder;
        this.filename = filename;
        this.dl = dl;
    }

    public Integer getId() {
        return id;
    }

    public Date getDate() {
        return date;
    }

    public String getFolder() {
        return folder;
    }

    public String getFilename() {
        return filename;
    }

    public String getDl() {
        return dl;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    public void setFolder(String folder) {
        this.folder = folder;
    }

    public void setFilename(String filename) {
        this.filename = filename;
    }

    public void setDl(String dl) {
        this.dl = dl;
    }

    @Override
    public String toString() {
        return "SimboxTimestampIdx{" +
                "id=" + id +
                ", date=" + date +
                ", folder='" + folder + '\'' +
                ", filename='" + filename + '\'' +
                ", dl='" + dl + '\'' +
                '}';
    }
}
