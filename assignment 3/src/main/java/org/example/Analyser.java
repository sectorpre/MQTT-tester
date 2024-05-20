package org.example;

import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttException;

import java.io.FileWriter;
import java.io.IOException;

import static org.example.MQTTclient.getHostAndPort;

class Analyser {
    static String broker = "tcp://127.0.0.1:1883";
    static String username = "admin";
    static String password = "ioSDYQY62u";
    static String clientid = "analyser";

    static long duration = 60 * 1000;
    static int[] skipto = {0,0,0,0};
    static int skiptoValue;
    static String csvFile = "data.csv";

    /**
     * Function for the analyser to subscribe to the relevant topics based on a give PubCommand
     * */
    public static void subscribeTopics( PubCommand pubc, MqttClient client, Integer subscribeQos) throws MqttException {
        for (int k = 1; k <= pubc.instanceCount; k++) {
            String topic = String.format("counter/%d/%d/%d",k, pubc.qos,pubc.delay);
            client.subscribe(topic, subscribeQos);
        }
    }

    /**
     * Publishes a PubCommand to the relevant topics to be read by Publishers
     * */
    public static void publishCommand(PubCommand pubc, MqttClient client) throws MqttException {
        client.publish("request/qos", Integer.toString(pubc.qos).getBytes(), 1, Boolean.FALSE);
        client.publish("request/delay", Integer.toString(pubc.delay).getBytes(), 1, Boolean.FALSE);
        client.publish("request/instancecount", Integer.toString(pubc.instanceCount).getBytes(), 1, Boolean.FALSE);
    }

    /**
     * Sends a Pubcommand to the relevant topics and receives data from the Broker
     * sent to it from the publishers based on the subscribeQos. Then receives statistics
     * from the transfer and returns it as a string.
     * */
    public static String receiveStats(PubCommand command, Integer subscribeQos) throws MqttException {
        // Analyser receives statistics through this class
        AnalystStat stat = new AnalystStat();

        // creates client and subscribes to relevant topics
        MqttClient client = MQTTclient.createClient(broker, clientid, username, password);
        client.setCallback(new AnalyserCallBack(stat));
        subscribeTopics(command, client, subscribeQos);

        // publishes command to receive counter
        publishCommand(command, client);

        long startTime = System.currentTimeMillis();
        while (System.currentTimeMillis() - startTime < duration) {

            // if at any point the analyser disconnects, attempts to reconnect and start from where it left off
            if (stat.disconnect == 1) {
                client = MQTTclient.createClient(broker, clientid, username, password);
                client.setCallback(new AnalyserCallBack(stat));
                subscribeTopics(command, client, subscribeQos);
                stat.disconnect = 0;
            }
        }

        client.disconnect();
        return stat.getAllStats();

    }

    /**
     * Calculates a "skip value" a point within the execution that the Analyser should start executing from
     * */
    public static int calculateSkip(int instance, int qos, int subscribeQos, int delay) {
        int value = instance * 1000 + qos * 100 + subscribeQos * 10 + delay;
        return value;
    }

    /**
     * Initializes the Analyser. Called once every execution.
     * */
    public static void initialize() {
        // which calculates which sequence to skip to
        skiptoValue = calculateSkip(skipto[0], skipto[1], skipto[2], skipto[3]);
        broker = getHostAndPort();

        // intialize headers for file to be written to
        try (FileWriter writer = new FileWriter(csvFile)) {
            String[] columns = new String[]{"system time","delay", "qos", "instance-count", "subscribe qos", "average messages per second", "number of out of count",
                    "med 1", "med 2", "med 3", "med 4", "med 5", "number of messages lost", "lost connection"};
            writer.append(String.join(",", columns));
            writer.append("\n");
        }
        catch (IOException e) {
        }
    }

    /**
     * Continuously calls receiveStats on a desired sequence of instance-count, qos, subcribeQos, delay
     * and writes the data received into the specified csvfile
     * */
    public static void runloop() throws MqttException, InterruptedException {
        for (int k = 1; k <= 5; k++) {
            for (int qos = 0; qos < 3; qos ++) {
                for (int subscribeQos = 0; subscribeQos < 3; subscribeQos ++) {
                    for (int delay = 0; delay < 5; delay ++) {
                        if (delay == 3) {continue;}
                        if (calculateSkip(k, qos, subscribeQos,delay) < skiptoValue) {continue;}

                        // acts as a checkpoint to ensure that the code executes from this point onwards
                        // if it were to be interrupted unexpectedly
                        skiptoValue = calculateSkip(k, qos, subscribeQos, delay);
                        System.out.printf("running for delay:%d, QoS:%d, instance-count:%d, subscribe qos:%d\n",
                                delay,qos,k, subscribeQos);

                        // receive statistics to be written to csv file
                        PubCommand currPubCommand = new PubCommand(qos, delay, k);
                        String outputString = receiveStats(currPubCommand, subscribeQos);

                        // write a line to csv file
                        try (FileWriter writer = new FileWriter(csvFile, true)) {
                            writer.append(String.format("%s,%d,%d,%d,%d,%s\n", java.time.LocalDateTime.now(),
                                    delay, qos, k ,subscribeQos, outputString));
                        }
                        catch (IOException e) {
                            System.out.println("unable to write to file error");
                        }
                    }
                }
            }

        }

    }

    public static void main(String[] args) {
        initialize();
        while(true) {
            try {runloop();}
            catch (MqttException | InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
}
