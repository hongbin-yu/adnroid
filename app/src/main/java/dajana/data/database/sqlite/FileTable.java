package dajana.data.database.sqlite;

import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteStatement;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.sql.Timestamp;

import dajana.data.database.DatabaseManager;


public class FileTable extends AbstractTable{
    @Expose
    @SerializedName("id")
    private Integer id;
    @Expose
    @SerializedName("url")
    private String url;
    @Expose
    @SerializedName("original_url")
    private String originalUrl;
    @Expose
    @SerializedName("filename")
    private String filename;
    @Expose
    @SerializedName("description")
    private String description;
    @Expose
    @SerializedName("timestamp")
    private Timestamp timestamp;
    @Expose
    @SerializedName("uploader")
    private String uploader;
    @Expose
    @SerializedName("expires")
    private int expires;
    @Expose
    @SerializedName("expiry_date")
    private int expiry_date;
    @Expose
    @SerializedName("public_allow")
    private int public_allow;
    @Expose
    @SerializedName("public_token")
    private String public_token;
    private static FileTable instance;

    private DatabaseManager databaseManager;
    private SQLiteStatement writeStatement;
    private Object writeLock;
    public FileTable() {
    }

    public static FileTable getInstance() {
        if (instance == null) {
            instance = new FileTable(DatabaseManager.getInstance());
        }

        return instance;
    }
    private FileTable(DatabaseManager databaseManager) {
        this.databaseManager = databaseManager;
        writeStatement = null;
        writeLock = new Object();
    }

    public FileTable(Integer id, String url, String originalUrl, String filename, String description, Timestamp timestamp, String uploader, int expires, int expiry_date, int public_allow, String public_token) {
        this.id = id;
        this.url = url;
        this.originalUrl = originalUrl;
        this.filename = filename;
        this.description = description;
        this.timestamp = timestamp;
        this.uploader = uploader;
        this.expires = expires;
        this.expiry_date = expiry_date;
        this.public_allow = public_allow;
        this.public_token = public_token;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getOriginalUrl() {
        return originalUrl;
    }

    public void setOriginalUrl(String originalUrl) {
        this.originalUrl = originalUrl;
    }

    public String getFilename() {
        return filename;
    }

    public void setFilename(String filename) {
        this.filename = filename;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Timestamp getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Timestamp timestamp) {
        this.timestamp = timestamp;
    }

    public String getUploader() {
        return uploader;
    }

    public void setUploader(String uploader) {
        this.uploader = uploader;
    }

    public int getExpires() {
        return expires;
    }

    public void setExpires(int expires) {
        this.expires = expires;
    }

    public int getExpiry_date() {
        return expiry_date;
    }

    public void setExpiry_date(int expiry_date) {
        this.expiry_date = expiry_date;
    }

    public int getPublic_allow() {
        return public_allow;
    }

    public void setPublic_allow(int public_allow) {
        this.public_allow = public_allow;
    }

    public String getPublic_token() {
        return public_token;
    }

    public void setPublic_token(String public_token) {
        this.public_token = public_token;
    }

    @Override
    protected String getTableName() {
        return null;
    }

    @Override
    protected String[] getProjection() {
        return new String[0];
    }

    @Override
    public void create(SQLiteDatabase db) {

    }
}
