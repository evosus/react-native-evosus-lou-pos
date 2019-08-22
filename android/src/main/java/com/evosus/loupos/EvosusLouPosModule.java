package com.evosus.loupos;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Toast;

import com.pax.poslink.BatchRequest;
import com.pax.poslink.BatchResponse;
import com.pax.poslink.CommSetting;
import com.pax.poslink.ManageRequest;
import com.pax.poslink.ManageResponse;
import com.pax.poslink.PaymentResponse;
import com.pax.poslink.PosLink;
import com.pax.poslink.ProcessTransResult;
import com.pax.poslink.constant.EDCType;
import com.pax.poslink.constant.TransType;
import com.pax.poslink.peripheries.POSLinkCashDrawer;
import com.pax.poslink.peripheries.POSLinkPrinter;
import com.pax.poslink.peripheries.ProcessResult;

import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.ReactContextBaseJavaModule;
import com.facebook.react.bridge.ReactMethod;
import com.facebook.react.bridge.Callback;
import com.pax.poslink.poslink.POSLinkCreator;

import java.io.File;
import java.io.IOException;

public class EvosusLouPosModule extends ReactContextBaseJavaModule {
    private static final String TAG = "EvosusLouPosModule";

    private final ReactApplicationContext reactContext;
    private PosLink posLink = null;
    private static ProcessTransResult ptr;

    public EvosusLouPosModule(ReactApplicationContext reactContext) {
        super(reactContext);
        this.reactContext = reactContext;
    }

    @Override
    public String getName() {
        return "EvosusLouPos";
    }

    /**
     * @param stringArgument
     * @param numberArgument
     * @param callback
     */

    @ReactMethod
    public void sampleMethod(String stringArgument, int numberArgument, Callback callback) {
        // TODO: Implement some actually useful functionality
        callback.invoke("Received numberArgument: " + numberArgument + " stringArgument: " + stringArgument);
    }

    /**
     * @param message
     */
    @ReactMethod
    public void showToast(String message) {
        Toast.makeText(getReactApplicationContext(), message, Toast.LENGTH_LONG).show();
    }

    /**
     *
     */
    @ReactMethod
    public void openCashDrawer() {
        final ProcessResult result = POSLinkCashDrawer.getInstance(getReactApplicationContext()).open();
        if (!result.getCode().equals(ProcessResult.CODE_OK)) {
            getCurrentActivity().runOnUiThread(new Runnable() {
                public void run() {
                    Toast.makeText(getReactApplicationContext(), "Error opening cash drawer: " + result.getMessage(), Toast.LENGTH_LONG).show();
                    return;
                }
            });
        }
    }


    /**
     * @param callback
     */
    @ReactMethod
    public void checkPOSLink(final Callback callback) {
        POSLinkCreatorWrapper.createSync(getReactApplicationContext(), new AppThreadPool.FinishInMainThreadCallback<PosLink>() {
            @Override
            public void onFinish(PosLink result) {
                posLink = result;
                CommSetting commSetting = new CommSetting();
                commSetting.setBaudRate("9600");
                commSetting.setEnableProxy(true);
                commSetting.setSerialPort("COM1");
                commSetting.setTimeOut("60000");
                commSetting.setType("USB");
                posLink.SetCommSetting(commSetting);
                callback.invoke();
                return;
            }
        });
    }

    private void initPOSLink() {
//        posLink = POSLinkCreator.createPoslink(getCurrentActivity());
//        CommSetting commSetting = new CommSetting();
//        commSetting.setBaudRate("9600");
//        commSetting.setEnableProxy(true);
//        commSetting.setSerialPort("COM1");
//        commSetting.setTimeOut("60000");
//        commSetting.setType("USB");
//        posLink.SetCommSetting(commSetting);
        POSLinkCreatorWrapper.createSync(getReactApplicationContext(), new AppThreadPool.FinishInMainThreadCallback<PosLink>() {
            @Override
            public void onFinish(PosLink result) {
                posLink = result;
                CommSetting commSetting = new CommSetting();
                commSetting.setBaudRate("9600");
                commSetting.setEnableProxy(true);
                commSetting.setSerialPort("COM1");
                commSetting.setTimeOut("60000");
                commSetting.setType("USB");
                posLink.SetCommSetting(commSetting);
                return;
            }
        });
    }

