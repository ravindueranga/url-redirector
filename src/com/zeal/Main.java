package com.zeal;

import java.io.IOException;

class Main {

    public static void main(String[] args) {

        Runner runner = new Runner();
        try {
            runner.run();
        } catch (IOException e) {
            e.printStackTrace();
        }


    }


}

