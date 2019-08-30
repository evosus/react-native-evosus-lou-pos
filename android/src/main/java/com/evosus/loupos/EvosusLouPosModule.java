package com.evosus.loupos;

import android.content.Context;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.telecom.Call;
import android.util.Log;
import android.widget.Toast;

import com.facebook.react.bridge.Arguments;
import com.facebook.react.bridge.WritableMap;
import com.pax.poslink.BatchRequest;
import com.pax.poslink.BatchResponse;
import com.pax.poslink.CommSetting;
import com.pax.poslink.LogSetting;
import com.pax.poslink.ManageRequest;
import com.pax.poslink.ManageResponse;
import com.pax.poslink.POSLinkAndroid;
import com.pax.poslink.PaymentRequest;
import com.pax.poslink.PaymentResponse;
import com.pax.poslink.PosLink;
import com.pax.poslink.ProcessTransResult;
import com.pax.poslink.constant.EDCType;
import com.pax.poslink.peripheries.POSLinkCashDrawer;
import com.pax.poslink.peripheries.POSLinkPrinter;
import com.pax.poslink.peripheries.ProcessResult;

import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.ReactContextBaseJavaModule;
import com.facebook.react.bridge.ReactMethod;
import com.facebook.react.bridge.Callback;
import com.pax.poslink.util.CountRunTime;

