package server;

import game.*;

import com.esotericsoftware.kryonet.*;
import server.Network.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Date;

public class Main {

    private Server server;
    private ArrayList<Game> games;
    private ArrayList<PlayerConnection> waitingPlayers;
    private HashMap<String, Integer> tracker;
    private ArrayList<String> blackList;
    private static final int MAX_CONNECTED_INTERVAL = 3;
    private static int max_connects = 3;
    private static Date serverStartTime;
    private static final long TIME_TO_RESET = 300000;

    /* constructor function */
    public Main() {

        /* initialize server object and handles new players connections. */
        server = new Server() {
            protected Connection newConnection () {
                System.out.println("New Player Connected.");
                return new PlayerConnection();
            }
        };

        /* initialize tracker map */
        tracker = new HashMap<>();

        /* initialize blacklist array */
        blackList = new ArrayList<>();

        Network.register(server);


        /* initialize games and waiting players. */
        games = new ArrayList<>();
        waitingPlayers = new ArrayList<>();

        /* setup a server listener to listen for various objects from connections. */
        server.addListener(new Listener() {

            public void received (Connection c, Object object) {

                PlayerConnection connection = (PlayerConnection) c;

                if(object instanceof RegisterPlayer) {

                    /* ensure connection has not already been assigned a name. */
                    if(connection.name != null) return;

                    /* store the name in the connection object. */
                    connection.name = ((Network.RegisterPlayer) object).name;

                    /* check if there is a player waiting, if there is, start a new game with current player and waiting player. */
                    if(!waitingPlayers.isEmpty()) {

                        /* there is one waiting player. */
                        PlayerConnection player1Connection = waitingPlayers.remove(0);

                        /* start a game with both players. */
                        System.out.println("Starting a new game with two players.");

                        /* assign player ids */
                        player1Connection.playerId = 0;
                        connection.playerId = 1;

                        /* send each player an object to let them know if they are player 1 or 2 from the server's perspective. */
                        Player player1 = new Player();
                        player1.playerId = 0;
                        player1Connection.sendTCP(player1);

                        Player player2 = new Player();
                        player2.playerId = 1;
                        connection.sendTCP(player2);

                        /* send the names of each player back. */
                        PlayerNames  namesObj = new PlayerNames();
                        namesObj.names[0] = player1Connection.name;
                        namesObj.names[1] = connection.name;
                        player1Connection.sendTCP(namesObj);
                        connection.sendTCP(namesObj);

                        /* find the total number of games running */
                        int totalGames = games.size();

                        /* the new gameId will be totalGames */
                        player1Connection.gameId = totalGames;
                        connection.gameId = totalGames;

                        /* create a new instance of game class. */
                        games.add(new Game(player1Connection, connection));
                    }
                    else {

                        /* since there are no waiting players, add player to waiting queue. */
                        System.out.println("Adding player to waiting queue.");

                        /* set a playerId of -1 to indicate this player is not in a game. */
                        //connection.playerId = -1;
                        waitingPlayers.add(connection);
                    }
                }
                else if(object instanceof QuestionResponse) {

                    int answer = ((QuestionResponse) object).answer;
                    System.out.println("Answer returned: " + answer);
                    try{
                        games.get(connection.gameId).receiveAnswer(connection.playerId, answer);
                    }
                    catch(Exception e){
                        System.out.println(e);
                    }

                }

            }

            /* this function will be executed when a client connects. */
            public void connected(Connection c){

                PlayerConnection connection = (PlayerConnection) c;

                Date temp = new Date();

                /* checking if enough time has passed to allow blocked players to play again */
                if(temp.getTime() > (serverStartTime.getTime() + TIME_TO_RESET)){
                    blackList.clear();
                    setServerTime(temp.getTime());
                    setMaxConnects(max_connects + MAX_CONNECTED_INTERVAL);
                }

                String ip = c.getRemoteAddressTCP().getAddress().getHostAddress();
                BlockStatus message = new BlockStatus();

                updateTracker(ip);

                if(!isBlocked(ip)) {
                    System.out.println(ip + " is cleared to play");

                    // send BlockStatus object to client to indicate that they are not blocked
                    message.state = false;
                    c.sendTCP(message);
                }

                else{
                    // set player id to -2 to indicate that this player has been blocked so disconnected method can handle accordingly
                    connection.playerId = -2;

                    System.out.println(ip + " is blocked");
                    // send BlockStatus object to client to indicate that they are blocked
                    message.state = true;
                    c.sendTCP(message);

                    // sever connection to this client
                    c.close();
                }
            }

            /* this function will be executed when a client disconnects. */
            public void disconnected(Connection c){

                PlayerConnection connection = (PlayerConnection) c;

                /* if the connection had a playerId of -2, they were blocked. */
                if(connection.playerId == -2){
                    System.out.println("DEBUG: Client disconnected because they are blocked.");
                    return;
                }

                /* if the connection had a playerId of -1, they were in the waiting queue. */
                if(connection.playerId == -1) {
                    /* remove connection from waiting queue. */
                    waitingPlayers.remove(connection);
                }
                /* player is in a game. */
                else {


                    try {
                        /* try to find the game the player belongs to. */
                        Game game = games.get(connection.gameId);

                        /* ensure the player is a member of this game. */
                        if(game.isPlayerInGame(connection)) {

                            /* ensure the game is finished, if not inform the other player that he/she won. */
                            if(!game.getGameFinished()) {

                                /* since game is not finished, send a packet to other player informing them that the other player quit */
                                game.playerForfeit(connection.playerId);
                                System.out.println("FORFEIT SENT TO"+connection.playerId);
                            }
                        }

                        /* we can remove the game from the list of games */
                        games.remove(connection.gameId);

                        /* update the game ids for the necessary games. */
                        for(int i = connection.gameId; i < games.size(); i++) {
                            games.get(i).updateGameId(i);
                        }
                    }
                    catch(Exception e) {
                        System.out.println(e);
                    }

                }

                System.out.println("disconnect");
            }

        });


        try {
            server.bind(Network.port, 8083);
            server.start();
            serverStartTime = new Date();
        }
        catch(Exception e) {
            System.out.println(e);
        }

    }

    public void blockIp(String ip){
        blackList.add(ip);
    }

    public void updateTracker(String ip){
        Integer value = tracker.get(ip);

        // if this is not the first time this IP address has connected
        if(value != null){
            // if this IP address has played the game for the max amount of plays allowed
            if(value == max_connects){
                // add IP address to blacklist
                blockIp(ip);
            }
            // IP address can still play game
            else{
                // update number of times played
                tracker.put(ip, value+1);
            }
        }

        // this is the first time this IP address has connected
        else{
            tracker.put(ip, 1);
        }
        System.out.println(ip + " has played " + tracker.get(ip) + " times");
    }

    // determines if an IP address has been blocked
    public boolean isBlocked(String ip){
        if(blackList.contains(ip))
            return true;
        return false;
    }

    public static void setServerTime(long time){
        serverStartTime.setTime(time);
    }

    public void setMaxConnects(int new_connects){
        max_connects = new_connects;
    }

    public class PlayerConnection extends Connection {
        public String name;
        public int playerId;
        public int gameId;
    }

    public static void main(String[] args) {
        new Main();
    }
}
