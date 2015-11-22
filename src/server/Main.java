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

                        /* create a new instance of game class. */

                        /* find the total number of games running */
                        int totalGames = games.size();

                        /* the new gameId will be totalGames */
                        player1Connection.gameId = totalGames;
                        connection.gameId = totalGames;

                        games.add(new Game(player1Connection, connection));
                    }
                    else {

                        /* since there are no waiting players, add player to waiting queue. */
                        System.out.println("Adding player to waiting queue.");
                        waitingPlayers.add(connection);
                    }
                }
                else if(object instanceof QuestionResponse) {

                    int answer = ((QuestionResponse) object).answer;
                    games.get(connection.gameId).receiveAnswer(connection, answer);


                }

            }

            /* this function will be executed when a client disconnects. */
            public void disconnected(Connection c){

            }

        });


        try {
            server.bind(Network.port);
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
