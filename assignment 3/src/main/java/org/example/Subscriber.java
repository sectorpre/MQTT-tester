package org.example;

import org.eclipse.paho.client.mqttv3.*;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;

import java.util.Queue;
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
        String password = "public";

        MqttClient client = MQTTclient.createClient(broker, "pub-1", username, password);
        client.setCallback(new MqttCallback() {
            public void messageArrived(String topic, MqttMessage message) throws Exception {
                System.out.println("topic: " + topic + " || qos: " + message.getQos() + " || message content: " + new String(message.getPayload()));
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
        System.out.println("test");
        client.subscribe(topic, qos);
    }
}
