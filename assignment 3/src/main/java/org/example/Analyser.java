package org.example;

import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttException;

import java.util.Scanner;

import static org.example.MQTTclient.createClient;

/**
 * our Analyser will start by publishing to the request/qos, request/delay and
 * request/instancecount topics, asking for some number of Publishers to deliver accordingly.
 * o It will then listen to the specified counter topic(s) on the broker and take measurements (below)
 * to report statistics on the performance of the publisher/broker/network combination.
 * o The measurements should be taken across the range of delay (4), QoS (3), and instance-count (5)
 * values as above, so that you can compare them; things can get weird under load.
 * o Run it with all three QoS values for the Broker->Analyser subscription as well; things can get weird
 * when the Publisher and Subscriber have very different QoS. You may need to disconnect and
 * reconnect when changing the subscription QoS.
 * o Yes, thats 3*3*4*5=180 tests, each taking 1min. Fortunately your code could do it all for you.
 * */
public class Analyser {


    static String broker = "tcp://127.0.0.1:1883";
    static String username = "admin";
    static String password = "password";
    static String clientid = "analyser";
    static State curentState;

    enum State {
        SUBBING, PUBBING
    }

    public static void subscribeTopics( int qos, int delay, int instanceCount, MqttClient client) throws MqttException {
        for (int k = 1; k <= instanceCount; k++) {
            //counter/<instance>/<qos>/<delay>
            String topic = String.format("counter/%d/%d/%d",k, qos,delay);
            client.subscribe(topic, qos); //TODO change this to pub qos
        }
    }
    public static void publishCommand(int qos, int  delay,int instance, MqttClient client) throws MqttException {
        client.publish("request/qos", Integer.toString(qos).getBytes(), 1, Boolean.FALSE);
        client.publish("request/delay", Integer.toString(delay).getBytes(), 1, Boolean.FALSE);
        client.publish("request/instancecount", Integer.toString(instance).getBytes(), 1, Boolean.FALSE);
    }


    public static void main(String[] args) {
        // Create a Scanner object to read input from the command line
        System.out.printf("running for delay:%d, QoS:%d, instance-count:%d", 0,0,1);

        //delay: (0ms, 1ms, 2ms, 4ms)
        //QoS: (0, 1 or 2)
        //instance-count: (1,2,3,4,5)
        //Broker QoS: (1,2,3)

        try {
            MqttClient client = createClient(broker, clientid, username, password);
            client.setCallback(new AnalyserCallBack());
            subscribeTopics(0, 0, 1, client);
            publishCommand(0,0,1,client);

//            client.disconnect();
//            client.close();
        } catch (MqttException e) {
            throw new RuntimeException(e);
        }
    }
}