    /**
     *
     */
    @ReactMethod
    public void batchClose() {
        // Recommend to use single thread pool instead.
        mHandler.postDelayed(MyRunnable, 25);

        if (posLink == null) {
            initPOSLink();
        }

        if (posLink == null) {
            Toast.makeText(getReactApplicationContext(), "Cannot initialize POSLink", Toast.LENGTH_LONG);
            return;
        }

        try {
            Thread.sleep(30);

            BatchRequest batchRequest = new BatchRequest();
            batchRequest.TransType = batchRequest.ParseTransType("BATCHCLOSE");
            batchRequest.EDCType = batchRequest.ParseEDCType(EDCType.ALL);
            posLink.BatchRequest = batchRequest;

            // ProcessTrans is Blocking call, will return when the transaction is complete.
            ptr = posLink.ProcessTrans();

            mHandler.removeCallbacks(MyRunnable);
        }
        catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    /**
     *
     */
    @ReactMethod
    public void rebootQ20() {
        // Recommend to use single thread pool instead.
        mHandler.postDelayed(MyRunnable, 25);

        if (posLink == null) {
            initPOSLink();
        }

        if (posLink == null) {
            Toast.makeText(getReactApplicationContext(), "Cannot initialize POSLink", Toast.LENGTH_LONG);
            return;
        }

        try {
            Thread.sleep(30);

            ManageRequest manageRequest = new ManageRequest();
            manageRequest.TransType = manageRequest.ParseTransType("REBOOT");
            posLink.ManageRequest = manageRequest;

            // ProcessTrans is Blocking call, will return when the transaction is complete.
            ptr = posLink.ProcessTrans();

            mHandler.removeCallbacks(MyRunnable);
        }
        catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    /**
     * @param receiptText
     * @param callback
     */
    @ReactMethod
    public void printReceipt(final String receiptText, final Callback callback) {

        POSLinkPrinter posLinkPrinter = POSLinkPrinter.getInstance(getReactApplicationContext());
        posLinkPrinter.setGray(POSLinkPrinter.GreyLevel.DEFAULT);
        posLinkPrinter.setPrintWidth(POSLinkPrinter.RecommendWidth.E500_RECOMMEND_WIDTH);

        POSLinkPrinter.getInstance(getReactApplicationContext()).print(receiptText, POSLinkPrinter.CutMode.PARTIAL_PAPER_CUT,SettingINI.getCommSettingFromFile(getReactApplicationContext().getFilesDir().getAbsolutePath() + "/" + SettingINI.FILENAME),  new POSLinkPrinter.PrintListener() {
            @Override
            public void onSuccess() {
                getCurrentActivity().runOnUiThread(new Runnable() {
                    public void run() {
                        callback.invoke("Printed");
                        Toast.makeText(getReactApplicationContext(), receiptText, Toast.LENGTH_LONG).show();
                        return;
                    }
                });
            }

            @Override
            public void onError(final ProcessResult processResult) {
                getCurrentActivity().runOnUiThread(new Runnable() {
                    public void run() {
                        callback.invoke(processResult.getMessage());
                        Toast.makeText(getReactApplicationContext(), processResult.getMessage(), Toast.LENGTH_LONG).show();
                        return;
                    }
                });
            }
        });

    }

    private Handler mHandler = new Handler(Looper.getMainLooper()) {

        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case Constant.TRANSACTION_SUCCESSED:
                    PaymentResponse response = (PaymentResponse) msg.obj;
                    break;
                case Constant.TRANSACTION_TIMEOOUT:
                case Constant.TRANSACTION_FAILURE:
                    String title = msg.getData().getString(Constant.DIALOG_TITLE);
                    String message = msg.getData().getString(Constant.DIALOG_MESSAGE);
                    Toast.makeText(getReactApplicationContext(), title + '\n' + message, Toast.LENGTH_LONG);
                    break;
                case Constant.TRANSACTION_STATUS:
                    String sTitle = "REPORTED STATUS";
                    String sMessage = msg.obj.toString();
                    Toast.makeText(getReactApplicationContext() , sTitle + '\n' + sMessage, Toast.LENGTH_LONG);
                    break;
            }
        }

    };

