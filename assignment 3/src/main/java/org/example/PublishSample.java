package org.example;

import org.eclipse.paho.client.mqttv3.*;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;

public class PublishSample {
    public static MqttConnectOptions setOptions(String username, String password) {
        MqttConnectOptions options = new MqttConnectOptions();
        options.setUserName(username);
        options.setPassword(password.toCharArray());
        options.setConnectionTimeout(60);
        options.setKeepAliveInterval(60);
        return options;
    }

    public static MqttClient createClient(String broker, String clientid, String username, String password) throws MqttException {
        MqttClient client = new MqttClient(broker, clientid, new MemoryPersistence());
        MqttConnectOptions options = setOptions(username, password);
        client.connect(options);

        return client;
    }

    public static void main(String[] args) {

        String broker = "tcp://broker.emqx.io:1883";
        String topic = "mqtt/test";
        String username = "emqx";
        String password = "public";
        String clientid = "publish_client";
        String content = "Hello MQTT";
        int qos = 0;

        try {
            // create client
            MqttClient client = createClient(broker, clientid, username, password);

            // create message and setup QoS
            client.publish(topic, content.getBytes(), 0, Boolean.FALSE);
            client.disconnect();
            client.close();
        } catch (MqttException e) {
            throw new RuntimeException(e);
        }
    }
}

