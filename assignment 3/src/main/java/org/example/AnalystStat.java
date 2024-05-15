package org.example;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Stack;

/**
 * Collect statistics, for each instance-count/delay/QoS combination, to measure over the 60sec
 * period
 *
 *- command
 *- stats {1:_____, 2:_______}
 *
 *
 * */
public class AnalystStat {
    int totaMessageCount = 0;
    int[] eachPubCount = {-1, -1, -1, -1, -1};
    List<Stack<Long>> timeStamps = new ArrayList<>();
    long startTime;

    int rateOfReceive; // total average rate of messages you actually receive from all publishers across the* period [messages/second].
    int rateOfMessageLoss; // The rate of message loss you see [percentage].* (how many messages did you see, versus how many should you have seen)

    int rateOfOutOfOrder;//The rate of any out-of-order messages you see [percentage] (i.e. how often do you get a smaller number after a larger number)
    int medianInterMessage; //The median inter-message-gap you see, compared to the requested delay [milliseconds].* Only measure for actually consecutive counter-value messages, ignore the gap if* you miss any messages in between.

    public AnalystStat() {
        for (int k = 0; k < 5; k++) {
            timeStamps.add(new Stack<>());
        }
        startTime = System.currentTimeMillis();
    }

    public String printAllStats() {
        return String.format("%d,%d,%s%d",
                this.getRateOfReceive((int) (Publisher.duration/1000)),
                this.rateOfOutOfOrder,
                this.getAllMedians(),
                this.getMessageLoss());

    }

    // time in second
    public int getRateOfReceive(Integer time) {
        return totaMessageCount/time;
    }

    public String getAllMedians() {
        String returnVal = "";
        for (var p: timeStamps) {
            returnVal = String.format("%s%.2f,",returnVal,getMedianPublisher(p));
        }
        //System.out.println(returnVal);
        return returnVal;
    }

    public int getMessageLoss() {
        int totalMessagesSent = 0;
        for (var p: eachPubCount) {
            totalMessagesSent += p + 1;
        }
        //System.out.printf("sent messages:%d - received messages:%d ", totalMessagesSent, totaMessageCount);
        return totalMessagesSent - totaMessageCount ;
    }


    public double getMedianPublisher(Stack<Long> stackTime) {
        if (stackTime.isEmpty()) {return -1;}
        List<Long> differenceList = new ArrayList<>();

        Long previous = (long) -1;
        while(!stackTime.isEmpty()) {
            Long timeframe = stackTime.pop();
            if (previous != -1) {
                differenceList.add(Math.abs(timeframe - previous));
            }
            previous = timeframe;
        }
        // Sort the list
        Collections.sort(differenceList);
        double median;
        int size = differenceList.size();
        if (size % 2 == 0) {
            //even
            median = (differenceList.get((size / 2) - 1) + differenceList.get(size / 2)) / 2.0;
        } else {
            //odd
            median = differenceList.get(size / 2);
        }

        return median;
    }


}
