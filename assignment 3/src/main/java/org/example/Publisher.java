package org.example;

import org.eclipse.paho.client.mqttv3.*;

import java.io.FileWriter;
import java.io.IOException;
import java.util.Stack;

import static org.example.MQTTclient.getHostAndPort;


public class Publisher extends Thread {
    static long duration = 60 * 1000;


    static String broker = "tcp://127.0.0.1:1883";
    String username = "admin";
    String password = "ioSDYQY62u";
    int id;
    String csvFile;
    String clientid;
    Stack<PubCommand> commandStack = new Stack<>();
    int fileIntialized = 0;

    public Publisher(int id) {
        this.id = id;
        this.csvFile = String.format("pub-%s.csv", id);
        this.clientid = String.format("pub-%s", id);
    }

    /**
     * Subscribees the client to the relevant topics to receive commands from
     * */
    public void multipleSub(MqttClient client) throws MqttException {
        client.subscribe("request/qos", 0);
        client.subscribe("request/delay", 0);
        client.subscribe("request/instancecount", 0);
    }

    /**
     * Initializes file to write statistics to with the relevant headers
     * */
    public void initializeFile() {
        String csvFile = String.format("pub-%s.csv", id);
        try (FileWriter writer = new FileWriter(csvFile)) {
            String[] columns = new String[]{"system time","delay", "qos", "total messages sent"};
            writer.append(String.join(",", columns));
            writer.append("\n");
        }
        catch (IOException e) {
        }
    }

    /**
     * initializes client
     * */
    public MqttClient initializeClient() throws MqttException {
        MqttClient client = MQTTclient.createClient(broker, clientid, username, password);
        client.setCallback(new PublisherCallBack(commandStack));
        multipleSub(client);
        return client;
    }

    /**
     * Thread process that will be run continuously. First subscribes to the relevant topics
     * to receive commands from. When a command is pushed to the commandStack, it is popped off
     * and the function sends messages containing an incrementing counter to the a desired MQTT topic.
     * After the timer has run out, it writes a new entry to a csv file and waits for more commands to
     * to come in.
     * */
    public void run() {
        try {
            if (fileIntialized == 0) {
                initializeFile();
                fileIntialized = 1;
            }
            System.out.printf("I am publisher %d\n", id);

            MqttClient client = initializeClient();
            PubCommand currentCommand;
            while (true) {
                Thread.sleep(1);

                // publisher has a command within its stack
                if (!commandStack.isEmpty()) {
                    currentCommand = commandStack.pop();

                    //client has disconnected
                    if (currentCommand.error == 1) {
                        client = initializeClient();
                        continue;
                    }

                    int counter = 0;
                    // if command is for this publisher
                    if (currentCommand.instanceCount >= id) {
                        // destination as listed in the command
                        String destination = String.format("counter/%d/%d/%d", id, currentCommand.qos, currentCommand.delay);
                        MqttTopic mqttTopic = client.getTopic(destination);

                        // publishes counter for duration as specified
                        long startTime = System.currentTimeMillis();
                        while (System.currentTimeMillis() - startTime < duration) {
                            mqttTopic.publish(Integer.toString(counter).getBytes(), 0, false);
                            Thread.sleep(currentCommand.delay);
                            counter += 1;
                        }
                    }
                    try (FileWriter writer = new FileWriter(csvFile, true)) {
                        writer.append(String.format("%s,%d,%d,%d\n", java.time.LocalDateTime.now(), currentCommand.delay, currentCommand.qos, counter));
                    }
                    catch (IOException e) {
                        System.out.println("unable to write to file error");
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            run();
        }

    }

    /**
     * Starts 5 Publishers
     * */
    public static void main(String[] args) {
        broker = getHostAndPort();

        Publisher[] pubs = new Publisher[5];
        for (int k = 1; k < 6; k++) {
            pubs[k-1] = new Publisher(k);
            pubs[k-1].start();
        }
    }
}
