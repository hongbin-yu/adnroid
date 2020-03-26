package dajana.data.database.realm;

import java.util.ArrayList;
import java.util.concurrent.atomic.AtomicInteger;

import io.realm.Realm;
import io.realm.RealmList;
import io.realm.RealmObject;
import io.realm.annotations.LinkingObjects;
import io.realm.annotations.PrimaryKey;

public class MyCloud extends RealmObject {
    public static class Fields {
        public static String UID = "id";
        public static String USERNAME = "username";
        public static String SERVER = "server";
        public static String FILELIST = "fileList";
    }
    @PrimaryKey
    private int id;
    private String username;
    private String server;
    @SuppressWarnings("unused")
    private RealmList<FileRealm> fileList = new RealmList<FileRealm>();

    private static AtomicInteger INTEGER_COUNTER = new AtomicInteger(0);
    public MyCloud() {
    }

    public MyCloud(String username, String server) {
        this.username = username;
        this.server = server;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getServer() {
        return server;
    }

    public void setServer(String server) {
        this.server = server;
    }

    public void setFileList(RealmList<FileRealm> fileList) {
        this.fileList = fileList;
    }


    public RealmList<FileRealm> getFileList() {
        return fileList;
    }

    public static MyCloud createMyCloud(Realm realm) {
        Number num = realm.where(MyCloud.class).max(Fields.UID);
        int maxid = 0;
        if(num != null) {
           maxid = num.intValue() ;
        }
        MyCloud counter = realm.createObject(MyCloud.class, maxid+1);
        return counter;
    }
    private static int increment() {
        return INTEGER_COUNTER.getAndIncrement();
    }


}
