package com.evosus.loupos;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;
import android.widget.Toast;

import com.pax.poslink.BatchRequest;
import com.pax.poslink.BatchResponse;
import com.pax.poslink.CommSetting;
import com.pax.poslink.ManageRequest;
import com.pax.poslink.ManageResponse;
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

import java.util.Arrays;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class EvosusLouPosModule extends ReactContextBaseJavaModule {
    private static final String TAG = "EvosusLouPosModule";

    private final ReactApplicationContext reactContext;
    private PosLink posLink = null;
    private static ProcessTransResult ptr;
    private static CommSetting commSetting;

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
     * @param amount
     * @param referenceNumber
     * @param success
     * @param error
     */
    @ReactMethod
    public void creditSale(Integer amount, String referenceNumber, Callback success, Callback error) {

        if (validatePOSLink(error)) return;

        // Recommend to use single thread pool instead.
        mHandler.postDelayed(MyRunnable, 25);

        try {
            Thread.sleep(30);

            PaymentRequest paymentRequest = new PaymentRequest();
            paymentRequest.TenderType = paymentRequest.ParseTenderType("CREDIT");
            paymentRequest.TransType = paymentRequest.ParseTransType("SALE");
            paymentRequest.ECRRefNum = referenceNumber;
            paymentRequest.Amount = amount.toString(); // It is expected that $1.23 will arrive as 123, $0.09 as 9
            posLink.PaymentRequest = paymentRequest;

            // ProcessTrans is Blocking call, will return when the transaction is complete.
            ptr = posLink.ProcessTrans();

            mHandler.removeCallbacks(MyRunnable);
        }
        catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    /**
     * @param amount
     * @param referenceNumber
     * @param success
     * @param error
     */
    @ReactMethod
    public void creditRefund(Integer amount, String referenceNumber, Callback success, Callback error) {

        if (validatePOSLink(error)) return;

        // Recommend to use single thread pool instead.
        mHandler.postDelayed(MyRunnable, 25);

        try {
            Thread.sleep(30);

            PaymentRequest paymentRequest = new PaymentRequest();
            paymentRequest.TenderType = paymentRequest.ParseTenderType("CREDIT");
            paymentRequest.TransType = paymentRequest.ParseTransType("REFUND");
            paymentRequest.ECRRefNum = referenceNumber;
            paymentRequest.Amount = amount.toString(); // It is expected that $1.23 will arrive as 123, $0.09 as 9
            posLink.PaymentRequest = paymentRequest;

            // ProcessTrans is Blocking call, will return when the transaction is complete.
            ptr = posLink.ProcessTrans();

            mHandler.removeCallbacks(MyRunnable);
        }
        catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    /**
     * @param amount
     * @param referenceNumber
     * @param success
     * @param error
     */
    @ReactMethod
    public void creditVoid(Integer amount, String referenceNumber, Callback success, Callback error) {

        if (validatePOSLink(error)) return;

        // Recommend to use single thread pool instead.
        mHandler.postDelayed(MyRunnable, 25);

        try {
            Thread.sleep(30);

            PaymentRequest paymentRequest = new PaymentRequest();
            paymentRequest.TenderType = paymentRequest.ParseTenderType("CREDIT");
            paymentRequest.TransType = paymentRequest.ParseTransType("VOID");
            paymentRequest.ECRRefNum = referenceNumber;
            posLink.PaymentRequest = paymentRequest;

            // ProcessTrans is Blocking call, will return when the transaction is complete.
            ptr = posLink.ProcessTrans();

            mHandler.removeCallbacks(MyRunnable);
        }
        catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    /**
     * @param amount
     * @param referenceNumber
     * @param authCode
     * @param success
     * @param error
     */
    @ReactMethod
    public void creditForceAuth(Integer amount, String referenceNumber, String authCode, Callback success, Callback error) {

        if (validatePOSLink(error)) return;

        // Recommend to use single thread pool instead.
        mHandler.postDelayed(MyRunnable, 25);

        try {
            Thread.sleep(30);

            PaymentRequest paymentRequest = new PaymentRequest();
            paymentRequest.TenderType = paymentRequest.ParseTenderType("CREDIT");
            paymentRequest.TransType = paymentRequest.ParseTransType("FORCEAUTH");
            paymentRequest.AuthCode = authCode;
            paymentRequest.ECRRefNum = referenceNumber;
            paymentRequest.Amount = amount.toString(); // It is expected that $1.23 will arrive as 123, $0.09 as 9
            posLink.PaymentRequest = paymentRequest;

            // ProcessTrans is Blocking call, will return when the transaction is complete.
            ptr = posLink.ProcessTrans();

            mHandler.removeCallbacks(MyRunnable);
        }
        catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    /**
     * @param amount
     * @param referenceNumber
     * @param success
     * @param error
     */
    @ReactMethod
    public void debitSale(Integer amount, String referenceNumber, Callback success, Callback error) {

        if (validatePOSLink(error)) return;

        // Recommend to use single thread pool instead.
        mHandler.postDelayed(MyRunnable, 25);

        try {
            Thread.sleep(30);

            PaymentRequest paymentRequest = new PaymentRequest();
            paymentRequest.TenderType = paymentRequest.ParseTenderType("DEBIT");
            paymentRequest.TransType = paymentRequest.ParseTransType("SALE");
            paymentRequest.ECRRefNum = referenceNumber;
            paymentRequest.Amount = amount.toString(); // It is expected that $1.23 will arrive as 123, $0.09 as 9
            posLink.PaymentRequest = paymentRequest;

            // ProcessTrans is Blocking call, will return when the transaction is complete.
            ptr = posLink.ProcessTrans();

            mHandler.removeCallbacks(MyRunnable);
        }
        catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    /**
     * @param amount
     * @param referenceNumber
     * @param success
     * @param error
     */
    @ReactMethod
    public void debitRefund(Integer amount, String referenceNumber, Callback success, Callback error) {

        if (validatePOSLink(error)) return;

        // Recommend to use single thread pool instead.
        mHandler.postDelayed(MyRunnable, 25);

        try {
            Thread.sleep(30);

            PaymentRequest paymentRequest = new PaymentRequest();
            paymentRequest.TenderType = paymentRequest.ParseTenderType("DEBIT");
            paymentRequest.TransType = paymentRequest.ParseTransType("REFUND");
            paymentRequest.ECRRefNum = referenceNumber;
            paymentRequest.Amount = amount.toString(); // It is expected that $1.23 will arrive as 123, $0.09 as 9
            posLink.PaymentRequest = paymentRequest;

            // ProcessTrans is Blocking call, will return when the transaction is complete.
            ptr = posLink.ProcessTrans();

            mHandler.removeCallbacks(MyRunnable);
        }
        catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    /**
     * @param Type
     * @param Timeout
     * @param IPAddress
     * @param EnableProxy
     * @param success
     * @param error
     */
    public void checkPOSLink(String CommType, String Timeout, String IPAddress, boolean EnableProxy, final Callback success, final Callback error) {
        // Type - one of USB, TCP

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
                    error.invoke("Type not one of TCP or USB");
                    return;
                }

                try {
                    Integer.parseInt(timeout);
                } catch(Exception e) {
                    error.invoke("Timeout is not an integer");
                    return;
                }

                if (!IsIPv4(ipAddress)) {
                    error.invoke("IPAddress is not an IPv4 address");
                    return;
                }

                commSetting = new CommSetting();
                commSetting.setType(commType);
                commSetting.setTimeOut(timeout);
                commSetting.setEnableProxy(enableProxy);
                commSetting.setDestIP(ipAddress);
                commSetting.setBaudRate("9600");
                commSetting.setSerialPort("COM1");
                posLink.SetCommSetting(commSetting);
                success.invoke("connected");
                return;
            }
        });
    }

    private void initPOSLink() {
        POSLinkCreatorWrapper.createSync(getReactApplicationContext(), new AppThreadPool.FinishInMainThreadCallback<PosLink>() {
            @Override
            public void onFinish(PosLink result) {
                posLink = result;
                posLink.SetCommSetting(commSetting);
                return;
            }
        });
    }

    /**
     * @param success
     * @param error
     */
    @ReactMethod
    public void batchClose(Callback success, Callback error) {

        if (validatePOSLink(error)) return;

        // Recommend to use single thread pool instead.
        mHandler.postDelayed(MyRunnable, 25);

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
     * @param error
     * @return
     */
    private boolean validatePOSLink(Callback error) {
        if (posLink == null) {
            Toast.makeText(getReactApplicationContext(), "Cannot initialize POSLink", Toast.LENGTH_LONG);
            error.invoke("Cannot initialize POSLink");
            return true;
        }
        if (posLink.GetCommSetting() == null) {
            Toast.makeText(getReactApplicationContext(), "POSLink missing Communication Settings", Toast.LENGTH_LONG);
            error.invoke("POSLink missing Communication Settings");
            return true;
        }
        return false;
    }


    /**
     * @param success
     * @param error
     */
    @ReactMethod
    public void rebootQ20(Callback success, Callback error) {

        if (validatePOSLink(error)) return;

        // Recommend to use single thread pool instead.
        mHandler.postDelayed(MyRunnable, 25);

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
     * @param success
     * @param error
     */
    @ReactMethod
    public void printReceipt(final String receiptText, final Callback success, final Callback error) {

        POSLinkPrinter posLinkPrinter = POSLinkPrinter.getInstance(getReactApplicationContext());

        if (posLinkPrinter == null){
            Toast.makeText(getReactApplicationContext(), "Cannot initialize POSLinkPrinter", Toast.LENGTH_LONG);
            error.invoke("Cannot initialize POSLinkPrinter");
            return;
        }

        posLinkPrinter.setGray(POSLinkPrinter.GreyLevel.DEFAULT);
        posLinkPrinter.setPrintWidth(POSLinkPrinter.RecommendWidth.E500_RECOMMEND_WIDTH);

        POSLinkPrinter.getInstance(getReactApplicationContext()).print(receiptText, POSLinkPrinter.CutMode.PARTIAL_PAPER_CUT,SettingINI.getCommSettingFromFile(getReactApplicationContext().getFilesDir().getAbsolutePath() + "/" + SettingINI.FILENAME),  new POSLinkPrinter.PrintListener() {
            @Override
            public void onSuccess() {
                getCurrentActivity().runOnUiThread(new Runnable() {
                    public void run() {
                        success.invoke("Printed");
                        Toast.makeText(getReactApplicationContext(), receiptText, Toast.LENGTH_LONG).show();
                        return;
                    }
                });
            }

            @Override
            public void onError(final ProcessResult processResult) {
                getCurrentActivity().runOnUiThread(new Runnable() {
                    public void run() {
                        error.invoke(processResult.getMessage());
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
//
//    void Foo(Callback successCB, Callback errorCB) {
//        class OneShotTask implements Runnable {
//            Callback success;
//            Callback error;
//            OneShotTask(Callback successC, Callback errorC) {
//                success = successC;
//                error = errorC;
//            }
//            public void run() {
//                someFunc(str);
//            }
//        }
//        Thread t = new Thread(new OneShotTask(successCB, errorCB));
//        t.start();
//    }

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
                    if (msg.obj instanceof ManageResponse)
                        Toast.makeText(getReactApplicationContext(), ((ManageResponse)msg.obj).ResultCode +'\n' + ((ManageResponse)msg.obj).ResultTxt, Toast.LENGTH_LONG).show();
                    else if (msg.obj instanceof PaymentResponse)
                        Toast.makeText(getReactApplicationContext(), ((PaymentResponse)msg.obj).ResultCode +'\n' + ((ManageResponse)msg.obj).ResultTxt, Toast.LENGTH_LONG).show();
                    else if (msg.obj instanceof BatchResponse)
                        Toast.makeText(getReactApplicationContext(), ((BatchResponse)msg.obj).ResultCode +'\n' + ((ManageResponse)msg.obj).ResultTxt, Toast.LENGTH_LONG).show();
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

}
