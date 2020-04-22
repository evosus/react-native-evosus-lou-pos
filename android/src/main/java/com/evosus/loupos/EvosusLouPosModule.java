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

import com.evosus.loupos.models.CustomerDisplay;
import com.evosus.loupos.models.LOUAPIJWT;
import com.evosus.loupos.models.SKU;
import com.evosus.loupos.models.TSYSMerchant;
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
import java.util.Arrays;
import java.util.Timer;
import java.util.TimerTask;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import io.realm.Case;
import io.realm.Realm;
import io.realm.RealmConfiguration;
import io.realm.RealmList;
import io.realm.RealmResults;

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
        if (getCurrentActivity() != null)
            getCurrentActivity().stopService(new Intent(reactContext, CustomerDisplayService.class));
    }

    /**
     *
     */
    @ReactMethod
    public void setCustomerDisplayTrxMD(String trxMarkdown) {

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

        OneShotManageTask oneShotManageTask = new OneShotManageTask(promise, manageRequest, "INIT");
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
        if (!validatePOSLink(promise)) return;

        ReportRequest reportRequest = new ReportRequest();
        reportRequest.TransType = reportRequest.ParseTransType("LOCALDETAILREPORT");
        reportRequest.EDCType = reportRequest.ParseEDCType("ALL");
        reportRequest.LastTransaction = "1";

        OneShotReportTask oneShotReportTask = new OneShotReportTask(promise, reportRequest, "LOCALDETAILREPORT");
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
    public void creditSale(String amount, String referenceNumber, String poNum, String taxAmt, String tipAmt, String extData, Promise promise) {

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
        OneShotPaymentTask2 oneShotPaymentTask = new OneShotPaymentTask2(promise, paymentRequest, "SALE");
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
    public void creditAuth(String amount, String referenceNumber, String poNum, String extData, Promise promise) {

        if (!validatePOSLink(promise)) return;

        PaymentRequest paymentRequest = new PaymentRequest();
        paymentRequest.TenderType = paymentRequest.ParseTenderType("CREDIT");
        paymentRequest.TransType = paymentRequest.ParseTransType("AUTH");
        paymentRequest.ECRRefNum = referenceNumber;
        paymentRequest.Amount = amount; // It is expected that $1.23 will arrive as "123", $0.09 as "9"
        paymentRequest.PONum = poNum;
        paymentRequest.ExtData = extData;

        // Recommend to use single thread pool instead.
        OneShotPaymentTask2 oneShotPaymentTask = new OneShotPaymentTask2(promise, paymentRequest, "AUTH");
        oneShotPaymentTask.execute();
    }

    /**
     * @param amount
     * @param referenceNumber
     * @param promise
     */
    @ReactMethod
    public void creditSaleEBT(String amount, String referenceNumber, Promise promise) {

        if (!validatePOSLink(promise)) return;

        PaymentRequest paymentRequest = new PaymentRequest();
        paymentRequest.TenderType = paymentRequest.ParseTenderType("EBT");
        paymentRequest.TransType = paymentRequest.ParseTransType("SALE");
        paymentRequest.ECRRefNum = referenceNumber;
        paymentRequest.Amount = amount; // It is expected that $1.23 will arrive as "123", $0.09 as "9"

        // Recommend to use single thread pool instead.
        OneShotPaymentTask2 oneShotPaymentTask = new OneShotPaymentTask2(promise, paymentRequest, "SALE");
        oneShotPaymentTask.execute();

    }

    /**
     * @param amount
     * @param referenceNumber
     * @param promise
     */
    @ReactMethod
    public void creditRefund(String amount, String referenceNumber, Promise promise) {

        if (!validatePOSLink(promise)) return;

        PaymentRequest paymentRequest = new PaymentRequest();
        paymentRequest.TenderType = paymentRequest.ParseTenderType("CREDIT");
        paymentRequest.TransType = paymentRequest.ParseTransType("RETURN");
        paymentRequest.ECRRefNum = referenceNumber;
        paymentRequest.Amount = amount; // It is expected that $1.23 will arrive as "123", $0.09 as "9"

        // Recommend to use single thread pool instead.
        OneShotPaymentTask2 oneShotPaymentTask = new OneShotPaymentTask2(promise, paymentRequest, "RETURN");
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
    public void creditVoid(String amount, String referenceNumber, String origRefNum, String origECRRefNum, Promise promise) {

        if (!validatePOSLink(promise)) return;

        PaymentRequest paymentRequest = new PaymentRequest();
        paymentRequest.TenderType = paymentRequest.ParseTenderType("CREDIT");
        paymentRequest.TransType = paymentRequest.ParseTransType("VOID");
        paymentRequest.ECRRefNum = referenceNumber;
        paymentRequest.OrigRefNum = origRefNum;
        paymentRequest.OrigECRRefNum = origECRRefNum;

        // Recommend to use single thread pool instead.
        OneShotPaymentTask2 oneShotPaymentTask = new OneShotPaymentTask2(promise, paymentRequest, "VOID");
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
    public void creditForceAuth(String amount, String referenceNumber, String authCode, String poNum, String taxAmt, String extData, Promise promise) {

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
        OneShotPaymentTask2 oneShotPaymentTask = new OneShotPaymentTask2(promise, paymentRequest, "FORCEAUTH");
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
    public void debitSale(String amount, String referenceNumber, String poNum, String taxAmt, String tipAmt, String extData, Promise promise) {

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
        OneShotPaymentTask2 oneShotPaymentTask = new OneShotPaymentTask2(promise, paymentRequest, "SALE");
        oneShotPaymentTask.execute();

    }

    /**
     * @param amount
     * @param referenceNumber
     * @param promise
     */
    @ReactMethod
    public void debitRefund(String amount, String referenceNumber, Promise promise) {

        if (!validatePOSLink(promise)) return;

        PaymentRequest paymentRequest = new PaymentRequest();
        paymentRequest.TenderType = paymentRequest.ParseTenderType("DEBIT");
        paymentRequest.TransType = paymentRequest.ParseTransType("RETURN");
        paymentRequest.ECRRefNum = referenceNumber;
        paymentRequest.Amount = amount; // It is expected that $1.23 will arrive as "123", $0.09 as "9"

        // Recommend to use single thread pool instead.
        OneShotPaymentTask2 oneShotPaymentTask = new OneShotPaymentTask2(promise, paymentRequest, "RETURN");
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
    public void checkPOSLink(String CommType, String Timeout, String IPAddress, boolean EnableProxy, final Promise promise) {
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
        PosLink posLink1 = POSLinkCreator.createPoslink(getReactApplicationContext());

        String iniFile = getReactApplicationContext().getFilesDir().getAbsolutePath() + "/" + SettingINI.FILENAME;
        commSetting = SettingINI.getCommSettingFromFile(iniFile);
        posLink1.SetCommSetting(commSetting);

        return posLink1;
    }

    private void initPOSLink() {
        bInited = true;
        AppThreadPool.getInstance();
        commSetting = setupSetting(getReactApplicationContext());
        POSLinkAndroid.init(getReactApplicationContext(), commSetting);
    }

    private Realm getRealmConfiguration() {
        // The RealmConfiguration is created using the builder pattern.
        // The Realm file will be located in Context.getFilesDir() with name "myrealm.realm"
        RealmConfiguration config = new RealmConfiguration.Builder()
                .name("evosus.db")
                .schemaVersion(42)
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

        if (!validatePOSLink(promise)) return;

        BatchRequest batchRequest = new BatchRequest();
        batchRequest.TransType = batchRequest.ParseTransType("BATCHCLOSE");
        batchRequest.EDCType = batchRequest.ParseEDCType(EDCType.ALL);

        // Recommend to use single thread pool instead.
        OneShotBatchTask oneShotBatchTask = new OneShotBatchTask(promise, batchRequest, "BATCHCLOSE");
        oneShotBatchTask.execute();

    }

    /**
     * @param entityName
     * @param jsonString
     * @param promise
     */
    @ReactMethod
    public void loadRealmFromJSON(String entityName, String jsonString, Promise promise) {

        Realm realm = getRealmConfiguration();
        if (realm == null)
            promise.resolve(false);

        switch (entityName) {
            case "ProductSetup.CustomerDisplay":
                realm.beginTransaction();
                realm.createOrUpdateAllFromJson(CustomerDisplay.class, jsonString);
                //final RealmResults<CustomerDisplay> customerDisplays = realm.where(CustomerDisplay.class).findAll();
                realm.commitTransaction();
//                Log.i(this.getName(), "Count " + entityName + ": " + customerDisplays.size());
                break;
            case "Inventory.SKU":
                realm.beginTransaction();
                realm.createOrUpdateAllFromJson(SKU.class, jsonString);
                //final RealmResults<SKU> skus = realm.where(SKU.class).findAll();
                realm.commitTransaction();
//                Log.i(this.getName(), "Count " + entityName + ": " + skus.size());
                break;
            case "LOUAPI.LOUAPIJWT":
                realm.beginTransaction();
                realm.createOrUpdateAllFromJson(LOUAPIJWT.class, jsonString);
//                final RealmResults<LOUAPIJWT> louapijwts = realm.where(LOUAPIJWT.class).findAll();
                realm.commitTransaction();
//                Log.i(this.getName(), "Count " + entityName + ": " + louapijwts.size());
                break;
            case "TSYS.TSYSMerchant":
                realm.beginTransaction();
                realm.createOrUpdateAllFromJson(TSYSMerchant.class, jsonString);
//                final RealmResults<TSYSMerchant> tsysMerchants = realm.where(TSYSMerchant.class).findAll();
                realm.commitTransaction();
//                Log.i(this.getName(), "Count " + entityName + ": " + tsysMerchants.size());
                break;
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
    public void findFirstRealmEntityByID(String entityName, String entityID, Boolean UseSKUID, Promise promise) {

        Realm realm = getRealmConfiguration();

        if (realm == null)
            promise.resolve(false);

        switch (entityName) {
            case "ProductSetup.CustomerDisplay":
                final CustomerDisplay customerDisplay = realm.where(CustomerDisplay.class).equalTo("CustomerVanityID", entityID).findFirst();
                if (customerDisplay != null) {
                    promise.resolve(new Gson().toJson(realm.copyFromRealm(customerDisplay)));
                    Log.i(this.getName(), "Lookup on CustomerVanityID for " + entityName);
                } else {
                    promise.reject("CustomerDisplay not found.");
                }
                break;
            case "Inventory.SKU":
                Log.e(this.getName(), "UseSKUID = " + UseSKUID);
                final SKU sku = UseSKUID?realm.where(SKU.class).equalTo("ReadableID", entityID).findFirst():realm.where(SKU.class).equalTo("UPC", entityID).or().equalTo("MySKU", entityID).findFirst();
                if (sku != null) {
                    promise.resolve(new Gson().toJson(realm.copyFromRealm(sku)));
                    Log.i(this.getName(), "Lookup on ReadableID for " + entityName);
                } else {
                    promise.reject("SKU not found.");
                }
                break;
            case "LOUAPI.LOUAPIJWT":
                final LOUAPIJWT louapijwt = realm.where(LOUAPIJWT.class).equalTo("Key", entityID).findFirst();
                promise.resolve(new Gson().toJson(realm.copyFromRealm(louapijwt)));
                Log.i(this.getName(), "Lookup on Key for " + entityName);
                break;
            case "TSYS.TSYSMerchant":
                final TSYSMerchant tsysMerchant = realm.where(TSYSMerchant.class).equalTo("_ID", entityID).findFirst();
                promise.resolve(new Gson().toJson(realm.copyFromRealm(tsysMerchant)));
                Log.i(this.getName(), "Lookup on _ID for " + entityName);
                break;
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
    public void findAllEntity(String entityName, String searchString, Integer limit, Promise promise) {
        Realm realm = getRealmConfiguration();
        Boolean search = (searchString != null && !searchString.isEmpty());
        if (realm == null)
            promise.resolve(false);

        Log.i(this.getName(), "Searching: " + search);
        Log.i(this.getName(), "Limiting results to " + limit);
        if (search) Log.i(this.getName(), "Search String: " + searchString);
        switch (entityName) {
            case "ProductSetup.CustomerDisplay":
                final RealmResults<CustomerDisplay> customerDisplay = search?realm.where(CustomerDisplay.class)
                        .contains("DropdownSearchString", searchString, Case.INSENSITIVE)
                        .limit(limit)
                        .findAll():realm.where(CustomerDisplay.class).limit(limit).findAll();
                promise.resolve(new Gson().toJson(realm.copyFromRealm(customerDisplay)));
                Log.i(this.getName(), "Find All " + entityName);
                break;
            case "Inventory.SKU":
                final RealmResults<SKU> sku = search?realm.where(SKU.class)
                        .contains("MySKU", searchString, Case.INSENSITIVE)
                        .or()
                        .contains("UPC", searchString, Case.INSENSITIVE)
                        .or()
                        .contains("Description", searchString, Case.INSENSITIVE)
                        .limit(limit)
                        .findAll():realm.where(SKU.class).limit(limit).findAll();
                promise.resolve(new Gson().toJson(realm.copyFromRealm(sku)));
                Log.i(this.getName(), "Find All  " + entityName);
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
    public void deleteRealmEntity(String entityName, Promise promise) {
        Realm realm = getRealmConfiguration();

        if (realm == null)
            promise.resolve(false);

        switch (entityName) {
            case "ProductSetup.CustomerDisplay":
                realm.beginTransaction();
                realm.delete(CustomerDisplay.class);
                Log.i(this.getName(), "Deleted realm entity " + entityName);
                realm.commitTransaction();
                break;
            case "Inventory.SKU":
                realm.beginTransaction();
                realm.delete(SKU.class);
                Log.i(this.getName(), "Deleted realm entity " + entityName);
                realm.commitTransaction();
                break;
            case "LOUAPI.LOUAPIJWT":
                realm.delete(LOUAPIJWT.class);
                Log.i(this.getName(), "Deleted realm entity " + entityName);
                break;
            case "TSYS.TSYSMerchant":
                realm.delete(TSYSMerchant.class);
                Log.i(this.getName(), "Deleted realm entity " + entityName);
                break;
        }

        // This is important
        realm.close();

        promise.resolve(true);
    }

    /**
     * @param promise
     */
    @ReactMethod
    public void deleteRealmDB(Promise promise) {

        Realm.deleteRealm(Realm.getDefaultConfiguration());

        Log.i(this.getName(), "DeleteRealm");
        promise.resolve(true);
    }

    /**
     * @param entityName
     * @param promise
     */
    @ReactMethod
    public void countEntity(String entityName, Promise promise) {
        Realm realm = getRealmConfiguration();
        Log.i(this.getName(), "Counting " + entityName);
        switch (entityName) {
            case "ProductSetup.CustomerDisplay":
                final long CustomerDisplays = realm.where(CustomerDisplay.class).count();
//                Integer i = new Integer(CustomerDisplays);
                promise.resolve((int)CustomerDisplays);
                Log.i(this.getName(), "Counted " + CustomerDisplays + ' ' +  entityName);
                break;
            case "Inventory.SKU":
                final long SKUs = realm.where(SKU.class).count();
                promise.resolve((int)SKUs);
                Log.i(this.getName(), "Counted " + SKUs + ' ' +  entityName);
                break;
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

        OneShotManageTask oneShotManageTask = new OneShotManageTask(promise, manageRequest, "REBOOT");
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
    public void setTransactionKey(String userName, String userPassword, String mid, String deviceID, Promise promise) {

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

        OneShotManageTask oneShotManageTask = new OneShotManageTask(promise, manageRequest, "SETTRANSACTIONKEY");
        oneShotManageTask.execute();

    }

    /**
     * @param receiptText
     * @param promise
     */
    @ReactMethod
    public void printReceipt(final String receiptText, final Promise promise) {

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

    private class OneShotPaymentTask2  extends AsyncTask<Void, Void, Void> {
        private ProgressDialog dialog;
        private PaymentRequest l_paymentRequest;
        private Promise l_promise;
        private String l_transType;
        private PosLink l_posLink;
        private ProcessTransResult l_ptr;
        private boolean l_allowCancel;
        private int l_timeToAllowCancel;

        public OneShotPaymentTask2(Promise promise, PaymentRequest paymentRequest, String transType) {

            l_paymentRequest = paymentRequest;
            l_promise= promise;
            l_transType = transType;
            l_posLink = getPOSLink();
            l_allowCancel = false;

            TimerTask l_task = new TimerTask() {
                @Override
                public void run() {
                    l_allowCancel = true;
                }
            };
            Timer l_timer = new Timer();
            l_timer.schedule(l_task, 5000); // 5 seconds

            dialog = new ProgressDialog(getCurrentActivity());
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

            l_posLink.PaymentRequest = l_paymentRequest;

            // ProcessTrans is Blocking call, will return when the transaction is complete.
            CountRunTime.start("Payment");
            l_ptr = l_posLink.ProcessTrans();
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
            // do UI work here
            if (dialog.isShowing()) {
                dialog.dismiss();
            }
        }
    }

    private class OneShotBatchTask  extends AsyncTask<Void, Void, Void> {
        private ProgressDialog dialog;
        private BatchRequest l_batchRequest;
        private Promise l_promise;
        private PosLink l_posLink;
        private String l_transType;
        private ProcessTransResult l_ptr;

        public OneShotBatchTask(Promise promise, BatchRequest request, String transType) {

            l_batchRequest = request;
            l_promise= promise;
            l_transType = transType;
            l_posLink = getPOSLink();

            dialog = new ProgressDialog(getCurrentActivity());
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
            if (dialog != null) {
                dialog.setMessage("Processing...");
                dialog.show();
            }
        }

        @Override
        protected Void doInBackground(Void... args) {

            l_posLink.BatchRequest = l_batchRequest;

            // ProcessTrans is Blocking call, will return when the transaction is complete.
            CountRunTime.start("Batch");
            l_ptr = l_posLink.ProcessTrans();
            CountRunTime.countPoint("Batch");
            getCurrentActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    taskCompleted(l_posLink, l_ptr, l_promise, l_transType);

                }
            });

            return null;
        }
        @Override
        protected void onPostExecute(Void result) {
            // do UI work here
            if (dialog.isShowing()) {
                dialog.dismiss();
            }
        }
    }

    private class OneShotManageTask  extends AsyncTask<Void, Void, Void> {
        private ProgressDialog dialog;
        private ManageRequest l_manageRequest;
        private Promise l_promise;
        private String l_transType;
        private PosLink l_posLink;
        private ProcessTransResult l_ptr;

        public OneShotManageTask(Promise promise, ManageRequest request, String transType) {

            l_manageRequest = request;
            l_promise= promise;
            l_transType = transType;
            l_posLink = getPOSLink();

            dialog = new ProgressDialog(getCurrentActivity());
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
            if (dialog != null) {
                dialog.setMessage("Processing...");
                dialog.show();
            }
        }

        @Override
        protected Void doInBackground(Void... args) {

            l_posLink.ManageRequest = l_manageRequest;

            // ProcessTrans is Blocking call, will return when the transaction is complete.
            CountRunTime.start("Manage");
            l_ptr = l_posLink.ProcessTrans();
            CountRunTime.countPoint("Manage");
            getCurrentActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    taskCompleted(l_posLink, l_ptr, l_promise, l_transType);
                }
            });

            return null;
        }
        @Override
        protected void onPostExecute(Void result) {
            // do UI work here
            if (dialog.isShowing()) {
                dialog.dismiss();
            }
        }
    }

    private class OneShotReportTask  extends AsyncTask<Void, Void, Void> {
        private ProgressDialog dialog;
        private ReportRequest l_reportRequest;
        private Promise l_promise;
        private String l_transType;
        private PosLink l_posLink;
        private ProcessTransResult l_ptr;

        public OneShotReportTask(Promise promise, ReportRequest request, String transType) {

            l_reportRequest = request;
            l_promise= promise;
            l_transType = transType;
            l_posLink = getPOSLink();

            dialog = new ProgressDialog(getCurrentActivity());
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
            if (dialog != null) {
                dialog.setMessage("Processing...");
                dialog.show();
            }
        }

        @Override
        protected Void doInBackground(Void... args) {

            l_posLink.ReportRequest = l_reportRequest;

            // ProcessTrans is Blocking call, will return when the transaction is complete.
            CountRunTime.start("Report");
            l_ptr = l_posLink.ProcessTrans();
            CountRunTime.countPoint("Report");
            getCurrentActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    taskCompleted(l_posLink, l_ptr, l_promise, l_transType);
                }
            });

            return null;
        }
        @Override
        protected void onPostExecute(Void result) {
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

    private void taskCompleted(PosLink poslink, ProcessTransResult ptr, Promise promise, String transType) {
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

    public void handleMessage(Message msg, Promise promise, String transType) {

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

    /*
     * This method is for the first time setup of CommSetting
     * */
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


    private static String getElementByTagName(Document document, String tagName) {

        // Returns value of first element matching tag name
        NodeList nodeList = document.getElementsByTagName(tagName);
        if (nodeList == null)
            return "";

        if (nodeList.getLength() ==  0)
            return "";

        return nodeList.item(0).getFirstChild().getNodeValue();

    }

    private static Document convertStringToXMLDocument(String xmlString)
    {
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

}
