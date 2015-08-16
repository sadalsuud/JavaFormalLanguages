import exception.ChomskyException;
import exception.GrammarException;
import grammar.Clean;
import grammar.Grammar;
import normalform.Chomsky;

import java.io.IOException;

public class Main {
    public static void main(String[] args) {
        try {
            Grammar g = new Grammar("grammars/clean1.txt");
            System.out.println("Parsed Grammar : ");
            System.out.println(g + "\n");

            Clean.normalize(g);
            System.out.println("Cleaned Grammar : ");
            System.out.println(g + "\n");

            Chomsky.normalize(g);
            System.out.println("Chomsky Normal Form Grammar : ");
            System.out.println(g + "\n");
        } catch (IOException | ChomskyException | GrammarException e) {
            e.printStackTrace();
        }
    }
}
