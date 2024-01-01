package com.iotmasterclass.mqttmaster.activities.mainactivity;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Bundle;

import com.google.android.material.snackbar.Snackbar;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.view.View;

import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.fragment.NavHostFragment;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import com.google.gson.Gson;
import com.hivemq.client.mqtt.datatypes.MqttQos;
import com.hivemq.client.mqtt.mqtt5.Mqtt5AsyncClient;
import com.hivemq.client.mqtt.mqtt5.Mqtt5Client;
import com.hivemq.client.mqtt.mqtt5.message.publish.Mqtt5Publish;
import com.iotmasterclass.mqttmaster.R;
import com.iotmasterclass.mqttmaster.databinding.ActivityMainBinding;
import com.iotmasterclass.mqttmaster.interfaces.ViewSetupInterface;

import android.view.Menu;
import android.view.MenuItem;

import java.util.HashMap;
import java.util.UUID;
import java.util.function.Consumer;

// Uses HiveMqtt: https://hivemq.github.io/hivemq-mqtt-client/docs/installation/android/

public class MainActivity extends AppCompatActivity implements ViewSetupInterface {

    private AppBarConfiguration appBarConfiguration;
    private ActivityMainBinding binding;
    public MainActivityViewModel mViewModel;
    private Mqtt5AsyncClient mqttAsyncClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initViewSetupInterfaceMethods();    //initViewModels() -> initViews() -> initListeners() -> initViewLoaded()
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onSupportNavigateUp() {
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment_content_main);
        return NavigationUI.navigateUp(navController, appBarConfiguration)
                || super.onSupportNavigateUp();
    }

    @Override
    public void initViewModels() {
        mViewModel = new ViewModelProvider(this).get(MainActivityViewModel.class);
    }

    @Override
    public void initViews() {
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        setSupportActionBar(binding.toolbar);

        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment_content_main);
        appBarConfiguration = new AppBarConfiguration.Builder(navController.getGraph()).build();
        NavigationUI.setupActionBarWithNavController(this, navController, appBarConfiguration);

    }

    @Override
    public void initListeners() {
        binding.fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

            }
        });

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.INTERNET)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.INTERNET}, 10);
        }

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.WAKE_LOCK)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WAKE_LOCK}, 11);
        }
    }

    @Override
    public void initViewLoaded() {

    }

    public void publishMessage(String topic, String message){
        if (mqttAsyncClient == null){
            return;
        }
        mqttAsyncClient.toBlocking().publishWith()
                .topic(topic)
                .qos(MqttQos.EXACTLY_ONCE)
                .payload(message.getBytes())
                .retain(true)
                .contentType("text/plain")  // our payload is text
                .messageExpiryInterval(120) // not so important, expire message after 2min if can not be delivered
                .send();

        System.out.println("published: " + message);
    }

    public void startMqttConnection(){
        mViewModel.setClientId(UUID.randomUUID().toString());
        String username = "";
        String password = "";

        Mqtt5Client mqttClient = Mqtt5Client.builder()
                .identifier(mViewModel.getClientId())
                .serverHost(mViewModel.MQTT_BROKER)
                .build();

        mqttAsyncClient = mqttClient.toAsync();

        mqttAsyncClient.connectWith()
                .simpleAuth()
                .username(username)
                .password(password.getBytes())
                .applySimpleAuth()
                .send()
                .whenComplete((connAck, throwable) -> {

                    if (connAck != null) {
                        System.out.println("Connected to MQTT broker!");

                        mqttAsyncClient.subscribeWith()
                                .topicFilter(mViewModel.MQTT_TELEMETRY_TOPIC)
                                .qos(MqttQos.AT_LEAST_ONCE)
                                .callback(mqtt5Publish -> {
                                    mqtt5Callback(mqtt5Publish);
                                })
                                .send()
                                .whenComplete((subAck, throwable1) -> {
                                    if (subAck != null) {
                                        System.out.println("Subscribed to topic: " + mViewModel.MQTT_TELEMETRY_TOPIC);
                                        getIoTDeviceStatus();
                                    } else {
                                        System.out.println("Subscribe failed: " + throwable1.getMessage());
                                    }
                                });
                    } else {
                        System.out.println("Connection failed: " + throwable.getMessage());
                    }
                });
    }

    public void getIoTDeviceStatus() {
        publishMessage(mViewModel.MQTT_CONTROL_TOPIC, "status");
    }

    public void getIoTDeviceStatus(String deviceId) {
        publishMessage(mViewModel.MQTT_CONTROL_TOPIC, deviceId + "/status");
    }

    private void mqtt5Callback(Mqtt5Publish mqtt5Publish) {
        String message = new String(mqtt5Publish.getPayloadAsBytes());
        String topic = mqtt5Publish.getTopic().toString();
        MainActivity.this.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Gson gson = new Gson();
                HashMap<String, Object> messageMap = gson.fromJson(message, HashMap.class);
                if(messageMap != null){
                    String dataType = messageMap.get("type").toString();
                    String deviceId = messageMap.get("device_id").toString();
                    if (dataType.equals(null)) {
                        mViewModel.setMessageForTopic(topic, messageMap);
                    } else {
                        mViewModel.setMessageForTopic(deviceId + "/" + topic + "/" + dataType, messageMap);
                    }
                }
            }
        });
    }


}