    private int lastReportedStatus = -1;
    final Runnable MyRunnable = new Runnable() {

        public void run() {
            int status;

            if (posLink != null) {
                try {
                    Thread.sleep(0);

                    status = posLink.GetReportedStatus();
                    if (status != lastReportedStatus) {
                        switch (status) {
                            case 0:
                                Message msg0 = new Message();
                                msg0.what = Constant.TRANSACTION_STATUS;
                                msg0.obj = "Ready for CARD INPUT";
                                mHandler.sendMessage(msg0);
                                break;
                            case 1:
                                Message msg1 = new Message();
                                msg1.what = Constant.TRANSACTION_STATUS;
                                msg1.obj = "Ready for PIN ENTRY";
                                mHandler.sendMessage(msg1);
                                break;
                            case 2:
                                Message msg2 = new Message();
                                msg2.what = Constant.TRANSACTION_STATUS;
                                msg2.obj = "Ready for SIGNATURE";
                                mHandler.sendMessage(msg2);
                                break;
                            case 3:
                                Message msg3 = new Message();
                                msg3.what = Constant.TRANSACTION_STATUS;
                                msg3.obj = "Ready for ONLINE PROCESSING";
                                mHandler.sendMessage(msg3);
                                break;
                            case 4:
                                Message msg4 = new Message();
                                msg4.what = Constant.TRANSACTION_STATUS;
                                msg4.obj = "Ready for NEW CARD INPUT";
                                mHandler.sendMessage(msg4);
                                break;
                            default:
                                break;
                        }

                        lastReportedStatus = status;
                    }

                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }

            mHandler.postDelayed(this, 500);
        }
    };

    private void taskCompleted(PosLink poslink) {
        // There will be 2 separate results that you must handle. First is the
        // ProcessTransResult, this will give you the result of the
        // request to call poslink. ManageResponse should only be checked if
        // ProcessTransResultCode.Code == OK.

        // transaction successful
        if (ptr.Code == ProcessTransResult.ProcessTransResultCode.OK) {
            // ManageResponse is the result of the manage transaction to the server.
            if (poslink.ManageResponse != null) {
                ManageResponse manageResponse = poslink.ManageResponse;
                Message msg = new Message();
                msg.what = Constant.TRANSACTION_SUCCESSED;
                msg.obj = manageResponse;
                handleMessage(msg);
            } else if (poslink.BatchResponse != null) {
                BatchResponse batchResponse = poslink.BatchResponse;
                Message msg = new Message();
                msg.what = Constant.TRANSACTION_SUCCESSED;
                msg.obj = batchResponse;
                handleMessage(msg);
            } else if (poslink.PaymentResponse != null) {
                PaymentResponse paymentResponse = poslink.PaymentResponse;
                Message msg = new Message();
                msg.what = Constant.TRANSACTION_SUCCESSED;
                msg.obj = paymentResponse;
                handleMessage(msg);
            }

            Log.i(TAG, "Transaction sucessed!");

        } else if (ptr.Code == ProcessTransResult.ProcessTransResultCode.TimeOut) {
            Message msg = new Message();
            msg.what = Constant.TRANSACTION_TIMEOOUT;
            Bundle b = new Bundle();
            b.putString(Constant.DIALOG_TITLE, String.valueOf(ptr.Code));
            b.putString(Constant.DIALOG_MESSAGE, ptr.Msg);
            msg.setData(b);
            handleMessage(msg);

            Log.e(TAG, "Transaction TimeOut! " + String.valueOf(ptr.Code));
            Log.e(TAG, "Transaction TimeOut! " + ptr.Msg);
        } else {
            Message msg = new Message();
            msg.what = Constant.TRANSACTION_FAILURE;
            Bundle b = new Bundle();
            b.putString(Constant.DIALOG_TITLE, String.valueOf(ptr.Code));
            b.putString(Constant.DIALOG_MESSAGE, ptr.Msg);
            msg.setData(b);
            handleMessage(msg);

            Log.e(TAG, "Transaction Error! " + String.valueOf(ptr.Code));
            Log.e(TAG, "Transaction Error! " + ptr.Msg);
        }
    }

    public void handleMessage(Message msg) {
        switch (msg.what) {
            case Constant.TRANSACTION_SUCCESSED:
                ManageResponse response = (ManageResponse) msg.obj;
                Toast.makeText(getReactApplicationContext(), response.ResultCode +'\n' + response.ResultTxt, Toast.LENGTH_LONG).show();
                break;
            case Constant.TRANSACTION_TIMEOOUT:
            case Constant.TRANSACTION_FAILURE:
                String title = msg.getData().getString(Constant.DIALOG_TITLE);
                String message = msg.getData().getString(Constant.DIALOG_MESSAGE);
                Toast.makeText(getReactApplicationContext(), title +'\n' + message, Toast.LENGTH_LONG).show();
                break;
        }
    }
}
