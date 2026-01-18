import java.util.HashMap;
import java.util.Random;

public class LanguageModel {

    // The map of this model.
    // Maps windows to lists of charachter data objects.
    HashMap<String, List> CharDataMap;
    
    // The window length used in this model.
    int windowLength;
    
    // The random number generator used by this model. 
	private Random randomGenerator;

    /** Constructs a language model with the given window length and a given
     *  seed value. Generating texts from this model multiple times with the 
     *  same seed value will produce the same random texts. Good for debugging. */
    public LanguageModel(int windowLength, int seed) {
        this.windowLength = windowLength;
        randomGenerator = new Random(seed);
        CharDataMap = new HashMap<String, List>();
    }

    /** Constructs a language model with the given window length.
     * Generating texts from this model multiple times will produce
     * different random texts. Good for production. */
    public LanguageModel(int windowLength) {
        this.windowLength = windowLength;
        randomGenerator = new Random();
        CharDataMap = new HashMap<String, List>();
    }

    /** Builds a language model from the text in the given file (the corpus). */
    public void train(String fileName) {

        In in = new In(fileName);
        String corpus = in.readAll();

        if (corpus.length() < windowLength + 1) {
            return;
        }

        for (int i = 0; i <= corpus.length() - windowLength - 1; i++) {
            String window = corpus.substring(i, i + windowLength);
            char nextChar = corpus.charAt(i + windowLength);

            List probs = CharDataMap.get(window);
            if (probs == null) {
                probs = new List();
                CharDataMap.put(window, probs);
            }

            probs.update(nextChar);   
        }

        for (List probs : CharDataMap.values()) {
            calculateProbabilities(probs);   
        }


    }


    // Computes and sets the probabilities (p and cp fields) of all the
	// characters in the given list. */
	void calculateProbabilities(List probs) {				
		ListIterator it = probs.listIterator(0);
        int total = 0;
        // סוכמים את כל התוויום
        while (it.hasNext()) {
            CharData cd = it.next();
            total += cd.count;
        }
        
        double cumulative = 0.0;
        ListIterator it2 = probs.listIterator(0);
        while (it2.hasNext()) {
            CharData cd2 = it2.next();
            cd2.p = (double) cd2.count / total;
            cumulative += cd2.p;
            cd2.cp = cumulative;
        }


	}

    // Returns a random character from the given probabilities list.
	char getRandomChar(List probs) {
		double r = randomGenerator.nextDouble();
        ListIterator it = probs.listIterator(0);
        // סוכמים את כל התוויום
        while (it.hasNext()) {
            CharData cd = it.next();
            if (cd.cp > r) {
                return cd.chr;
            }
        }
        return probs.listIterator(0).next().chr;
	}

    /**
	 * Generates a random text, based on the probabilities that were learned during training. 
	 * @param initialText - text to start with. If initialText's last substring of size numberOfLetters
	 * doesn't appear as a key in Map, we generate no text and return only the initial text. 
	 * @param numberOfLetters - the size of text to generate
	 * @return the generated text
	 */
	public String generate(String initialText, int textLength) {
                // If initial text is shorter than windowLength, we cannot generate.
        if (initialText.length() < windowLength) {
            return initialText;
        }

        StringBuilder generated = new StringBuilder(initialText);

        // Keep generating until reaching desired length
        while (generated.length() < textLength) {

            String window = generated.substring(generated.length() - windowLength);

            List probs = CharDataMap.get(window);
            if (probs == null) {
                // If window not found, stop and return what we have so far
                break;
            }

            char nextChar = getRandomChar(probs);
            generated.append(nextChar);
        }

        return generated.toString();
            
	}

    /** Returns a string representing the map of this language model. */
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

            // Create the LanguageModel object
            LanguageModel lm;
            if (randomGeneration)
                lm = new LanguageModel(windowLength);
            else
                lm = new LanguageModel(windowLength, 20);

            // Train the model
            lm.train(fileName);

            // Generate and print text
            System.out.println(lm.generate(initialText, generatedTextLength)); 
    }
}
