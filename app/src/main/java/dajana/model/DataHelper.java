package dajana.model;

import android.content.ContentResolver;
import android.content.Context;
import android.net.Uri;
import android.provider.MediaStore;
import android.text.format.DateFormat;
import android.util.Log;
import android.widget.Toast;

import dajana.utils.FileManager;

import java.io.File;
import java.util.Collection;
import java.util.Date;

import dajana.data.database.realm.FileRealm;
import dajana.data.database.realm.MyCloud;
import io.realm.OrderedRealmCollection;
import io.realm.Realm;
import io.realm.RealmAsyncTask;

public class DataHelper {

    public static void setPrimaryKeyAsync(Realm realm) {
        realm.executeTransactionAsync(new Realm.Transaction() {
            @Override
            public void execute(Realm realm) {
                Number currentIdNum = realm.where(FileRealm.class).max(FileRealm.Fields.UID);
                if(currentIdNum == null) {
                    FileRealm.INTEGER_COUNTER.set(1);
                }else {
                    FileRealm.INTEGER_COUNTER.set(currentIdNum.intValue()+1);
                }
            }
        });
    }


    // Create 3 counters and insert them into random place of the list.
    public static void randomAddItemAsync(Realm realm) {
        realm.executeTransactionAsync(new Realm.Transaction() {
            @Override
            public void execute(Realm realm) {
                for (int i = 0; i < 3; i++) {
                    FileRealm.create(realm, true);
                }
            }
        });
    }

    public static void addItemAsync(Realm realm) {
        realm.executeTransactionAsync(new Realm.Transaction() {
            @Override
            public void execute(Realm realm) {
                FileRealm.create(realm);
            }
        });
    }

    public static boolean exists(Realm realm, String username, String server, String path) {
        MyCloud myCloud = realm.where(MyCloud.class).equalTo(MyCloud.Fields.USERNAME, username)
                .equalTo(MyCloud.Fields.SERVER, server)
                .equalTo(MyCloud.Fields.FILELIST + "." + FileRealm.Fields.ORIGINALURL, path)
                .contains(MyCloud.Fields.FILELIST + "." + FileRealm.Fields.ORIGINALURL,"/Dajana/")
                .findFirst();

        if (myCloud != null) {
            return true;
        } else {
            return false;
        }
    }

    public static boolean exists(Realm realm, String username, String server, String path, String uri) {
        MyCloud myCloud = realm.where(MyCloud.class).equalTo(MyCloud.Fields.USERNAME, username)
                .equalTo(MyCloud.Fields.SERVER, server)
                .equalTo(MyCloud.Fields.FILELIST + "." + FileRealm.Fields.ORIGINALURL, path)
                .or().equalTo(MyCloud.Fields.FILELIST + "." + FileRealm.Fields.URI,uri)
                .findFirst();

        if (myCloud != null) {
            return true;
        } else {
            return false;
        }
    }
    public static boolean exists(Realm realm, String path) {
        FileRealm fileRealm = realm.where(FileRealm.class).equalTo(FileRealm.Fields.ORIGINALURL, path)
                .findFirst();

        if (fileRealm != null) {
            return true;
        } else {
            return false;
        }
    }

    public static boolean exists(Realm realm, String path,String uri) {
        FileRealm fileRealm = realm.where(FileRealm.class).equalTo(FileRealm.Fields.ORIGINALURL, path).or().equalTo(FileRealm.Fields.URI,uri)
                .findFirst();

        if (fileRealm != null) {
            return true;
        } else {
            return false;
        }
    }

    public static FileRealm getFileRealm(Realm realm, long uid) {
        FileRealm fileDB = realm.where(FileRealm.class).equalTo(FileRealm.Fields.UID,uid).findFirst();
        return fileDB;
    }

    public static FileRealm getFileRealm(Realm realm,Context context, String username, String server, String path) {
        FileRealm fileDB = realm.where(FileRealm.class).equalTo(FileRealm.Fields.ORIGINALURL,path).findFirst();
        return fileDB;
    }

    public static void addItemAsync(Realm realm, Context context,String username, String server, String path) {
        realm.executeTransactionAsync(new Realm.Transaction() {
            @Override
            public void execute(Realm realm) {
                MyCloud myCloud = realm.where(MyCloud.class).equalTo(MyCloud.Fields.USERNAME,username).equalTo(MyCloud.Fields.SERVER,server).findFirst();
                FileRealm item = realm.createObject(FileRealm.class,FileRealm.increment());//FileRealm.createFileRealm(realm);
                if(path != null) {
                    item.setOriginalUrl(path);
                    File file = new File(path);
                    if(file.exists()) {
                        item.setFilename(FileManager.getBaseName(path));
                        item.setContentType(FileManager.getMimeType(path));
                        String timestamp = android.text.format.DateFormat.format("yyyy-MM-dd hh:mm:ss a", new java.util.Date(file.lastModified())).toString();
                        item.setTimestamp(timestamp);
                        item.setUploader(username);
                        item.setSize(file.length());
                    }
                }

                myCloud.getFileList().add(item);
            }
        }, new Realm.Transaction.OnError() {
            @Override
            public void onError(Throwable error) {
                Toast.makeText(context, error.getMessage(), Toast.LENGTH_LONG);
            }
        });
    }

