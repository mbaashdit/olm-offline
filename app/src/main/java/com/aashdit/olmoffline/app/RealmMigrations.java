package com.aashdit.olmoffline.app;

import android.util.Log;

import io.realm.DynamicRealm;
import io.realm.RealmMigration;
import io.realm.RealmObjectSchema;
import io.realm.RealmSchema;

/**
 * Created by Manabendu on 02/07/20
 */
public class RealmMigrations implements RealmMigration {

    private static final String TAG = "RealmMigrations";

    @Override
    public void migrate(DynamicRealm realm, long oldVersion, long newVersion) {
        final RealmSchema schema = realm.getSchema();

        Log.i(TAG, "migrate: oldVersion " + oldVersion);
        Log.i(TAG, "migrate: newVersion " + newVersion);

        if (oldVersion == 1) {
            final RealmObjectSchema userSchema = schema.get("UserData");
            if (userSchema != null) {
                userSchema.addField("age", int.class);
            }
            oldVersion++;
        }
        if (oldVersion == 2) {
            final RealmObjectSchema userSchema = schema.get("UserData");
            if (userSchema != null) {
                userSchema.addField("imagePath", String.class);
            }
            oldVersion++;
        }
        if (oldVersion == 3) {
            schema.create("Session")
                    .addField("userId", String.class);
            oldVersion++;
        }
        if (oldVersion == 4) {
            final RealmObjectSchema userSchema = schema.get("Session");
            if (userSchema != null) {
                userSchema.addField("userName", String.class);
            }
            oldVersion++;
        }
        if (oldVersion == 5) {
            final RealmObjectSchema userSchema = schema.get("Session");
            if (userSchema != null) {
                userSchema.addField("userStatus", String.class);
            }
            oldVersion++;
        }
        if (oldVersion == 6) {
            final RealmObjectSchema userSchema = schema.get("Session");
            if (userSchema != null) {
                userSchema.addRealmListField("dataList", String.class);
            }
//            oldVersion++;
        }
    }
}
