package com.evosus.loupos;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.net.Uri;
import android.nfc.Tag;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.IBinder;
import android.os.Message;
import android.provider.Settings;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.bugsnag.android.BreadcrumbType;
import com.bugsnag.android.Bugsnag;
import com.evosus.loupos.models.CustomerDisplay;
import com.evosus.loupos.models.EvosusCompany;
import com.evosus.loupos.models.LOUAPIJWT;
import com.evosus.loupos.models.POSStationSession;
import com.evosus.loupos.models.POS_LineItem_NPE;
import com.evosus.loupos.models.SKU;
import com.evosus.loupos.models.SKUKitLine;
import com.evosus.loupos.models.SessionInfo;
import com.evosus.loupos.models.TSYSMerchant;
import com.evosus.loupos.models.POS_Transaction_NPE;
import com.facebook.react.bridge.ActivityEventListener;
import com.facebook.react.bridge.Arguments;
import com.facebook.react.bridge.LifecycleEventListener;
import com.facebook.react.bridge.Promise;
import com.facebook.react.bridge.WritableMap;
//import com.google.gson.Gson;
import com.facebook.react.module.annotations.ReactModule;
import com.google.gson.Gson;
import com.pax.poslink.BatchRequest;
import com.pax.poslink.BatchResponse;
import com.pax.poslink.CommSetting;
import com.pax.poslink.LogSetting;
import com.pax.poslink.ManageRequest;
import com.pax.poslink.ManageResponse;
import com.pax.poslink.POSLinkAndroid;
import com.pax.poslink.PaymentRequest;
//import com.pax.poslink.PaymentReqfuest.CommercialCard;
import com.pax.poslink.PaymentResponse;
import com.pax.poslink.PosLink;
import com.pax.poslink.ProcessTransResult;
import com.pax.poslink.ReportRequest;
import com.pax.poslink.ReportResponse;
import com.pax.poslink.connection.INormalConnection;
import com.pax.poslink.constant.EDCType;
import com.pax.poslink.peripheries.POSLinkCashDrawer;
import com.pax.poslink.peripheries.POSLinkPrinter;
import com.pax.poslink.peripheries.ProcessResult;

import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.ReactContextBaseJavaModule;
import com.facebook.react.bridge.ReactMethod;
import com.pax.poslink.poslink.POSLinkCreator;
import com.pax.poslink.util.CountRunTime;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.lang.ref.WeakReference;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import io.realm.Case;
import io.realm.DynamicRealm;
import io.realm.FieldAttribute;
import io.realm.Realm;
import io.realm.RealmConfiguration;
import io.realm.RealmList;
import io.realm.RealmMigration;
import io.realm.RealmObjectSchema;
import io.realm.RealmResults;
import io.realm.RealmSchema;

@ReactModule(name = EvosusLouPosModule.NAME)
public class EvosusLouPosModule extends ReactContextBaseJavaModule implements ActivityEventListener, LifecycleEventListener, ServiceConnection {

    public static final String NAME = "EvosusLouPos";

    private static final String CODE_ERROR = "CODE_ERROR";
    private final ReactApplicationContext reactContext;

    //    private PosLink posLink = null;
    private static CommSetting commSetting;
    private static Boolean bInited = false;
    private static String currentPageName = "";

    // This is used to communicate with the CustomerDisplay Service
    private CustomerDisplayService customerDisplayService;

    public void onNewIntent(Intent intent) {};

    public EvosusLouPosModule(ReactApplicationContext reactContext) {
        super(reactContext);
        this.reactContext = reactContext;

        // Add the listener for `onActivityResult`
        reactContext.addActivityEventListener(this);
        reactContext.addLifecycleEventListener(this);

        //
//        if (customerFacingManager != null) {
//            customerFacingView = customerFacingManager.getCustomerFacingView();
//        }

    }

    public EvosusLouPosModule(ReactApplicationContext reactContext, CustomerFacingManager customerFacingManager) {
        super(reactContext);
        this.reactContext = reactContext;

        // Add the listener for `onActivityResult`
        reactContext.addActivityEventListener(this);

//        if (customerFacingManager != null) {
//            customerFacingView = customerFacingManager.getCustomerFacingView();
//        }
    }

    @Override
    public void onActivityResult(Activity activity, int requestCode, int resultCode, Intent data) {
        getReactApplicationContext().onActivityResult(activity, requestCode, resultCode, data);
        if (requestCode == 1234) {
            getReactApplicationContext().startActivityForResult(data, 1234, null);
        }
    }

    @Override
    public void onHostResume() {
        // Activity `onResume`
        Intent intent= new Intent(getCurrentActivity(), CustomerDisplayService.class);
        getReactApplicationContext().bindService(intent, this, Context.BIND_AUTO_CREATE);
    }

    @Override
    public void onHostPause() {
        // Activity `onPause`
        getReactApplicationContext().unbindService(this);
    }

    @Override
    public void onHostDestroy() {
        // Activity `onDestroy`
    }

    @Override
    public void onServiceConnected(ComponentName name, IBinder service) {
        CustomerDisplayService.MyBinder b = (CustomerDisplayService.MyBinder) service;
        customerDisplayService = b.getService();
//        Toast.makeText(getCurrentActivity(), "Connected to Customer Display", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onServiceDisconnected(ComponentName name) {
        customerDisplayService = null;
    }

//    @ReactMethod
//    public void mySuperDuperFunction(Promise promise) {
//        if (customerFacingView != null) {
//            customerFacingView.mySuperDuperFunction(promise); // <-- Magic
//        }
//    }

    @Override
    public String getName() {
        return "EvosusLouPos";
    }

