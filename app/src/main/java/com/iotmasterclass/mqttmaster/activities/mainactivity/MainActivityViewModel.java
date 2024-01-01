package com.iotmasterclass.mqttmaster.activities.mainactivity;

import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import java.util.HashMap;

public class MainActivityViewModel extends ViewModel {
    public MainActivityViewModel() {
        topicMessage = new MutableLiveData<>();
        topicMessage.setValue(new HashMap<>());
    }

    public String getClientId() {
        return clientId;
    }

    public void setClientId(String clientId) {
        this.clientId = clientId;
    }

    public String getConnectionMessage() {
        return connectionMessage;
    }

    public void setConnectionMessage(String connectionMessage) {
        this.connectionMessage = connectionMessage;
    }

    public HashMap<String, Object> getMessageForTopic(String topic) {
        return topicMessage.getValue().get(topic);
    }

    public void setMessageForTopic(String topic, HashMap<String, Object> message) {
        HashMap<String, HashMap<String, Object>> messageData = this.topicMessage.getValue();
        messageData.put(topic, message);
        topicMessage.setValue(messageData);
    }


    public MutableLiveData<HashMap<String, HashMap<String, Object>>> getTopicMessage() {
        return topicMessage;
    }

    private MutableLiveData<HashMap<String, HashMap<String, Object>>> topicMessage;
    private String connectionMessage;
    private String clientId;

    public final String MQTT_TELEMETRY_TOPIC    = "iot/telemetry";
    public final String MQTT_CONTROL_TOPIC    = "iot/control";
    public final String MQTT_BROKER = "mqtt-dashboard.com";

}
