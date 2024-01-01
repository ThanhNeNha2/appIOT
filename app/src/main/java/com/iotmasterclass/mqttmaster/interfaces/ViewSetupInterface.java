package com.iotmasterclass.mqttmaster.interfaces;

public interface ViewSetupInterface {
    void initViewModels();
    void initViews();
    void initListeners();
    void initViewLoaded();

    default void initViewSetupInterfaceMethods(){
        initViewModels();
        initViews();
        initListeners();
        initViewLoaded();
    }
}
