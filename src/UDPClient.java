/*
 *
 *  UDPClient
 *  * Compile: java UDPClient.java
 *  * Run: java UDPClient
 */

import java.io.*;
import java.net.*;
import java.util.concurrent.locks.ReentrantLock;

public class UDPClient extends Thread {

    private ReentrantLock syncLock = new ReentrantLock();
    private int pingInterval;//milliseconds
    private DatagramSocket clientSocket;
    private InetAddress IPAddress;
    private Peer peer;
    private byte[] sendData;
    private byte[] receiveData;
    private byte[] receiveData1;
    private DatagramPacket receivePacket1 = null;
    private DatagramPacket receivePacket2 = null;
    private int lost1, lost2;

    public UDPClient(Peer peer, int pingInterval) throws Exception {
        this.peer = peer;
        this.pingInterval = pingInterval * 1000;
        // Define socket parameters, address and Port No
        IPAddress = InetAddress.getByName("localhost");
        // create socket which connects to server
        clientSocket = new DatagramSocket();
        //prepare for sending
        sendData = new byte[1024];
        //prepare buffer to receive reply
        receiveData = new byte[1024];
        receiveData1 = new byte[1024];
    }

    // We will send from this thread
    public void run() {
        while (true) {
            syncLock.lock();
            if (peer.getFstSuccessorId() != 0 && peer.getSecSuccessorId() != 0)
                System.out.println("Ping requests sent to Peers " + peer.getFstSuccessorId() + " and " + peer.getSecSuccessorId());
            // Check is the 1st successor alive
            try {
                String sentence = peer.getId() + "-Are you alive?-1";
                sendData = sentence.getBytes();
                // write to server, need to create DatagramPacket with server address and port No
                DatagramPacket sendToFstPacket = new DatagramPacket(sendData, sendData.length, IPAddress, peer.getFstSuccessorPort());
                clientSocket.setSoTimeout(2000);
                clientSocket.send(sendToFstPacket);
                receivePacket1 = new DatagramPacket(new byte[1024], 1024);
                clientSocket.receive(receivePacket1);
                String isFstAlive = new String(receivePacket1.getData()).trim();
                if (isFstAlive.contains("Yes"))
                    if (Integer.parseInt(isFstAlive.split("-")[0]) == peer.getFstSuccessorId())
                        System.out.println("Ping response received from Peer " + peer.getFstSuccessorId());
                else System.out.println(isFstAlive);
            } catch (IOException e) {
                lost1++;
                if (lost1 >= 2) {
                    try {
                        if (peer.getFstSuccessorId() != 0)
                            System.out.println("Peer " + peer.getFstSuccessorId() + " is no longer alive");
                        peer.request(peer.getSecSuccessorPort(), "Who is your first successor?");
                    } catch (IOException ex) { // If quit, successors id are 0 and cannot sent request
//                        clientSocket.close();
//                        break;
                    }
                }
            }

            try {
                String sentence = peer.getId() + "-Are you alive?-2";
                sendData = sentence.getBytes();
                DatagramPacket sendToSecPacket = new DatagramPacket(sendData, sendData.length, IPAddress, peer.getSecSuccessorPort());
                clientSocket.setSoTimeout(2000);
                clientSocket.send(sendToSecPacket);
                receivePacket2 = new DatagramPacket(new byte[1024], 1024);
                clientSocket.receive(receivePacket2);

                String isSecAlive = new String(receivePacket2.getData()).trim();
                if (isSecAlive.contains("Yes"))
                    if (Integer.parseInt(isSecAlive.split("-")[0]) == peer.getSecSuccessorId())
                        System.out.println("Ping response received from Peer " + peer.getSecSuccessorId());
                else System.out.println(isSecAlive);
            } catch (IOException e) {
                lost2++;
                if (lost2 >= 2) {
                    try {
                        // Send request to 1st successor
                        if (peer.getSecSuccessorId() != 0)
                            System.out.println("Peer " + peer.getSecSuccessorId() + " is no longer alive");
                        peer.request(peer.getFstSuccessorPort(), "Who are your successors?");
                    } catch (IOException ex) {
//                        ex.printStackTrace();
//                        clientSocket.close();
//                        break;
                    }
                }
            }
            syncLock.unlock();
            try {
                Thread.sleep(this.pingInterval); //in milliseconds
            } catch (InterruptedException e) {
                e.printStackTrace();
            }


        }// while ends
    } //run ends

} // end of class UDPClient