    /**
     *
     */
    @ReactMethod
    public void startCustomerDisplay() {
        Bugsnag.leaveBreadcrumb("startCustomerDisplay");
        if (Build.VERSION.SDK_INT >= 23) {
            if (!Settings.canDrawOverlays(getReactApplicationContext())) {
                Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION, Uri.parse("package:" + getReactApplicationContext().getPackageName()));

                if (getCurrentActivity() != null)
                    getCurrentActivity().startActivityForResult(intent,1234, null );
            }
            if (getCurrentActivity() != null)
                getCurrentActivity().startService(new Intent(reactContext, CustomerDisplayService.class));

        } else {
            if (getCurrentActivity() != null)
                getCurrentActivity().startService(new Intent(reactContext, CustomerDisplayService.class));
        }
    }

    /**
     *
     */
    @ReactMethod
    public void stopCustomerDisplay() {
        sendBugsnagBreadcrumb("stopCustomerDisplay");
        if (getCurrentActivity() != null)
            getCurrentActivity().stopService(new Intent(reactContext, CustomerDisplayService.class));
    }

    /**
     *
     */
    @ReactMethod
    public void setCustomerDisplayTrxMD(final String trxMarkdown) {
        sendBugsnagBreadcrumb("setCustomerDisplayTrxMD", new HashMap<String, Object>() {{
            put("trxMarkdown", trxMarkdown);
        }});
        // This is used to communicate with the CustomerDisplay Service
        customerDisplayService.setCustomerDisplayTrxMD(trxMarkdown);
    }

    /**
     * @param promise
     */
    @ReactMethod
    public void getInitInfo(Promise promise)
    {
//        ReactApplicationContext context = getReactApplicationContext();
//        WebPresentationFragment webFrag = new WebPresentationFragment();
//        webFrag.
//        Intent intent = new Intent(context, MainActivity.class);
//        context.startActivity(intent,);

        if (!validatePOSLink(promise)) return;

        ManageRequest manageRequest = new ManageRequest();
        manageRequest.TransType = manageRequest.ParseTransType("INIT");

        OneShotManageTask oneShotManageTask = new OneShotManageTask(promise, manageRequest, "INIT", getPOSLink(), getCurrentActivity());
        oneShotManageTask.execute();

    }

    /**
     * @param pageName
     */
    @ReactMethod
    public void setCurrentPageName(String pageName) {

        currentPageName = pageName;
    }

    private String readTextFile(InputStream inputStream) {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

        byte buf[] = new byte[1024];
        int len;
        try {
            while ((len = inputStream.read(buf)) != -1) {
                outputStream.write(buf, 0, len);
            }
            outputStream.close();
            inputStream.close();
        } catch (IOException e) {

        }
        return outputStream.toString();
    }
    /**
     */
    @ReactMethod
    public void getRuntimeUrl(Promise promise) {

        InputStream inputStream = getReactApplicationContext().getResources().openRawResource(
                getReactApplicationContext().getResources().getIdentifier("runtime_url",
                        "raw", getReactApplicationContext().getPackageName()));

        String runtime_url = readTextFile(inputStream);
        promise.resolve(runtime_url.trim());
    }

    /**
     * @param message
     */
    @ReactMethod
    public void showToast(String message) {
        Toast.makeText(getReactApplicationContext(), message, Toast.LENGTH_LONG).show();
    }
    /**
     */
    @ReactMethod
    public String getCurrentPageName() {
        return currentPageName;
    }


    /**
     *
     */
    @ReactMethod
    public void openCashDrawer() {
        sendBugsnagBreadcrumb("openCashDrawer");
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
     * @param promise
     */
    @ReactMethod
    public void getLastTransaction(Promise promise)
    {
        sendBugsnagBreadcrumb("getLastTransaction");
        if (!validatePOSLink(promise)) return;

        ReportRequest reportRequest = new ReportRequest();
        reportRequest.TransType = reportRequest.ParseTransType("LOCALDETAILREPORT");
        reportRequest.EDCType = reportRequest.ParseEDCType("ALL");
        reportRequest.LastTransaction = "1";

        OneShotReportTask oneShotReportTask = new OneShotReportTask(promise, reportRequest, "LOCALDETAILREPORT", getPOSLink(), getCurrentActivity());
        oneShotReportTask.execute();

    }

    /**
     * @param amount
     * @param referenceNumber
     * @param poNum
     * @param taxAmt
     * @param tipAmt
     * @param extData
     * @param promise
     */
    @ReactMethod
    public void creditSale(final String amount, final String referenceNumber, final String poNum, final String taxAmt, final String tipAmt, final String extData, Promise promise) {
        sendBugsnagBreadcrumb("creditSale", new HashMap<String, Object>(){{
            put("amount", amount);
            put("referenceNumber", referenceNumber);
            put("poNum", poNum);
            put("taxAmt", taxAmt);
            put("tipAmt", tipAmt);
            put("extData", extData);
        }});

        if (!validatePOSLink(promise)) return;

        PaymentRequest paymentRequest = new PaymentRequest();
        paymentRequest.TenderType = paymentRequest.ParseTenderType("CREDIT");
        paymentRequest.TransType = paymentRequest.ParseTransType("SALE");
        paymentRequest.ECRRefNum = referenceNumber;
        paymentRequest.Amount = amount; // It is expected that $1.23 will arrive as "123", $0.09 as "9"
        paymentRequest.PONum = poNum;
        paymentRequest.TaxAmt = taxAmt;
        paymentRequest.TipAmt = tipAmt;
        paymentRequest.ExtData = extData;

        // Recommend to use single thread pool instead.
        OneShotPaymentTask2 oneShotPaymentTask = new OneShotPaymentTask2(promise, paymentRequest, "SALE", getPOSLink(), getCurrentActivity());
        oneShotPaymentTask.execute();
    }

    /**
     * @param amount
     * @param referenceNumber
     * @param poNum
     * @param extData
     * @param promise
     */
    @ReactMethod
    public void creditAuth(final String amount, final String referenceNumber, final String poNum, final String extData, Promise promise) {
        sendBugsnagBreadcrumb("creditAuth", new HashMap<String, Object>(){{
            put("amount", amount);
            put("referenceNumber", referenceNumber);
            put("poNum", poNum);
            put("extData", extData);
        }});
        if (!validatePOSLink(promise)) return;

        PaymentRequest paymentRequest = new PaymentRequest();
        paymentRequest.TenderType = paymentRequest.ParseTenderType("CREDIT");
        paymentRequest.TransType = paymentRequest.ParseTransType("AUTH");
        paymentRequest.ECRRefNum = referenceNumber;
        paymentRequest.Amount = amount; // It is expected that $1.23 will arrive as "123", $0.09 as "9"
        paymentRequest.PONum = poNum;
        paymentRequest.ExtData = extData;

        // Recommend to use single thread pool instead.
        OneShotPaymentTask2 oneShotPaymentTask = new OneShotPaymentTask2(promise, paymentRequest, "AUTH", getPOSLink(), getCurrentActivity());
        oneShotPaymentTask.execute();
    }

    /**
     * @param amount
     * @param referenceNumber
     * @param promise
     */
    @ReactMethod
    public void creditSaleEBT(final String amount, final String referenceNumber, Promise promise) {
        sendBugsnagBreadcrumb("creditSaleEBT", new HashMap<String, Object>(){{
            put("amount", amount);
            put("referenceNumber", referenceNumber);
        }});
        if (!validatePOSLink(promise)) return;

        PaymentRequest paymentRequest = new PaymentRequest();
        paymentRequest.TenderType = paymentRequest.ParseTenderType("EBT");
        paymentRequest.TransType = paymentRequest.ParseTransType("SALE");
        paymentRequest.ECRRefNum = referenceNumber;
        paymentRequest.Amount = amount; // It is expected that $1.23 will arrive as "123", $0.09 as "9"

        // Recommend to use single thread pool instead.
        OneShotPaymentTask2 oneShotPaymentTask = new OneShotPaymentTask2(promise, paymentRequest, "SALE", getPOSLink(), getCurrentActivity());
        oneShotPaymentTask.execute();

    }

    /**
     * @param amount
     * @param referenceNumber
     * @param promise
     */
    @ReactMethod
    public void creditRefund(final String amount, final String referenceNumber, Promise promise) {
        sendBugsnagBreadcrumb("creditRefund", new HashMap<String, Object>(){{
            put("amount", amount);
            put("referenceNumber", referenceNumber);
        }});
        if (!validatePOSLink(promise)) return;

        PaymentRequest paymentRequest = new PaymentRequest();
        paymentRequest.TenderType = paymentRequest.ParseTenderType("CREDIT");
        paymentRequest.TransType = paymentRequest.ParseTransType("RETURN");
        paymentRequest.ECRRefNum = referenceNumber;
        paymentRequest.Amount = amount; // It is expected that $1.23 will arrive as "123", $0.09 as "9"

        // Recommend to use single thread pool instead.
        OneShotPaymentTask2 oneShotPaymentTask = new OneShotPaymentTask2(promise, paymentRequest, "RETURN", getPOSLink(), getCurrentActivity());
        oneShotPaymentTask.execute();

    }

    /**
     * @param amount
     * @param referenceNumber
     * @param origRefNum
     * @param origECRRefNum
     * @param promise
     */
    @ReactMethod
    public void creditVoid(final String amount, final String referenceNumber, final String origRefNum, final String origECRRefNum, Promise promise) {
        sendBugsnagBreadcrumb("creditVoid", new HashMap<String, Object>(){{
            put("amount", amount);
            put("referenceNumber", referenceNumber);
            put("origRefNum", origRefNum);
            put("origECRRefNum", origECRRefNum);
        }});
        if (!validatePOSLink(promise)) return;

        PaymentRequest paymentRequest = new PaymentRequest();
        paymentRequest.TenderType = paymentRequest.ParseTenderType("CREDIT");
        paymentRequest.TransType = paymentRequest.ParseTransType("VOID");
        paymentRequest.ECRRefNum = referenceNumber;
        paymentRequest.OrigRefNum = origRefNum;
        paymentRequest.OrigECRRefNum = origECRRefNum;

        // Recommend to use single thread pool instead.
        OneShotPaymentTask2 oneShotPaymentTask = new OneShotPaymentTask2(promise, paymentRequest, "VOID", getPOSLink(), getCurrentActivity());
        oneShotPaymentTask.execute();

    }

    /**
     * @param amount
     * @param referenceNumber
     * @param authCode
     * @param poNum
     * @param taxAmt
     * @param extData
     * @param promise
     */
    @ReactMethod
    public void creditForceAuth(final String amount, final String referenceNumber, final String authCode, final String poNum, final String taxAmt, final String extData, Promise promise) {
        sendBugsnagBreadcrumb("creditForceAuth", new HashMap<String, Object>(){{
            put("amount", amount);
            put("referenceNumber", referenceNumber);
            put("authCode", authCode);
            put("poNum", poNum);
            put("poNum", taxAmt);
            put("poNum", extData);
        }});
        if (!validatePOSLink(promise)) return;

        PaymentRequest paymentRequest = new PaymentRequest();
        paymentRequest.TenderType = paymentRequest.ParseTenderType("CREDIT");
        paymentRequest.TransType = paymentRequest.ParseTransType("FORCEAUTH");
        paymentRequest.AuthCode = authCode;
        paymentRequest.ECRRefNum = referenceNumber;
        paymentRequest.Amount = amount; // It is expected that $1.23 will arrive as "123", $0.09 as "9"
        paymentRequest.PONum = poNum;
        paymentRequest.TaxAmt = taxAmt;
        paymentRequest.ExtData = extData;

        // Recommend to use single thread pool instead.
        OneShotPaymentTask2 oneShotPaymentTask = new OneShotPaymentTask2(promise, paymentRequest, "FORCEAUTH", getPOSLink(), getCurrentActivity());
        oneShotPaymentTask.execute();

    }

    /**
     * @param amount
     * @param referenceNumber
     * @param poNum
     * @param taxAmt
     * @param tipAmt
     * @param extData
     * @param promise
     */
    @ReactMethod
    public void debitSale(final String amount, final String referenceNumber, final String poNum, final String taxAmt, final String tipAmt, final String extData, Promise promise) {
        sendBugsnagBreadcrumb("debitSale", new HashMap<String, Object>(){{
            put("amount", amount);
            put("referenceNumber", referenceNumber);
            put("poNum", poNum);
            put("poNum", taxAmt);
            put("tipAmt", tipAmt);
            put("poNum", extData);
        }});

        if (!validatePOSLink(promise)) return;

        PaymentRequest paymentRequest = new PaymentRequest();
        paymentRequest.TenderType = paymentRequest.ParseTenderType("DEBIT");
        paymentRequest.TransType = paymentRequest.ParseTransType("SALE");
        paymentRequest.ECRRefNum = referenceNumber;
        paymentRequest.Amount = amount; // It is expected that $1.23 will arrive as "123", $0.09 as "9"
        paymentRequest.PONum = poNum;
        paymentRequest.TipAmt = tipAmt;
        // Got an error on TaxAmt during testing - dropping it out
//            paymentRequest.TaxAmt = taxAmt;
        paymentRequest.ExtData = extData;

        // Recommend to use single thread pool instead.
        OneShotPaymentTask2 oneShotPaymentTask = new OneShotPaymentTask2(promise, paymentRequest, "SALE", getPOSLink(), getCurrentActivity());
        oneShotPaymentTask.execute();

    }

    /**
     * @param amount
     * @param referenceNumber
     * @param promise
     */
    @ReactMethod
    public void debitRefund(final String amount, final String referenceNumber, Promise promise) {
        sendBugsnagBreadcrumb("debitRefund", new HashMap<String, Object>(){{
            put("amount", amount);
            put("referenceNumber", referenceNumber);
        }});

        if (!validatePOSLink(promise)) return;

        PaymentRequest paymentRequest = new PaymentRequest();
        paymentRequest.TenderType = paymentRequest.ParseTenderType("DEBIT");
        paymentRequest.TransType = paymentRequest.ParseTransType("RETURN");
        paymentRequest.ECRRefNum = referenceNumber;
        paymentRequest.Amount = amount; // It is expected that $1.23 will arrive as "123", $0.09 as "9"

        // Recommend to use single thread pool instead.
        OneShotPaymentTask2 oneShotPaymentTask = new OneShotPaymentTask2(promise, paymentRequest, "RETURN", getPOSLink(), getCurrentActivity());
        oneShotPaymentTask.execute();

    }

    /**
     * @param CommType
     * @param Timeout
     * @param IPAddress
     * @param EnableProxy
     * @param promise
     */
    @ReactMethod
    public void checkPOSLink(final String CommType, final String Timeout, final String IPAddress, final boolean EnableProxy, final Promise promise) {
        sendBugsnagBreadcrumb("checkPOSLink", new HashMap<String, Object>(){{
            put("CommType", CommType);
            put("Timeout", Timeout);
            put("IPAddress", IPAddress);
            put("EnableProxy", EnableProxy);
        }});

        // Type - one of USB, TCP, AIDL
        // AIDL not implemented yet
        if (!bInited)
            initPOSLink();

        final String commType = CommType.toUpperCase();
        final String timeout = Timeout;
        final String ipAddress = IPAddress;
        final boolean enableProxy = EnableProxy;

        String[] TypeList = new String[]{"USB", "TCP", "AIDL"};
        if (!Arrays.asList(TypeList).contains(commType)) {
            promise.reject(CODE_ERROR, "Type not one of TCP, USB, or AIDL");
            return;
        }

        try {
            Integer.parseInt(timeout);
        } catch(Exception e) {
            promise.reject(CODE_ERROR, "Timeout is not an integer");
            return;
        }

        if (commType.toUpperCase() == "USB" && !IsIPv4(ipAddress)) {
            promise.reject(CODE_ERROR, "IPAddress is not an IPv4 address");
            return;
        }

        String iniFile = getReactApplicationContext().getFilesDir().getAbsolutePath() + "/" + SettingINI.FILENAME;
        commSetting = SettingINI.getCommSettingFromFile(iniFile);

        commSetting.setType(commType);
        commSetting.setTimeOut(timeout);
        commSetting.setEnableProxy(enableProxy);
        commSetting.setDestIP(ipAddress);
        commSetting.setBaudRate("9600");
        commSetting.setDestPort("10009");
        commSetting.setSerialPort("COM1");

        SettingINI.saveCommSettingToFile(iniFile, commSetting);

        POSLinkCreatorWrapper.createSync(getReactApplicationContext(), new AppThreadPool.FinishInMainThreadCallback<PosLink>() {
            @Override
            public void onFinish(PosLink result, String errMsg) {
                PosLink posLink = result;
                posLink.appDataFolder = getCurrentActivity().getApplicationContext().getFilesDir().getAbsolutePath();
                posLink.SetCommSetting(commSetting);

                if (errMsg != "") {
                    promise.reject(CODE_ERROR, errMsg);
                }
                else {
                    promise.resolve("connected");
                }
            }
        });
    }

    private PosLink getPOSLink() {
        sendBugsnagBreadcrumb("getPOSLink");

        PosLink posLink1 = POSLinkCreator.createPoslink(getReactApplicationContext());

        String iniFile = getReactApplicationContext().getFilesDir().getAbsolutePath() + "/" + SettingINI.FILENAME;
        commSetting = SettingINI.getCommSettingFromFile(iniFile);
        posLink1.SetCommSetting(commSetting);

        return posLink1;
    }

    private void initPOSLink() {
        sendBugsnagBreadcrumb("initPOSLink");
        bInited = true;
        AppThreadPool.getInstance();
        commSetting = setupSetting(getReactApplicationContext());
        POSLinkAndroid.init(getReactApplicationContext(), commSetting);
    }

    private static Realm getRealmConfiguration() {
        Bugsnag.leaveBreadcrumb("getRealmConfiguration");
        // The RealmConfiguration is created using the builder pattern.
        // The Realm file will be located in Context.getFilesDir() with name "myrealm.realm"
        RealmConfiguration config = new RealmConfiguration.Builder()
                .name("evosus.db")
                .schemaVersion(51)
                .migration(new MyMigration())
                .build();
        // Use the config
        //Realm realm = Realm.getInstance(config);
        return  Realm.getInstance(config);
    }

    /**
     * @param promise
     */
    @ReactMethod
    public void batchClose(Promise promise) {
        sendBugsnagBreadcrumb("batchClose");

        if (!validatePOSLink(promise)) return;

        BatchRequest batchRequest = new BatchRequest();
        batchRequest.TransType = batchRequest.ParseTransType("BATCHCLOSE");
        batchRequest.EDCType = batchRequest.ParseEDCType(EDCType.ALL);

        // Recommend to use single thread pool instead.
        OneShotBatchTask oneShotBatchTask = new OneShotBatchTask(promise, batchRequest, "BATCHCLOSE", getPOSLink(), getCurrentActivity());
        oneShotBatchTask.execute();

    }

    /**
     * @param entityName
     * @param jsonString
     * @param promise
     */
    @ReactMethod
    public void loadRealmFromJSON(final String entityName, String jsonString, Promise promise) {
        String updatedJsonString;
        final String finalJsonString = jsonString;
        sendBugsnagBreadcrumb("loadRealmFromJSON", new HashMap<String, Object>(){{
            put("entityName", entityName);
            put("jsonString", finalJsonString);
        }});
        Log.d(this.getName(), entityName);
        if (jsonString.startsWith("{") && jsonString.endsWith("}")) {
            updatedJsonString = '[' + jsonString + ']';
        } else {
            updatedJsonString = jsonString;
        }
        Realm realm = getRealmConfiguration();
        if (realm == null) {
            promise.resolve(false);
        } else {
            switch (entityName) {
                case "ProductSetup.CustomerDisplay":
                    realm.beginTransaction();
                    realm.createOrUpdateAllFromJson(CustomerDisplay.class, updatedJsonString);
                    realm.commitTransaction();
                    break;
                case "Inventory.SKU":
                    realm.beginTransaction();
                    realm.createOrUpdateAllFromJson(SKU.class, updatedJsonString);
                    realm.commitTransaction();
                    break;
                case "LOUAPI.LOUAPIJWT":
                    realm.beginTransaction();
                    realm.createOrUpdateAllFromJson(LOUAPIJWT.class, updatedJsonString);
                    realm.commitTransaction();
                    break;
                case "TSYS.TSYSMerchant":
                    realm.beginTransaction();
                    realm.createOrUpdateAllFromJson(TSYSMerchant.class, updatedJsonString);
                    realm.commitTransaction();
                    break;
                case "AdminConsole.EvosusCompany":
                    realm.beginTransaction();
                    realm.createOrUpdateAllFromJson(EvosusCompany.class, updatedJsonString);
                    realm.commitTransaction();
                    break;
                case "POS.POS_Transaction":
                case "POS.POS_Transaction_NPE":
                    realm.beginTransaction();
                    realm.createOrUpdateAllFromJson(POS_Transaction_NPE.class, updatedJsonString);
                    realm.commitTransaction();
                    break;
                case "POS.POS_LineItem":
                case "POS.POS_LineItem_NPE":
                    realm.beginTransaction();
                    realm.createOrUpdateAllFromJson(POS_LineItem_NPE.class, updatedJsonString);
                    realm.commitTransaction();
                    break;
                case "Inventory.SKUKitLine":
                    realm.beginTransaction();
                    realm.createOrUpdateAllFromJson(SKUKitLine.class, updatedJsonString);
                    realm.commitTransaction();
                    break;
                case "POS.SessionInfo":
                    realm.beginTransaction();
                    realm.createOrUpdateAllFromJson(SessionInfo.class, updatedJsonString);
                    realm.commitTransaction();
                    break;
                case "Accounting.POSStationSession":
                    realm.beginTransaction();
                    realm.createOrUpdateAllFromJson(POSStationSession.class, updatedJsonString);
                    realm.commitTransaction();
                    break;
            }
        }
        // This is important
        realm.close();
        promise.resolve(true);
    }

    /**
     * @param entityName
     * @param promise
     */
    @ReactMethod
    public void findFirstRealmEntityByID(final String entityName, final String entityID, final Boolean UseSKUID, final String EvosusCompanySN, Promise promise) {
        sendBugsnagBreadcrumb("findFirstRealmEntityByID", new HashMap<String, Object>(){{
            put("entityName", entityName);
            put("entityID", entityID);
            put("UseSKUID", UseSKUID);
            put("EvosusCompanySN", EvosusCompanySN);
        }});

        Realm realm = null;
        try {
            realm = getRealmConfiguration();
        } catch (Error | RuntimeException e) {
            promise.reject(e);
        }

        if (realm == null) {
            promise.resolve(false);
        } else {
            Log.d(this.getName(), "findFirstRealmEntityByID " + entityName + ": " + entityID);
            switch (entityName) {
                case "ProductSetup.CustomerDisplay":
                    final CustomerDisplay customerDisplay = realm.where(CustomerDisplay.class)
                            .equalTo("EvosusCompanySN", EvosusCompanySN)
                            .equalTo("CustomerVanityID", entityID)
                            .findFirst();
                    if (customerDisplay != null) {
                        promise.resolve(new Gson().toJson(realm.copyFromRealm(customerDisplay)));
                        Log.d(this.getName(), "Lookup on CustomerVanityID for " + entityName);
                    } else {
                        promise.reject("CustomerDisplay not found.");
                    }
                    break;
                case "Inventory.SKU":
                    Log.d(this.getName(), "UseSKUID = " + UseSKUID);
                    final SKU sku = UseSKUID ? realm.where(SKU.class)
                            .equalTo("EvosusCompanySN", EvosusCompanySN)
                            .equalTo("ReadableID", entityID)
                            .findFirst() :
                            realm.where(SKU.class)
                                    .equalTo("EvosusCompanySN", EvosusCompanySN)
                                    .equalTo("UPC", entityID)
                                    .or()
                                    .equalTo("MySKU", entityID)
                                    .findFirst();
                    if (sku != null) {
                        promise.resolve(new Gson().toJson(realm.copyFromRealm(sku)));
                        Log.d(this.getName(), "Lookup on ReadableID for " + entityName);
                    } else {
                        promise.reject("SKU not found.");
                    }
                    break;
                case "LOUAPI.LOUAPIJWT":
                    final LOUAPIJWT louapijwt = realm.where(LOUAPIJWT.class).equalTo("Key", entityID).findFirst();
                    promise.resolve(new Gson().toJson(realm.copyFromRealm(louapijwt)));
                    Log.d(this.getName(), "Lookup on Key for " + entityName);
                    break;
                case "TSYS.TSYSMerchant":
                    final TSYSMerchant tsysMerchant = realm.where(TSYSMerchant.class).equalTo("_ID", entityID).findFirst();
                    promise.resolve(new Gson().toJson(realm.copyFromRealm(tsysMerchant)));
                    Log.d(this.getName(), "Lookup on _ID for " + entityName);
                    break;
                case "AdminConsole.EvosusCompany":
                    try {
                        final EvosusCompany evosusCompany = realm.where(EvosusCompany.class).equalTo("SerialNumber", entityID).findFirst();
                        if (evosusCompany != null) {
                            promise.resolve(new Gson().toJson(realm.copyFromRealm(evosusCompany)));
                            Log.d(this.getName(), "Lookup on SerialNumber for " + entityName);
                        } else {
                            promise.resolve("");
                        }
                    } catch (Error e) {
                        Log.e(this.getName(), e.getMessage());
                    }
                    break;
                case "POS.POS_Transaction_NPE":
                    final POS_Transaction_NPE pos_transaction_npe = realm.where(POS_Transaction_NPE.class)
                            .equalTo("EvosusCompanySN", EvosusCompanySN)
                            .equalTo("ID_", entityID)
                            .findFirst();
                    if (pos_transaction_npe != null) {
                        promise.resolve(new Gson().toJson(realm.copyFromRealm(pos_transaction_npe)));
                        Log.d(this.getName(), "Lookup on POS_Transaction_NPE _ID for " + entityName);
                    } else {
                        promise.reject("POS_Transaction_NPE not found.");
                    }
                    break;
                case "POS.POS_LineItem_NPE":
                    final POS_LineItem_NPE pos_lineitem_npe = realm.where(POS_LineItem_NPE.class)
                            .equalTo("EvosusCompanySN", EvosusCompanySN)
                            .equalTo("ID_", entityID)
                            .findFirst();
                    if (pos_lineitem_npe != null) {
                        promise.resolve(new Gson().toJson(realm.copyFromRealm(pos_lineitem_npe)));
                        Log.d(this.getName(), "Lookup on POS_LineItem_NPE _ID for " + entityName);
                    } else {
                        promise.reject("POS_LineItem_NPE not found.");
                    }
                    break;
                case "Inventory.SKUKitLine":
                    final SKUKitLine skuKitLine = realm.where(SKUKitLine.class)
                            .equalTo("EvosusCompanySN", EvosusCompanySN)
                            .equalTo("ID_", entityID)
                            .findFirst();
                    if (skuKitLine != null) {
                        promise.resolve(new Gson().toJson(realm.copyFromRealm(skuKitLine)));
                        Log.d(this.getName(), "Lookup on SKUKitLine ID_ for " + entityName);
                    } else {
                        promise.reject("SKUKitLine not found.");
                    }
                    break;
                case "Accounting.POSStationSession":
                    final POSStationSession posStationSession = realm.where(POSStationSession.class)
                            .equalTo("_ID", entityID)
                            .findFirst();
                    if (posStationSession != null) {
                        promise.resolve(new Gson().toJson(realm.copyFromRealm(posStationSession)));
                        Log.d(this.getName(), "Lookup on POSStationSession _ID for " + entityName);
                    } else {
                        promise.reject("POSStationSession not found.");
                    }
                    break;
            }

            // This is important
            realm.close();

            promise.resolve(true);
        }
    }


    /**
     * @param entityName
     * @param promise
     */
    @ReactMethod
    public void findAllEntity(final String entityName, final String searchString, final Integer limit, final String EvosusCompanySN, Promise promise) {
        sendBugsnagBreadcrumb("findAllEntity", new HashMap<String, Object>(){{
            put("entityName", entityName);
            put("searchString", searchString);
            put("limit", limit);
            put("EvosusCompanySN", EvosusCompanySN);
        }});

        Realm realm = getRealmConfiguration();
        Boolean search = (searchString != null && !searchString.isEmpty());
        if (realm == null)
            promise.resolve(false);

        Log.d(this.getName(), "Searching: " + search);
        Log.d(this.getName(), "Limiting results to " + limit);
        if (search) Log.d(this.getName(), "Search String: " + searchString);
        switch (entityName) {
            case "ProductSetup.CustomerDisplay":
                final RealmResults<CustomerDisplay> customerDisplay = search?realm.where(CustomerDisplay.class)
                        .equalTo("EvosusCompanySN", EvosusCompanySN)
                        .contains("DropdownSearchString", searchString, Case.INSENSITIVE)
                        .limit(limit)
                        .findAll():
                        realm.where(CustomerDisplay.class)
                                .equalTo("EvosusCompanySN", EvosusCompanySN)
                                .limit(limit)
                                .findAll();
                promise.resolve(new Gson().toJson(realm.copyFromRealm(customerDisplay)));
                Log.d(this.getName(), "Find All " + entityName);
                break;
            case "Inventory.SKU":
                final RealmResults<SKU> sku = search?realm.where(SKU.class)
                        .equalTo("EvosusCompanySN", EvosusCompanySN)
                        .contains("MySKU", searchString, Case.INSENSITIVE)
                        .or()
                        .contains("UPC", searchString, Case.INSENSITIVE)
                        .or()
                        .contains("Description", searchString, Case.INSENSITIVE)
                        .limit(limit)
                        .findAll():
                        realm.where(SKU.class)
                                .equalTo("EvosusCompanySN", EvosusCompanySN)
                                .limit(limit)
                                .findAll();
                promise.resolve(new Gson().toJson(realm.copyFromRealm(sku)));
                Log.d(this.getName(), "Find All  " + entityName);
                break;
            case "POS.POS_Transaction_NPE":
                final RealmResults<POS_Transaction_NPE> pos_transaction = search?realm.where(POS_Transaction_NPE.class)
                        .equalTo("EvosusCompanySN", EvosusCompanySN)
                        .contains("TrxStatus", searchString, Case.INSENSITIVE)
                        .limit(limit)
                        .findAll():
                        realm.where(POS_Transaction_NPE.class)
                                .equalTo("EvosusCompanySN", EvosusCompanySN)
                                .limit(limit)
                                .findAll();
                promise.resolve(new Gson().toJson(realm.copyFromRealm(pos_transaction)));
                Log.d(this.getName(), "Find All " + entityName);
                break;
            case "POS.POS_LineItem_NPE":
                final RealmResults<POS_LineItem_NPE> pos_lineitem = search?realm.where(POS_LineItem_NPE.class)
                        .equalTo("EvosusCompanySN", EvosusCompanySN)
                        .contains("Status", searchString, Case.INSENSITIVE)
                        .limit(limit)
                        .findAll():
                        realm.where(POS_LineItem_NPE.class)
                                .equalTo("EvosusCompanySN", EvosusCompanySN)
                                .limit(limit)
                                .findAll();
                promise.resolve(new Gson().toJson(realm.copyFromRealm(pos_lineitem)));
                Log.d(this.getName(), "Find All " + entityName);
                break;
            case "Inventory.SKUKitLine":
                final RealmResults<SKUKitLine> skuKitLines = search?realm.where(SKUKitLine.class)
                        .equalTo("EvosusCompanySN", EvosusCompanySN)
                        .contains("KitSKUID", searchString, Case.INSENSITIVE)
                        .limit(limit)
                        .findAll():
                        realm.where(SKUKitLine.class)
                                .equalTo("EvosusCompanySN", EvosusCompanySN)
                                .limit(limit)
                                .findAll();
                promise.resolve(new Gson().toJson(realm.copyFromRealm(skuKitLines)));
                Log.d(this.getName(), "Find All " + entityName);
                break;
            case "Accounting.POSStationSession":
                final RealmResults<POSStationSession> posStationSessions = realm.where(POSStationSession.class)
                        .limit(limit)
                        .findAll();
                promise.resolve(new Gson().toJson(realm.copyFromRealm(posStationSessions)));
                Log.d(this.getName(), "Find All " + entityName);
                break;
        }
        promise.resolve(true);

        // This is important
        realm.close();
    }

    /**
     * @param entityName
     * @param promise
     */
    @ReactMethod
    public void deleteRealmEntity(final String entityName, final String EvosusCompanySN, Promise promise) {
        sendBugsnagBreadcrumb("deleteRealmEntity", new HashMap<String, Object>(){{
            put("entityName", entityName);
            put("EvosusCompanySN", EvosusCompanySN);
        }});
        Realm realm = getRealmConfiguration();

        if (realm == null) {
            promise.resolve(false);
        } else {
            switch (entityName) {
                case "ProductSetup.CustomerDisplay":
                    realm.executeTransaction(r -> {
                        r.delete(CustomerDisplay.class);
                    });
                    Log.d(this.getName(), "Deleted realm entity " + entityName);
                    break;
                case "Inventory.SKU":
                    realm.executeTransaction(r -> {
                        r.delete(SKU.class);
                    });
                    Log.d(this.getName(), "Deleted realm entity " + entityName);
                    break;
                case "LOUAPI.LOUAPIJWT":
                    realm.executeTransaction(r -> {
                        r.delete(LOUAPIJWT.class);
                    });
                    Log.d(this.getName(), "Deleted realm entity " + entityName);
                    break;
                case "TSYS.TSYSMerchant":
                    realm.executeTransaction(r -> {
                        r.delete(TSYSMerchant.class);
                    });
                    Log.d(this.getName(), "Deleted realm entity " + entityName);
                    break;
                case "AdminConsole.EvosusCompany":
                    realm.executeTransaction(r -> {
                        final RealmResults<EvosusCompany> EvosusCompanies = r.where(EvosusCompany.class)
                                .equalTo("SerialNumber", EvosusCompanySN)
                                .findAll();
                        EvosusCompanies.deleteAllFromRealm();
                    });
                    Log.d(this.getName(), "Deleted realm entity " + entityName);
                    break;
                case "POS.POS_Transaction_NPE":
                    realm.executeTransaction(r -> {
                        final RealmResults<POS_Transaction_NPE> POS_Transactions = r.where(POS_Transaction_NPE.class)
                                .equalTo("EvosusCompanySN", EvosusCompanySN)
                                .findAll();
                        POS_Transactions.deleteAllFromRealm();
                    });
                    Log.d(this.getName(), "Deleted realm entity " + entityName);
                    break;
                case "POS.POS_LineItem_NPE":
                    realm.executeTransaction(r -> {
                        final RealmResults<POS_LineItem_NPE> POS_LineItems = r.where(POS_LineItem_NPE.class)
                                .equalTo("EvosusCompanySN", EvosusCompanySN)
                                .findAll();
                        POS_LineItems.deleteAllFromRealm();
                    });
                    Log.d(this.getName(), "Deleted realm entity " + entityName);
                    break;
                case "Inventory.SKUKitLine":
                    realm.executeTransaction(r -> {
                        r.delete(SKUKitLine.class);
                    });
                    Log.d(this.getName(), "Deleted realm entity " + entityName);
                    break;
                case "POS.SessionInfo":
                    realm.executeTransaction(r -> {
                        final RealmResults<SessionInfo> sessionInfos = r.where(SessionInfo.class)
                                .findAll();
                        sessionInfos.deleteAllFromRealm();
                    });
                    Log.d(this.getName(), "Deleted realm entity " + entityName);
                    break;
                case "Accounting.POSStationSession":
                    realm.executeTransaction(r -> {
                        r.delete(POSStationSession.class);
                    });
                    Log.d(this.getName(), "Deleted realm entity " + entityName);
                    break;
            }
            // This is important
            realm.close();
            promise.resolve(true);
        }
    }

    /**
     * @param entityName
     * @param promise
     */
    @ReactMethod
    public void deleteRealmObject(final String entityName, final String objectID, final String EvosusCompanySN, Promise promise) {
        sendBugsnagBreadcrumb("deleteRealmObject", new HashMap<String, Object>(){{
            put("entityName", entityName);
            put("objectID", objectID);
            put("EvosusCompanySN", EvosusCompanySN);
        }});
        Realm realm = getRealmConfiguration();

        if (realm == null) {
            promise.resolve(false);
        } else {
            switch (entityName) {
                case "ProductSetup.CustomerDisplay":
                    realm.executeTransaction(r -> {
                        RealmResults<CustomerDisplay> customerDisplaystoDelete = r.where(CustomerDisplay.class)
                                .equalTo("EvosusCompanySN", EvosusCompanySN)
                                .equalTo("CustomerVanityID", objectID)
                                .findAll();
                        customerDisplaystoDelete.deleteAllFromRealm();
                    });
                    Log.d(this.getName(), "Deleted realm entity " + entityName);
                    break;
                case "Inventory.SKU":
                    realm.executeTransaction(r -> {
                        RealmResults<SKU> SKUsToDelete = r.where(SKU.class)
                                .equalTo("EvosusCompanySN", EvosusCompanySN)
                                .equalTo("CustomerVanityID", objectID)
                                .findAll();
                        SKUsToDelete.deleteAllFromRealm();
                    });
                    Log.d(this.getName(), "Deleted realm entity " + entityName);
                    break;
                case "LOUAPI.LOUAPIJWT":
                    realm.executeTransaction(r -> {
                        r.delete(LOUAPIJWT.class);
                    });
                    Log.d(this.getName(), "Deleted realm entity " + entityName);
                    break;
                case "TSYS.TSYSMerchant":
                    realm.executeTransaction(r -> {
                        r.delete(TSYSMerchant.class);
                    });
                    Log.d(this.getName(), "Deleted realm entity " + entityName);
                    break;
                case "AdminConsole.EvosusCompany":
                    realm.executeTransaction(r -> {
                        r.delete(EvosusCompany.class);
                    });
                    Log.d(this.getName(), "Deleted realm entity " + entityName);
                    break;
                case "POS.POS_Transaction_NPE":
                    realm.executeTransaction(r -> {
                        RealmResults<POS_Transaction_NPE> transactionsToDelete = r.where(POS_Transaction_NPE.class)
                                .equalTo("EvosusCompanySN", EvosusCompanySN)
                                .equalTo("ID_", objectID)
                                .findAll();
                        transactionsToDelete.deleteAllFromRealm();
                    });
                    break;
                case "POS.POS_LineItem_NPE":
                    realm.executeTransaction(r -> {
                        RealmResults<POS_LineItem_NPE> lineItemsToDelete = r.where(POS_LineItem_NPE.class)
                                .equalTo("EvosusCompanySN", EvosusCompanySN)
                                .equalTo("ID_", objectID)
                                .findAll();
                        Log.d(this.getName(), "objectToDelete: " + lineItemsToDelete.asJSON());
                        lineItemsToDelete.deleteAllFromRealm();
                    });
                    Log.d(this.getName(), "Deleted realm object " + entityName);
                    break;
                case "Inventory.SKUKitLine":
                    realm.executeTransaction(r -> {
                        RealmResults<SKUKitLine> skuKitLinesToDelete = r.where(SKUKitLine.class)
                                .equalTo("EvosusCompanySN", EvosusCompanySN)
                                .equalTo("ID_", objectID)
                                .findAll();
                        Log.d(this.getName(), "objectToDelete: " + skuKitLinesToDelete.asJSON());
                        skuKitLinesToDelete.deleteAllFromRealm();
                    });
                    Log.d(this.getName(), "Deleted realm object " + entityName);
                    break;
                case "Accounting.POSStationSession":
                    realm.executeTransaction(r -> {
                        RealmResults<POSStationSession> posStationSessionsToDelete = r.where(POSStationSession.class)
                                .equalTo("_ID", objectID)
                                .findAll();
                        Log.d(this.getName(), "objectToDelete: " + posStationSessionsToDelete.asJSON());
                        posStationSessionsToDelete.deleteAllFromRealm();
                    });
                    Log.d(this.getName(), "Deleted realm object " + entityName);
                    break;
            }
            realm.close();
            promise.resolve(true);
        }
    }

    /**
     * @param promise
     */
    @ReactMethod
    public void deleteRealmDB(Promise promise) {
        sendBugsnagBreadcrumb("deleteRealmDB");

        Realm.deleteRealm(Realm.getDefaultConfiguration());

        Log.d(this.getName(), "DeleteRealm");
        promise.resolve(true);
    }

    /**
     * @param entityName
     * @param promise
     */
    @ReactMethod
    public void countEntity(final String entityName, final String EvosusCompanySN, Promise promise) {
        sendBugsnagBreadcrumb("countEntity", new HashMap<String, Object>(){{
            put("entityName", entityName);
            put("EvosusCompanySN", EvosusCompanySN);
        }});
        Realm realm = getRealmConfiguration();
        Log.d(this.getName(), "Counting " + entityName);
        switch (entityName) {
            case "ProductSetup.CustomerDisplay":
                realm.executeTransaction(r -> {

                });
                final long CustomerDisplays = realm.where(CustomerDisplay.class)
                        .equalTo("EvosusCompanySN", EvosusCompanySN)
                        .count();
                promise.resolve((int)CustomerDisplays);
                Log.d(this.getName(), "Counted " + CustomerDisplays + ' ' +  entityName);
                break;
            case "Inventory.SKU":
                final long SKUs = realm.where(SKU.class)
                        .equalTo("EvosusCompanySN", EvosusCompanySN)
                        .count();
                promise.resolve((int)SKUs);
                Log.d(this.getName(), "Counted " + SKUs + ' ' +  entityName);
                break;
            case "AdminConsole.EvosusCompany":
                final long EvosusCompanies = realm.where(EvosusCompany.class).count();
                promise.resolve((int)EvosusCompanies);
                Log.d(this.getName(), "Counted " + EvosusCompanies + ' ' +  entityName);
                break;
            case "POS.POS_Transaction_NPE":
                final long POS_Transactions = realm.where(POS_Transaction_NPE.class)
                        .equalTo("EvosusCompanySN", EvosusCompanySN)
                        .count();
                promise.resolve((int)POS_Transactions);
                Log.d(this.getName(), "Counted " + POS_Transactions + ' ' +  entityName);
                break;
            case "POS.POS_LineItem_NPE":
                final long POS_LineItems = realm.where(POS_LineItem_NPE.class)
                        .equalTo("EvosusCompanySN", EvosusCompanySN)
                        .count();
                promise.resolve((int)POS_LineItems);
                Log.d(this.getName(), "Counted " + POS_LineItems + ' ' +  entityName);
                break;
            case "Inventory.SKUKitLine":
                final long SKUKitLines = realm.where(SKUKitLine.class)
                        .equalTo("EvosusCompanySN", EvosusCompanySN)
                        .count();
                promise.resolve((int)SKUKitLines);
                Log.d(this.getName(), "Counted " + SKUKitLines + ' ' +  entityName);
                break;
            case "Accounting.POSStationSession":
                final long POSStationSessions = realm.where(POSStationSession.class)
                        .count();
                promise.resolve((int)POSStationSessions);
                Log.d(this.getName(), "Counted " + POSStationSessions + ' ' +  entityName);
                break;
        }
    }

    /**
     * @param entityName
     * @param promise
     */
    @ReactMethod
    public void countEntitySearch(final String entityName, final String searchString, final String EvosusCompanySN, Promise promise) {
        sendBugsnagBreadcrumb("countEntitySearch", new HashMap<String, Object>(){{
            put("entityName", entityName);
            put("searchString", searchString);
            put("EvosusCompanySN", EvosusCompanySN);
        }});
        Realm realm = getRealmConfiguration();
        Log.i(this.getName(), "Counting " + entityName);
        switch (entityName) {
            case "POS.POS_Transaction_NPE":
                final long POS_Transactions = realm.where(POS_Transaction_NPE.class)
                        .equalTo("EvosusCompanySN", EvosusCompanySN)
                        .contains("TrxStatus", searchString, Case.INSENSITIVE).count();
                promise.resolve((int)POS_Transactions);
                Log.d(this.getName(), "Counted " + POS_Transactions + ' ' +  entityName + " with TrxStatus of " + searchString);
                break;
            case "POS.POS_LineItem_NPE":
                final long POS_LineItems = realm.where(POS_LineItem_NPE.class)
                        .equalTo("EvosusCompanySN", EvosusCompanySN)
                        .contains("Status", searchString, Case.INSENSITIVE)
                        .count();
                promise.resolve((int)POS_LineItems);
                Log.d(this.getName(), "Counted " + POS_LineItems + ' ' +  entityName + " with Status of " + searchString);
                break;
        }
    }

    /**
     * @param daysString
     * @param promise
     */
    @ReactMethod
    public void deleteExpiredPOSTransactions(final String daysString, final String EvosusCompanySN, Promise promise) {
        sendBugsnagBreadcrumb("deleteExpiredPOSTransactions", new HashMap<String, Object>(){{
            put("daysString", daysString);
            put("EvosusCompanySN", EvosusCompanySN);
        }});
        Integer days = Integer.parseInt(daysString);
//        Log.e(this.getName(), "days: " + days);
        Realm realm = getRealmConfiguration();
//        Date d = new Date();
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.DATE, -days);
        Date d = cal.getTime();
        Log.d(this.getName(), "Date d: "+ d.toString());
        RealmResults<POS_Transaction_NPE> objectsToDelete = realm.where(POS_Transaction_NPE.class)
                .equalTo("EvosusCompanySN", EvosusCompanySN)
                .equalTo("TrxStatus", "Hold")
                .lessThan("HoldDate", d)
                .findAll();
        deletePOSTransactions(objectsToDelete, EvosusCompanySN);
        promise.resolve(true);
    }

    /**
     * @param customerVanityID
     * @param promise
     */
    @ReactMethod
    public void getPOSLineItemsFromCustomerVanityID(final String customerVanityID, final String EvosusCompanySN, Promise promise) {
        sendBugsnagBreadcrumb("getPOSLineItemsFromCustomerVanityID", new HashMap<String, Object>(){{
            put("customerVanityID", customerVanityID);
            put("EvosusCompanySN", EvosusCompanySN);
        }});
        Realm realm = getRealmConfiguration();
        RealmResults<POS_LineItem_NPE> customerHistoryLineItems = realm.where(POS_LineItem_NPE.class)
                .equalTo("EvosusCompanySN", EvosusCompanySN)
                .equalTo("CustomerVanityID", customerVanityID)
                .findAll();
        promise.resolve(customerHistoryLineItems);
    }

    private void deletePOSTransactions(final RealmResults<POS_Transaction_NPE> pos_transactions, final String EvosusCompanySN) {
        sendBugsnagBreadcrumb("deletePOSTransactions", new HashMap<String, Object>(){{
            put("customerVanityID", pos_transactions.asJSON());
            put("EvosusCompanySN", EvosusCompanySN);
        }});
        Realm realm = getRealmConfiguration();
        realm.beginTransaction();
        for (POS_Transaction_NPE POS_Transaction_NPE: pos_transactions) {
            RealmResults<POS_LineItem_NPE> lineItems = realm.where(POS_LineItem_NPE.class)
                    .equalTo("EvosusCompanySN", EvosusCompanySN)
                    .equalTo("POS_TransactionID", POS_Transaction_NPE.getID_())
                    .findAll();
            Log.d(this.getName(), lineItems.size() + " POS_LineItems found for POS_Transaction_NPE " + POS_Transaction_NPE.getID_());
            lineItems.deleteAllFromRealm();
        }
        Log.d(this.getName(), " POS_Transactions found to delete: " + pos_transactions.size());
        pos_transactions.deleteAllFromRealm();
        realm.commitTransaction();
    }

    /**
     * @param promise
     */
    @ReactMethod
    private void getPOSTransactionsToSync(final String EvosusCompanySN, Promise promise) {
        sendBugsnagBreadcrumb("getPOSTransactionsToSync", new HashMap<String, Object>(){{
            put("EvosusCompanySN", EvosusCompanySN);
        }});
        Realm realm = getRealmConfiguration();
        RealmResults<POS_Transaction_NPE> pos_transactions = realm.where(POS_Transaction_NPE.class)
                .equalTo("EvosusCompanySN", EvosusCompanySN)
                .equalTo("TrxStatus", "Complete")
                .findAll();
        promise.resolve(new Gson().toJson(realm.copyFromRealm(pos_transactions)));
    }

    private RealmResults<POS_Transaction_NPE> findSessionTransactions(Realm realm, final String sessionID, final String status, final String EvosusCompanySN) {
        sendBugsnagBreadcrumb("findSessionTransactions", new HashMap<String, Object>(){{
            put("sessionID", sessionID);
            put("status", status);
            put("EvosusCompanySN", EvosusCompanySN);
        }});
        RealmResults<POS_Transaction_NPE> pos_transactions = realm.where(POS_Transaction_NPE.class)
                .equalTo("EvosusCompanySN", EvosusCompanySN)
                .equalTo("POSStationSessionID", sessionID)
                .equalTo("TrxStatus", status)
                .findAll();
        return pos_transactions;
    }

    /**
     * @param sessionID
     * @param status
     * @param promise
     */
    @ReactMethod
    public void getSessionTransactions(final String sessionID, final String status, final String EvosusCompanySN, Promise promise) {
        sendBugsnagBreadcrumb("getSessionTransactions", new HashMap<String, Object>(){{
            put("sessionID", sessionID);
            put("status", status);
            put("EvosusCompanySN", EvosusCompanySN);
        }});
        Realm realm = getRealmConfiguration();
        RealmResults<POS_Transaction_NPE> pos_transactions = findSessionTransactions(realm, sessionID, status, EvosusCompanySN);
        promise.resolve(new Gson().toJson(realm.copyFromRealm(pos_transactions)));
    }

    /**
     * @param sessionID
     * @param status
     * @param promise
     */
    @ReactMethod
    public void deleteSessionTransactions(final String sessionID, final String status, final String EvosusCompanySN, Promise promise) {
        sendBugsnagBreadcrumb("deleteSessionTransactions", new HashMap<String, Object>(){{
            put("sessionID", sessionID);
            put("status", status);
            put("EvosusCompanySN", EvosusCompanySN);
        }});
        Realm realm = getRealmConfiguration();
        RealmResults<POS_Transaction_NPE> pos_transactions = findSessionTransactions(realm, sessionID, status, EvosusCompanySN);
        realm.beginTransaction();
        pos_transactions.deleteAllFromRealm();
        realm.commitTransaction();
        promise.resolve(true);
    }

    /**
     * @param posTransactionID
     * @param EvosusCompanySN
     * @param promise
     */
    @ReactMethod
    public void deletePOSLineItemsFromPOSTransaction(final String posTransactionID, final String EvosusCompanySN, Promise promise) {
        sendBugsnagBreadcrumb("deletePOSLineItemsFromPOSTransaction", new HashMap<String, Object>(){{
            put("sessionID", posTransactionID);
            put("EvosusCompanySN", EvosusCompanySN);
        }});
        Realm realm = getRealmConfiguration();

        if (realm == null) {
            promise.resolve(false);
        } else {
            Log.d(this.getName(), "POS_TransactionID: " + posTransactionID);
            final RealmResults<POS_LineItem_NPE> POS_LineItems = realm.where(POS_LineItem_NPE.class)
                    .equalTo("EvosusCompanySN", EvosusCompanySN)
                    .equalTo("POS_TransactionID", posTransactionID)
                    .findAll();
            if (POS_LineItems != null) {
                realm.beginTransaction();

                POS_LineItems.deleteAllFromRealm();
                promise.resolve(true);
                realm.commitTransaction();

                Log.d(this.getName(), "Deleted POSLineItems by POSTransaction");
            } else {
                promise.reject("POS_LineItems not found.");
            }
        }
    }


    /**
     * @param entityID
     * @param promise
     */
    @ReactMethod
    public void getPOSLineItemsByPOSTransactionID(final String entityID, final String EvosusCompanySN, Promise promise) {
        sendBugsnagBreadcrumb("getPOSLineItemsByPOSTransactionID", new HashMap<String, Object>(){{
            put("entityID", entityID);
            put("EvosusCompanySN", EvosusCompanySN);
        }});
        Realm realm = getRealmConfiguration();

        if (realm == null) {
            promise.resolve(false);
        } else {
            Log.d(this.getName(), "POS_TransactionID: " + entityID);
            final RealmResults<POS_LineItem_NPE> POS_LineItems = realm.where(POS_LineItem_NPE.class)
                    .equalTo("EvosusCompanySN", EvosusCompanySN)
                    .equalTo("POS_TransactionID", entityID)
                    .findAll();
            if (POS_LineItems != null) {
                promise.resolve(new Gson().toJson(realm.copyFromRealm(POS_LineItems)));
                Log.d(this.getName(), "Lookup on POS_TransactionID for POS_LineItem_NPE");
            } else {
                promise.reject("POS_LineItems not found.");
            }
        }
    }

    public static class MyMigration implements RealmMigration {
        @Override
        public void migrate(DynamicRealm realm, long oldVersion, long newVersion) {

            RealmSchema schema = realm.getSchema();

            if (oldVersion < 43) {
                schema.create("POS_Transaction")
                        .addField("ID_", String.class, FieldAttribute.PRIMARY_KEY)
                        .addField("Subtotal", Double.class)
                        .addField("SubtotalDisplay", String.class)
                        .addField("Total", Double.class)
                        .addField("TotalDisplay", String.class)
                        .addField("ReturnMode", String.class)
                        .addField("SKU_Search", String.class)
                        .addField("ReceiptCompanyName", String.class)
                        .addField("ReceiptCustomMessage", String.class)
                        .addField("TrxStatus", String.class)
                        .addField("PaymentMethod", String.class)
                        .addField("Tax", Double.class)
                        .addField("TaxDisplay", String.class)
                        .addField("Tendered", Double.class)
                        .addField("TenderedDisplay", String.class)
                        .addField("ChangeDue", Double.class)
                        .addField("ChangeDueDisplay", String.class)
                        .addField("PrintReceipt", Boolean.class)
                        .addField("EmailReceipt", Boolean.class)
                        .addField("PaymentAttempts", Integer.class)
                        .addField("DiscountRate", Double.class)
                        .addField("DiscountType", String.class)
                        .addField("SubtotalAfterDiscount", Double.class)
                        .addField("SubtotalAfterDiscount_Display", String.class)
                        .addField("SubtotalBeforeDiscount_Display", String.class)
                        .addField("Discount_Total", Double.class)
                        .addField("SubtotalBeforeDiscount", Double.class)
                        .addField("TaxExemptDocumentation", String.class)
                        .addField("Invoiced", Boolean.class)
                        .addField("OrderType", String.class)
                        .addField("Request", String.class)
                        .addField("NumberOfLineItems", Integer.class)
                        .addField("ClerkName", String.class)
                        .addField("InvoiceID", String.class)
                        .addField("IsProcessed", Boolean.class)
                        .addField("ActiveListenersCount", Integer.class)
                        .addField("CardType", String.class)
                        .addField("CustomerName", String.class)
                        .addField("CustomerVanityID", String.class)
                        .addField("TaxableTotal", Double.class)
                        .addField("TaxCodeID", Integer.class)
                        .addField("HoldDate", Date.class)
                        .addField("HasError", Boolean.class)
                        .addField("Synced", Boolean.class)
                        .addField("POSStationSessionID", String.class)
                        .addField("DepartmentID", Integer.class)
                        .addField("POSStationID", Integer.class);

                schema.create("POS_LineItem")
                        .addField("ID_", String.class, FieldAttribute.PRIMARY_KEY)
                        .addField("POS_TransactionID", String.class)
                        .addField("LineNumber", Integer.class)
                        .addField("MySKU", String.class)
                        .addField("Description", String.class)
                        .addField("DescriptionFull", String.class)
                        .addField("SKUType", String.class)
                        .addField("UnitPrice", Double.class)
                        .addField("UnitPriceDisplay", String.class)
                        .addField("Quantity", Double.class)
                        .addField("Discount", Double.class)
                        .addField("DiscountDisplay", String.class)
                        .addField("Subtotal", Double.class)
                        .addField("SubtotalDisplay", String.class)
                        .addField("isComment", Boolean.class)
                        .addField("Comment", String.class)
                        .addField("ServiceDate", Date.class)
                        .addField("Status", String.class)
                        .addField("CustomerVanityID", String.class)
                        .addField("SKUID", String.class)
                        .addField("SerialNumber", String.class)
                        .addField("Taxable", Boolean.class);
                oldVersion++;
            }
            if (oldVersion < 44) {
                schema.create("SKUKitLine")
                        .addField("ID_", String.class, FieldAttribute.PRIMARY_KEY)
                        .addField("MySKU", String.class)
                        .addField("Description", String.class)
                        .addField("SKUType", String.class)
                        .addField("Quantity", Double.class)
                        .addField("KitPriceLineEnum", String.class)
                        .addField("UnitMeasureName", String.class)
                        .addField("ConversionFactor", Double.class)
                        .addField("PriceCalculated", Double.class)
                        .addField("SubtotalCalculated", Double.class)
                        .addField("Order", Integer.class)
                        .addField("isUpdatedSubtotal", Boolean.class)
                        .addField("RetailPrice", Double.class)
                        .addField("RetailMinusPercent", Double.class)
                        .addField("FixedPrice", Double.class)
                        .addField("isRetailMinusPercent", Boolean.class)
                        .addField("isFixedPrice", Boolean.class)
                        .addField("isNoCharge", Boolean.class)
                        .addField("SKUID", String.class)
                        .addField("KitSKUID", String.class);
                oldVersion++;
            }
            if (oldVersion < 45) {
                schema.get("CustomerDisplay")
                        .addField("EvosusCompanySN", String.class);
                schema.get("POS_LineItem_NPE")
                        .addField("EvosusCompanySN", String.class);
                schema.get("POS_Transaction_NPE")
                        .addField("EvosusCompanySN", String.class);
                schema.get("SKU")
                        .addField("EvosusCompanySN", String.class);
                schema.get("SKUKitLine")
                        .addField("EvosusCompanySN", String.class);
                oldVersion++;
            }
            if (oldVersion < 46) {
                schema.get("POS_Transaction_NPE")
                        .addField("DepartmentName", String.class);
                oldVersion++;
            }
            if (oldVersion < 47) {
                schema.create("SessionInfo")
                        .addField("Email", String.class, FieldAttribute.PRIMARY_KEY)
                        .addField("Username", String.class)
                        .addField("CompanySerialNumber", String.class)
                        .addField("CompanyName", String.class)
                        .addField("PosStationName", String.class)
                        .addField("PosStationId", String.class);
                oldVersion++;
            }
            if (oldVersion < 49) {
                try {
                    RealmObjectSchema posTrxSchema = schema.get("POS_Transaction_NPE");
                    if (posTrxSchema.hasField("DateCompleted")) {
                        posTrxSchema.removeField("DateCompleted");
                    }
                } catch (Error e) {
                    Bugsnag.notify(e);
                    Log.e("REALM Migration", e.getMessage());
                }
                oldVersion++;
            }
            if (oldVersion < 50) {
                try {
                    Boolean sessionInfoExists = schema.contains("SessionInfo");
                    if (!sessionInfoExists) {
                        schema.create("SessionInfo")
                                .addField("Email", String.class, FieldAttribute.PRIMARY_KEY)
                                .addField("Username", String.class)
                                .addField("CompanySerialNumber", String.class)
                                .addField("CompanyName", String.class)
                                .addField("PosStationName", String.class)
                                .addField("PosStationId", String.class);
                    }
                } catch (Error e) {
                    Bugsnag.notify(e);
                    Log.e("REALM Migration", e.getMessage());
                }
                oldVersion++;
            }
            if (oldVersion < 51) {
                schema.create("POS_Transaction_NPE")
                        .addField("ID_", String.class, FieldAttribute.PRIMARY_KEY)
                        .addField("Subtotal", Double.class)
                        .addField("SubtotalDisplay", String.class)
                        .addField("Total", Double.class)
                        .addField("TotalDisplay", String.class)
                        .addField("ReturnMode", String.class)
                        .addField("SKU_Search", String.class)
                        .addField("ReceiptCompanyName", String.class)
                        .addField("ReceiptCustomMessage", String.class)
                        .addField("TrxStatus", String.class)
                        .addField("PaymentMethod", String.class)
                        .addField("Tax", Double.class)
                        .addField("TaxDisplay", String.class)
                        .addField("Tendered", Double.class)
                        .addField("TenderedDisplay", String.class)
                        .addField("ChangeDue", Double.class)
                        .addField("ChangeDueDisplay", String.class)
                        .addField("PrintReceipt", Boolean.class)
                        .addField("EmailReceipt", Boolean.class)
                        .addField("PaymentAttempts", Integer.class)
                        .addField("DiscountRate", Double.class)
                        .addField("DiscountType", String.class)
                        .addField("SubtotalAfterDiscount", Double.class)
                        .addField("SubtotalAfterDiscount_Display", String.class)
                        .addField("SubtotalBeforeDiscount_Display", String.class)
                        .addField("Discount_Total", Double.class)
                        .addField("SubtotalBeforeDiscount", Double.class)
                        .addField("TaxExemptDocumentation", String.class)
                        .addField("Invoiced", Boolean.class)
                        .addField("OrderType", String.class)
                        .addField("Request", String.class)
                        .addField("NumberOfLineItems", Integer.class)
                        .addField("ClerkName", String.class)
                        .addField("InvoiceID", String.class)
                        .addField("IsProcessed", Boolean.class)
                        .addField("ActiveListenersCount", Integer.class)
                        .addField("CardType", String.class)
                        .addField("CustomerName", String.class)
                        .addField("CustomerVanityID", String.class)
                        .addField("TaxableTotal", Double.class)
                        .addField("TaxCodeID", Integer.class)
                        .addField("HoldDate", Date.class)
                        .addField("HasError", Boolean.class)
                        .addField("Synced", Boolean.class)
                        .addField("POSStationSessionID", String.class)
                        .addField("DepartmentID", Integer.class)
                        .addField("POSStationID", Integer.class);
                schema.create("POS_LineItem_NPE")
                        .addField("ID_", String.class, FieldAttribute.PRIMARY_KEY)
                        .addField("POS_TransactionID", String.class)
                        .addField("LineNumber", Integer.class)
                        .addField("MySKU", String.class)
                        .addField("Description", String.class)
                        .addField("DescriptionFull", String.class)
                        .addField("SKUType", String.class)
                        .addField("UnitPrice", Double.class)
                        .addField("UnitPriceDisplay", String.class)
                        .addField("Quantity", Double.class)
                        .addField("Discount", Double.class)
                        .addField("DiscountDisplay", String.class)
                        .addField("Subtotal", Double.class)
                        .addField("SubtotalDisplay", String.class)
                        .addField("isComment", Boolean.class)
                        .addField("Comment", String.class)
                        .addField("ServiceDate", Date.class)
                        .addField("Status", String.class)
                        .addField("CustomerVanityID", String.class)
                        .addField("SKUID", String.class)
                        .addField("SerialNumber", String.class)
                        .addField("Taxable", Boolean.class);
                schema.create("POSStationSession")
                        .addField("_ID", String.class, FieldAttribute.PRIMARY_KEY)
                        .addField("OpenerName", String.class)
                        .addField("OpenDateTime", Date.class)
                        .addField("OpenCashAmount", Double.class)
                        .addField("OpenOverUnderAmount", Double.class)
                        .addField("CloserName", String.class)
                        .addField("CloseDateTime", Date.class)
                        .addField("CloseCashExpectedAmount", Double.class)
                        .addField("CloseCashAmount", Double.class)
                        .addField("CloseOverUnderAmount", Double.class)
                        .addField("CashLeftInDrawerAmount", Double.class)
                        .addField("CashAvailableToDepositAmount", String.class)
                        .addField("CloseOverUnderMessaged", Boolean.class)
                        .addField("OpenOverUnderMessaged", Boolean.class)
                        .addField("GUIDString", String.class)
                        .addField("LastPOSTransactionID", String.class);
                oldVersion++;
            }
        }
        @Override
        public int hashCode() {
            return MyMigration.class.hashCode();
        }

        @Override
        public boolean equals(Object object) {
            if (object == null) {
                return false;
            }
            return object instanceof MyMigration;
        }
    }

    /**
     * @param promise
     * @return
     */
    private boolean validatePOSLink(Promise promise) {

//        if (!bInited)
//            initPOSLink();
//
//        if (posLink == null) {
//            Toast.makeText(getReactApplicationContext(), "Cannot initialize POSLink", Toast.LENGTH_LONG);
//            promise.reject(CODE_ERROR, "Cannot initialize POSLink");
//            return false;
//        }
//        if (posLink.GetCommSetting() == null) {
//            Toast.makeText(getReactApplicationContext(), "POSLink missing Communication Settings", Toast.LENGTH_LONG);
//            promise.reject(CODE_ERROR, "POSLink missing Communication Settings");
//            return false;
//        }
        return true;
    }

    /**
     * @param promise
     */
    @ReactMethod
    public void rebootQ20(Promise promise) {

        if (!validatePOSLink(promise)) return;

        ManageRequest manageRequest = new ManageRequest();
        manageRequest.TransType = manageRequest.ParseTransType("REBOOT");
        manageRequest.EDCType = manageRequest.ParseEDCType("ALL");

        OneShotManageTask oneShotManageTask = new OneShotManageTask(promise, manageRequest, "REBOOT", getPOSLink(), getCurrentActivity());
        oneShotManageTask.execute();

    }

    /**
     * @param userName
     * @param userPassword
     * @param mid
     * @param deviceID
     * @param promise
     */
    @ReactMethod
    public void setTransactionKey(final String userName, final String userPassword, final String mid, final String deviceID, Promise promise) {
        sendBugsnagBreadcrumb("setTransactionKey", new HashMap<String, Object>(){{
            put("userName", userName);
            put("userPassword", userPassword);
            put("mid", mid);
            put("deviceID", deviceID);
        }});
        if (!validatePOSLink(promise)) return;

        ManageRequest manageRequest = new ManageRequest();
        manageRequest.TransType = manageRequest.ParseTransType("SETVAR");
        manageRequest.EDCType = manageRequest.ParseEDCType("CREDIT");
        manageRequest.VarName = "UserName";
        manageRequest.VarValue = userName;
        manageRequest.VarName1 = "UserPassword";
        manageRequest.VarValue1 = userPassword;
        manageRequest.VarName2 = "MID";
        manageRequest.VarValue2 = mid;
        manageRequest.VarName3 = "DeviceID";
        manageRequest.VarValue3 = deviceID;

        OneShotManageTask oneShotManageTask = new OneShotManageTask(promise, manageRequest, "SETTRANSACTIONKEY", getPOSLink(), getCurrentActivity());
        oneShotManageTask.execute();

    }

    /**
     * @param receiptText
     * @param promise
     */
    @ReactMethod
    public void printReceipt(final String receiptText, final Promise promise) {
        sendBugsnagBreadcrumb("printReceipt", new HashMap<String, Object>(){{
            put("receiptText", receiptText);
        }});
        POSLinkPrinter posLinkPrinter = POSLinkPrinter.getInstance(getReactApplicationContext());

        if (posLinkPrinter == null){
            Toast.makeText(getReactApplicationContext(), "Cannot initialize POSLinkPrinter", Toast.LENGTH_LONG);
            promise.reject(CODE_ERROR,"Cannot initialize POSLinkPrinter");
            return;
        }

        posLinkPrinter.setGray(POSLinkPrinter.GreyLevel.DEFAULT);
        posLinkPrinter.setPrintWidth(POSLinkPrinter.RecommendWidth.E500_RECOMMEND_WIDTH);

        POSLinkPrinter.getInstance(getReactApplicationContext()).print(receiptText, POSLinkPrinter.CutMode.PARTIAL_PAPER_CUT,SettingINI.getCommSettingFromFile(getReactApplicationContext().getFilesDir().getAbsolutePath() + "/" + SettingINI.FILENAME),  new POSLinkPrinter.PrintListener() {
            @Override
            public void onSuccess() {
                getCurrentActivity().runOnUiThread(new Runnable() {
                    public void run() {
                        promise.resolve("Printed");
                        // Toast.makeText(getReactApplicationContext(), receiptText, Toast.LENGTH_LONG).show();
                        return;
                    }
                });
            }

            @Override
            public void onError(final ProcessResult processResult) {
                getCurrentActivity().runOnUiThread(new Runnable() {
                    public void run() {
                        promise.reject(CODE_ERROR, processResult.getMessage());
                        Toast.makeText(getReactApplicationContext(), processResult.getMessage(), Toast.LENGTH_LONG).show();
                        return;
                    }
                });
            }
        });

    }

    private static class OneShotPaymentTask2  extends AsyncTask<Void, Void, Void> {
        private final ProgressDialog dialog;
        private final PaymentRequest l_paymentRequest;
        private final Promise l_promise;
        private final String l_transType;
        private final PosLink l_posLink;
        private boolean l_allowCancel;
        private int l_timeToAllowCancel;


        public OneShotPaymentTask2(Promise promise, final PaymentRequest paymentRequest, final String transType, PosLink posLink, Activity activity) {
            sendBugsnagBreadcrumb("OneShotPaymentTask2", new HashMap<String, Object>(){{
                put("paymentRequest", paymentRequest);
                put("transType", transType);
            }});
            l_paymentRequest = paymentRequest;
            l_promise= promise;
            l_transType = transType;
            l_posLink = posLink;
            l_allowCancel = false;
            WeakReference<Activity> l_activity = new WeakReference<Activity>(activity);

            TimerTask l_task = new TimerTask() {
                @Override
                public void run() {
                    l_allowCancel = true;
                }
            };
            Timer l_timer = new Timer();
            l_timer.schedule(l_task, 5000); // 5 seconds

            dialog = new ProgressDialog(l_activity.get());
            dialog.setMessage("Processing...");
            dialog.setCancelable(true);
            dialog.setCanceledOnTouchOutside(false);
            dialog.setButton(DialogInterface.BUTTON_NEGATIVE, "Cancel", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {

                }
            });
        }

        @Override
        protected void onPreExecute() {
            sendBugsnagBreadcrumb("onPreExecute");

            if (dialog != null) {
                dialog.setMessage("Processing...(cancel allowed in 5 seconds)");
                dialog.show();
                dialog.getButton(AlertDialog.BUTTON_NEGATIVE).setOnClickListener(new View.OnClickListener(){
                    @Override
                    public void onClick(View v) {
                        Log.i(NAME, "onClick l_allowCancel = " + String.valueOf(l_allowCancel));
                        if (l_allowCancel) {
                            l_posLink.CancelTrans();
                            dialog.dismiss();
                        }
                    }
                });
                new CountDownTimer(5000, 1000) {
                    public void onTick(long millisUntilFinished) {
                        //here you can have your logic to set message
                        dialog.setMessage("Processing ... (can cancel in " + millisUntilFinished/1000 + " seconds)");
                    }
                    public void onFinish() {
                        //the progress is finish
                        dialog.setMessage("Processing ...");
                    }
                }.start();
            }
        }

        @Override
        protected Void doInBackground(Void... args) {
            sendBugsnagBreadcrumb("doInBackground");

            l_posLink.PaymentRequest = l_paymentRequest;

            // ProcessTrans is Blocking call, will return when the transaction is complete.
            CountRunTime.start("Payment");
            ProcessTransResult l_ptr = l_posLink.ProcessTrans();
            CountRunTime.countPoint("Payment");
            taskCompleted(l_posLink, l_ptr, l_promise, l_transType);
//            getCurrentActivity().runOnUiThread(new Runnable() {
//                @Override
//                public void run() {
//                    taskCompleted(l_posLink, l_ptr, l_promise, l_transType);
//                }
//            });

            return null;
        }
        @Override
        protected void onPostExecute(Void result) {
            sendBugsnagBreadcrumb("onPostExecute");
            // do UI work here
            if (dialog.isShowing()) {
                dialog.dismiss();
            }
        }
    }

    private static class OneShotBatchTask  extends AsyncTask<Void, Void, Void> {
        private final ProgressDialog dialog;
        private final BatchRequest l_batchRequest;
        private final Promise l_promise;
        private final PosLink l_posLink;
        private final String l_transType;
        private ProcessTransResult l_ptr;
        private final WeakReference<Activity> l_activity;

        public OneShotBatchTask(Promise promise, final BatchRequest request, final String transType, PosLink posLink, Activity activity) {
            sendBugsnagBreadcrumb("OneShotBatchTask", new HashMap<String, Object>(){{
                put("request", request);
                put("transType", transType);
            }});

            l_batchRequest = request;
            l_promise= promise;
            l_transType = transType;
            l_posLink = posLink;
            l_activity = new WeakReference<Activity>(activity);
            dialog = new ProgressDialog(l_activity.get());
            dialog.setMessage("Processing...");
            dialog.setCancelable(true);
            dialog.setButton(DialogInterface.BUTTON_NEGATIVE, "Cancel", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {

                    if (l_posLink != null)
                        l_posLink.CancelTrans();

                }
            });
        }

        @Override
        protected void onPreExecute() {
            sendBugsnagBreadcrumb("onPreExecute");
            if (dialog != null) {
                dialog.setMessage("Processing...");
                dialog.show();
            }
        }

        @Override
        protected Void doInBackground(Void... args) {
            sendBugsnagBreadcrumb("doInBackground");

            l_posLink.BatchRequest = l_batchRequest;

            // ProcessTrans is Blocking call, will return when the transaction is complete.
            CountRunTime.start("Batch");
            l_ptr = l_posLink.ProcessTrans();
            CountRunTime.countPoint("Batch");
            l_activity.get().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    taskCompleted(l_posLink, l_ptr, l_promise, l_transType);

                }
            });

            return null;
        }
        @Override
        protected void onPostExecute(Void result) {
            sendBugsnagBreadcrumb("onPostExecute");
            // do UI work here
            if (dialog.isShowing()) {
                dialog.dismiss();
            }
        }
    }

    private static class OneShotManageTask  extends AsyncTask<Void, Void, Void> {
        private final ProgressDialog dialog;
        private final ManageRequest l_manageRequest;
        private final Promise l_promise;
        private final String l_transType;
        private final PosLink l_posLink;
        private ProcessTransResult l_ptr;
        private final WeakReference<Activity> l_activity;

        public OneShotManageTask(Promise promise, final ManageRequest request, final String transType, PosLink posLink, Activity activity) {
            sendBugsnagBreadcrumb("OneShotManageTask", new HashMap<String, Object>(){{
                put("request", request);
                put("transType", transType);
            }});

            l_manageRequest = request;
            l_promise= promise;
            l_transType = transType;
            l_posLink = posLink;
            l_activity = new WeakReference<Activity>(activity);

            dialog = new ProgressDialog(l_activity.get());
            dialog.setMessage("Processing...");
            dialog.setCancelable(true);
            dialog.setButton(DialogInterface.BUTTON_NEGATIVE, "Cancel", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {

                    if (l_posLink != null)
                        l_posLink.CancelTrans();

                }
            });
        }

        @Override
        protected void onPreExecute() {
            sendBugsnagBreadcrumb("onPreExecute");
            if (dialog != null) {
                dialog.setMessage("Processing...");
                dialog.show();
            }
        }

        @Override
        protected Void doInBackground(Void... args) {
            sendBugsnagBreadcrumb("doInBackground");

            l_posLink.ManageRequest = l_manageRequest;

            // ProcessTrans is Blocking call, will return when the transaction is complete.
            CountRunTime.start("Manage");
            l_ptr = l_posLink.ProcessTrans();
            CountRunTime.countPoint("Manage");
            l_activity.get().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    taskCompleted(l_posLink, l_ptr, l_promise, l_transType);
                }
            });

            return null;
        }
        @Override
        protected void onPostExecute(Void result) {
            sendBugsnagBreadcrumb("onPostExecute");
            // do UI work here
            if (dialog.isShowing()) {
                dialog.dismiss();
            }
        }
    }

    private static class OneShotReportTask  extends AsyncTask<Void, Void, Void> {
        private final ProgressDialog dialog;
        private final ReportRequest l_reportRequest;
        private final Promise l_promise;
        private final String l_transType;
        private final PosLink l_posLink;
        private ProcessTransResult l_ptr;
        private final WeakReference<Activity> l_activity;

        public OneShotReportTask(Promise promise, final ReportRequest request, final String transType, PosLink posLink, Activity activity) {
            sendBugsnagBreadcrumb("OneShotReportTask", new HashMap<String, Object>(){{
                put("request", request);
                put("transType", transType);
            }});
            l_reportRequest = request;
            l_promise= promise;
            l_transType = transType;
            l_posLink = posLink;
            l_activity = new WeakReference<Activity>(activity);

            dialog = new ProgressDialog(l_activity.get());
            dialog.setMessage("Processing...");
            dialog.setCancelable(true);
            dialog.setButton(DialogInterface.BUTTON_NEGATIVE, "Cancel", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {

                    if (l_posLink != null)
                        l_posLink.CancelTrans();

                }
            });
        }

        @Override
        protected void onPreExecute() {
            sendBugsnagBreadcrumb("onPreExecute");
            if (dialog != null) {
                dialog.setMessage("Processing...");
                dialog.show();
            }
        }

        @Override
        protected Void doInBackground(Void... args) {
            sendBugsnagBreadcrumb("doInBackground");

            l_posLink.ReportRequest = l_reportRequest;

            // ProcessTrans is Blocking call, will return when the transaction is complete.
            CountRunTime.start("Report");
            l_ptr = l_posLink.ProcessTrans();
            CountRunTime.countPoint("Report");
            l_activity.get().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    taskCompleted(l_posLink, l_ptr, l_promise, l_transType);
                }
            });

            return null;
        }
        @Override
        protected void onPostExecute(Void result) {
            sendBugsnagBreadcrumb("onPostExecute");
            // do UI work here
            if (dialog.isShowing()) {
                dialog.dismiss();
            }
        }
    }

    //
