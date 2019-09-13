package com.evosus.loupos;

import android.content.Context;
import android.hardware.usb.UsbDevice;
import android.util.Log;
import android.widget.Toast;

import com.pax.poslink.CommSetting;
import com.pax.poslink.PosLink;
import com.pax.poslink.network.TcpConnection;
import com.pax.poslink.poslink.POSLinkCreator;
import com.pax.poslink.usb.UsbUtil;

public class POSLinkCreatorWrapper {

    private static String lastError;
    private static PosLink create(Context context) {
        lastError = "";
        String iniFile = context.getFilesDir().getAbsolutePath() + "/" + SettingINI.FILENAME;
        CommSetting commset = SettingINI.getCommSettingFromFile(iniFile);
        if (commset.getType().equals(CommSetting.TCP)) {
            Integer port = Integer.parseInt(commset.getDestPort());
            Integer timeout = Integer.parseInt(commset.getTimeOut());
            TcpConnection tcpConnection = new TcpConnection(commset.getDestIP(), port, timeout);
            if (tcpConnection == null) {
                lastError = "Cannot create TCP connection to terminal.";
                Toast.makeText(context, "Cannot create TCP connection to terminal.", Toast.LENGTH_SHORT).show();
            } else {
                Integer i = tcpConnection.connect();
                if (i != 0) {
                    lastError = "Exception opening TCP connection to terminal.";
                    Toast.makeText(context, "Exception opening TCP connection to terminal.", Toast.LENGTH_SHORT).show();
                }
                try {
                    tcpConnection.close();
                } catch (Exception e) {
                    lastError = "Exception closing TCP connection to terminal.";
                    Toast.makeText(context, "Exception closing TCP connection to terminal.", Toast.LENGTH_SHORT).show();
                }
            }
        }
        if (commset.getType().equals(CommSetting.USB) && !UsbUtil.hasPermission(context)) {
            UsbDevice usbDevice = UsbUtil.getDevice(context);
            if (usbDevice == null) {
                Toast.makeText(context, "Please plug in the POS machine with USB.", Toast.LENGTH_SHORT).show();
            }
        }
        return POSLinkCreator.createPoslink(context);
    }

    public static void createSync(final Context context, final AppThreadPool.FinishInMainThreadCallback<PosLink> callback) {
        Log.i("DEBUG", "Start Create POSLink");
        callback.onFinish(POSLinkCreatorWrapper.create(context), lastError);
        Log.i("DEBUG", "Finish Create POSLink");

    }
}
