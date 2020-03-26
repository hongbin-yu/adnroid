package wang.switchy.hin2n;

import android.annotation.TargetApi;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.os.Build;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.multidex.MultiDexApplication;
import android.util.Log;

import dajana.data.database.realm.RealmMigrations;
import io.realm.Realm;
import io.realm.RealmConfiguration;
import wang.switchy.hin2n.storage.db.base.DaoMaster;
import wang.switchy.hin2n.storage.db.base.DaoSession;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;


/**
 * Created by janiszhang on 2018/4/19.
 */

public class Application extends MultiDexApplication {

    public Context AppContext;

    private DaoMaster.DevOpenHelper mHelper;
    private SQLiteDatabase db;
    private DaoMaster mDaoMaster;
    private DaoSession mDaoSession;
    /**
     * Thread to execute tasks in background..
     */
    private final ExecutorService backgroundExecutor;
    private final ExecutorService backgroundExecutorForUserActions;
    /**
     * Handler to execute runnable in UI thread.
     */
    private final Handler handler;
    static {
//        System.loadLibrary("slog");
//        System.loadLibrary("uip");
//        System.loadLibrary("n2n_v2s");
//        System.loadLibrary("n2n_v2");
//        System.loadLibrary("n2n_v1");
//        System.loadLibrary("edge_v2s");
//        System.loadLibrary("edge_v2");
//        System.loadLibrary("edge_v1");
//       System.loadLibrary("edge_jni");
    }

    public Application() {
        handler = new Handler();
        backgroundExecutor = createSingleThreadExecutor("Background executor service");
        backgroundExecutorForUserActions = Executors.newFixedThreadPool(
                Runtime.getRuntime().availableProcessors(),
                new ThreadFactory() {
                    @Override
                    public Thread newThread(@NonNull Runnable runnable) {
                        Thread thread = new Thread(runnable);
                        thread.setPriority(Thread.MIN_PRIORITY);
                        thread.setDaemon(true);
                        return thread;
                    }
                });
    }

    //静态单例
    public static Application instance;

    @Override
    public void onCreate() {
        super.onCreate();
        instance = this;

        AppContext = this;

        setDatabase();
        //set realm database
        Realm.init(this);
        RealmConfiguration realmConfig = new RealmConfiguration.Builder()
                .name("dajana.db")
                .schemaVersion(1)
                .migration(new RealmMigrations())
                .build();
        Realm.setDefaultConfiguration(realmConfig);

        //Realm.deleteRealm(realmConfig);
//        UMConfigure.init(this, N2nTools.getMetaData(this, N2nTools.MetaUmengAppKey), N2nTools.getMetaData(this, N2nTools.MetaUmengChannel), UMConfigure.DEVICE_TYPE_PHONE, "");

//        MobclickAgent.setScenarioType(this, MobclickAgent.EScenarioType.E_UM_NORMAL);

        //Bugly.init(this, N2nTools.getMetaData(this, N2nTools.MetaBuglyAppId), BuildConfig.DEBUG);
        //initShare();

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            initNotificationChannel();
        }
        closeAndroidPDialog();
    }

 //   private void initShare() {
 //       PlatformConfig.setWeixin(N2nTools.getMetaData(this, N2nTools.MetaShareWxAppId), N2nTools.getMetaData(this, N2nTools.MetaShareWxAppSecret));
 //   }

    public static Application getInstance() {
        return instance;
    }

    /**
     * 设置greenDao
     */
    private void setDatabase() {
        mHelper = new DaoMaster.DevOpenHelper(this, "N2N-db", null);
        db = mHelper.getWritableDatabase();
        mDaoMaster = new DaoMaster(db);
        mDaoSession = mDaoMaster.newSession();
    }

    public DaoSession getDaoSession() {
        return mDaoSession;
    }

    public SQLiteDatabase getDb() {
        return db;
    }

    @TargetApi(Build.VERSION_CODES.O)
    private void initNotificationChannel() {
        String id = getString(R.string.notification_channel_id_default);
        String name = getString(R.string.notification_channel_name_default);
        createNotificationChannel(id, name, NotificationManager.IMPORTANCE_HIGH);
    }

    @TargetApi(Build.VERSION_CODES.O)
    private void createNotificationChannel(String id, CharSequence name, int importance) {
        NotificationManager notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        notificationManager.createNotificationChannel(new NotificationChannel(id, name, importance));
    }

    @NonNull
    private ExecutorService createSingleThreadExecutor(final String threadName) {
        return Executors.newSingleThreadExecutor(new ThreadFactory() {
            @Override
            public Thread newThread(@NonNull Runnable runnable) {
                Thread thread = new Thread(runnable, threadName);
                thread.setPriority(Thread.MIN_PRIORITY);
                thread.setDaemon(true);
                return thread;
            }
        });
    }

    /**
     * Submits request to be executed in background.
     */
    public void runInBackground(final Runnable runnable) {
        backgroundExecutor.submit(new Runnable() {
            @Override
            public void run() {
                try {
                    runnable.run();
                } catch (Exception e) {
                    Log.e("Application", e.getMessage());
                }
            }
        });
    }

    public void runInBackgroundUserRequest(final Runnable runnable) {
        backgroundExecutorForUserActions.submit(new Runnable() {
            @Override
            public void run() {
                try {
                    runnable.run();
                } catch (Exception e) {
                    Log.e("Application", e.getMessage());
                }
            }
        });
    }
    /**
     * Submits request to be executed in UI thread.
     */
    public void runOnUiThread(final Runnable runnable) {
        handler.post(runnable);
    }

    /**
     * Submits request to be executed in UI thread.
     */
    public void runOnUiThreadDelay(final Runnable runnable, long delayMillis) {
        handler.postDelayed(runnable, delayMillis);
    }

    private void closeAndroidPDialog(){

        try {

            Class aClass = Class.forName("android.content.pm.PackageParser$Package");

            Constructor declaredConstructor = aClass.getDeclaredConstructor(String.class);

            declaredConstructor.setAccessible(true);

        } catch (Exception e) {

            e.printStackTrace();

        }

        try {

            Class cls = Class.forName("android.app.ActivityThread");

            Method declaredMethod = cls.getDeclaredMethod("currentActivityThread");

            declaredMethod.setAccessible(true);

            Object activityThread = declaredMethod.invoke(null);

            Field mHiddenApiWarningShown = cls.getDeclaredField("mHiddenApiWarningShown");

            mHiddenApiWarningShown.setAccessible(true);

            mHiddenApiWarningShown.setBoolean(activityThread, true);

        } catch (Exception e) {

            e.printStackTrace();

        }

    }
}
