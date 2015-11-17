package game;

/**
 * Created by Anderson on 11/15/2015.
 */

public class Question {

    /* properties of a question. */
    private String text;
    private String[] options;
    private int answer;
    private int points;

    /* constructor function. */
    public Question(String text, String option1, String option2, String option3, String option4, int answer, int points) {
        setText(text);
        setOptions(option1, option2, option3, option4);
        setAnswer(answer);
        setPoints(points);
    }

    /* setters for  properties of a question. */
    public void setText(String text) {
        this.text = text;
    }

    public void setOptions(String option1, String option2, String option3, String option4) {
        this.options = new String[4];
        this.options[0] = option1;
        this.options[1] = option2;
        this.options[2] = option3;
        this.options[3] = option4;
    }

    public void setAnswer(int answer) {
        this.answer = answer;
    }

    public void setPoints(int points) {
        this.points = points;
    }

    /* getters for properties of a question. */
    public String getText() {
        return this.text;
    }

    public String[] getOptions() {
        return this.options;
    }

    public int getAnswer() {
        return this.answer;
    }

    public int getPoints() {
        return this.points;
    }

}
