package org.example;

import org.eclipse.paho.client.mqttv3.*;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;

public class SubscribeSample {
    public static void main(String[] args) {
        String broker = "tcp://broker.emqx.io:1883";
        String topic = "mqtt/test";
        String username = "emqx";
        String password = "public";
        String clientid = "subscribe_client";
        int qos = 0;

        try {
            MqttClient client = PublishSample.createClient(broker, clientid, username, password);
            client.setCallback(new CallBack());
            client.subscribe(topic, qos);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
