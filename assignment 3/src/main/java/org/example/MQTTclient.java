package org.example;

import org.eclipse.paho.client.mqttv3.*;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;

import java.util.Scanner;

/**
 * Class of functions that are used repeatedly by Analyser and Publisher
 * */
public class MQTTclient {

    /**
     * returns an MqttConnectOptions with default options set.
     * */
    public static MqttConnectOptions setOptions(String username, String password) {
        MqttConnectOptions options = new MqttConnectOptions();
        options.setUserName(username);
        options.setPassword(password.toCharArray());
        options.setAutomaticReconnect(true);
        options.setConnectionTimeout(0);
        options.setKeepAliveInterval(60);
        return options;
    }

    /**
     * Create a MqttClient based on a given broker, clientid, username and password.
     * */
    public static MqttClient createClient(String broker, String clientid, String username, String password) throws MqttException {
        MqttClient client = new MqttClient(broker, clientid, new MemoryPersistence());
        MqttConnectOptions options = setOptions(username, password);
        client.connect(options);

        return client;
    }

    /**
     * Retrieves a host:port combination from the commandline
     * */
    public static String getHostAndPort() {
        System.out.println("please input ip address or hostname and port");
        Scanner in = new Scanner(System.in);
        String s = in.nextLine();
        if (!s.isEmpty()) {return String.format("tcp://%s", s);}
        return "tcp://127.0.0.1:1883";
    }
}

