package org.example;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

public class Client implements Runnable {

    private Socket client;
    private BufferedReader in;
    private boolean done;
    private PrintWriter out;
    @Override
    public void run(){
        try{
            Socket client = new Socket("127.0.0.1",9999);
            out = new PrintWriter(client.getOutputStream(),true);
            in = new BufferedReader(new InputStreamReader(client.getInputStream()));
            InputHandler inputHandler = new InputHandler();
            Thread t = new Thread(inputHandler);
            t.start();
            String inMessage;
            while((inMessage = in.readLine())!=null){
                System.out.println(inMessage);
            }
        }catch(IOException e){
            e.printStackTrace();
        }
    }
    public void shutdwon(){
        done = true;
        try{
            in.close();
            out.close();
        }catch(IOException e){
            e.printStackTrace();
        }
    }


    class InputHandler implements Runnable{


        @Override
        public void run(){
            try{
                BufferedReader inReader = new BufferedReader(new InputStreamReader(System.in));
                while(!done){
                    String message = inReader.readLine();
                    if(message.equals("/quit")){
                        inReader.close();
                        shutdwon();
                    }else{
                         out.println(message);
                    }
                }
            }catch(IOException e){
                e.printStackTrace();
            }
        }
    }
    //main function here
}
