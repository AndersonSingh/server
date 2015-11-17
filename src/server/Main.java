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

                PlayerConnection connection = (PlayerConnection) object;

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
                    }
                    else {

                        /* since there are no waiting players, add player to waiting queue. */
                        System.out.println("Adding player to waiting queue.");
                        waitingPlayers.add(connection);
                    }
                }
                else if(object instanceof QuestionResponse) {

                    int answer = ((QuestionResponse) object).answer;



                }

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

        /* let's test the question class. */
        Question question = new Question("How many inches are there in a foot?", "1", "2", "3", "12", 3, 10);
        System.out.println("Question is : " + question.getText());
        System.out.println("Option 1 is : " + question.getOptions()[0]);
        System.out.println("Option 2 is : " + question.getOptions()[1]);
        System.out.println("Option 3 is : " + question.getOptions()[2]);
        System.out.println("Option 4 is : " + question.getOptions()[3]);
        System.out.println("Correct answer is at index: " + question.getAnswer());
        System.out.println("Points awarded for correct answer is: " + question.getPoints());

        new Main();
    }
}
