package org.example;

import java.util.Scanner;

public class PubController {


    public static void main(String[] args) {
        System.out.println("please input ip address or hostname");
        Scanner in = new Scanner(System.in);
        String s = in.nextLine();
        if (!s.isEmpty()) {Publisher.broker = String.format("tcp://%s:1883", s);}

        Publisher[] pubs = new Publisher[5];
        for (int k = 1; k < 6; k++) {
            pubs[k-1] = new Publisher(k);
            pubs[k-1].start();
        }



    }
}
