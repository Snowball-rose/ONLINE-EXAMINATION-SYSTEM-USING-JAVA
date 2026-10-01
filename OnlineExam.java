import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import javax.swing.*;
import javax.swing.border.*;

public class OnlineExam extends JFrame implements ActionListener {

    private static final int TOTAL_TIME = 60;
    private static final double PASS_PERCENT = 40.0;

    private static final Color BG        = new Color(243, 240, 255);
    private static final Color HEADER    = new Color(88, 60, 200);
    private static final Color OPT_BG    = Color.WHITE;
    private static final Color OPT_SEL   = new Color(200, 225, 255);
    private static final Color OPT_BORDER= new Color(180, 170, 230);
    private static final Color PREV_CLR  = new Color(52, 120, 246);
    private static final Color NEXT_CLR  = new Color(0, 168, 150);
    private static final Color SUBMIT_CLR= new Color(255, 120, 40);

    private ArrayList<Question> questions = new ArrayList<>();
    private int[] userAnswers;
    private int currentIndex = 0;
    private int timeLeft = TOTAL_TIME;
    private boolean submitted = false;

    private JLabel titleLabel, timerLabel, questionLabel;
    private JRadioButton[] optionButtons = new JRadioButton[4];
    private ButtonGroup group = new ButtonGroup();
    private JButton prevButton, nextButton, submitButton;
    private JProgressBar progressBar;
    private Timer timer;

    public OnlineExam() {
        loadQuestions();
        Collections.shuffle(questions);
        userAnswers = new int[questions.size()];
        Arrays.fill(userAnswers, -1);

        setupUI();
        showQuestion();
        startTimer();
    }

    private void loadQuestions() {
        questions.add(new Question("Which keyword is used to inherit a class in Java?",
                new String[]{"implements", "extends", "inherits", "super"}, 1));
        questions.add(new Question("Which collection class allows duplicates and maintains insertion order?",
                new String[]{"HashSet", "TreeSet", "ArrayList", "HashMap"}, 2));
        questions.add(new Question("What is the default value of an int instance variable?",
                new String[]{"null", "0", "1", "undefined"}, 1));
        questions.add(new Question("Which method is the entry point of a Java program?",
                new String[]{"start()", "run()", "main()", "init()"}, 2));
        questions.add(new Question("Which of these is NOT a primitive data type in Java?",
                new String[]{"int", "boolean", "String", "char"}, 2));
        questions.add(new Question("Which Swing class is used to group radio buttons?",
                new String[]{"RadioGroup", "ButtonGroup", "GroupButton", "JGroup"}, 1));
        questions.add(new Question("Which access modifier makes a member visible only within its own class?",
                new String[]{"public", "protected", "default", "private"}, 3));
        questions.add(new Question("Which keyword is used to create an object in Java?",
                new String[]{"new", "create", "object", "alloc"}, 0));
        questions.add(new Question("What does JVM stand for?",
                new String[]{"Java Variable Machine", "Java Virtual Machine", "Joint Virtual Method", "Java Verified Module"}, 1));
        questions.add(new Question("Which OOP concept means wrapping data and methods into a single unit?",
                new String[]{"Polymorphism", "Inheritance", "Abstraction", "Encapsulation"}, 3));
    }

