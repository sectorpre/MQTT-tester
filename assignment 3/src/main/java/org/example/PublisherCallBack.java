package org.example;

import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttMessage;

public class PublisherCallBack implements MqttCallback {
    public PublisherCallBack(Publisher client) {
        this.client = client;
    }
    Publisher client;

    public void connectionLost(Throwable cause) {
        System.out.println("connectionLost: " + cause.getMessage());
    }

    /**
     * Receives a message from a broker topic
     * Determines the QoS, delay and instancecount the publisher should
     * be publishing at.
     *
     * client.subscribe("request/qos", qos);
     *             client.subscribe("request/delay", qos);
     *             client.subscribe("request/instancecount", qos);
     * */
    public void messageArrived(String topic, MqttMessage message) {
        //System.out.println("topic received " + topic);
        switch (topic) {
            case "request/qos" -> client.qos = Integer.parseInt(new String(message.getPayload()));
            case "request/delay" -> client.delay = Integer.parseInt(new String(message.getPayload()));
            case "request/instancecount" -> {
                int instance = Integer.parseInt(new String(message.getPayload()));
                if (instance >= client.id) {
                    client.run = Boolean.TRUE;
                } else {
                    client.qos = -1;
                    client.delay = -1;
                }
            }
        }
        //System.out.printf("qos:%d delay:%d run:%s\n", client.qos,client.delay,client.run);

//        System.out.println("topic: " + topic);
//        System.out.println("Qos: " + message.getQos());
//        System.out.println("message content: " + new String(message.getPayload()));

    }

    public void deliveryComplete(IMqttDeliveryToken token) {
        //System.out.println("deliveryComplete---------" + token.isComplete());
    }
}
