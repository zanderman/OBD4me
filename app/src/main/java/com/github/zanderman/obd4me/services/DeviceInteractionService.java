package com.github.zanderman.obd4me.services;

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.support.annotation.Nullable;

/**
 * Created by zanderman on 3/21/16.
 */
public class DeviceInteractionService extends Service {

    /**
     * Service Members
     */
    public IBinder binder;

    public DeviceInteractionService() {
        super();

        /**
         * Initialize Members.
         */
        this.binder = new Binder();
    }

    /**
     * Method:
     *      onBind( Intent )
     *
     * Description:
     *      ...
     *
     * @param intent
     * @return
     */
    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }


    // NOTE: When service is first started (before binding), it runs this command.
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return super.onStartCommand(intent, flags, startId);
    }


    @Override
    public void onDestroy() {
        super.onDestroy();
    }
}