    private void setupUI() {
        setTitle("Online Examination System");
        setSize(720, 480);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        getContentPane().setBackground(BG);
        setLayout(new BorderLayout());

        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(HEADER);
        header.setBorder(new EmptyBorder(14, 20, 14, 20));

        titleLabel = new JLabel("\u2728 Java Online Exam");
        titleLabel.setFont(new Font("SansSerif", Font.BOLD, 22));
        titleLabel.setForeground(Color.WHITE);

        timerLabel = new JLabel("\u23F1 " + TOTAL_TIME + "s", SwingConstants.CENTER);
        timerLabel.setFont(new Font("SansSerif", Font.BOLD, 18));
        timerLabel.setOpaque(true);
        timerLabel.setBackground(new Color(46, 204, 113));
        timerLabel.setForeground(Color.WHITE);
        timerLabel.setBorder(new EmptyBorder(6, 16, 6, 16));

        header.add(titleLabel, BorderLayout.WEST);
        header.add(timerLabel, BorderLayout.EAST);

        progressBar = new JProgressBar(0, questions.size());
        progressBar.setStringPainted(true);
        progressBar.setForeground(new Color(255, 193, 7));
        progressBar.setBackground(new Color(220, 215, 250));

        JPanel north = new JPanel(new BorderLayout());
        north.add(header, BorderLayout.CENTER);
        north.add(progressBar, BorderLayout.SOUTH);
        add(north, BorderLayout.NORTH);

        JPanel center = new JPanel();
        center.setLayout(new BoxLayout(center, BoxLayout.Y_AXIS));
        center.setBackground(BG);
        center.setBorder(new EmptyBorder(20, 30, 10, 30));

        questionLabel = new JLabel();
        questionLabel.setFont(new Font("SansSerif", Font.BOLD, 17));
        questionLabel.setForeground(new Color(50, 30, 120));
        questionLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        center.add(questionLabel);
        center.add(Box.createVerticalStrut(18));

        for (int i = 0; i < 4; i++) {
            final JRadioButton rb = new JRadioButton();
            rb.setFont(new Font("SansSerif", Font.PLAIN, 15));
            rb.setOpaque(true);
            rb.setBackground(OPT_BG);
            rb.setFocusPainted(false);
            rb.setAlignmentX(Component.LEFT_ALIGNMENT);
            rb.setMaximumSize(new Dimension(Integer.MAX_VALUE, 42));
            rb.setBorder(new CompoundBorder(
                    new LineBorder(OPT_BORDER, 1, true),
                    new EmptyBorder(8, 14, 8, 14)));
            rb.addItemListener(e -> rb.setBackground(rb.isSelected() ? OPT_SEL : OPT_BG));
            optionButtons[i] = rb;
            group.add(rb);
            center.add(rb);
            center.add(Box.createVerticalStrut(10));
        }
        add(center, BorderLayout.CENTER);

        JPanel bottom = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 14));
        bottom.setBackground(BG);
        prevButton   = makeButton("\u25C0  Previous", PREV_CLR);
        nextButton   = makeButton("Next  \u25B6", NEXT_CLR);
        submitButton = makeButton("\u2714  Submit", SUBMIT_CLR);
        bottom.add(prevButton);
        bottom.add(nextButton);
        bottom.add(submitButton);
        add(bottom, BorderLayout.SOUTH);
    }

    private JButton makeButton(String text, Color color) {
        JButton b = new JButton(text);
        b.setFont(new Font("SansSerif", Font.BOLD, 15));
        b.setBackground(color);
        b.setForeground(Color.WHITE);
        b.setOpaque(true);
        b.setBorderPainted(false);
        b.setFocusPainted(false);
        b.setPreferredSize(new Dimension(150, 42));
        b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        b.addActionListener(this);
        return b;
    }

    private void showQuestion() {
        Question q = questions.get(currentIndex);
        questionLabel.setText("<html>Q" + (currentIndex + 1) + " of " + questions.size()
                + ":  " + q.getQuestionText() + "</html>");

        String[] opts = q.getOptions();
        for (int i = 0; i < 4; i++) {
            optionButtons[i].setText(opts[i]);
        }

        group.clearSelection();
        for (JRadioButton rb : optionButtons) rb.setBackground(OPT_BG);
        if (userAnswers[currentIndex] != -1) {
            optionButtons[userAnswers[currentIndex]].setSelected(true);
        }

        progressBar.setValue(currentIndex + 1);
        progressBar.setString("Question " + (currentIndex + 1) + " / " + questions.size());
        prevButton.setEnabled(currentIndex > 0);
        nextButton.setEnabled(currentIndex < questions.size() - 1);
    }

    private void saveAnswer() {
        for (int i = 0; i < 4; i++) {
            if (optionButtons[i].isSelected()) {
                userAnswers[currentIndex] = i;
                return;
            }
        }
    }

    private void startTimer() {
        timer = new Timer(1000, e -> {
            timeLeft--;
            timerLabel.setText("\u23F1 " + timeLeft + "s");
            if (timeLeft <= 10) {
                timerLabel.setBackground(new Color(231, 76, 60));
            } else if (timeLeft <= 30) {
                timerLabel.setBackground(new Color(243, 156, 18));
            }
            if (timeLeft <= 0) {
                timer.stop();
                JOptionPane.showMessageDialog(this, "\u23F0 Time is up! Submitting your exam...");
                submitExam();
            }
        });
        timer.start();
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (submitted) return;
        saveAnswer();

        if (e.getSource() == nextButton && currentIndex < questions.size() - 1) {
            currentIndex++;
            showQuestion();
        } else if (e.getSource() == prevButton && currentIndex > 0) {
            currentIndex--;
            showQuestion();
        } else if (e.getSource() == submitButton) {
            int confirm = JOptionPane.showConfirmDialog(this,
                    "Are you sure you want to submit?", "Confirm Submit",
                    JOptionPane.YES_NO_OPTION);
            if (confirm == JOptionPane.YES_OPTION) {
                submitExam();
            }
        }
    }

    private void submitExam() {
        if (submitted) return;
        submitted = true;
        timer.stop();
        saveAnswer();

        int total = questions.size();
        int correct = 0;
        int wrong = 0;
        for (int i = 0; i < total; i++) {
            if (userAnswers[i] == -1) continue;
            if (questions.get(i).isCorrect(userAnswers[i])) correct++;
            else wrong++;
        }
        int unattempted = total - correct - wrong;
        double percentage = (correct * 100.0) / total;
        boolean pass = percentage >= PASS_PERCENT;
        String statusColor = pass ? "#1e9e50" : "#d63031";

        String result = "<html><body style='width:230px; font-family:sans-serif; font-size:13px'>"
                + "<h2 style='color:#583cc8'>Exam Result</h2>"
                + "<table cellpadding='3'>"
                + "<tr><td>Total Questions</td><td><b>" + total + "</b></td></tr>"
                + "<tr><td>Correct Answers</td><td><b style='color:#1e9e50'>" + correct + "</b></td></tr>"
                + "<tr><td>Wrong Answers</td><td><b style='color:#d63031'>" + wrong + "</b></td></tr>"
                + "<tr><td>Unattempted</td><td><b style='color:#e67e22'>" + unattempted + "</b></td></tr>"
                + "<tr><td>Percentage</td><td><b style='color:#3478f6'>" + String.format("%.2f", percentage) + "%</b></td></tr>"
                + "</table><br>"
                + "<h2 style='color:" + statusColor + "'>Result: " + (pass ? "PASS \uD83C\uDF89" : "FAIL") + "</h2>"
                + "</body></html>";

        JOptionPane.showMessageDialog(this, result, "Exam Result",
                pass ? JOptionPane.INFORMATION_MESSAGE : JOptionPane.WARNING_MESSAGE);

        dispose();
        System.exit(0);
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new OnlineExam().setVisible(true));
    }
}