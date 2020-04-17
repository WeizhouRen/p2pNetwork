import java.awt.*;
import java.io.*;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;

public class TCPServer extends Thread {

    private int serverPort;
    private ServerSocket serverSocket;
    private String reply;
    private Peer peer;
    private String[] clientSentenceWords;

    public TCPServer(Peer peer) throws Exception {
        this.peer = peer;
        this.serverPort = peer.getPort() + 256;
        this.reply = "test";
		/* Create a server socket
        We will listen on this port for connection request from clients */
        this.serverSocket = new ServerSocket(serverPort);
    }

    /**
     * Calculate the correct position using hash function modulus(filename/256),
     * and check if current peer is correct position
     *
     * @param filename
     * @return true iff correct
     */
    private boolean isStoredPos(String filename) {
        int position = Integer.parseInt(filename) % 256;
        if ((position <= peer.getId() && position > peer.getFstPredecessor())
                || (position < peer.getFstSuccessorId() && position > peer.getId() && peer.isFirstPeer())) {
            return true;
        }
        return false;
    }

    /**
     * Print out the updated successors' id
     */
    private void printSuccessors() {
        System.out.println("My new first successor is Peer " + peer.getFstSuccessorId());
        System.out.println("My new second successor is Peer " + peer.getSecSuccessorId());
    }

