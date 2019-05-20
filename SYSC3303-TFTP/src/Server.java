import java.io.*;
import java.net.*;
import java.util.*;
import java.nio.file.*;

public class Server{
    DatagramSocket receiveSocket;
    DatagramPacket receivePacket;

    public Server(){
        try{
            receiveSocket = new DatagramSocket(69);
        }catch(SocketException se){
            se.printStackTrace();
        }
    }

    public void receivingNewPacket(){
        

        while(true){
            byte [] data = new byte[100]; 
            receivePacket = new DatagramPacket(data,data.length);

            // Server listen on port 69 forever (for now, in later iteration this needs to change)
            // Server receives on port 69
            System.out.println("******Server Waiting for Packet****\n");
            try{
                receiveSocket.receive(receivePacket);
            }catch(IOException ioe){
                ioe.printStackTrace();
            }

            System.out.println("New Packet Received!!");
            System.out.println("From Host: "+receivePacket.getAddress());
            System.out.println("Host Port: "+receivePacket.getPort());
            System.out.println("Length: "+receivePacket.getLength());
            System.out.println("Containing: ");

            for(int x = 0; x<receivePacket.getLength(); x++){
                System.out.print(data[x]+" ");
            }

            System.out.println("\n====================================");

            // Server decides if this is a read/write request
            // Server creates Thread to deal with Request and gives it the type
            Thread dealWithClientRequest = new DealWithClientRequest(receivePacket, processingReadOrWrite(data));
            dealWithClientRequest.start();

            data = new byte[100]; 
            receivePacket = new DatagramPacket(data,data.length);
        }
    }

    // Processes the type of the request
    // If it is anything besides read/write it quites
    private String processingReadOrWrite(byte [] data){
        if(data[0]!=0 || (data[1]!=1 && data[1]!=2)){
            System.out.print(new Exception("ERROR: unknown request type."));
            System.exit(1);
        }

        if(data[1]==1)
            return "READ";
        return "WRITE";
    }

    public static void main(String [] args){
        Server serv = new Server();
        serv.receivingNewPacket();
    }
}

// New Thread which deals with Client Request
class DealWithClientRequest extends Thread{
    DatagramSocket sendReceiveSocket;
    DatagramPacket receivePacket, sendPacket;
    String type;
    public DealWithClientRequest(DatagramPacket pckt, String type){
        this.receivePacket = pckt;
        this.type = type;

        try{
            sendReceiveSocket = new DatagramSocket();
        }catch(SocketException se){
            se.printStackTrace();
            System.exit(1);
        }
    }

    public String[] getFilenameAndMode(){
       
        // temp[0] => filename
        // temp[1] => mode
        byte [] data = receivePacket.getData();
        String [] temp = new String [2];
        int len = receivePacket.getLength();
        int x;

        for(x = 2; x<len; x++){
            if(data[x]==0) break;
        }

        if(x==len || x==2) {
            System.out.println(new Exception("Filename not provided Or data not properly formatted"));
            System.exit(1);
        }

        temp[0] = new String(data, 2, x-2);
        int y;
        for(y=x+1; y<len; y++){
            if(data[y]==0)break;
        }

        if(y==x+1 || y==len){
            System.out.println(new Exception("Mode not provided Or data not properly formatted"));
            System.exit(1);
        }

        temp[1] = new String(data, y, y-x-1);

        return temp;
    }

    public void run(){
        System.out.println("New Thread with Packet: "+receivePacket+" of Type: "+type);

        
        
        String [] information = getFilenameAndMode();
        System.out.println("Filename: "+information[0]+" Mode:"+information[1]);
        
        // for(byte e: receivePacket.getData()){
        //     System.out.print(e);
        // }

        if(type.equals("READ")) communicateReadRequest(information[0]);
        else if(type.equals("WRITE")) communicateWriteRequest();
    }

    public void communicateReadRequest(String filename){
        File fn = new File("./temp/"+filename);

        if(!fn.exists()){
            System.out.println(new Exception("The file: temp/"+filename+" doesn't exists!"));
            System.exit(1);
        }

        byte [] wholeBlock = null;
        try{
            wholeBlock = Files.readAllBytes(fn.toPath());
        }catch(IOException ioe){
            ioe.printStackTrace();
            System.exit(1);
        }

        // for(byte e: wholeBlock){
        //     System.out.println(e+" ");
        // }

        int blockNum = 1;
        int index = 0;
        while((blockNum-1)*512 < wholeBlock.length){
            byte [] msg = new byte [516];
            int ind = 0;

            msg[ind++] = 0;
            msg[ind++] = 3;
            msg[ind++] = (byte)(blockNum/256);
            msg[ind++] = (byte)(blockNum%256);

            if((blockNum)*512<wholeBlock.length)
                System.arraycopy(wholeBlock, (blockNum-1)*512, msg, 4, 512);
            else
                System.arraycopy(wholeBlock, (blockNum-1)*512, msg, 4, wholeBlock.length % 512);

            sendPacket = new DatagramPacket(msg, msg.length, receivePacket.getAddress(), receivePacket.getPort());

            try{
                sendReceiveSocket.send(sendPacket);
            }catch(IOException ioe){
                ioe.printStackTrace();
                System.exit(1);
            }

            try{
                sendReceiveSocket.receive(receivePacket);
            }catch(IOException ioe){
                ioe.printStackTrace();
                System.exit(1);
            }

            System.out.println(new String(receivePacket.getData()));

            blockNum++;

            //System.out.println(new String(msg));
        }
    }

    public void communicateWriteRequest(){
        System.out.println("Not yet Implemented Client Write Request!");
    }
}

