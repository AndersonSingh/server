package server;

/**
 * Created by andersonsingh on 11/16/15.
 */

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryonet.EndPoint;

public class Network {
    static public final int port = 8080;

    // This registers objects that are going to be sent over the network.
    static public void register (EndPoint endPoint) {
        Kryo kryo = endPoint.getKryo();
        kryo.register(String[].class);
        kryo.register(RegisterPlayer.class);
        kryo.register(PlayerScores.class);
        kryo.register(QuestionResponse.class);
        kryo.register(QuestionFeedback.class);
        kryo.register(PlayerWait.class);
        kryo.register(EndGame.class);

    }


    static public class RegisterPlayer {
        public String name;
    }

    static public class PlayerScores {
        public int player1Score;
        public int player2Score;
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


}