    public void run() {
        while (true) {
            try {
                // accept connection from connection queue
                Socket connectionSocket = serverSocket.accept();

                // create read stream to get input
                BufferedReader inFromClient
                        = new BufferedReader(new InputStreamReader(connectionSocket.getInputStream()));

                //data from client is stored in clientSentence e.g. "15-Forward"
                String clientSentence = inFromClient.readLine();

                /* STEP 2 - JOIN */
                if (clientSentence.contains("Join request forwarded to my successor")) {
                    // e.g. "Peer 15 Join request forwarded to my successor"
                    System.out.println(clientSentence);
                    clientSentenceWords = clientSentence.split(" ");
                    int requestJoinPeerId = Integer.parseInt(clientSentenceWords[1]); // e.g. 15

                    // Find out the position of joining peer
                    if ((requestJoinPeerId > this.peer.getId() && requestJoinPeerId < this.peer.getFstSuccessorId())
                            || (requestJoinPeerId > this.peer.getId() && this.peer.isLastPeer())) {

                        // e.g. "Peer 15 Join request received"
                        reply = "Peer " + requestJoinPeerId + " Join request received";

                        // Before updating successor ids, tell the joining peer of current successors
                        peer.updateJoinSuccessor(requestJoinPeerId);

                        // Update predecessor's successor
                        peer.setSecSuccessorId(peer.getFstSuccessorId());
                        peer.setFstSuccessorId(requestJoinPeerId);
                        printSuccessors();
                    } else {
                        System.out.println("Join request forwarded to my successor");
                        peer.findResponsiblePeer(peer.getFstSuccessorId(), requestJoinPeerId);
                    }
                }
                // e.g. Updating successors of joining peer-19-2
                else if (clientSentence.contains("Updating successors of joining peer")) {
                    clientSentenceWords = clientSentence.split("-");
                    int fstSuc = Integer.parseInt(clientSentenceWords[1]);
                    int secSuc = Integer.parseInt(clientSentenceWords[2]);
                    peer.setFstSuccessorId(fstSuc);
                    peer.setSecSuccessorId(secSuc);
                    reply = "Join request has been accepted";
                    System.out.println(reply);
                    printSuccessors();
                }
                /* STEP 4 - QUIT */
                else if (clientSentence.contains("Quit")) {
                    // Notify it's predecessors e.g. "9-Quit-My successors are-8-9"
                    clientSentenceWords = clientSentence.split("-");
                    int fstSuc = Integer.parseInt(clientSentenceWords[3]);
                    int secSuc = Integer.parseInt(clientSentenceWords[4]);
                    int quitId = Integer.parseInt(clientSentenceWords[0]);
                    reply = "Peer " + quitId + " will depart from the network";
                    System.out.println(reply);
                    if (peer.getFstSuccessorId() == quitId) {
                        peer.setFstSuccessorId(fstSuc);
                        peer.setSecSuccessorId(secSuc);
                        printSuccessors();
                    } else if (peer.getSecSuccessorId() == quitId) {
                        peer.setSecSuccessorId(fstSuc);
                        printSuccessors();
                    }
                }

                /* STEP 5 - DEPARTURE */
                else if (clientSentence.equals("Who is your first successor?")) {
                    reply = "First successor is " + peer.getFstSuccessorId();
                } else if (clientSentence.equals("Who are your successors?")) {
                    // e.g. "Your 1st successor's successors are 19 2"
                    reply = "Your 1st successor's successors are "
                            + peer.getFstSuccessorId() + " " + peer.getSecSuccessorId();
                }

                /* STEP 6 - DATE INSERTION */
                else if (clientSentence.contains("Store")) {
                    String filename = clientSentence.split(" ")[1];
                    if (isStoredPos(filename)) {
                        reply = "Store " + filename + " request accepted";
                        peer.storedFiles(filename);
                        System.out.println(reply);
                    } else {
                        reply = "Store " + filename + " request forwarded to my successor";
                        System.out.println(reply);
                        peer.request(peer.getFstSuccessorPort(), clientSentence);
                    }
                }

                /* STEP 7 - DATA RETRIEVAL */
                else if (clientSentence.contains("Request")) {
                    // e.g. "Request 4103 from peer 2"
                    clientSentenceWords = clientSentence.split(" ");
                    String filename = clientSentenceWords[1];
                    int source = Integer.parseInt(clientSentenceWords[4]);

                    if (isStoredPos(filename)) { // Current peer is the correct position
                        System.out.println("File " + filename + " is stored here");
//                        peer.request(source + 12000, "Peer " + peer.getId() + " has file " + filename);
                        // TODO transfer file here
//                        System.out.println("Sending file " + filename + " to Peer " + source);
//                        //Initialize Sockets
//                        ServerSocket ssock = new ServerSocket(source + 12000 + 256 * 2);
//                        Socket socket = ssock.accept();
//
//                        //Specify the file
//                        File file = new File(filename+".txt");
//                        FileInputStream fis = new FileInputStream(file);
//                        BufferedInputStream bis = new BufferedInputStream(fis);
//
//                        //Get socket's output stream
//                        OutputStream os = socket.getOutputStream();
//
//                        //Read File Contents into contents array
//                        byte[] contents;
//                        long fileLength = file.length();
//                        long current = 0;
//                        while (current != fileLength) {
//                            int size = 10000;
//                            if (fileLength - current >= size)
//                                current += size;
//                            else {
//                                size = (int) (fileLength - current);
//                                current = fileLength;
//                            }
//                            contents = new byte[size];
//                            bis.read(contents, 0, size);
//                            os.write(contents);
//                        }
//                        os.flush();
//                        //File transfer done. Close the socket connection!
//                        socket.close();
//                        ssock.close();
//                        System.out.println("The file has been sent");

                    } else {
                        if (source == peer.getId())
                            System.out.println("File request for " + filename + " has been sent to my successor");
                        else
                            System.out.println("Request for File " + filename
                                    + " has been received, but the file is not stored here");
                        // Send request to next peer
                        peer.request(peer.getFstSuccessorPort(), clientSentence);
                    }
                }
                // e.g. "Peer 8 has file 4103";
                else if (clientSentence.contains(" has file ")) {
                    System.out.println(clientSentence);
//                    int destination = Integer.parseInt(clientSentence.split(" ")[1]);
//                    String fn = clientSentence.split(" ")[4];
//
//                    //Initialize socket
//                    Socket socket = new Socket(InetAddress.getByName("localhost"), peer.getPort() + 256 * 2);
//                    byte[] contents = new byte[10000];
//
//                    //Initialize the FileOutputStream to the output file's full path.
//                    FileOutputStream fos = new FileOutputStream("_" + fn);
//                    BufferedOutputStream bos = new BufferedOutputStream(fos);
//                    InputStream is = socket.getInputStream();
//
//                    //No of bytes read in one read() call
//                    int bytesRead = 0;
//
//                    while((bytesRead=is.read(contents))!=-1)
//                        bos.write(contents, 0, bytesRead);
//
//                    bos.flush();
//                    socket.close();
//
//                    System.out.println("File saved successfully!");
                }

                // send reply
                DataOutputStream outToClient = new DataOutputStream(connectionSocket.getOutputStream());

                outToClient.writeBytes(String.valueOf(reply));

                connectionSocket.close();
            } catch (Exception e) {
                e.printStackTrace();
            }

        } // end of while (true)
    }

} // end of class TCPServer

