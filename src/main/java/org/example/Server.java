package org.example;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Server implements  Runnable{

    private ArrayList<ConnectionHandler> connections;
    private ServerSocket server;
    private boolean done;
    private ExecutorService pool;
    public Server(){
        connections= new ArrayList<>();
        done = false;
    }

    @Override
    public void run(){
        try{
            server = new ServerSocket(9999);
            pool = Executors.newCachedThreadPool();
            while(!done) {
                Socket client = server.accept();
                ConnectionHandler handler = new ConnectionHandler(client);
                connections.add(handler);
                pool.execute(handler);
            }
        }catch (IOException e){
            //handle exception
            e.printStackTrace();
        }
    }
    public void shutdown() throws IOException {
        if(!server.isClosed()){
            server.close();
        }
    }
    public void broadcast(String message){
        done = true;
        for(ConnectionHandler ch: connections){
            if(ch != null){
                ch.sendMessage(message);
            }
        }
    }
    class ConnectionHandler implements  Runnable{

        private Socket clientSocket;
        private BufferedReader in;
        private PrintWriter out;
        private  String nickName;

        public ConnectionHandler(Socket socket){
            this.clientSocket = socket;
        }


        @Override
        public  void run(){
            try{
                out = new PrintWriter(clientSocket.getOutputStream(),true);
                in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
                out.println("Please enter nickname:");
                nickName = in.readLine();//validate the name
                broadcast(nickName+" joined chat");
                String message;
                while((message = in.readLine())!= null){
                    if(message.startsWith("/"+nickName)){
                        String[] messageSplit = message.split(" ",2);
                        if(messageSplit.length==2){
                            broadcast(nickName+" changed their name to "+messageSplit[1]);
                            nickName = messageSplit[1];
                            //out.println("Name s=changed to "+nickName);
                        }else{
                            out.println("no name provided");
                        }
                    }else if(message.startsWith("/quit")){
                        shutdown();
                    }else{
                        broadcast(nickName+" left chat");
                        broadcast(nickName +": "+message);
                    }
                }
            }catch(IOException e){
                    e.printStackTrace();
            }
        }
        public void sendMessage(String message){
            out.println(message);
        }

        public void shutdown() throws IOException {
            in.close();
            out.close();
            if(!clientSocket.isConnected()){
                clientSocket.close();
            }
        }

    }
//main function here
    public static void mani(String[] args){
        Socket sc = new Socket();
        Server sv = new Server();
        sv.run();
    }
}
