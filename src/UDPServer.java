/*
 * Threaded UDPServer
 * Compile: javac UDPServer.java
 * Run: java UDPServer PortNo
 */

import java.io.*;
import java.net.*;
import java.util.*;
import java.text.SimpleDateFormat;
import java.util.concurrent.locks.ReentrantLock;
//import java.util.concurrent.locks.*;

public class UDPServer extends Thread {

    private byte[] sendData = new byte[1024];
    private DatagramSocket serverSocket;
    private ReentrantLock syncLock = new ReentrantLock();
    private List<Integer> predecessors =new ArrayList<Integer>();

    //prepare buffers
    private byte[] receiveData;
    private String serverMessage;
    private SocketAddress sAddr;
    private Peer peer;
    private int port;

    public UDPServer(Peer peer) throws SocketException {
        this.peer = peer;
//        this.serverSocket = serverSocket;
        this.serverSocket = new DatagramSocket(peer.getPort());
        this.receiveData = new byte[1024];
        this.serverMessage = peer.getId() + "-No";
        this.port = peer.getPort();
    }

    // We will send from this thread
    public void run() {

        while (true) {
            try {
                DatagramPacket receivePacket = new DatagramPacket(receiveData, receiveData.length);
                syncLock.lock();
                serverSocket.receive(receivePacket);

                //get data
                String sentence = new String(receivePacket.getData()).trim();

                String predecessorId = sentence.split("-")[0];
                System.out.println("Ping request message received from Peer " + predecessorId);

                sAddr = receivePacket.getSocketAddress(); // get info of the client with whom we are communicating

                if (sentence.contains(predecessorId + "-Are you alive?")) {
                    serverMessage = peer.getId() + "-Yes";

                    // Store/Update predecessors
                    if (Integer.parseInt(sentence.split("-")[2]) == 1) {
                        peer.setFstPredecessor(Integer.parseInt(predecessorId));
                    } else {
                        peer.setSecPredecessor(Integer.parseInt(predecessorId));
                    }
                }
                //prepare to send reply back
                sendData = serverMessage.getBytes();

                //send it back to client on SocktAddress sAddr
                DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length, sAddr);
                serverSocket.send(sendPacket);

            } catch (Exception e) {
//                System.out.println("timeout");
            }
            syncLock.unlock();

        } // end of while (true)
    } //run ends
} // end of class UDPServer
