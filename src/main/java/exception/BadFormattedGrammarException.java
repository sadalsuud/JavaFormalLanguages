package exception;

public class BadFormattedGrammarException extends GrammarException {
    public BadFormattedGrammarException(String message) {
        super("Error while parsing grammar : " + message);
    }
}
