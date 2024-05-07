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
}
