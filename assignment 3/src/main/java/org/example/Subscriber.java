package org.example;

import org.eclipse.paho.client.mqttv3.*;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;

import java.io.FileWriter;
import java.io.IOException;
import java.util.Queue;
import java.util.Scanner;
import java.util.Stack;

import static org.example.MQTTclient.setOptions;


public class Subscriber extends Thread {
    static long duration = 10 * 1000;


    String broker = "tcp://iot.eclipse.org:1883";
    String username = "admin";
    String password = "password";
    int id;
    Stack<PubCommand> commandStack = new Stack<>();

    public Subscriber(int id) {
        this.id = id;
    }

    public void multipleSub(MqttClient client) throws MqttException {
        client.subscribe("request/qos", 0);
        client.subscribe("request/delay", 0);
        client.subscribe("request/instancecount", 0);
    }


    public void run() {
        String clientid = String.format("pub-%s", id);
        try {
            MqttClient client = MQTTclient.createClient(broker, clientid, username, password);
            client.setCallback(new PublisherCallBack(commandStack));
            multipleSub(client);

            System.out.printf("I am publisher %d\n", id);
            PubCommand currentCommand;
            while (true) {
                Thread.sleep(1);

                // publisher has a command within its stack
                if (!commandStack.isEmpty()) {
                    currentCommand = commandStack.pop();

                    // command listed is not for it
                    if (currentCommand.instanceCount < id) {continue;}

                    System.out.println(id + ": message received");

                    // destination as listed in the command
                    String destination = String.format("counter/%d/%d/%d", id, currentCommand.qos, currentCommand.delay);

                    long startTime = System.currentTimeMillis();

                    int counter = 0;
                    while (System.currentTimeMillis() - startTime < duration) {
                        MqttTopic mqttTopic = client.getTopic(destination);
                        mqttTopic.publish(Integer.toString(counter).getBytes(), 0, false);
                        Thread.sleep(currentCommand.delay);
                        counter += 1;
                    }

                    System.out.println(id + " finished publishing: " + counter);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    public static void main(String[] args) throws MqttException {
        String broker = "tcp://127.0.0.1:1883";
        String username = "admin";
        String password = "ioSDYQY62u";

        System.out.println("please input ip address or hostname");
        Scanner in = new Scanner(System.in);
        String s = in.nextLine();
        if (!s.isEmpty()) {broker = String.format("tcp://%s:1883", s);}

        MqttClient client = MQTTclient.createClient(broker, "syslog", username, password);
        try (FileWriter writer = new FileWriter("syslog.csv")) {
            writer.append("system time,topic,qos,message content\n");
        }
        catch (IOException e) {
            System.out.println("unable to write to file error");
        }
        client.setCallback(new MqttCallback() {
            String csvFile = "syslog.csv";
  
            public void messageArrived(String topic, MqttMessage message) throws Exception {
                String log = String.format("%s,%s,%d,%s",java.time.LocalDateTime.now(),topic,message.getQos(),new String(message.getPayload()));
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
