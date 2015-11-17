package game;

import server.Main;
import server.Main.PlayerConnection;
import server.Network;
import server.Network.*;

import java.util.ArrayList;

/**
 * Created by andersonsingh on 11/16/15.
 */
public class Game {

    private int[] playerScores;
    private int[] playerCurrentQuestions;

    private PlayerConnection[] playerConnections;

    private ArrayList<Question> questions;

    private boolean otherPlayerFinish;

    public Game(PlayerConnection a, PlayerConnection b) {

        /* initialize scores for both players. */
        playerScores = new int[2];
        playerScores[a.playerId] = 0;
        playerScores[b.playerId] = 0;

        /* initialize current question for both players. */
        playerCurrentQuestions = new int[2];
        playerCurrentQuestions[a.playerId] = 0;
        playerCurrentQuestions[b.playerId] = 0;

        /* initialize connections for both players. */
        playerConnections = new PlayerConnection[2];
        playerConnections[a.playerId] = a;
        playerConnections[b.playerId] = b;

        /* initialize waiting boolean */
        otherPlayerFinish = false;

        /* initialize test questions, consider creating a separate method for this. */
        questions = new ArrayList<>();
        questions.add(new Question("Test: The answer is D", "A", "B", "C", "D", 3, 10));
        questions.add(new Question("Test: The answer is A", "A", "B", "C", "D", 0, 10));

        /* send out initial question and scores. */
        sendQuestion(playerConnections[0]);
        sendQuestion(playerConnections[1]);
        sendPlayerScores();
    }

    public void sendQuestion(PlayerConnection c) {

        /* before sending question ensure there are more questions. */
        if(playerCurrentQuestions[c.playerId] == questions.size() && otherPlayerFinish == false) {

            /* there are no more questions for this player, but before ending the game, need to wait for other player to finish. */
            otherPlayerFinish = true;

            PlayerWait waitObject = new PlayerWait();
            waitObject.wait = true;
            c.sendTCP(waitObject);

        }
        else if(playerCurrentQuestions[c.playerId] == questions.size() && otherPlayerFinish == true) {

            /* the other player has finished so, the game can now end. */
            EndGame endObject = new EndGame();
            endObject.end = true;
            c.sendTCP(endObject);

        }
        else{
            /* send question to player. */
            c.sendTCP(questions.get(playerCurrentQuestions[c.playerId]));
        }

    }

    public void sendPlayerScores(){

        PlayerScores scores = new PlayerScores();
        scores.player1Score = playerScores[0];
        scores.player2Score = playerScores[1];

        playerConnections[0].sendTCP(scores);
        playerConnections[1].sendTCP(scores);
    }

    public void receiveQuestion(PlayerConnection c, int answer){

        /* if the player got the question correct, send new scores to player, and send feedback. */
        if(questions.get(playerCurrentQuestions[c.playerId]).getAnswer() == answer) {
            playerScores[c.playerId] = playerScores[c.playerId] + questions.get(playerCurrentQuestions[c.playerId]).getPoints();
            sendPlayerScores();

            /* send feedback. */
            QuestionFeedback feedbackObj = new QuestionFeedback();
            feedbackObj.feedback = "You got the question correct.";
        }


    }





}
