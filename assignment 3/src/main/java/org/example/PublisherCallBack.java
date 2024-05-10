package org.example;

import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttMessage;

import java.util.Stack;

public class PublisherCallBack implements MqttCallback {
    public PublisherCallBack(Stack<PubCommand> commandStack) {
        this.commandStack = commandStack;
        currentCommand = new PubCommand();
    }
    Stack<PubCommand> commandStack;
    PubCommand currentCommand;

    public void connectionLost(Throwable cause) {
        System.out.println("connectionLost: " + cause.getMessage());
    }

    /**
     * Receives a message from a broker topic
     * Determines the QoS, delay and instancecount the publisher should
     * be publishing at.
     *
     * client.subscribe("request/qos", qos);
     *             client.subscribe("request/delay", qos);
     *             client.subscribe("request/instancecount", qos);
     * */
    public void messageArrived(String topic, MqttMessage message) {
        switch (topic) {
            case "request/qos" -> currentCommand.qos = Integer.parseInt(new String(message.getPayload()));
            case "request/delay" -> currentCommand.delay = Integer.parseInt(new String(message.getPayload()));
            case "request/instancecount" -> {currentCommand.instanceCount = Integer.parseInt(new String(message.getPayload()));}
        }
        if (currentCommand.isFilled()) {
            commandStack.push(currentCommand);
            currentCommand = new PubCommand();
        }

    }

    public void deliveryComplete(IMqttDeliveryToken token) {
        //System.out.println("deliveryComplete---------" + token.isComplete());
    }
}
