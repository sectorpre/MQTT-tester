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

    public static void subscribeTopics( PubCommand pubc, MqttClient client) throws MqttException {
        for (int k = 1; k <= pubc.instanceCount; k++) {
            //counter/<instance>/<qos>/<delay>
            String topic = String.format("counter/%d/%d/%d",k, pubc.qos,pubc.delay);
            client.subscribe(topic, pubc.qos); //TODO change this to pub qos
        }
    }

    public static void unSubscribeTopics( PubCommand pubc, MqttClient client) throws MqttException {
        for (int k = 1; k <= pubc.instanceCount; k++) {
            //counter/<instance>/<qos>/<delay>
            String topic = String.format("counter/%d/%d/%d",k, pubc.qos,pubc.delay);
            client.unsubscribe(topic); //TODO change this to pub qos
        }
    }

    public static void publishCommand(PubCommand pubc, MqttClient client) throws MqttException {
        client.publish("request/qos", Integer.toString(pubc.qos).getBytes(), 1, Boolean.FALSE);
        client.publish("request/delay", Integer.toString(pubc.delay).getBytes(), 1, Boolean.FALSE);
        client.publish("request/instancecount", Integer.toString(pubc.instanceCount).getBytes(), 1, Boolean.FALSE);
    }

    public static void receiveStats(MqttClient client, PubCommand command) throws InterruptedException, MqttException {
        AnalystStat stat = new AnalystStat();

        client.setCallback(new AnalyserCallBack(stat));
        subscribeTopics(command, client);
        publishCommand(command, client);

        Thread.sleep(Publisher.duration);

        unSubscribeTopics(command,client);
        stat.printAllStats();

    }

    public static void main(String[] args) {
        // Create a Scanner object to read input from the command line
        // MQTT QoS level (0, 1 or 2), and with a
        // requested delay(0ms, 1ms, 2ms, 4ms) for 60 seconds.

        try {
            MqttClient client = MQTTclient.createClient(broker, clientid, username, password);
            for (int k = 1; k <= 5; k++) {
                for (int qos = 0; qos < 3; qos ++) {
                    for (int delay = 0; delay < 5; delay ++) {
                        if (delay == 3) {continue;}
                        System.out.printf("running for delay:%d, QoS:%d, instance-count:%d\n", delay,qos,k);
                        PubCommand currPubCommand = new PubCommand(qos, delay, k);
                        receiveStats(client, currPubCommand);}
                }

            }

        } catch (MqttException e) {
            throw new RuntimeException(e);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }
}
