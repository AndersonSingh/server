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

    private boolean gameFinished;

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

        gameFinished = false;

        /* initialize test questions, consider creating a separate method for this. */
        questions = new ArrayList<>();
        questions.add(new Question("Test: The answer is D", "A", "B", "C", "D", 3, 10));
        questions.add(new Question("Test: The answer is A", "A", "B", "C", "D", 0, 10));

        /* send out initial question and scores. */
        sendQuestion(0);
        sendQuestion(1);
        sendPlayerScores();
    }

    public void sendQuestion(int playerId) {

        /* before sending question ensure there are more questions. */
        if(playerCurrentQuestions[playerId] == questions.size() && otherPlayerFinish == false) {

            /* there are no more questions for this player, but before ending the game, need to wait for other player to finish. */
            otherPlayerFinish = true;

            PlayerWait waitObject = new PlayerWait();
            waitObject.wait = true;
            playerConnections[playerId].sendTCP(waitObject);

        }
        else if(playerCurrentQuestions[playerId] == questions.size() && otherPlayerFinish == true) {

            /* the other player has finished so, the game can now end. */
            EndGame endObject = new EndGame();
            endObject.end = true;
            gameFinished = true;

            /* send end object to both players. */
            playerConnections[0].sendTCP(endObject);
            playerConnections[1].sendTCP(endObject);

        }
        else{
            /* send question to player. */
            playerConnections[playerId].sendTCP(questions.get(playerCurrentQuestions[playerId]));
        }

    }

    public void sendPlayerScores(){

        PlayerScores scoresObj = new PlayerScores();
        scoresObj.scores[0] = playerScores[0];
        scoresObj.scores[1] = playerScores[1];

        playerConnections[0].sendTCP(scoresObj);
        playerConnections[1].sendTCP(scoresObj);
    }

    public void receiveAnswer(int playerId, int answer){

        QuestionFeedback feedbackObj = new QuestionFeedback();

        /* if the player got the question correct, send new scores to player, and send feedback. */
        if(questions.get(playerCurrentQuestions[playerId]).getAnswer() == answer) {
            playerScores[playerId] = playerScores[playerId] + questions.get(playerCurrentQuestions[playerId]).getPoints();
            sendPlayerScores();

            feedbackObj.feedback = "You got the question correct.";
        }
        /* if the player got the question wrong, just send feedback that they got the question wrong. */
        else{


            feedbackObj.feedback = "You got the question wrong.";


        }

        playerConnections[playerId].sendTCP(feedbackObj);

        /* update the current question for the player and then send question. */
        playerCurrentQuestions[playerId] = playerCurrentQuestions[playerId] + 1;
        sendQuestion(playerId);
    }

    public void updateGameId(int newGameId){

        for(int i = 0; i < playerConnections.length; i++){
            playerConnections[i].gameId = newGameId;
        }
    }
    public void playerForfeit(int playerId){
        int otherPlayer;
        Forfeit forfeitObject = new Forfeit();
        forfeitObject.playerForfeit = true;

        if(playerId == 1){
            otherPlayer = 0;
        }
        else{
            otherPlayer = 1;
        }
        System.out.println("SENDING FOR TO "+otherPlayer);
        playerConnections[otherPlayer].sendTCP(forfeitObject);
    }

    public boolean getGameFinished(){
        return gameFinished;
    }

    public PlayerConnection[] getPlayerConnections(){
        return playerConnections;
    }

    public boolean isPlayerInGame(PlayerConnection c) {
        if(playerConnections[0] == c) {
            return true;
        }
        else if(playerConnections[1] == c) {
            return true;
        }
        else {
            return false;
        }
    }

}
