package org.example;

import org.eclipse.paho.client.mqttv3.*;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;

public class MQTTclient {
    public static MqttConnectOptions setOptions(String username, String password) {
        MqttConnectOptions options = new MqttConnectOptions();
        options.setUserName(username);
        options.setPassword(password.toCharArray());
        options.setAutomaticReconnect(true);
        options.setConnectionTimeout(0);
        options.setKeepAliveInterval(60);
        return options;
    }

    public static MqttClient createClient(String broker, String clientid, String username, String password) throws MqttException {
        MqttClient client = new MqttClient(broker, clientid, new MemoryPersistence());
        MqttConnectOptions options = setOptions(username, password);
        client.connect(options);

        return client;
    }
}

