import algorithm.CYK;
import exception.CYKException;
import exception.ChomskyException;
import exception.GrammarException;
import grammar.Clean;
import grammar.Grammar;
import normalform.Chomsky;

import java.io.IOException;

public class Main {
    public static void main(String[] args) {
        try {
            System.out.println("----------------------------------------------------------------------------------------------");
            System.out.println("TEST CLEAN + CHOMSKY");
            System.out.println("----------------------------------------------------------------------------------------------");
            Grammar g = new Grammar("grammars/clean1.txt");
            System.out.println("Parsed Grammar : ");
            System.out.println(g);

            Clean.normalize(g);
            System.out.println("Cleaned Grammar : ");
            System.out.println(g);

            Chomsky.normalize(g);
            System.out.println("Chomsky Normal Form Grammar : ");
            System.out.println(g);

            System.out.println("----------------------------------------------------------------------------------------------");
            System.out.println("TEST CYK");
            System.out.println("----------------------------------------------------------------------------------------------");
            g = new Grammar("grammars/cyk3.txt");
            System.out.println("Parsed Grammar : ");
            System.out.println(g);
            System.out.println("CYK.isMember(aab) : " + CYK.isMember(g, "aab"));
            System.out.println("CYK.isMember(aba) : " + CYK.isMember(g, "aba"));

        } catch (IOException | ChomskyException | GrammarException | CYKException e) {
            e.printStackTrace();
        }
    }
}
