package dajana.data.database.realm;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.sql.Timestamp;
import java.util.Date;
import java.util.Random;
import java.util.concurrent.atomic.AtomicInteger;

import dajana.data.database.sqlite.FileTable;
import dajana.utils.FileManager;
import io.realm.Realm;
import io.realm.RealmList;
import io.realm.RealmObject;
import io.realm.RealmResults;
import io.realm.annotations.Index;
import io.realm.annotations.LinkingObjects;
import io.realm.annotations.PrimaryKey;

public class FileRealm extends RealmObject {

    public static class Fields {
        public static final String UID = "uid";
        public static final String ID = "id";
        public static final String URL = "url";
        public static final String ORIGINALURL = "originalUrl";
        public static final String URI = "uri";
        public static final String FILENAME = "filename";
        public static final String DESCRIPTION = "description";
        public static final String TIMESTAMP = "timestamp";
        public static final String UPLOADER = "uploader";
        public static final String EXPIRES = "expires";
        public static final String EXPIRYDATE = "expiry_date";
        public static final String PUBLICALLOW = "public_allow";
        public static final String PUBLICTOKEN = "public_token";
        public static final String CONTENTTYPE = "contentType";
        public static final String LASTMODFIED = "lastModified";
        public static final String LASTSYNC = "lastSync";
        public static final String ACTION = "action";
        public static final String RESULT = "result";
        public static final String TAGS = "tags";
        public static final String MYCLOUD = "cloud";
    }
    @PrimaryKey
    private Integer uid;
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
    @Index
    private String filename;
    @Expose
    @SerializedName("description")
    @Index
    private String description;
    @Expose
    @SerializedName("timestamp")
    private String timestamp;
    @Expose
    @SerializedName("uploader")
    private String uploader;
    @Expose
    @SerializedName("expires")
    private int expires;
    @Expose
    @SerializedName("expiry_date")
    private String expiry_date;
    @Expose
    @SerializedName("public_allow")
    private int public_allow;
    @Expose
    @SerializedName("public_token")
    private String public_token;
    private String filePath;
    @SerializedName("size")
    private Long size;
    @SerializedName("contentType")
    private String contentType;
    @SerializedName("uri")
    private String uri;
    private String lastModified;
    private Date lastSync;
    private String action;
    private String result;
    private String tags;
    @LinkingObjects("fileList")
    private final RealmResults<MyCloud> cloud = null;
    @SuppressWarnings("unused")
    private RealmList<UserRealm>  userList = new RealmList<>();


    public FileRealm() {
    }

    public FileRealm(Integer id, String url, String originalUrl, String filename, String description, String timestamp, String uploader, int expires, String expiry_date, int public_allow, String public_token) {
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
    public static AtomicInteger INTEGER_COUNTER = new AtomicInteger(0);

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

    public String getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(String timestamp) {
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

    public String getExpiry_date() {
        return expiry_date;
    }

    public void setExpiry_date(String expiry_date) {
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

    public Integer getUid() {
        return uid;
    }

    public void setUid(Integer uid) {
        this.uid = uid;
    }

    public String getContentType() {
        if(contentType == null) {
            if (this.url != null) {
                return FileManager.getMimeType(url);
            } else if (this.getOriginalUrl() != null) ;
                return FileManager.getMimeType(originalUrl);
        }
        return contentType;
    }

    public void setContentType(String contentType) {
        this.contentType = contentType;
    }

    public String getLastModified() {
        return lastModified;
    }

    public void setLastModified(String lastModified) {
        this.lastModified = lastModified;
    }

    public Date getLastSync() {
        return lastSync;
    }

    public void setLastSync(Date lastSync) {
        this.lastSync = lastSync;
    }

    public String getAction() {
        return action;
    }

    public void setAction(String action) {
        this.action = action;
    }

    public String getResult() {
        return result;
    }

    public void setResult(String result) {
        this.result = result;
    }

    public Long getSize() {
        return size;
    }

    public void setSize(Long size) {
        this.size = size;
    }

    public String getFilePath() {
        return filePath;
    }

    public void setFilePath(String filePath) {
        this.filePath = filePath;
    }

    public String getUri() {
        return uri;
    }

    public void setUri(String uri) {
        this.uri = uri;
    }

    public String getTags() {
        return tags;
    }

    public void setTags(String tags) {
        this.tags = tags;
    }

    public RealmList<UserRealm> getUserList() {
        return userList;
    }

    public void setUserList(RealmList<UserRealm> userList) {
        this.userList = userList;
    }

    public RealmResults<MyCloud> getCloud() {
        return cloud;
    }

    public String getCountString() {
        return Integer.toString(id);
    }

    //  create() & delete() needs to be called inside a transaction.
    public static void create(Realm realm) {
        create(realm, false);
    }

    public static void create(Realm realm, boolean randomlyInsert) {

        MyCloud parent = realm.where(MyCloud.class).findFirst();
        RealmList<FileRealm> items = parent.getFileList();
        FileRealm counter = realm.createObject(FileRealm.class, increment());
        if (randomlyInsert && items.size() > 0) {
            Random rand = new Random();
            items.listIterator(rand.nextInt(items.size())).add(counter);
        } else {
            items.add(counter);
        }
    }

    public static void delete(Realm realm, long id) {
        FileRealm item = realm.where(FileRealm.class).equalTo(Fields.UID, id).findFirst();
        // Otherwise it has been deleted already.
        if (item != null) {
            item.deleteFromRealm();
        }
    }
    public static FileRealm createFileRealm(Realm realm) {
        Number num = realm.where(FileRealm.class).max(MyCloud.Fields.UID);
        int maxid = increment();
        if(num != null && maxid < num.intValue()+1) {
            maxid = num.intValue() + 1 ;
            INTEGER_COUNTER.set(maxid);
        }
        FileRealm counter = realm.createObject(FileRealm.class, maxid);
        return counter;
    }


    public static int increment() {
        return INTEGER_COUNTER.getAndIncrement();
    }

    @Override
    public int hashCode() {
        return uid==null?-1:uid;
        //return super.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if(this == obj)
            return true;
        if(obj == null || obj.getClass()!= this.getClass())
            return false;
        FileRealm file = (FileRealm)obj;
        if(this.url == null) return false;
        return (this.url.equals(file.getUrl()));
    }
}
