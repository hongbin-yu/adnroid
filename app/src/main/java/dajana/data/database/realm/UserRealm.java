package dajana.data.database.realm;

import dajana.model.User;
import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;

public class UserRealm extends RealmObject {
    public static class Fields {
        public static final String ID = "id";
        public static final String USERNAME = "username";
        public static final String NAME = "name";


    }

    @PrimaryKey
    private int id;
    private String username;
    private String name;

    public UserRealm() {
    }

    public UserRealm(int id, String username, String name) {
        this.id = id;
        this.username = username;
        this.name = name;
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

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Override
    public int hashCode() {
        return id;
        //return super.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if(this == obj)
            return true;
        if(obj == null || obj.getClass()!= this.getClass())
            return false;
        UserRealm user = (UserRealm)obj;
        return (this.username.equals(user.getUsername()));
    }
}
