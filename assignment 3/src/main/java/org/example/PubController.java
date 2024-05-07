package org.example;

public class PubController {


    public static void main(String[] args) {
        Publisher[] pubs = new Publisher[5];
        for (int k = 1; k < 6; k++) {
            pubs[k-1] = new Publisher(k);
            pubs[k-1].start();
        }



    }
}
