import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.util.Hashtable;

public class p2p {

    public static void main(String[] args) throws Exception {

        int peerId = Integer.parseInt(args[1]);
        int fstSucId;
        int secSucId;
        int pingInterval = Integer.parseInt(args[args.length - 1]);
        int knownPeerId;
        String command = args[0];

        Peer peer = new Peer(peerId);

        // Set up TCP Server and keep listening the TCP request from other peers
        TCPServer ts = new TCPServer(peer);
        ts.start();
        // Set up TCP Client and keep listening on the input from keyboard
        TCPClient tc = new TCPClient(peer);
        tc.start();
        // Set up UDP connect between peer and its 2 successors
        UDPServer us = new UDPServer(peer);
        us.start();
        UDPClient uc = new UDPClient(peer, pingInterval);
        uc.start();

        // java p2p init 2 4 5 30
        if (args.length == 5 && command.equals("init")) {
            fstSucId = Integer.parseInt(args[2]);
            secSucId = Integer.parseInt(args[3]);
            peer.setFstSuccessorId(fstSucId);
            peer.setSecSuccessorId(secSucId);
        }

        // java p2p join 15 4 30
        else if (args.length == 4 && command.equals("join")) {
            knownPeerId = Integer.parseInt(args[2]);
            peer.findResponsiblePeer(knownPeerId, peerId);
        }
    }
}
