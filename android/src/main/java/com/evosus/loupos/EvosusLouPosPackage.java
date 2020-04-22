package com.evosus.loupos;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import com.facebook.react.ReactPackage;
import com.facebook.react.bridge.NativeModule;
import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.uimanager.ViewManager;
import com.facebook.react.bridge.JavaScriptModule;
import com.facebook.react.views.image.ReactImageManager;

import io.realm.Realm;

public class EvosusLouPosPackage implements ReactPackage {

    private CustomerFacingManager customerFacingManager;

    @Override
    public List<NativeModule> createNativeModules(ReactApplicationContext reactContext) {
//        if (customerFacingManager == null) {
//            customerFacingManager = new CustomerFacingManager(reactContext);
//        }
//        return Arrays.<NativeModule>asList(
//            new EvosusLouPosModule(reactContext, customerFacingManager)
//        );

        return Arrays.<NativeModule>asList(
                new EvosusLouPosModule(reactContext)
        );
    }

    @Override
    public List<ViewManager> createViewManagers(ReactApplicationContext reactContext) {
//        if (customerFacingManager == null) {
//            customerFacingManager = new CustomerFacingManager(reactContext);
//        }
//        return Arrays.<ViewManager>asList(
//            customerFacingManager
//        );
//
        return Collections.emptyList();
    }
}
