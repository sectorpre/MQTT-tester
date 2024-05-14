package org.example;

import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttMessage;


public class AnalyserCallBack implements MqttCallback {
    AnalystStat stat;
    int startOfOUTCOUNT = -1;
    public AnalyserCallBack(AnalystStat stat) {
        this.stat = stat;
    }

    public void connectionLost(Throwable cause) {
        System.out.println("connectionLost: " + cause.getMessage());
    }

    public void messageArrived(String topic, MqttMessage message) {
        Integer count = Integer.parseInt(new String(message.getPayload()));
        int currentPub = Integer.parseInt(topic.substring(8,9)) - 1;

        if (stat.eachPubCount[currentPub] + 1 != count) {
            System.out.println("redelivery of packet error " + count + " start: " + startOfOUTCOUNT);
            stat.rateOfOutOfOrder += 1;
            stat.eachPubCount[currentPub] = count;
            return;
        }
        stat.eachPubCount[currentPub] = count;
        stat.timeStamps.get(currentPub).push(System.currentTimeMillis());
        stat.totaMessageCount += 1;
    }

    public void deliveryComplete(IMqttDeliveryToken token) {
    }
}
