/***
 Copyright (c) 2013-2014 CommonsWare, LLC

 Licensed under the Apache License, Version 2.0 (the "License"); you may
 not use this file except in compliance with the License. You may obtain
 a copy of the License at
 http://www.apache.org/licenses/LICENSE-2.0
 Unless required by applicable law or agreed to in writing, software
 distributed under the License is distributed on an "AS IS" BASIS,
 WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 See the License for the specific language governing permissions and
 limitations under the License.
 */

package com.evosus.loupos;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Binder;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.text.Spanned;
import android.util.Log;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.core.app.NotificationCompat;

import com.commonsware.cwac.preso.PresentationService;

import org.commonmark.node.Node;

import io.noties.markwon.Markwon;
import io.noties.markwon.ext.tables.TablePlugin;
import io.noties.markwon.ext.tables.TableTheme;
import io.noties.markwon.html.HtmlPlugin;
import io.noties.markwon.image.ImagesPlugin;

public class CustomerDisplayService extends PresentationService implements Runnable {

    private final IBinder mBinder = new MyBinder();

    private static final String TAG = "CustomerDisplayService";
    private static final String CHANNEL_CUSTOMER_DISPLAY="CH_CUSTOMER_DISPLAY";
    private static final String ACTION_STOP="stop";

    private static final int[] SLIDES= { R.drawable.img0,
            R.drawable.img1, R.drawable.img2, R.drawable.img3,
            R.drawable.img4, R.drawable.img5, R.drawable.img6,
            R.drawable.img7, R.drawable.img8, R.drawable.img9,
            R.drawable.img10, R.drawable.img11, R.drawable.img12,
            R.drawable.img13, R.drawable.img14, R.drawable.img15,
            R.drawable.img16, R.drawable.img17, R.drawable.img18,
            R.drawable.img19 };

    private ImageView iv = null;
    private TextView textView = null;
    private String customerDisplayMarkdown = "";
    private int iteration=0;
    private Handler handler=null;
    private Markwon markwon = null;

    @Override
    public IBinder onBind(Intent intent) {
        // Do something
        return mBinder;
    }

    public class MyBinder extends Binder {
        CustomerDisplayService getService() {
            return CustomerDisplayService.this;
        }
    }

    public void setCustomerDisplayTrxMD(String trxMarkdown) {

        customerDisplayMarkdown = trxMarkdown;
        Log.i(TAG, trxMarkdown);

    }

    @Override
    public void onCreate() {
        handler=new Handler(Looper.getMainLooper());
        super.onCreate();

        NotificationManager mgr = (NotificationManager)getSystemService(NOTIFICATION_SERVICE);

        if (Build.VERSION.SDK_INT>=Build.VERSION_CODES.O && mgr.getNotificationChannel(CHANNEL_CUSTOMER_DISPLAY)==null) {
            mgr.createNotificationChannel(new NotificationChannel(CHANNEL_CUSTOMER_DISPLAY,"Customer Facing Display", NotificationManager.IMPORTANCE_DEFAULT));
        }

        startForeground(1338, buildForegroundNotification());

    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (ACTION_STOP.equals(intent.getAction())) {
            stopSelf();
        }

        return(super.onStartCommand(intent, flags, startId));

//        return Service.START_NOT_STICKY;
    }

    @Override
    protected int getThemeId() {
        return(R.style.AppTheme);
    }

    @Override
    protected View buildPresoView(Context ctxt, LayoutInflater inflater) {


        // create default instance of TablePlugin
        final TableTheme tableTheme = TableTheme.emptyBuilder()
                .tableBorderColor(Color.RED)
                .tableBorderWidth(0)
                .tableCellPadding(0)
                .tableHeaderRowBackgroundColor(Color.DKGRAY)
                .tableEvenRowBackgroundColor(Color.GRAY)
                .tableOddRowBackgroundColor(Color.GRAY)
                .build();

        markwon = Markwon.builder(ctxt)
                .usePlugin(TablePlugin.create(tableTheme))
                .usePlugin(HtmlPlugin.create())
                .usePlugin(ImagesPlugin.create())
                .build();

        textView = new TextView(ctxt);
        textView.setBackgroundColor(Color.BLACK);
        textView.setTextColor(Color.WHITE);
        textView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 24f);

        run();
        return(textView);

//
//        iv = new ImageView(ctxt);
//        run();
//        return(iv);
    }

    String lastCustomerMarkdown;
    @Override
    public void run() {
//        iv.setImageResource(SLIDES[iteration % SLIDES.length]);
//        iteration+=1;
        // set markdown
//        markwon.setMarkdown(textView, customerDisplayMarkdown);
        // parse markdown to commonmark-java Node
        if (markwon != null && lastCustomerMarkdown != customerDisplayMarkdown) {
            final Node node = markwon.parse(customerDisplayMarkdown);

            // create styled text from parsed Node
            final Spanned markdown = markwon.render(node);

            // use it on a TextView
            markwon.setParsedMarkdown(textView, markdown);
            lastCustomerMarkdown = customerDisplayMarkdown;
        }

        handler.postDelayed(this, 5000);
    }

    @Override
    public void onDestroy() {
        handler.removeCallbacks(this);
        super.onDestroy();
    }

    private Notification buildForegroundNotification() {
        NotificationCompat.Builder b = new NotificationCompat.Builder(this, CHANNEL_CUSTOMER_DISPLAY);

        b.setOngoing(true)
                .setContentTitle(getString(R.string.msg_foreground))
                .setSmallIcon(R.drawable.ic_stat_screen)
                .addAction(android.R.drawable.ic_media_pause, getString(R.string.msg_stop), buildStopPendingIntent());

        return(b.build());
    }

    private PendingIntent buildStopPendingIntent() {
        Intent i=new Intent(this, getClass()).setAction(ACTION_STOP);

        return(PendingIntent.getService(this, 0, i, 0));
    }
}
