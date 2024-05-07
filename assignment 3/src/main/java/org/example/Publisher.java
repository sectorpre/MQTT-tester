package org.example;

import org.eclipse.paho.client.mqttv3.*;

import java.util.Queue;
import java.util.Stack;

/**
 * A Publisher will first subscribe (listen) to a set of ‘request’ topics, namely request/qos,
 * request/delay and request/instancecount. When it sees new values for these, it will start
 * publishing accordingly.
 * o You will have 5 instances of a Publisher running at the same time, called pub-1 to pub-5. These will
 * help stress the broker (and network, if you have separate computers). The ‘instancecount’ will tell
 * you how many publishers should be active, while the rest should keep quiet.
 * o Each Publisher will send a sequence of simple message to the broker, namely an incrementing
 * counter (0, 1, 2, 3, …). It will publish those messages to the broker at a requested MQTT QoS level
 * (0, 1 or 2), and with a requested delay between messages (0ms, 1ms, 2ms, 4ms) for 60 seconds.
 * o Each Publisher will publish to the topic counter/<instance>/<qos>/<delay>, so e.g. counter/1/0/4
 * is the messages coming from Publisher-instance-1 at qos=0 and delay=4.
 * o After it has finished its 60sec burst of messages, each Publisher should go back to listening to the
 * ‘request’ topics for the next round of instructions.
 * */

public class Publisher extends Thread {
    String broker = "tcp://127.0.0.1:1883";
    String username = "admin";
    String password = "password";
    int id;
    Stack<PubCommand> commandStack = new Stack<>();

    public Publisher(int id) {
        this.id = id;
    }


    public void run() {
        String clientid = String.format("pub-%s", id);
        try {
            MqttClient client = MQTTclient.createClient(broker, clientid, username, password);
            client.setCallback(new PublisherCallBack(commandStack));
            client.subscribe("request/qos", 0);
            client.subscribe("request/delay", 0);
            client.subscribe("request/instancecount", 0);


            System.out.printf("I am publisher %d\n", id);
            PubCommand currentCommand;
            while (true) {
                Thread.sleep(1);
                if (!commandStack.isEmpty()) {
                    currentCommand = commandStack.pop();
                    if (currentCommand.instanceCount < id) {continue;}
                    System.out.println(id + ": meesage received");
                    String destination = String.format("counter/%d/%d/%d", id, currentCommand.qos, currentCommand.delay);

                    long startTime = System.currentTimeMillis();
                    long duration = 60 * 1000;

                    int counter = 0;
                    while (System.currentTimeMillis() - startTime < duration) {
                        MqttTopic mqttTopic =  client.getTopic(destination);
                        mqttTopic.publish(Integer.toString(counter).getBytes(), 0, false);
                        Thread.sleep(currentCommand.delay);
                        counter += 1;
                    }
                    System.out.println(id + " finished publishing");
                }

            }
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    public static void main(String[] args) {
        Publisher pub1 = new Publisher(1);
        pub1.run();
    }
}
