public class Main {

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
    }
}
