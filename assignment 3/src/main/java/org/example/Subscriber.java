package org.example;

import org.eclipse.paho.client.mqttv3.*;

import java.io.FileWriter;
import java.io.IOException;

import static org.example.MQTTclient.getHostAndPort;


/**
 * This class is primarily for subscribing to the $SYS topics and logging any
 * information that is being pushed from it.
 * */
public class Subscriber extends Thread {
    static String broker = "tcp://iot.eclipse.org:1883";
    static String username = "admin";
    static String password = "password";
    static String csvFile = "syslog.csv";
    int id;

    public Subscriber(int id) {
        this.id = id;
    }

    /**
     * Subscribe to $SYS/ and writes all data it receives into a given csvFIle.
     * */
    public static void main(String[] args) throws MqttException {
        broker = getHostAndPort();

        // initialize file to write to
        try (FileWriter writer = new FileWriter("syslog.csv")) {
            writer.append("system time,topic,qos,message content\n");
        }
        catch (IOException e) {
            System.out.println("unable to write to file error");
        }

        MqttClient client = MQTTclient.createClient(broker, "syslog", username, password);
        client.setCallback(new MqttCallback() {
            public void messageArrived(String topic, MqttMessage message) throws Exception {
                String log = String.format("%s,%s,%d,%s",
                        java.time.LocalDateTime.now(),
                        topic,
                        message.getQos(),
                        new String(message.getPayload()));

                try (FileWriter writer = new FileWriter(csvFile, true)) {
                    writer.append(String.format("%s\n", log));
                }
                catch (IOException e) {
                    System.out.println("unable to write to file error");
                }
            }

            public void connectionLost(Throwable cause) {
                System.out.println("connectionLost: " + cause.getMessage());
            }

            public void deliveryComplete(IMqttDeliveryToken token) {
                System.out.println("deliveryComplete: " + token.isComplete());
            }
        });

        String topic = "$SYS/#";
        int qos = 2;
        client.subscribe(topic, qos);
    }
}
