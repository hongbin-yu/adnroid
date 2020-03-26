package com.wave.fileuploadservice;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

public class StartupReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {

        Intent myIntent = new Intent(context, FileSystemObserverService.class);
        myIntent.putExtra(FileSystemObserverService.INTENT_EXTRA_FILEPATH,"Camera");
        Log.d("StartupReceiver","Start FileSystemObserverService with filter Camera");
        context.startService(myIntent);

    }
}
