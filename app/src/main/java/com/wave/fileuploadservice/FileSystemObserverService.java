package com.wave.fileuploadservice;


import android.app.Service;
import android.content.Intent;
import android.os.Environment;
import android.os.FileObserver;
import android.os.IBinder;
import android.util.Log;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

public class FileSystemObserverService extends Service {

    private String internalPath;
    private String externalPath;
    public static String INTENT_EXTRA_FILEPATH = "INTENT_EXTRA_FILEPATH";
    private static final String TAG = "FileSystemObserverService";
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        if((intent.hasExtra(INTENT_EXTRA_FILEPATH))) {// we store the path of directory inside the intent that starts the service
           String filter = intent.getStringExtra(INTENT_EXTRA_FILEPATH);
           Log.i("Filter:",filter);
           observe(filter);
        }else {
            observe();
        }
        return Service.START_NOT_STICKY;

        //return super.onStartCommand(intent, flags, startId);
    }
    @Override
    public void onCreate() {
        super.onCreate();
    }

    @Override
    public IBinder onBind(Intent intent) {
        //TODO for communication return IBinder implementation
        return null;
    }
    public File getInternalStoragePath() {
        File parent = Environment.getExternalStorageDirectory().getParentFile();
        File external = Environment.getExternalStorageDirectory();
        File[] files = parent.listFiles();
        File internal = null;
        if (files != null) {
            for (int i = 0; i < files.length; i++) {
                if (files[i].getName().toLowerCase().startsWith("sdcard") && !files[i].equals(external)) {
                    internal = files[i];
                }
            }
        }

        return internal;
    }
    public File getExtenerStoragePath() {

        return Environment.getExternalStorageDirectory();
    }

    public void observe(String filter) {
        Thread t = new Thread(new Runnable() {

            @Override
            public void run() {


                //File[]   listOfFiles = new File(path).listFiles();
                File str = getInternalStoragePath();
                if (str != null) {
                    internalPath = str.getAbsolutePath();
                    Log.i("obsever","Obser on "+internalPath);
                    new Obsever(internalPath,filter).startWatching();
                }
                str = getExtenerStoragePath();
                if (str != null) {
                    externalPath = str.getAbsolutePath();
                    Log.i("obsever","Obser on "+externalPath);
                    new Obsever(externalPath,filter).startWatching();
                }


            }
        });
        t.setPriority(Thread.MIN_PRIORITY);
        t.start();


    }
    public void observe() {
        Thread t = new Thread(new Runnable() {

            @Override
            public void run() {


                //File[]   listOfFiles = new File(path).listFiles();
                File str = getInternalStoragePath();
                if (str != null) {
                    internalPath = str.getAbsolutePath();
                    Log.i("obsever","Obser on "+internalPath);
                    new Obsever(internalPath).startWatching();
                }
                str = getExtenerStoragePath();
                if (str != null) {
                    externalPath = str.getAbsolutePath();
                    Log.i("obsever","Obser on "+externalPath);
                    new Obsever(externalPath).startWatching();
                }



            }
        });
        t.setPriority(Thread.MIN_PRIORITY);
        t.start();


    }

    class Obsever extends FileObserver {
        List<SingleFileObserver> mObservers;
        String mPath;
        String mFilter;
        int mMask;

        public Obsever(String path) {
            // TODO Auto-generated constructor stub
            this(path, ALL_EVENTS);
        }

        public Obsever(String path, int mask) {
            super(path, ALL_EVENTS);
            mPath = path;
            mMask = mask;

        }

        public Obsever(String path, String filter) {
            super(path, FileObserver.CREATE);
            mPath = path;
            mFilter = filter;
            // TODO Auto-generated constructor stub

        }
        public Obsever(String path, String filter,int mask) {
            super(path, mask);
            mPath = path;
            mFilter = filter;
            mMask = mask;


        }

        @Override
        public void startWatching() {
            // TODO Auto-generated method stub
            if (mObservers != null)
                return;
            mObservers = new ArrayList<SingleFileObserver>();
            Stack<String> stack = new Stack<String>();
            stack.push(mPath);
            while (!stack.empty()) {
                String parent = stack.pop();
                mObservers.add(new SingleFileObserver(parent, mMask));
                File path = new File(parent);
                File[] files = path.listFiles();
                if (files == null) continue;
                for (int i = 0; i < files.length; ++i) {
                    if (files[i].isDirectory() && !files[i].getName().equals(".") && !files[i].getName().equals("..")) {
                            stack.push(files[i].getPath());
                    }
                }
            }
            for (int i = 0; i < mObservers.size(); i++) {
                if(mFilter != null) {
                    if(mObservers.get(i).mPath.indexOf(mFilter)>0 || mObservers.get(i).mPath.endsWith("/0")) {
                        Log.d("WATCHING_ON",mObservers.get(i).mPath);
                        mObservers.get(i).startWatching();
                    }else {
                        Log.d("SKIP_ON",mObservers.get(i).mPath);
                    }
                }else {
                    Log.d("WATCHING_ON",mObservers.get(i).mPath);
                    mObservers.get(i).startWatching();
                }

            }
        }

        @Override
        public void stopWatching() {
            // TODO Auto-generated method stub
            if (mObservers == null)
                return;
            for (int i = 0; i < mObservers.size(); ++i) {
                mObservers.get(i).stopWatching();
            }
            mObservers.clear();
            mObservers = null;
        }

        @Override
        public void onEvent(int event, final String path) {
            if (event == FileObserver.OPEN) {
                Log.d("OPEN",path);
                //do whatever you want
            } else if (event == FileObserver.CREATE) {
                Log.d("CREATE",path);
                FileUploadService.qFileToUpload.add(path);
            } else if (event == FileObserver.DELETE_SELF || event == FileObserver.DELETE) {

                //do whatever you want
            } else if (event == FileObserver.MOVE_SELF || event == FileObserver.MOVED_FROM || event == FileObserver.MOVED_TO) {
                //do whatever you want

            }
        }

        private class SingleFileObserver extends FileObserver {
            private String mPath;

            public SingleFileObserver(String path, int mask) {
                super(path, mask);
                // TODO Auto-generated constructor stub
                mPath = path;
            }

            @Override
            public void onEvent(int event, String path) {
                // TODO Auto-generated method stub
                String newPath = mPath + "/" + path;
                Obsever.this.onEvent(event, newPath);
            }

        }
    }
}
