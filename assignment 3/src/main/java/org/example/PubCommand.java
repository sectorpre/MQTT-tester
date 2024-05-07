package org.example;

public class PubCommand {
    int qos;
    int delay;
    int instanceCount;

    public PubCommand(int qos, int delay, int instanceCount) {
        this.qos = qos;
        this.delay = delay;
        this.instanceCount = instanceCount;
    }

    public PubCommand() {
        qos = -1;
        delay= -1;
        instanceCount = -1;
    }

    public boolean isFilled() {
        return qos != -1 && delay != -1 && instanceCount != -1;
    }
}