import java.util.Arrays;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class EvosusLouPosModule extends ReactContextBaseJavaModule {
    private static final String TAG = "EvosusLouPosModule";

    private final ReactApplicationContext reactContext;
    private PosLink posLink = null;
    private static ProcessTransResult ptr;
    private static CommSetting commSetting;
    private static Boolean bInited = false;
    private static Callback success;
    private static Callback error;

    public EvosusLouPosModule(ReactApplicationContext reactContext) {
        super(reactContext);
        this.reactContext = reactContext;
    }

    @Override
    public String getName() {
        return "EvosusLouPos";
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
     * @param amount
     * @param referenceNumber
     * @param successCb
     * @param errorCb
     */
    @ReactMethod
    public void creditSale(String amount, String referenceNumber, Callback successCb, Callback errorCb) {

        if (!validatePOSLink(error)) return;

        success = successCb;
        error = errorCb;

        // Recommend to use single thread pool instead.
        OneShotPaymentTask oneShotPaymentTask = new OneShotPaymentTask(success, error);
        mHandler.postDelayed(oneShotPaymentTask, 25);

        try {
            Thread.sleep(30);

            PaymentRequest paymentRequest = new PaymentRequest();
            paymentRequest.TenderType = paymentRequest.ParseTenderType("CREDIT");
            paymentRequest.TransType = paymentRequest.ParseTransType("SALE");
            paymentRequest.ECRRefNum = referenceNumber;
            paymentRequest.Amount = amount; // It is expected that $1.23 will arrive as "123", $0.09 as "9"

            processPayment(paymentRequest);

            mHandler.removeCallbacks(oneShotPaymentTask);
        }
        catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    /**
     * @param amount
     * @param referenceNumber
     * @param successCb
     * @param errorCb
     */
    @ReactMethod
    public void creditRefund(String amount, String referenceNumber, Callback successCb, Callback errorCb) {

        if (!validatePOSLink(error)) return;

        success= successCb;
        error = errorCb;

        // Recommend to use single thread pool instead.
        OneShotPaymentTask oneShotPaymentTask = new OneShotPaymentTask(success, error);
        mHandler.postDelayed(oneShotPaymentTask, 25);

        try {
            Thread.sleep(30);

            PaymentRequest paymentRequest = new PaymentRequest();
            paymentRequest.TenderType = paymentRequest.ParseTenderType("CREDIT");
            paymentRequest.TransType = paymentRequest.ParseTransType("REFUND");
            paymentRequest.ECRRefNum = referenceNumber;
            paymentRequest.Amount = amount; // It is expected that $1.23 will arrive as "123", $0.09 as "9"

            processPayment(paymentRequest);

            mHandler.removeCallbacks(oneShotPaymentTask);
        }
        catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    /**
     * @param amount
     * @param referenceNumber
     * @param successCb
     * @param errorCb
     */
    @ReactMethod
    public void creditVoid(String amount, String referenceNumber, Callback successCb, Callback errorCb) {

        if (!validatePOSLink(error)) return;

        success= successCb;
        error = errorCb;

        // Recommend to use single thread pool instead.
        OneShotPaymentTask oneShotPaymentTask = new OneShotPaymentTask(success, error);
        mHandler.postDelayed(oneShotPaymentTask, 25);

        try {
            Thread.sleep(30);

            PaymentRequest paymentRequest = new PaymentRequest();
            paymentRequest.TenderType = paymentRequest.ParseTenderType("CREDIT");
            paymentRequest.TransType = paymentRequest.ParseTransType("VOID");
            paymentRequest.ECRRefNum = referenceNumber;

            processPayment(paymentRequest);

            mHandler.removeCallbacks(oneShotPaymentTask);
        }
        catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    /**
     * @param amount
     * @param referenceNumber
     * @param authCode
     * @param successCb
     * @param errorCb
     */
    @ReactMethod
    public void creditForceAuth(String amount, String referenceNumber, String authCode, Callback successCb, Callback errorCb) {

        if (!validatePOSLink(error)) return;

        success= successCb;
        error = errorCb;

        // Recommend to use single thread pool instead.
        OneShotPaymentTask oneShotPaymentTask = new OneShotPaymentTask(success, error);
        mHandler.postDelayed(oneShotPaymentTask, 25);

        try {
            Thread.sleep(30);

            PaymentRequest paymentRequest = new PaymentRequest();
            paymentRequest.TenderType = paymentRequest.ParseTenderType("CREDIT");
            paymentRequest.TransType = paymentRequest.ParseTransType("FORCEAUTH");
            paymentRequest.AuthCode = authCode;
            paymentRequest.ECRRefNum = referenceNumber;
            paymentRequest.Amount = amount; // It is expected that $1.23 will arrive as "123", $0.09 as "9"

            processPayment(paymentRequest);

            mHandler.removeCallbacks(oneShotPaymentTask);
        }
        catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    /**
     * @param amount
     * @param referenceNumber
     * @param successCb
     * @param errorCb
     */
    @ReactMethod
    public void debitSale(String amount, String referenceNumber, Callback successCb, Callback errorCb) {

        if (!validatePOSLink(error)) return;

        success= successCb;
        error = errorCb;

        // Recommend to use single thread pool instead.
        OneShotPaymentTask oneShotPaymentTask = new OneShotPaymentTask(success, error);
        mHandler.postDelayed(oneShotPaymentTask, 25);

        try {
            Thread.sleep(30);

            PaymentRequest paymentRequest = new PaymentRequest();
            paymentRequest.TenderType = paymentRequest.ParseTenderType("DEBIT");
            paymentRequest.TransType = paymentRequest.ParseTransType("SALE");
            paymentRequest.ECRRefNum = referenceNumber;
            paymentRequest.Amount = amount; // It is expected that $1.23 will arrive as "123", $0.09 as "9"

            processPayment(paymentRequest);

            mHandler.removeCallbacks(oneShotPaymentTask);
        }
        catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    /**
     * @param amount
     * @param referenceNumber
     * @param successCb
     * @param errorCb
     */
    @ReactMethod
    public void debitRefund(String amount, String referenceNumber, Callback successCb, Callback errorCb) {

        if (!validatePOSLink(error)) return;

        success= successCb;
        error = errorCb;

        // Recommend to use single thread pool instead.
        OneShotPaymentTask oneShotPaymentTask = new OneShotPaymentTask(success, error);
        mHandler.postDelayed(oneShotPaymentTask, 25);

        try {
            Thread.sleep(30);

            PaymentRequest paymentRequest = new PaymentRequest();
            paymentRequest.TenderType = paymentRequest.ParseTenderType("DEBIT");
            paymentRequest.TransType = paymentRequest.ParseTransType("REFUND");
            paymentRequest.ECRRefNum = referenceNumber;
            paymentRequest.Amount = amount; // It is expected that $1.23 will arrive as "123", $0.09 as "9"

            processPayment(paymentRequest);

            mHandler.removeCallbacks(oneShotPaymentTask);
        }
        catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    /**
     * @param CommType
     * @param Timeout
     * @param IPAddress
     * @param EnableProxy
     * @param successCb
     * @param errorCb
     */
    @ReactMethod
    public void checkPOSLink(String CommType, String Timeout, String IPAddress, boolean EnableProxy, final Callback successCb, final Callback errorCb) {
        // Type - one of USB, TCP

        if (!bInited)
            initPOSLink();

        final String commType = CommType;
        final String timeout = Timeout;
        final String ipAddress = IPAddress;
        final boolean enableProxy = EnableProxy;

        POSLinkCreatorWrapper.createSync(getReactApplicationContext(), new AppThreadPool.FinishInMainThreadCallback<PosLink>() {
            @Override
            public void onFinish(PosLink result) {
                posLink = result;
                String[] TypeList = new String[]{"USB", "TCP"};
                if (!Arrays.asList(TypeList).contains(commType)) {
                    errorCb.invoke("Type not one of TCP or USB");
                    return;
                }

                try {
                    Integer.parseInt(timeout);
                } catch(Exception e) {
                    errorCb.invoke("Timeout is not an integer");
                    return;
                }

                if (!IsIPv4(ipAddress)) {
                    errorCb.invoke("IPAddress is not an IPv4 address");
                    return;
                }

                String iniFile = getReactApplicationContext().getFilesDir().getAbsolutePath() + "/" + SettingINI.FILENAME;
                CommSetting commSetting =SettingINI.getCommSettingFromFile(iniFile);

                commSetting.setType(commType);
                commSetting.setTimeOut(timeout);
                commSetting.setEnableProxy(enableProxy);
                commSetting.setDestIP(ipAddress);
                commSetting.setBaudRate("9600");
                commSetting.setDestPort("10009");
                commSetting.setSerialPort("COM1");
                commSetting.SaveCommSettingFile();

                SettingINI.saveCommSettingToFile(iniFile, commSetting);
                posLink.appDataFolder = getCurrentActivity().getApplicationContext().getFilesDir().getAbsolutePath();
                posLink.SetCommSetting(commSetting);

                successCb.invoke("connected");

            }
        });
    }

    private void initPOSLink() {
        bInited = true;
        AppThreadPool.getInstance();
        commSetting = setupSetting(getReactApplicationContext());
        POSLinkAndroid.init(getReactApplicationContext(), commSetting);
    }

    /**
     * @param successCb
     * @param errorCb
     */
    @ReactMethod
    public void batchClose(Callback successCb, Callback errorCb) {

        if (!validatePOSLink(error)) return;

        success = successCb;
        error = errorCb;

        processBatch();
    }

    private void processBatch() {

        // Recommend to use single thread pool instead.
        new Thread(new Runnable() {
            @Override
            public void run() {

                BatchRequest batchRequest = new BatchRequest();
                batchRequest.TransType = batchRequest.ParseTransType("BATCHCLOSE");
                batchRequest.EDCType = batchRequest.ParseEDCType(EDCType.ALL);

                posLink.BatchRequest = batchRequest;

                // ProcessTrans is Blocking call, will return when the transaction is complete.
                CountRunTime.start("Batch");
                ptr = posLink.ProcessTrans();
                CountRunTime.countPoint("Batch");
                getCurrentActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                    taskCompleted(posLink);
                    }
                });
            }
        }).start();
    }

    private void processPayment(final PaymentRequest request) {

        // Recommend to use single thread pool instead.
        new Thread(new Runnable() {
            @Override
            public void run() {

                posLink.PaymentRequest = request;

                // ProcessTrans is Blocking call, will return when the transaction is complete.
                CountRunTime.start("Payment");
                ptr = posLink.ProcessTrans();
                CountRunTime.countPoint("Payment");
                getCurrentActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                    taskCompleted(posLink);
                    }
                });
            }
        }).start();
    }

    private void processManage(final ManageRequest request) {

        // Recommend to use single thread pool instead.
        new Thread(new Runnable() {
            @Override
            public void run() {

                posLink.ManageRequest = request;

                // ProcessTrans is Blocking call, will return when the transaction is complete.
                CountRunTime.start("Manage");
                ptr = posLink.ProcessTrans();
                CountRunTime.countPoint("Manage");
                getCurrentActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                    taskCompleted(posLink);
                    }
                });
            }
        }).start();
    }

    /**
     * @param errorCb
     * @return
     */
    private boolean validatePOSLink(Callback errorCb) {

        if (!bInited)
            initPOSLink();

        if (posLink == null) {
            Toast.makeText(getReactApplicationContext(), "Cannot initialize POSLink", Toast.LENGTH_LONG);
            errorCb.invoke("Cannot initialize POSLink");
            return false;
        }
        if (posLink.GetCommSetting() == null) {
            Toast.makeText(getReactApplicationContext(), "POSLink missing Communication Settings", Toast.LENGTH_LONG);
            errorCb.invoke("POSLink missing Communication Settings");
            return false;
        }
        return true;
    }


    /**
     * @param successCb
     * @param errorCb
     */
    @ReactMethod
    public void rebootQ20(Callback successCb, Callback errorCb) {

        if (!validatePOSLink(error)) return;

        success = successCb;
        error = errorCb;

        ManageRequest manageRequest = new ManageRequest();
        manageRequest.TransType = manageRequest.ParseTransType("REBOOT");

        processManage(manageRequest);

    }

    /**
     * @param receiptText
     * @param successCb
     * @param errorCb
     */
    @ReactMethod
    public void printReceipt(final String receiptText, final Callback successCb, final Callback errorCb) {

        POSLinkPrinter posLinkPrinter = POSLinkPrinter.getInstance(getReactApplicationContext());

        if (posLinkPrinter == null){
            Toast.makeText(getReactApplicationContext(), "Cannot initialize POSLinkPrinter", Toast.LENGTH_LONG);
            errorCb.invoke("Cannot initialize POSLinkPrinter");
            return;
        }

        posLinkPrinter.setGray(POSLinkPrinter.GreyLevel.DEFAULT);
        posLinkPrinter.setPrintWidth(POSLinkPrinter.RecommendWidth.E500_RECOMMEND_WIDTH);

        POSLinkPrinter.getInstance(getReactApplicationContext()).print(receiptText, POSLinkPrinter.CutMode.PARTIAL_PAPER_CUT,SettingINI.getCommSettingFromFile(getReactApplicationContext().getFilesDir().getAbsolutePath() + "/" + SettingINI.FILENAME),  new POSLinkPrinter.PrintListener() {
            @Override
            public void onSuccess() {
                getCurrentActivity().runOnUiThread(new Runnable() {
                    public void run() {
                        successCb.invoke("Printed");
                        Toast.makeText(getReactApplicationContext(), receiptText, Toast.LENGTH_LONG).show();
                        return;
                    }
                });
            }

            @Override
            public void onError(final ProcessResult processResult) {
                getCurrentActivity().runOnUiThread(new Runnable() {
                    public void run() {
                        errorCb.invoke(processResult.getMessage());
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
                    taskCompleted(posLink);
                    break;
                case Constant.TRANSACTION_TIMEOUT:
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

    class OneShotPaymentTask implements Runnable {
        int lastStatus = -1;
        Callback success;
        Callback error;

        OneShotPaymentTask(Callback successC, Callback errorC) {
            success = successC;
            error = errorC;
        }
        public void run() {
            int status;

            if (posLink != null) {
                try {
                    Thread.sleep(0);

                    status = posLink.GetReportedStatus();
                    if (status != lastStatus) {
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

                        lastStatus = status;
                    }

                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }

            mHandler.postDelayed(this, 500);
        }
    }

//
//    private int lastReportedStatus = -1;
//    final Runnable MyRunnable = new Runnable() {
//
//        public void run() {
//            int status;
//
//            if (posLink != null) {
//                try {
//                    Thread.sleep(0);
//
//                    status = posLink.GetReportedStatus();
//                    if (status != lastReportedStatus) {
//                        switch (status) {
//                            case 0:
//                                Message msg0 = new Message();
//                                msg0.what = Constant.TRANSACTION_STATUS;
//                                msg0.obj = "Ready for CARD INPUT";
//                                mHandler.sendMessage(msg0);
//                                break;
//                            case 1:
//                                Message msg1 = new Message();
//                                msg1.what = Constant.TRANSACTION_STATUS;
//                                msg1.obj = "Ready for PIN ENTRY";
//                                mHandler.sendMessage(msg1);
//                                break;
//                            case 2:
//                                Message msg2 = new Message();
//                                msg2.what = Constant.TRANSACTION_STATUS;
//                                msg2.obj = "Ready for SIGNATURE";
//                                mHandler.sendMessage(msg2);
//                                break;
//                            case 3:
//                                Message msg3 = new Message();
//                                msg3.what = Constant.TRANSACTION_STATUS;
//                                msg3.obj = "Ready for ONLINE PROCESSING";
//                                mHandler.sendMessage(msg3);
//                                break;
//                            case 4:
//                                Message msg4 = new Message();
//                                msg4.what = Constant.TRANSACTION_STATUS;
//                                msg4.obj = "Ready for NEW CARD INPUT";
//                                mHandler.sendMessage(msg4);
//                                break;
//                            default:
//                                break;
//                        }
//
//                        lastReportedStatus = status;
//                    }
//
//                } catch (InterruptedException e) {
//                    e.printStackTrace();
//                }
//            }
//
//            mHandler.postDelayed(this, 500);
//        }
//    };

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
            msg.what = Constant.TRANSACTION_TIMEOUT;
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
                if (msg.obj != null) {
                    if (msg.obj instanceof ManageResponse) {
                        ManageResponse manageResponse = (ManageResponse) msg.obj;
                        Toast.makeText(getReactApplicationContext(), manageResponse.ResultCode + '\n' + manageResponse.ResultTxt, Toast.LENGTH_LONG).show();
                        WritableMap map = Arguments.createMap();
                        map.putString("ResultCode", manageResponse.ResultCode);
                        map.putString("ResultTxt", manageResponse.ResultTxt);
                        success.invoke(map);
                    }
                    else if (msg.obj instanceof PaymentResponse) {
                        PaymentResponse paymentResponse = (PaymentResponse) msg.obj;
                        Toast.makeText(getReactApplicationContext(), paymentResponse.ResultCode + '\n' + paymentResponse.ResultTxt, Toast.LENGTH_LONG).show();
                        WritableMap map = Arguments.createMap();
                        map.putString("ResultCode", paymentResponse.ResultCode);
                        map.putString("ResultTxt", paymentResponse.ResultTxt);
                        map.putString("AuthCode", paymentResponse.AuthCode);
                        map.putString("HostCode", paymentResponse.HostCode);
                        map.putString("HostResponse", paymentResponse.HostResponse);
                        map.putString("Message", paymentResponse.Message);
                        map.putString("Timestamp", paymentResponse.Timestamp);
                        if (paymentResponse.RequestedAmount == "")
                            map.putString("RequestedAmount", "0");
                        else
                            map.putString("RequestedAmount", paymentResponse.RequestedAmount);
                        if (paymentResponse.RemainingBalance == "")
                            map.putString("RemainingBalance", "0");
                        else
                            map.putString("RemainingBalance", paymentResponse.RemainingBalance);
                        map.putString("RawResponse", paymentResponse.RawResponse);
                        map.putString("SigFileName", paymentResponse.SigFileName);
                        map.putString("SignData", paymentResponse.SignData);
                        if (paymentResponse.ExtraBalance == "")
                            map.putString("ExtraBalance", "0");
                        else
                            map.putString("ExtraBalance", paymentResponse.ExtraBalance);
                        map.putString("CvResponse", paymentResponse.CvResponse);
                        map.putString("ExtData", paymentResponse.ExtData);
                        map.putString("BogusAccountNum", paymentResponse.BogusAccountNum);
                        map.putString("RefNum", paymentResponse.RefNum);
                        map.putString("CardType", paymentResponse.CardType);
                        map.putString("AvsResponse", paymentResponse.AvsResponse);
                        if (paymentResponse.ApprovedAmount == "")
                            map.putString("ApprovedAmount", "0");
                        else
                            map.putString("ApprovedAmount", paymentResponse.ApprovedAmount);
                        success.invoke(map);
                    }
                    else if (msg.obj instanceof BatchResponse) {
                        BatchResponse batchResponse = (BatchResponse) msg.obj;
                        Toast.makeText(getReactApplicationContext(), batchResponse.ResultCode + '\n' + batchResponse.ResultTxt, Toast.LENGTH_LONG).show();
                        WritableMap map = Arguments.createMap();
                        map.putString("ResultCode", batchResponse.ResultCode);
                        map.putString("ResultTxt", batchResponse.ResultTxt);
                        map.putString("AuthCode", batchResponse.AuthCode);
                        map.putString("HostCode", batchResponse.HostCode);
                        map.putString("HostResponse", batchResponse.HostResponse);
                        map.putString("Message", batchResponse.Message);
                        map.putString("Timestamp", batchResponse.Timestamp);
                        map.putString("MID", batchResponse.MID);
                        map.putString("BatchFailedRefNum", batchResponse.BatchFailedRefNum);
                        if (batchResponse.BatchFailedCount == "")
                            map.putString("BatchFailedCount", "0");
                        else
                            map.putString("BatchFailedCount", batchResponse.BatchFailedCount);
                        if (batchResponse.DebitCount == "")
                            map.putString("DebitCount", "0");
                        else
                            map.putString("DebitCount", batchResponse.DebitCount);
                        if (batchResponse.DebitAmount == "")
                            map.putString("DebitAmount", "0");
                        else
                            map.putString("DebitAmount", batchResponse.DebitAmount);
                        if (batchResponse.CreditCount == "")
                            map.putString("CreditCount", "0");
                        else
                            map.putString("CreditCount", batchResponse.CreditCount);
                        if (batchResponse.CreditAmount == "")
                            map.putString("CreditAmount", "0");
                        else
                            map.putString("CreditAmount", batchResponse.CreditAmount);
                        if (batchResponse.BatchNum == "")
                            map.putString("BatchNum", "0");
                        else
                            map.putString("BatchNum", batchResponse.BatchNum);
                        map.putString("ExtData", batchResponse.ExtData);
                        map.putString("TID", batchResponse.TID);
                        success.invoke(map);
                    }
                }
                break;
            case Constant.TRANSACTION_TIMEOUT:
            case Constant.TRANSACTION_FAILURE:
                String title = msg.getData().getString(Constant.DIALOG_TITLE);
                String message = msg.getData().getString(Constant.DIALOG_MESSAGE);
                Toast.makeText(getReactApplicationContext(), title +'\n' + message, Toast.LENGTH_LONG).show();
                break;
        }
    }

    private Boolean IsIPv4(String ipaddress) {

        final String IPv4_REGEX =
                "^(25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)\\." +
                        "(25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)\\." +
                        "(25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)\\." +
                        "(25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)$";

        final Pattern IPv4_PATTERN = Pattern.compile(IPv4_REGEX);

        Matcher matcher = IPv4_PATTERN.matcher(ipaddress);

        return matcher.matches();
    }

    private static CommSetting setupSetting(Context context) {
        String settingIniFile = context.getFilesDir().getAbsolutePath() + "/" + SettingINI.FILENAME;
        CommSetting commSetting = SettingINI.getCommSettingFromFile(settingIniFile);

        if (Build.MODEL.startsWith("E")) {
            commSetting.setType(CommSetting.USB);
        } else if (Build.MODEL.startsWith("A9")){
            commSetting.setType(CommSetting.AIDL);
        } else {
            commSetting.setType(CommSetting.TCP);
        }
        commSetting.setTimeOut("60000");
        commSetting.setSerialPort("COM1");
        commSetting.setBaudRate("9600");
        commSetting.setDestIP("172.16.20.15");
        commSetting.setDestPort("10009");
        commSetting.setMacAddr("");
        if (Build.MODEL.startsWith("E")) {
            commSetting.setEnableProxy(true);
        } else {
            commSetting.setEnableProxy(false);
        }
        SettingINI.saveCommSettingToFile(settingIniFile, commSetting);

        if (!SettingINI.loadSettingFromFile(settingIniFile)) {
            //String LogOutputFile = getApplicationContext().getFilesDir().getAbsolutePath() + "/POSLog.txt";
            String LogOutputFile = context.getExternalFilesDir(null).getPath();
            LogSetting.setLogMode(true);
            LogSetting.setLevel(LogSetting.LOGLEVEL.DEBUG);
            LogSetting.setOutputPath(LogOutputFile);
            SettingINI.saveLogSettingToFile(settingIniFile);
        }
        return SettingINI.getCommSettingFromFile(settingIniFile);
    }

}
