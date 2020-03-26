package wang.switchy.hin2n.storage.db.base;

import android.database.Cursor;
import android.database.sqlite.SQLiteStatement;

import org.greenrobot.greendao.AbstractDao;
import org.greenrobot.greendao.AbstractDaoSession;
import org.greenrobot.greendao.Property;
import org.greenrobot.greendao.database.Database;
import org.greenrobot.greendao.database.DatabaseStatement;
import org.greenrobot.greendao.internal.DaoConfig;

import wang.switchy.hin2n.storage.db.base.model.ZerotierSettingModel;

public class ZerotierSettingModelDao extends AbstractDao <ZerotierSettingModel, Long>{

    public static final String TABLENAME = "ZerotierSettingList";

    /**
     * Properties of entity ZrotierSettingModel.<br/>
     * Can be used for QueryBuilder and for referencing column names.
     */
    public static class Properties {
        public final static Property Id = new Property(0, Long.class, "id", true, "_id");
        public final static Property Nwid = new Property(1, String.class, "nwid", false, "NWID");
        public final static Property Name = new Property(2, String.class, "name", false, "NAME");
        public final static Property Ip = new Property(3, String.class, "ip", false, "IP");
        public final static Property Username = new Property(4, String.class, "username", false, "USERNAME");
        public final static Property Password = new Property(5, String.class, "password", false, "PASSWORD");
        public final static Property Macaddress = new Property(6, String.class, "macaddress", false, "MACADDRES");
        public final static Property Netmask = new Property(7, String.class, "netmask", false, "NETMASK");
        public final static Property IsSelcected = new Property(20, boolean.class, "isSelcected", false, "IS_SELCECTED");
        public final static Property IsAuto = new Property(20, boolean.class, "isAuto", false, "IS_AUTO");
        public final static Property IsWifi= new Property(20, boolean.class, "isWifi", false, "IS_WIFI");

    }
    /** Creates the underlying database table. */
    public static void createTable(Database db, boolean ifNotExists) {
        String constraint = ifNotExists ? "IF NOT EXISTS " : "";
        db.execSQL("CREATE TABLE " + constraint + "\"ZrotierSettingList\" (" + //
                "\"_id\" INTEGER PRIMARY KEY AUTOINCREMENT ," + // 0: id
                "\"NWID\" TEXT NOT NULL ," + // 1: nwid
                "\"NAME\" TEXT," + // 2: name
                "\"IP\" TEXT," + // 3: ip
                "\"USERNAME\" TEXT," + // 4: username
                "\"PASSWORD\" TEXT," + // 5: password
                "\"MACADDRESS\" TEXT," + // 6: macaddress
                "\"NETMASK\" TEXT,"+ // 7: netmask
                "\"IS_SELCECTED\" INTEGER NOT NULL ," + // 8: isSelcected
                "\"IS_AUTO\" INTEGER NOT NULL ," + // 9: isAuto
                "\"IS_Wifi\" INTEGER NOT NULL"  // 10: isWifi
        );
    }

    /** Drops the underlying database table. */
    public static void dropTable(Database db, boolean ifExists) {
        String sql = "DROP TABLE " + (ifExists ? "IF EXISTS " : "") + "\"ZerotierSettingList\"";
        db.execSQL(sql);
    }


    public ZerotierSettingModelDao(DaoConfig config) {
        super(config);
    }

    public ZerotierSettingModelDao(DaoConfig config, AbstractDaoSession daoSession) {
        super(config, daoSession);
    }

    @Override
    protected ZerotierSettingModel readEntity(Cursor cursor, int offset) {
        ZerotierSettingModel entity = new ZerotierSettingModel( //
                cursor.isNull(offset + 0) ? null : cursor.getLong(offset + 0), // id
                cursor.isNull(offset + 1) ? null : cursor.getString(offset + 1), // nwid
                cursor.isNull(offset + 2) ? null : cursor.getString(offset + 2), // name
                cursor.isNull(offset + 3) ? null : cursor.getString(offset + 3), // ip
                cursor.isNull(offset + 4) ? null : cursor.getString(offset + 4), // username
                cursor.isNull(offset + 5) ? null : cursor.getString(offset + 5), // password
                cursor.isNull(offset + 6) ? null : cursor.getString(offset + 6), // macaddress
                cursor.isNull(offset + 7) ? null : cursor.getString(offset + 7), // netmask
                cursor.getShort(offset + 8) != 0, // isSelcected
                cursor.getShort(offset + 9) != 0, // isAuto
                cursor.getShort(offset + 10) != 0 // isWIfi
        );
        return entity;
    }

