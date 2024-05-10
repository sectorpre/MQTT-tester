package org.example;

import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttMessage;


public class AnalyserCallBack implements MqttCallback {
    AnalystStat stat;
    public AnalyserCallBack(AnalystStat stat) {
        this.stat = stat;
    }

    public void connectionLost(Throwable cause) {
        System.out.println("connectionLost: " + cause.getMessage());
    }

    public void messageArrived(String topic, MqttMessage message) {
        Integer count = Integer.parseInt(new String(message.getPayload()));
        stat.totaMessageCount += 1;

        int currentPub = Integer.parseInt(topic.split("/")[1]) - 1;
        if (stat.eachPubCount[currentPub] > count) {
            System.out.println("out of order =======================");
            stat.rateOfMessageLoss += 1;
        }

        stat.eachPubCount[currentPub] = count;
        stat.timeStamps.get(currentPub).push(System.currentTimeMillis());
    }

    public void deliveryComplete(IMqttDeliveryToken token) {
    }
}
