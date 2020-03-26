package dajana.data.database.realm;

import io.realm.DynamicRealm;
import io.realm.Realm;
import io.realm.RealmMigration;
import io.realm.RealmObjectSchema;
import io.realm.RealmSchema;

public class RealmMigrations implements RealmMigration {
    @Override
    public void migrate(DynamicRealm realm, long oldVersion, long newVersion) {
        final RealmSchema schema = realm.getSchema();
        if(newVersion == 0) {
            Realm.deleteRealm(realm.getConfiguration());
        }else if(oldVersion == 1) {
            //final RealmObjectSchema myCloudSchema = schema.get("MyCloud");
            //myCloudSchema.addField("id",int.class);
            //myCloudSchema.addField("username",String.class);
            //myCloudSchema.addField("server",String.class);
        }
    }
}
