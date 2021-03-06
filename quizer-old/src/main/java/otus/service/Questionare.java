package otus.service;

import org.springframework.stereotype.Service;
import otus.dao.QuestionDAO;
import otus.model.Question;
import otus.utils.InOutService;

import java.util.List;

@Service
public class Questionare {

    private static final String ERROR_ANSWER_EXCEPTION = "errorAnswer";
    private static final String YOUR_NAME = "name";
    private static final String QUIZ = "quiz";
    private static final String Y = "y";
    private static final String N = "n";
    private static final String ANON = "anon";
    private static final String RESULT = "result";
    private static final String BREAK = "break";

    private static final String CHOOSE_YOUR_LANG = "Choose your language (EN/RU)";

    private final InOutService inOutService;
    private final QuestionDAO questionDAO;
    private final LangService langService;

    public Questionare(InOutService inOutService, QuestionDAO questionDAO, LangService langService) {
        this.inOutService = inOutService;
        this.questionDAO = questionDAO;
        this.langService = langService;
    }

    public void quizing() {
        List<Question> questions = null;
        try {
            //выбираем язык
            langService.defineLanguage(inOutService.askQuestion(CHOOSE_YOUR_LANG));
            //подбираем все ответы из CSV через репозиторий
            questions = questionDAO.getQuestions(langService.getFilename());
            if (questions == null || questions.isEmpty()) {
                inOutService.show(langService.getLangMessage(BREAK));
                return;
            }
        } catch (Exception e) {
            inOutService.show(e.getMessage());
            return;
        }

        String userName = tellMeYourName();

        //оценка
        inOutService.show(String.format(langService.getLangMessage(RESULT), userName, testing(questions), questions.size()));
    }

    //сам опрос
    private int testing(List<Question> questions) {
        int score = 0;
        String userAnswer;
        for (Question q : questions) {
            userAnswer = inOutService.askQuestion(q.getQuestion());
            if (!Y.equals(userAnswer) && !N.equals(userAnswer)) {
                inOutService.show(langService.getLangMessage(ERROR_ANSWER_EXCEPTION));
                continue;
            }
            if ((Y.equals(userAnswer) && q.getAnswer()) || (N.equals(userAnswer) && !q.getAnswer())) {
                score++;
            }
        }
        return score;
    }

    //знакомство
    private String tellMeYourName() {
        String userName = inOutService.askQuestion(langService.getLangMessage(YOUR_NAME));
        userName = userName.isEmpty() ? langService.getLangMessage(ANON) : userName;
        inOutService.show(String.format(langService.getLangMessage(QUIZ), userName));
        return userName;
    }
}
