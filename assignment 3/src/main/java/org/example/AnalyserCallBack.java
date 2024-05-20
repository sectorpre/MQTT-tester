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
        stat.numberOfDisconnects += 1;
        stat.disconnect = 1;
    }

    public void messageArrived(String topic, MqttMessage message) {
        Integer count = Integer.parseInt(new String(message.getPayload()));
        int currentPub = Integer.parseInt(topic.substring(8,9)) - 1;

        // if the counter within the message is not one more than the analyser's counter
        if (stat.eachPubCount[currentPub] + 1 != count) {
            stat.rateOfOutOfOrder += 1;
            stat.eachPubCount[currentPub] = count;
            return;
        }

        // updates analyser counter to the current counter in the message
        stat.eachPubCount[currentPub] = count;
        // pushes new timeframe into timestamps stack
        stat.timeStamps.get(currentPub).push(System.currentTimeMillis());

        stat.totaMessageCount += 1;
    }

    public void deliveryComplete(IMqttDeliveryToken token) {
    }
}