//    class OneShotPaymentTask implements Runnable {
//        int lastStatus = -1;
//
//        OneShotPaymentTask(Promise promise) {
//            m_promise = promise;
//        }
//
//        public void run() {
//            int status;
//
//            if (posLink != null) {
//                try {
//                    Thread.sleep(0);
//
//                    status = posLink.GetReportedStatus();
//                    if (status != lastStatus) {
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
//                        lastStatus = status;
//                    }
//
//                } catch (InterruptedException e) {
//                    e.printStackTrace();
//                }
//            }
//
//            mHandler.postDelayed(this, 500);
//        }
//    }

    private static void taskCompleted(final PosLink poslink, final ProcessTransResult ptr, Promise promise, final String transType) {
        sendBugsnagBreadcrumb("taskCompleted", new HashMap<String, Object>(){{
            put("poslink", poslink.toString());
            put("ptr", ptr);
            put("transType", transType);
        }});
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
                handleMessage(msg, promise, transType);
            } else if (poslink.BatchResponse != null) {
                BatchResponse batchResponse = poslink.BatchResponse;
                Message msg = new Message();
                msg.what = Constant.TRANSACTION_SUCCESSED;
                msg.obj = batchResponse;
                handleMessage(msg, promise, transType);
            } else if (poslink.PaymentResponse != null) {
                PaymentResponse paymentResponse = poslink.PaymentResponse;
                Message msg = new Message();
                msg.what = Constant.TRANSACTION_SUCCESSED;
                msg.obj = paymentResponse;
                handleMessage(msg, promise, transType);
            } else if (poslink.ReportRequest != null) {
                ReportResponse paymentResponse = poslink.ReportResponse;
                Message msg = new Message();
                msg.what = Constant.TRANSACTION_SUCCESSED;
                msg.obj = paymentResponse;
                handleMessage(msg, promise, transType);
            }

            Log.i(NAME, "Transaction success!");

        } else if (ptr.Code == ProcessTransResult.ProcessTransResultCode.TimeOut) {
            Message msg = new Message();
            msg.what = Constant.TRANSACTION_TIMEOUT;
            Bundle b = new Bundle();
            b.putString(Constant.DIALOG_TITLE, String.valueOf(ptr.Code));
            b.putString(Constant.DIALOG_MESSAGE, ptr.Msg);
            msg.setData(b);
            handleMessage(msg, promise, transType);

            Log.e(NAME, "Transaction Timeout! " + String.valueOf(ptr.Code));
            Log.e(NAME, "Transaction Timeout! " + ptr.Msg);
        } else {
            Message msg = new Message();
            msg.what = Constant.TRANSACTION_FAILURE;
            Bundle b = new Bundle();
            b.putString(Constant.DIALOG_TITLE, String.valueOf(ptr.Code));
            b.putString(Constant.DIALOG_MESSAGE, ptr.Msg);
            msg.setData(b);
            handleMessage(msg, promise, transType);

            Log.e(NAME, "Transaction Error! " + String.valueOf(ptr.Code));
            Log.e(NAME, "Transaction Error! " + ptr.Msg);
        }
    }

    public static void handleMessage(Message msg, Promise promise, final String transType) {
        sendBugsnagBreadcrumb("handleMessage", new HashMap<String, Object>(){{
            put("transType", transType);
        }});

        switch (msg.what) {
            case Constant.TRANSACTION_SUCCESSED:
                if (msg.obj != null) {
                    if (msg.obj instanceof ManageResponse) {
                        ManageResponse manageResponse = (ManageResponse) msg.obj;
//                        Toast.makeText(getReactApplicationContext(), manageResponse.ResultCode + '\n' + manageResponse.ResultTxt, Toast.LENGTH_LONG).show();
                        WritableMap map = Arguments.createMap();
                        map.putString("ResultCode", manageResponse.ResultCode);
                        map.putString("ResultTxt", manageResponse.ResultTxt);
                        if (transType.equalsIgnoreCase("INIT")) {
                            map.putString("SN", manageResponse.SN);
                            map.putString("ModelName", manageResponse.ModelName);
                            map.putString("MacAddress", manageResponse.MacAddress);
                            map.putString("OSVersion", manageResponse.PrimaryFirmVersion);
                            map.putString("LinesPerScreen", manageResponse.LinesPerScreen);
                            map.putString("CharsPerLine", manageResponse.CharsPerLine);
                            map.putString("ExtData", manageResponse.ExtData);
                        }

                        promise.resolve(map);
                    }
                    else if (msg.obj instanceof PaymentResponse) {
                        PaymentResponse paymentResponse = (PaymentResponse) msg.obj;
//                        Toast.makeText(getReactApplicationContext(), paymentResponse.ResultCode + '\n' + paymentResponse.ResultTxt, Toast.LENGTH_LONG).show();
                        WritableMap map = Arguments.createMap();
                        map.putString("ResultCode", paymentResponse.ResultCode);
                        map.putString("ResultTxt", paymentResponse.ResultTxt);
                        map.putString("AuthCode", paymentResponse.AuthCode);
                        map.putString("HostCode", paymentResponse.HostCode);
                        map.putString("HostResponse", paymentResponse.HostResponse);
                        map.putString("Message", paymentResponse.Message);
                        map.putString("Timestamp", paymentResponse.Timestamp);
                        map.putString("RequestedAmount", paymentResponse.RequestedAmount);
                        map.putString("RemainingBalance", paymentResponse.RemainingBalance);
                        map.putString("ApprovedAmount", paymentResponse.ApprovedAmount);
                        map.putString("ExtraBalance", paymentResponse.ExtraBalance);
                        map.putString("RawResponse", paymentResponse.RawResponse);
                        map.putString("SigFileName", paymentResponse.SigFileName);
                        map.putString("SignData", paymentResponse.SignData);
                        map.putString("CvResponse", paymentResponse.CvResponse);
                        map.putString("ExtData", paymentResponse.ExtData);
                        map.putString("BogusAccountNum", paymentResponse.BogusAccountNum);
                        map.putString("RefNum", paymentResponse.RefNum);
                        map.putString("CardType", paymentResponse.CardType);
                        map.putString("AvsResponse", paymentResponse.AvsResponse);
                        map.putString("TransactionId", "");
                        map.putString("CardOnFileTransactionId", "");
                        map.putString("Token", "");
                        map.putString("ExpDate", "");
                        map.putString("EntryMode", "");

                        //Use method to convert XML string content to XML Document object
                        Document doc = convertStringToXMLDocument( "<body>" + paymentResponse.ExtData + "</body>");
                        if (doc != null) {
                            String TransactionID = getElementByTagName(doc, "HRef");
                            String COFTransactionID = getElementByTagName(doc, "TransactionIdentifier");
                            String Token = getElementByTagName(doc, "Token");
                            String ExpDate = getElementByTagName(doc, "ExpDate");
                            String EntryMode = getElementByTagName(doc, "PLEntryMode");
                            map.putString("TransactionId", TransactionID);
                            map.putString("CardOnFileTransactionId", COFTransactionID);
                            map.putString("Token", Token);
                            map.putString("ExpDate", ExpDate);
                            switch (EntryMode) {
                                case "0":
                                    map.putString("EntryMode", "MANUAL");
                                    break;
                                case "1":
                                    map.putString("EntryMode", "SWIPED");
                                    break;
                                case "2":
                                    map.putString("EntryMode", "CONTACTLESS");
                                    break;
                                case "3":
                                    map.putString("EntryMode", "SCANNER");
                                    break;
                                case "4":
                                    map.putString("EntryMode", "CHIP");
                                    break;
                                case "5":
                                    map.putString("EntryMode", "FALLBACK SWIPE");
                                    break;
                            }
                        }
                        promise.resolve(map);
                    }
                    else if (msg.obj instanceof BatchResponse) {
                        BatchResponse batchResponse = (BatchResponse) msg.obj;
//                        Toast.makeText(getReactApplicationContext(), batchResponse.ResultCode + '\n' + batchResponse.ResultTxt, Toast.LENGTH_LONG).show();
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
                        map.putString("BatchFailedCount", batchResponse.BatchFailedCount);
                        map.putString("DebitCount", batchResponse.DebitCount);
                        map.putString("DebitAmount", batchResponse.DebitAmount);
                        map.putString("CreditCount", batchResponse.CreditCount);
                        map.putString("CreditAmount", batchResponse.CreditAmount);
                        map.putString("EBTCount", batchResponse.EBTCount);
                        map.putString("EBTAmount", batchResponse.EBTAmount);
                        map.putString("BatchNum", batchResponse.BatchNum);
                        map.putString("ExtData", batchResponse.ExtData);
                        map.putString("TID", batchResponse.TID);
                        promise.resolve(map);
                    }
                    else if (msg.obj instanceof ReportResponse) {
                        ReportResponse reportResponse = (ReportResponse) msg.obj;
//                        Toast.makeText(getReactApplicationContext(), batchResponse.ResultCode + '\n' + batchResponse.ResultTxt, Toast.LENGTH_LONG).show();
                        if (transType == "LOCALDETAILREPORT") {
                            WritableMap map = Arguments.createMap();
                            map.putString("ResultCode", reportResponse.ResultCode);
                            map.putString("ResultTxt", reportResponse.ResultTxt);
                            map.putString("AuthCode", reportResponse.AuthCode);
                            map.putString("HostCode", reportResponse.HostCode);
                            map.putString("HostResponse", reportResponse.HostResponse);
                            map.putString("Message", reportResponse.Message);
                            map.putString("Timestamp", reportResponse.Timestamp);
                            map.putString("RequestedAmount", reportResponse.ECRRefNum);
                            map.putString("RemainingBalance", reportResponse.RemainingBalance);
                            map.putString("ApprovedAmount", reportResponse.ApprovedAmount);
                            map.putString("ExtraBalance", reportResponse.ExtraBalance);
                            map.putString("CvResponse", reportResponse.CvResponse);
                            map.putString("ExtData", reportResponse.ExtData);
                            map.putString("BogusAccountNum", reportResponse.BogusAccountNum);
                            map.putString("RefNum", reportResponse.RefNum);
                            map.putString("CardType", reportResponse.CardType);
                            promise.resolve(map);
                        }
                    }
                }
                break;
            case Constant.TRANSACTION_TIMEOUT:
            case Constant.TRANSACTION_FAILURE:
                String title = msg.getData().getString(Constant.DIALOG_TITLE);
                String message = msg.getData().getString(Constant.DIALOG_MESSAGE);
                promise.reject(CODE_ERROR, message);
                break;
        }
    }

    private Boolean IsIPv4(final String ipaddress) {
        sendBugsnagBreadcrumb("IsIPv4", new HashMap<String, Object>(){{
            put("ipaddress", ipaddress);
        }});
        final String IPv4_REGEX =
                "^(25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)\\." +
                        "(25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)\\." +
                        "(25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)\\." +
                        "(25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)$";

        final Pattern IPv4_PATTERN = Pattern.compile(IPv4_REGEX);

        Matcher matcher = IPv4_PATTERN.matcher(ipaddress);

        return matcher.matches();
    }

    /*
     * This method is for the first time setup of CommSetting
     * */
    private static CommSetting setupSetting(Context context) {
        sendBugsnagBreadcrumb("setupSetting");

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


    private static String getElementByTagName(final Document document, final String tagName) {
        sendBugsnagBreadcrumb("getElementByTagName", new HashMap<String, Object>(){{
            put("document", document);
            put("tagName", tagName);
        }});

        // Returns value of first element matching tag name
        NodeList nodeList = document.getElementsByTagName(tagName);
        if (nodeList == null)
            return "";

        if (nodeList.getLength() ==  0)
            return "";

        return nodeList.item(0).getFirstChild().getNodeValue();

    }

    private static Document convertStringToXMLDocument(final String xmlString)
    {
        sendBugsnagBreadcrumb("convertStringToXMLDocument", new HashMap<String, Object>(){{
            put("xmlString", xmlString);
        }});
        //Parser that produces DOM object trees from XML content
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();

        //API to obtain DOM Document instance
        DocumentBuilder builder = null;
        try
        {
            //Create DocumentBuilder with default configuration
            builder = factory.newDocumentBuilder();

            //Parse the content to Document object
            Document doc = builder.parse(new InputSource(new StringReader(xmlString)));
            return doc;
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
        return null;
    }

    public static SessionInfo getSessionInfo() {
        sendBugsnagBreadcrumb("getSessionInfo");
        try {
            Realm realm = getRealmConfiguration();
            return realm.where(SessionInfo.class).findFirst();
        } catch (Error e) {
            return null;
        }
    }

    /**
     * @param promise
     */
    @ReactMethod
    public void getUUID(final Promise promise) {
        sendBugsnagBreadcrumb("getUUID");
        promise.resolve(UUID.randomUUID().toString());
    }

    private static void sendBugsnagBreadcrumb(String methodName) {
        sendBugsnagBreadcrumb(methodName, null);
    }

    private static void sendBugsnagBreadcrumb(String methodName, Map<String, Object> parameters) {
        if (parameters != null) {
            Bugsnag.leaveBreadcrumb(methodName, parameters, BreadcrumbType.LOG);
        } else {
            Bugsnag.leaveBreadcrumb(methodName);
        }
    }
}
