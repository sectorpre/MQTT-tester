package org.example;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Stack;

/**
 * Class to be used for each combination of instance-count/qos/delay/subscribe-qos sent
 * to the broker. Stores all the information gathered from transfers from the broker.
 * */
public class AnalystStat {
    int totaMessageCount = 0;
    int[] eachPubCount = {-1, -1, -1, -1, -1};

    // stacks for time stamps for each message sent by each publisher
    List<Stack<Long>> timeStamps = new ArrayList<>();

    // field to see if the client has been active or has been disconnected
    int disconnect = 0;

    // number of disconnects
    int numberOfDisconnects = 0;

    // number of out of order packets
    int rateOfOutOfOrder;

    public AnalystStat() {
        // initializing time stamp stacks
        for (int k = 0; k < 5; k++) {
            timeStamps.add(new Stack<>());
        }
    }

    /**
     * Returns a string of all desired stats.
     * */
    public String getAllStats() {
        return String.format("%d,%d,%s%d,%d",
                this.getRateOfReceive((int) (Publisher.duration/1000)),
                this.rateOfOutOfOrder,
                this.getAllMedians(),
                this.getMessageLoss(),
                this.numberOfDisconnects);

    }

    /**
     * Gets the average rate of messages received for a certain time frame.
     * */
    public int getRateOfReceive(Integer time) {
        return totaMessageCount/time;
    }

    /**
     * Gets the median inter-message delay for each publisher
     * */
    public String getAllMedians() {
        String returnVal = "";
        for (var p: timeStamps) {
            returnVal = String.format("%s%.2f,",returnVal,getMedianPublisher(p));
        }
        return returnVal;
    }

    /**
     * Gets the message loss based on the number the final count the PubCounts indicate -
     * the number of messages actually received.
     * */
    public int getMessageLoss() {
        int totalMessagesSent = 0;
        for (var p: eachPubCount) {
            totalMessagesSent += p + 1;
        }
        int returnvalue = totalMessagesSent - totaMessageCount;
        if (returnvalue != 0) {
            System.out.println("message loss detected");
        }
        //System.out.printf("sent messages:%d - received messages:%d ", totalMessagesSent, totaMessageCount);
        return totalMessagesSent - totaMessageCount ;
    }

    /**
     * Gets the median inter-message delay for a publisher based on a stack of timestamps
     * */
    public double getMedianPublisher(Stack<Long> stackTime) {
        // if no timestamps in the stack, returns -1
        if (stackTime.isEmpty()) {return -1;}

        // gets the difference between all timestamps for a stack of timestamps
        List<Long> differenceList = new ArrayList<>();
        Long previous = (long) -1;
        while(!stackTime.isEmpty()) {
            Long timeframe = stackTime.pop();
            if (previous != -1) {differenceList.add(Math.abs(timeframe - previous));}
            previous = timeframe;
        }

        // Sort the list
        Collections.sort(differenceList);

        // find the median from the differenceList
        double median;
        int size = differenceList.size();
        if (size % 2 == 0) {median = (differenceList.get((size / 2) - 1) + differenceList.get(size / 2)) / 2.0;}
        else {median = differenceList.get(size / 2);}
        return median;
    }


}
