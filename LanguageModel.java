import java.util.HashMap;
import java.util.Random;

public class LanguageModel {

    // The map of this model. [cite: 179-180]
    HashMap<String, List> CharDataMap;
    
    // The window length used in this model. [cite: 226]
    int windowLength;
    
    // The random number generator used by this model. [cite: 255]
    private Random randomGenerator;

    public LanguageModel(int windowLength, int seed) {
        this.windowLength = windowLength;
        randomGenerator = new Random(seed);
        CharDataMap = new HashMap<String, List>();
    }

    public LanguageModel(int windowLength) {
        this.windowLength = windowLength;
        randomGenerator = new Random();
        CharDataMap = new HashMap<String, List>();
    }

    /** Builds a language model from the corpus. [cite: 147-154] */
    public void train(String fileName) {
        String window = "";
        char c;
        In in = new In(fileName);

        // Reads just enough characters to form the first window. [cite: 380-381]
        for (int i = 0; i < windowLength; i++) {
            if (!in.isEmpty()) {
                window += in.readChar();
            }
        }

        while (!in.isEmpty()) {
            c = in.readChar();
            List probs = CharDataMap.get(window);
            
            if (probs == null) {
                probs = new List();
                CharDataMap.put(window, probs);
            }

            probs.update(c);
            window = window.substring(1) + c;
        }

        for (List probs : CharDataMap.values()) {
            calculateProbabilities(probs);
        }
    }

    /** Computes and sets p and cp fields. [cite: 117-123] */
    void calculateProbabilities(List probs) {               
        ListIterator it = probs.listIterator(0);
        if (it == null) return;

        int total = 0;
        while (it.hasNext()) {
            CharData cd = it.next();
            total += cd.count;
        }

        double cumulative = 0.0;
        ListIterator it2 = probs.listIterator(0);
        CharData last = null;

        while (it2.hasNext()) {
            CharData cd2 = it2.next();
            last = cd2;
            cd2.p = (double) cd2.count / total;
            cumulative += cd2.p;
            cd2.cp = cumulative;
        }

        if (last != null) {
            last.cp = 1.0;
        }
    }

    /** Returns a random character using Monte Carlo technique. [cite: 128-144] */
    char getRandomChar(List probs) {
        double r = randomGenerator.nextDouble();
        ListIterator it = probs.listIterator(0);

        CharData cd = null;
        while (it.hasNext()) {
            cd = it.next();
            if (cd.cp > r) {
                return cd.chr;
            }
        }
        return cd.chr;
    }

    /** Generates a random text. [cite: 204-211] */
    public String generate(String initialText, int textLength) {
        if (initialText.length() < windowLength) {
            return initialText;
        }

        StringBuilder generated = new StringBuilder(initialText);
        // תיקון: מייצר textLength תווים נוספים מעבר לטקסט ההתחלתי
        int targetLength = initialText.length() + textLength;

        while (generated.length() < targetLength) {
            String window = generated.substring(generated.length() - windowLength);
            List probs = CharDataMap.get(window);
            
            if (probs == null) {
                break;
            }

            char nextChar = getRandomChar(probs);
            generated.append(nextChar);
        }

        return generated.toString();
    }

    public String toString() {
        StringBuilder str = new StringBuilder();
        for (String key : CharDataMap.keySet()) {
            List keyProbs = CharDataMap.get(key);
            str.append(key + " : " + keyProbs + "\n");
        }
        return str.toString();
    }

    public static void main(String[] args) {
        int windowLength = Integer.parseInt(args[0]);
        String initialText = args[1];
        int generatedTextLength = Integer.parseInt(args[2]);
        Boolean randomGeneration = args[3].equals("random");
        String fileName = args[4];

        LanguageModel lm;
        if (randomGeneration) {
            lm = new LanguageModel(windowLength);
        } else {
            lm = new LanguageModel(windowLength, 20);
        }

        lm.train(fileName);
        System.out.println(lm.generate(initialText, generatedTextLength)); 
    }
}