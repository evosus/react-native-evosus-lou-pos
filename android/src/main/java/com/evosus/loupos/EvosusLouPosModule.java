package com.evosus.loupos;

import android.widget.Toast;

import com.pax.poslink.ManageRequest;
import com.pax.poslink.PosLink;
import com.pax.poslink.ProcessTransResult;
import com.pax.poslink.peripheries.POSLinkCashDrawer;
import com.pax.poslink.peripheries.POSLinkPrinter;
import com.pax.poslink.peripheries.ProcessResult;

import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.ReactContextBaseJavaModule;
import com.facebook.react.bridge.ReactMethod;
import com.facebook.react.bridge.Callback;

public class EvosusLouPosModule extends ReactContextBaseJavaModule {

    private final ReactApplicationContext reactContext;
    private static boolean initPOSlink;
    private static PosLink posLink = null;

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
     *
     */
    @ReactMethod
    public void initPOSLink() {
        POSLinkCreatorWrapper.createSync(getReactApplicationContext(), new AppThreadPool.FinishInMainThreadCallback<PosLink>() {
            @Override
            public void onFinish(PosLink result) {
                posLink = result;
            }
        });
    }

    /**
     *
     */
    @ReactMethod
    public void rebootQ20() {
        // Recommend to use single thread pool instead.
        new Thread(new Runnable() {
            @Override
            public void run() {
                POSLinkCreatorWrapper.createSync(getReactApplicationContext(), new AppThreadPool.FinishInMainThreadCallback<PosLink>() {
                    @Override
                    public void onFinish(final PosLink poslink) {

                        ManageRequest mgrequest = new ManageRequest();
                        mgrequest.TransType =mgrequest.ParseTransType("REBOOT");

                        // set the folder where to read the "comsetting.ini" file
                        poslink.appDataFolder = getReactApplicationContext().getFilesDir().getAbsolutePath();
                        poslink.ManageRequest = mgrequest;
                        //poslink.debug_mode = 1;
                        poslink.SetCommSetting(SettingINI.getCommSettingFromFile(poslink.appDataFolder + "/" + SettingINI.FILENAME));

                        // ProcessTrans is Blocking call, will return when the transaction is
                        // complete.
                        ProcessTransResult ptr = poslink.ProcessTrans();
                    }
                });
            }
        }).start();
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
}
