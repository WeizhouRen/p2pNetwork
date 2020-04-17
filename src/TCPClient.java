/*
 *  * Compile: java TCPClient.java
 *  * Run: java TCPClient
 *  * Listening to the TCP client to get input from keyboard
 */

import java.io.*;

public class TCPClient extends Thread {

    private Peer peer;
    private String request;

    public TCPClient(Peer peer) throws Exception {
        this.peer = peer;
        request = null;
    }

    public void run() {
        // keep listening on keyboard
        while (true) {
            try {
                // get input from keyboard
                BufferedReader inFromUser = new BufferedReader(new InputStreamReader(System.in));
                // e.g. "Quit"
                String command = inFromUser.readLine();

                if (command.equals("Quit")) {
                    request = peer.getId() + "-Quit-My successors are-" + peer.getFstSuccessorId() + "-" + peer.getSecSuccessorId();
                    peer.request(peer.getFstPredecessorPort(), request);
                    peer.request(peer.getSecPredecessorPort(), request);
                    peer.setFstSuccessorId(0);
                    peer.setSecSuccessorId(0);
                } else if (command.contains("Store")) {
                    peer.request(peer.getPort(), command);
                } else if (command.contains("Request")) {
//                    System.out.println("File request for 4103 has been sent to my successor");
                    command += " from peer " + peer.getId(); // e.g. "Request 4103 from peer 2"
                    peer.request(peer.getPort(), command);
                }

            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
} // end of class TCPClient