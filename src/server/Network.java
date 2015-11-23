package server;

/**
 * Created by andersonsingh on 11/16/15.
 */

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryonet.EndPoint;
import game.Question;

public class Network {
    static public final int port = 8082;
    // This registers objects that are going to be sent over the network.
    static public void register (EndPoint endPoint) {
        Kryo kryo = endPoint.getKryo();

        kryo.register(String[].class);
        kryo.register(int[].class);
        kryo.register(RegisterPlayer.class);
        kryo.register(PlayerScores.class);
        kryo.register(QuestionResponse.class);
        kryo.register(QuestionFeedback.class);
        kryo.register(PlayerWait.class);
        kryo.register(EndGame.class);
        kryo.register(Question.class);
        kryo.register(Player.class);
        kryo.register(Forfeit.class);
    }



    static public class RegisterPlayer {
        public String name;
    }

    static public class Player{
        public int playerId;
    }

    static public class PlayerScores {
        public int scores[] = new int[2];
    }

    static public class QuestionResponse {
        public int answer;
    }

    static public class QuestionFeedback {
        public String feedback;
    }

    static public class PlayerWait {
        public boolean wait;
    }

    static public class EndGame {
        public boolean end;
    }

    static public class Forfeit {
        public boolean playerForfeit;
    }


}