    public static void updateItemAsync(Realm realm, Context context,FileRealm source) {
        realm.executeTransactionAsync(new Realm.Transaction() {
            @Override
            public void execute(Realm realm) {
                FileRealm item = realm.where(FileRealm.class).equalTo(FileRealm.Fields.UID, source.getUid()).findFirst();
                if (item != null) {
                    item.setFilePath(source.getFilePath());
                }

            }
        }, new Realm.Transaction.OnError() {
            @Override
            public void onError(Throwable error) {
                Toast.makeText(context, error.getMessage(), Toast.LENGTH_LONG);
            }
        });
    }


    public static void addItemAsync(Realm realm, Context context, String username, String server, String path, String uri,String contentType,String filePath, String lastModified, long size) {
        realm.executeTransactionAsync(new Realm.Transaction() {
            @Override
            public void execute(Realm realm) {
                MyCloud myCloud = realm.where(MyCloud.class).equalTo(MyCloud.Fields.USERNAME,username).equalTo(MyCloud.Fields.SERVER,server).findFirst();
                FileRealm item = FileRealm.createFileRealm(realm);
                item.setOriginalUrl(path);
                item.setFilePath(filePath);
                item.setFilename(FileManager.getBaseName(path));
                item.setContentType(contentType);
                item.setTimestamp(lastModified);
                item.setUploader(username);
                item.setUri(uri);
                item.setSize(size);
                myCloud.getFileList().add(item);
            }
        }, new Realm.Transaction.OnError() {
            @Override
            public void onError(Throwable error) {
                Toast.makeText(context, error.getMessage(), Toast.LENGTH_LONG);
            }
        });
    }

    public static void deleteItemAsync(Realm realm, final long id) {
        realm.executeTransactionAsync(new Realm.Transaction() {
            @Override
            public void execute(Realm realm) {
                FileRealm fileRealm = realm.where(FileRealm.class).equalTo(FileRealm.Fields.UID,id).findFirst();
                if(fileRealm!=null && fileRealm.getFilePath()!=null) {
                    new File(fileRealm.getFilePath()).delete();
                }
                FileRealm.delete(realm, id);
            }
        });
    }

    public static void deleteLocalStorageAsync(Realm realm, Context context, final long id) {
        realm.executeTransactionAsync(new Realm.Transaction() {
            @Override
            public void execute(Realm realm) {
                FileRealm fileRealm = realm.where(FileRealm.class).equalTo(FileRealm.Fields.UID, id).findFirst();
                if (fileRealm != null && fileRealm.getFilePath() != null) {
                    new File(fileRealm.getFilePath()).delete();
                    fileRealm.setFilePath(null);
                }
                if (fileRealm != null && fileRealm.getOriginalUrl() != null) {
                    File file = new File(fileRealm.getOriginalUrl());
                    if (delete(context, file)) {
                        fileRealm.setOriginalUrl(null);
                        fileRealm.setUri(null);
                    } else {
                        Toast.makeText(context, file.getName() + " does not deleted", Toast.LENGTH_LONG);
                    }

                }


            }
        }, new Realm.Transaction.OnError() {
            @Override
            public void onError(Throwable error) {
                Toast.makeText(context, error.getMessage(), Toast.LENGTH_LONG);
            }
        });
    }

    public static boolean delete(final Context context,File file) {
        final String where = MediaStore.MediaColumns.DATA+"=?";
        final String[] selectionArgs = new String[] {
                file.getAbsolutePath()
        };
        final ContentResolver contentResolver = context.getContentResolver();
        final Uri filesUri = MediaStore.Files.getContentUri("external");
        contentResolver.delete(filesUri,where,selectionArgs);
        if(file.exists()) {
            contentResolver.delete(filesUri,where,selectionArgs);
        }
        return !file.exists();
    }


    public static void deleteItemsAsync(Realm realm, Collection<Integer> ids) {
        // Create an new array to avoid concurrency problem.
        final Integer[] idsToDelete = new Integer[ids.size()];
        ids.toArray(idsToDelete);
        RealmAsyncTask realmAsyncTask = realm.executeTransactionAsync(new Realm.Transaction() {
            @Override
            public void execute(Realm realm) {
                for (Integer id : idsToDelete) {
                    FileRealm fileRealm = realm.where(FileRealm.class).equalTo(FileRealm.Fields.UID,id).findFirst();
                    if(fileRealm!=null && fileRealm.getFilePath()!=null) {
                        new File(fileRealm.getFilePath()).delete();
                    }
                    FileRealm.delete(realm, id);
                }
            }
        }, new Realm.Transaction.OnError() {
            @Override
            public void onError(Throwable error) {
                Log.e("DataHelper",error.getMessage());
            }
        });
    }


}