    @Override
    protected Long readKey(Cursor cursor, int offset) {
        return cursor.isNull(offset + 0) ? null : cursor.getLong(offset + 0);
    }

    @Override
    protected void readEntity(Cursor cursor, ZerotierSettingModel entity, int offset) {
        entity.setId(cursor.isNull(offset + 0) ? null : cursor.getLong(offset + 0));
        entity.setNwid(cursor.getString(offset + 1));
        entity.setName(cursor.isNull(offset + 2) ? null : cursor.getString(offset + 2));
        entity.setIp(cursor.isNull(offset + 3) ? null : cursor.getString(offset + 3));
        entity.setUsername(cursor.isNull(offset + 4) ? null : cursor.getString(offset + 4));
        entity.setPassword(cursor.isNull(offset + 5) ? null : cursor.getString(offset + 5));
        entity.setMacaddress(cursor.isNull(offset + 6) ? null : cursor.getString(offset + 6));
        entity.setNetmask(cursor.isNull(offset + 7) ? null : cursor.getString(offset + 7));
    }

    @Override
    protected void bindValues(DatabaseStatement stmt, ZerotierSettingModel entity) {
        stmt.clearBindings();

        Long id = entity.getId();
        if (id != null) {
            stmt.bindLong(1, id);
        }
        String nwid = entity.getNwid();
        if (nwid != null) {
            stmt.bindString(2, nwid);
        }

        String name = entity.getName();
        if (name != null) {
            stmt.bindString(3, name);
        }

        String ip = entity.getIp();
        if (ip != null) {
            stmt.bindString(4, ip);
        }

        String username = entity.getName();
        if (username != null) {
            stmt.bindString(5, username);
        }

        String password = entity.getPassword();
        if (password != null) {
            stmt.bindString(6, password);
        }

        String macaddress = entity.getMacaddress();
        if (macaddress != null) {
            stmt.bindString(7, macaddress);
        }

        String netmask = entity.getNetmask();
        if (netmask != null) {
            stmt.bindString(8, netmask);
        }
    }

    @Override
    protected void bindValues(SQLiteStatement stmt, ZerotierSettingModel entity) {
        stmt.clearBindings();

        Long id = entity.getId();
        if (id != null) {
            stmt.bindLong(1, id);
        }
        String nwid = entity.getNwid();
        if (nwid != null) {
            stmt.bindString(2, nwid);
        }

        String name = entity.getName();
        if (name != null) {
            stmt.bindString(3, name);
        }

        String ip = entity.getIp();
        if (ip != null) {
            stmt.bindString(4, ip);
        }

        String username = entity.getName();
        if (username != null) {
            stmt.bindString(5, username);
        }

        String password = entity.getPassword();
        if (password != null) {
            stmt.bindString(6, password);
        }

        String macaddress = entity.getMacaddress();
        if (macaddress != null) {
            stmt.bindString(7, macaddress);
        }

        String netmask = entity.getNetmask();
        if (netmask != null) {
            stmt.bindString(8, netmask);
        }
    }

    @Override
    protected Long updateKeyAfterInsert(ZerotierSettingModel entity, long rowId) {
        entity.setId(rowId);
        return rowId;
    }

    @Override
    protected Long getKey(ZerotierSettingModel entity) {
        if(entity != null) {
            return entity.getId();
        } else {
            return null;
        }
    }

    @Override
    protected boolean hasKey(ZerotierSettingModel entity) {
        return entity.getId() != null;
    }


    @Override
    protected boolean isEntityUpdateable() {
        return true;
    }
}
