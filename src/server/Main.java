package server;

import game.*;

import com.esotericsoftware.kryonet.*;
import server.Network.*;

import java.util.ArrayList;

public class Main {

    private Server server;
    private ArrayList<Game> games;
    private ArrayList<PlayerConnection> waitingPlayers;

    /* constructor function */
    public Main() {

        /* initialize server object and handles new players connections. */
        server = new Server() {
            protected Connection newConnection () {
                System.out.println("New Player Connected.");
                return new PlayerConnection();
            }
        };

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

            /* this function will be executed when a client disconnects. */
            public void disconnected(Connection c){

//                PlayerConnection connection = (PlayerConnection) c;
//
//                /* if the connection had a playerId of -1, they were in the waiting queue. */
//                if(connection.playerId == -1) {
//                    /* remove connection from waiting queue. */
//                    waitingPlayers.remove(connection);
//                }
//                /* player is in a game. */
//                else {
//
//
//                    try {
//                        /* try to find the game the player belongs to. */
//                        Game game = games.get(connection.gameId);
//
//                        /* ensure the player is a member of this game. */
//                        if(game.isPlayerInGame(connection)) {
//
//                            /* ensure the game is finished, if not inform the other player that he/she won. */
//                            if(!game.getGameFinished()) {
//
//                                /* since game is not finished, send a packet to other player informing them that the other player quit */
//                                game.playerForfeit(connection.playerId);
//                            }
//                        }
//
//                        /* we can remove the game from the list of games */
//                        games.remove(connection.gameId);
//
//                        /* update the game ids for the necessary games. */
//                        for(int i = connection.gameId; i < games.size(); i++) {
//                            games.get(i).updateGameId(i);
//                        }
//                    }
//                    catch(Exception e) {
//                        System.out.println(e);
//                    }
//
//                }

                System.out.println("disconnect");
            }

        });


        try {
            server.bind(Network.port, 8083);
            server.start();
        }
        catch(Exception e) {
            System.out.println(e);
        }

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
