package com.evosus.loupos;

import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.uimanager.SimpleViewManager;
import com.facebook.react.uimanager.ThemedReactContext;

public class CustomerFacingManager extends SimpleViewManager<CustomerFacingView> {
    public static final String REACT_CLASS = "RCTCustomerFacingView";
    private CustomerFacingView customerFacingView;

    public CustomerFacingManager(ReactApplicationContext reactContext) {
        super();
    }

    @Override
    public String getName() {
        return REACT_CLASS;
    }

    @Override
    public CustomerFacingView createViewInstance(ThemedReactContext context) {
        customerFacingView  = new CustomerFacingView(context, null);
        return customerFacingView;
    }

    public CustomerFacingView getCustomerFacingView() { // <-- returns the View instance
        return customerFacingView;
    }

}
