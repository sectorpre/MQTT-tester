package org.example;

/**
 * Used to store a qos/delay/instance-count/error combination to be sent to
 * the broker
 * */
public class PubCommand {
    int qos;
    int delay;
    int instanceCount;
    int error;

    public PubCommand(int qos, int delay, int instanceCount) {
        this.qos = qos;
        this.delay = delay;
        this.instanceCount = instanceCount;
        error = 0;
    }

    public PubCommand() {
        qos = -1;
        delay= -1;
        instanceCount = -1;
        error = 0;
    }
    public PubCommand(int error) {
        this.error = error;
    }

    public boolean isFilled() {
        return qos != -1 && delay != -1 && instanceCount != -1;
    }
}
