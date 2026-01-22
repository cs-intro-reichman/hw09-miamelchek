import java.util.HashMap;
import java.util.Random;

public class LanguageModel {

    // The map of this model. [cite: 179-180]
    HashMap<String, List> CharDataMap;
    
    // The window length used in this model. [cite: 226]
    int windowLength;
    
    // The random number generator used by this model. [cite: 255]
    private Random randomGenerator;

    /** Constructs a language model with the given window length and seed. [cite: 256] */
    public LanguageModel(int windowLength, int seed) {
        this.windowLength = windowLength;
        randomGenerator = new Random(seed);
        CharDataMap = new HashMap<String, List>();
    }

    /** Constructs a language model with the given window length. [cite: 256] */
    public LanguageModel(int windowLength) {
        this.windowLength = windowLength;
        randomGenerator = new Random();
        CharDataMap = new HashMap<String, List>();
    }

    /** Builds a language model from the corpus. [cite: 147-154, 376-406] */
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

        // Processes the text, one character at a time. [cite: 382-399]
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

        // Sets the p and cp fields of all CharData objects. [cite: 401-405]
        for (List probs : CharDataMap.values()) {
            calculateProbabilities(probs);
        }
    }

    /** Computes and sets probabilities (p and cp fields). [cite: 117-123] */
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
            cd2.p = (double) cd2.count / total;
            cumulative += cd2.p;
            cd2.cp = cumulative;
            last = cd2;
        }

        // Ensure the last cumulative probability is exactly 1.0 [cite: 119]
        if (last != null) {
            last.cp = 1.0;
        }
    }

    /** Returns a random character using Monte Carlo technique. [cite: 128-144] */
    char getRandomChar(List probs) {
        double r = randomGenerator.nextDouble(); // [cite: 259]
        ListIterator it = probs.listIterator(0);

        CharData cd = null;
        while (it.hasNext()) {
            cd = it.next();
            if (cd.cp > r) { // [cite: 140]
                return cd.chr;
            }
        }
        return cd.chr;
    }

    /** Generates a random text of the specified length. [cite: 204-211, 230-233] */
    public String generate(String initialText, int textLength) {
        if (initialText.length() < windowLength) { // [cite: 227-228]
            return initialText;
        }

        StringBuilder generated = new StringBuilder(initialText);

        while (generated.length() < textLength) {
            String window = generated.substring(generated.length() - windowLength);
            List probs = CharDataMap.get(window);
            
            if (probs == null) { // [cite: 233]
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
            str.append(key).append(" : ").append(keyProbs).append("\n");
        }
        return str.toString();
    }

    /** Main method to run the program from command line. [cite: 262-278] */
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
            lm = new LanguageModel(windowLength, 20); // [cite: 256]
        }

        lm.train(fileName);
        System.out.println(lm.generate(initialText, generatedTextLength)); 
    }
}