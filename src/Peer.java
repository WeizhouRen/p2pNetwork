import java.io.*;
import java.net.*;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

public class Peer {

    private int id;
    private int fstSuccessorId;
    private int secSuccessorId;
    private int fstPredecessor;
    private int secPredecessor;
    private int port;
    private int BASE_PORT = 12000;
    private List<String> storedFiles;

    public Peer(int id) throws Exception {
        this.id = id;
        port = BASE_PORT + id;
        fstPredecessor = 0;
        secPredecessor = 0;
        storedFiles = new LinkedList<>();
    }

    public void setFstSuccessorId(int firstSuccessor) {
        this.fstSuccessorId = firstSuccessor;
    }

    public void setSecSuccessorId(int secondSuccessor) {
        this.secSuccessorId = secondSuccessor;
    }

    public int getFstSuccessorId() {
        return fstSuccessorId;
    }

    public int getSecSuccessorId() {
        return secSuccessorId;
    }

    public int getFstSuccessorPort() {
        return BASE_PORT + fstSuccessorId;
    }

    public int getSecSuccessorPort() {
        return BASE_PORT + secSuccessorId;
    }

    public void setFstPredecessor(int firstPredecessor) {
        this.fstPredecessor = firstPredecessor;
    }

    public void setSecPredecessor(int secondPredecessor) {
        this.secPredecessor = secondPredecessor;
    }

    public int getFstPredecessor() {
        return fstPredecessor;
    }

    public int getSecPredecessor() {
        return secPredecessor;
    }

    public int getFstPredecessorPort() {
        return BASE_PORT + fstPredecessor;
    }

    public int getSecPredecessorPort() {
        return BASE_PORT + secPredecessor;
    }

    public boolean isLastPeer() {
        return id > fstSuccessorId;
    }

    public boolean isFirstPeer() {
        return id < fstPredecessor;
    }

    public int getId() {
        return id;
    }

    public int getPort() {
        return port;
    }

    private void printUpdatedSuccessors() {
        System.out.println("My new first successor is Peer " + fstSuccessorId);
        System.out.println("My new second successor is Peer " + secSuccessorId);
    }

    /**
     * Setup TCP client and send request to corresponding TCP server
     * @param serverPort destination server port
     * @param sentence request content
     * @return replied sentence from server
     * @throws IOException
     */
    public String request(int serverPort, String sentence) throws IOException {

        // create socket which connects to server
        Socket clientSocket = new Socket("localhost", serverPort + 256);
        // write to server, send forward msg
        DataOutputStream outToServer = new DataOutputStream(clientSocket.getOutputStream());
        outToServer.writeBytes(sentence + "\n");

        // create read stream and receive from server
        BufferedReader inFromServer = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
        String sentenceFromServer = inFromServer.readLine();
        System.out.println("回复：" + sentenceFromServer);

        // e.g. "Peer 15 Join request received"
        if (sentenceFromServer.contains("Join request received")) {
            setSecSuccessorId(Integer.parseInt(sentenceFromServer.split(" ")[1]));
            System.out.println(sentenceFromServer);
            printUpdatedSuccessors();
        }

        // e.g. "Peer 9 will depart from the network"
        else if (sentenceFromServer.contains("will depart from the network")) {
            System.out.println(sentenceFromServer);
            if (id == Integer.parseInt(sentenceFromServer.split(" ")[1])) {
                this.fstSuccessorId = 0;
                this.secSuccessorId = 0;
            }
        }

        // e.g. "First successor is 9"
        else if (sentenceFromServer.contains("First successor is")) {
            // update successors of the predecessor of quited peer
            setFstSuccessorId(secSuccessorId);
            setSecSuccessorId(Integer.parseInt(sentenceFromServer.split(" ")[3]));
            printUpdatedSuccessors();
        }

        // e.g. "Your 1st successor's successors are 19 2"
        else if (sentenceFromServer.contains("Your 1st successor's successors are ")) {
            int fst = Integer.parseInt(sentenceFromServer.split(" ")[5]);
            int sec = Integer.parseInt(sentenceFromServer.split(" ")[6]);
            // if Peer 9 has not updated, it's 1st successor should be 14 which is Peer 8 's 2nd successor
            if (secSuccessorId != fst) { // has updated
                setSecSuccessorId(fst);
            } else {
                setSecSuccessorId(sec);
            }
            printUpdatedSuccessors();
        }

        // e.g. "File 2067 should store at peer 19"
        else if (sentenceFromServer.contains(" should store at peer ")) {
            String filename = sentenceFromServer.split(" ")[1];
            System.out.println("Store " + filename + " request accepted");
            storedFiles.add(filename);
        }

        // close client socket
        clientSocket.close();
        return sentenceFromServer;
    }

    /**
     *
     * @param id - The known Id that will receive the request
     * @param joinId - Id of the peer who is requesting to join the network
     * @throws Exception
     */
    public void findResponsiblePeer(int id, int joinId) throws Exception {
        // e.g. Peer-15-Join request forwarded to my successor-5-8
        String sentence = "Peer " + joinId + " Join request forwarded to my successor";
        // e.g. "Peer 15 Join request received-19-2"
        request(id + BASE_PORT, sentence);
    }

    /**
     * Update the joining peer's successor before joining request complete
     * @param joinId
     * @throws IOException
     */
    public void updateJoinSuccessor(int joinId) throws IOException {
        // e.g. 19-2
        String successors = "Updating successors of joining peer-" + fstSuccessorId + "-" + secSuccessorId;
        request(joinId + BASE_PORT, successors);
    }

    /**
     * Add the filename to this peer
     * @param filename
     */
    public void storedFiles(String filename) {
        storedFiles.add(filename);
    }
}
