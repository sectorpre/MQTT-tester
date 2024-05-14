package org.example;

import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttException;

import java.io.FileWriter;
import java.io.IOException;
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

    public static void subscribeTopics( PubCommand pubc, MqttClient client, Integer subscribeQos) throws MqttException {
        for (int k = 1; k <= pubc.instanceCount; k++) {
            //counter/<instance>/<qos>/<delay>
            String topic = String.format("counter/%d/%d/%d",k, pubc.qos,pubc.delay);
            client.subscribe(topic, subscribeQos); //TODO change this to pub qos
        }
    }

    public static void unSubscribeTopics( PubCommand pubc, MqttClient client) throws MqttException {
        for (int k = 1; k <= pubc.instanceCount; k++) {
            //counter/<instance>/<qos>/<delay>
            String topic = String.format("counter/%d/%d/%d",k, pubc.qos,pubc.delay);
            client.unsubscribe(topic);
        }
    }

    public static void publishCommand(PubCommand pubc, MqttClient client) throws MqttException {
        client.publish("request/qos", Integer.toString(pubc.qos).getBytes(), 1, Boolean.FALSE);
        client.publish("request/delay", Integer.toString(pubc.delay).getBytes(), 1, Boolean.FALSE);
        client.publish("request/instancecount", Integer.toString(pubc.instanceCount).getBytes(), 1, Boolean.FALSE);
    }

    public static String receiveStats(MqttClient client, PubCommand command, Integer subscribeQos) throws InterruptedException, MqttException {
        AnalystStat stat = new AnalystStat();

        client.setCallback(new AnalyserCallBack(stat));
        subscribeTopics(command, client, subscribeQos);
        publishCommand(command, client);

        Thread.sleep(Publisher.duration);

        unSubscribeTopics(command,client);
        return stat.printAllStats();

    }

    public static void main(String[] args) {
        // Create a Scanner object to read input from the command line
        // MQTT QoS level (0, 1 or 2), and with a
        // requested delay(0ms, 1ms, 2ms, 4ms) for 60 seconds.
        String csvFile = "data.csv";
        try (FileWriter writer = new FileWriter(csvFile)) {
            String[] columns = new String[]{"delay", "qos", "instance-count", "subscribe qos", "average messages per second", "percentage of out of count",
                    "med 1", "med 2", "med 3", "med 4", "med 5", "percentage of messages lost"};
            writer.append(String.join(",", columns));
            writer.append("\n");
        }
        catch (IOException e) {
        }

        try {
                MqttClient client = MQTTclient.createClient(broker, clientid, username, password);
                for (int k = 1; k <= 5; k++) {
                    for (int qos = 0; qos < 3; qos ++) {
                        for (int subscribeQos = 0; subscribeQos < 3; subscribeQos ++) {
                            for (int delay = 0; delay < 5; delay ++) {
                                if (delay == 3) {continue;}
                                System.out.printf("running for delay:%d, QoS:%d, instance-count:%d, subscribe qos:%d\n", delay,qos,k, subscribeQos);
                                PubCommand currPubCommand = new PubCommand(qos, delay, k);

                                String outputString = receiveStats(client, currPubCommand, subscribeQos);

                                try (FileWriter writer = new FileWriter(csvFile, true)) {
                                    writer.append(String.format("%d,%d,%d,%d,%s\n", delay, qos, k ,subscribeQos, outputString));
                                }
                                catch (IOException e) {
                                    System.out.println("unable to write to file error");
                                }
                            }
                        }
                    }

                }
            }
        catch (MqttException e) {
            throw new RuntimeException(e);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }
}
