import java.util.HashMap;
import java.util.Random;

public class LanguageModel {

    // The map of this model.
    // Maps windows to lists of charachter data objects. [cite: 179-180]
    HashMap<String, List> CharDataMap;
    
    // The window length used in this model. [cite: 226]
    int windowLength;
    
    // The random number generator used by this model. [cite: 255]
    private Random randomGenerator;

    /** Constructs a language model with the given window length and a given
     * seed value. [cite: 256] */
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

    /** Builds a language model from the text in the given file (the corpus). [cite: 147-154] */
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

        // Processes the entire text, one character at a time. [cite: 382]
        while (!in.isEmpty()) {
            c = in.readChar(); // [cite: 386]
            List probs = CharDataMap.get(window); // [cite: 388]
            
            if (probs == null) {
                probs = new List();
                CharDataMap.put(window, probs); // [cite: 393-395]
            }

            probs.update(c); // [cite: 397]
            
            // Advances the window. [cite: 398]
            window = window.substring(1) + c;
        }

        // Computes and sets the p and cp fields. [cite: 401-405]
        for (List probs : CharDataMap.values()) {
            calculateProbabilities(probs);
        }
    }

    // Computes and sets the probabilities (p and cp fields) of all the
    // characters in the given list. [cite: 117-123]
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

        // כדי להימנע מבעיות דיוק של double, נוודא שהאחרון תמיד מגיע ל-1.0 [cite: 119]
        if (last != null) {
            last.cp = 1.0;
        }
    }

    // Returns a random character from the given probabilities list. [cite: 128-144]
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

    /**
     * Generates a random text. [cite: 204-211]
     */
    public String generate(String initialText, int textLength) {
        if (initialText.length() < windowLength) { // [cite: 227]
            return initialText;
        }

        StringBuilder generated = new StringBuilder(initialText);

        while (generated.length() < textLength) { // [cite: 232]
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
        if (randomGeneration)
            lm = new LanguageModel(windowLength);
        else
            lm = new LanguageModel(windowLength, 20);

        lm.train(fileName);
        System.out.println(lm.generate(initialText, generatedTextLength)); 
    }
}