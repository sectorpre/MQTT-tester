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
        commandStack.push(new PubCommand(1));
    }

    /**
     * Fills a PubCommand class based on information retrieved from broker topics
     * and pushes it to the stack once it has been completely filled.
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
