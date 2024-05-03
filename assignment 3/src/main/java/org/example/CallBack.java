package org.example;

import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttMessage;

public class CallBack implements MqttCallback {
    public void connectionLost(Throwable cause) {
        System.out.println("connectionLost: " + cause.getMessage());
    }

    public void messageArrived(String topic, MqttMessage message) {
        System.out.println("topic: " + topic);
        System.out.println("Qos: " + message.getQos());
        System.out.println("message content: " + new String(message.getPayload()));

    }

    public void deliveryComplete(IMqttDeliveryToken token) {
        System.out.println("deliveryComplete---------" + token.isComplete());
    }
}
