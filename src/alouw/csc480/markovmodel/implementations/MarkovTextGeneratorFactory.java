package alouw.csc480.markovmodel.implementations;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.IntStream;

import alouw.csc480.markovmodel.interfaces.MarkovTextGenerator;
import alouw.csc480.markovmodel.interfaces.PseudoRandomElementGenerator;

public class MarkovTextGeneratorFactory {
	public static MarkovTextGenerator getNewModel(final int modelOrder, final List<String> trainingData) {
		if (modelOrder <= 0) 
			throw new IllegalArgumentException("Model order must be greater than 0");
		if (trainingData == null || trainingData.isEmpty()) 
			throw new IllegalArgumentException("Empty or null training dataset");
		
		return new MarkovTextGeneratorImpl(modelOrder, trainingData);
	};
}

class MarkovTextGeneratorImpl implements MarkovTextGenerator {
	
	// the character that precedes a training dataset element; needed for N-Order models
	private final char BOUNDARY_CHAR = '*';
	private final int modelOrder;
	// a deep copy of the original data
	private final List<String> trainingData;
	// a formatted version of the training data
	private final List<String> formattedData;
	// the data structure used to generate a model
	// note that each possible N-order char sequence has its own random element generator object
	// in the event that a char sequence is at the END OF A WORD, the random element generator object will likely be empty
	private final Map<String, PseudoRandomElementGenerator> model = new HashMap<String, PseudoRandomElementGenerator>();
	
	MarkovTextGeneratorImpl(final int modelOrder, final List<String> trainingData) {
		this.modelOrder = modelOrder;
		this.trainingData =  new ArrayList<String>(trainingData);
		this.formattedData = formatTrainingData();
		buildModel();
	}

	/*
	 * Format the data for processing as follows:
	 * 		(a) convert to lowercase
	 * 		(b) trim whitespace
	 * 		(c) prepend reserved character to assist n-order modeling
	 * 		NOTE: we do not append since the random element generator signifies when there are no more characters to append
	 */
	private List<String> formatTrainingData() {
		final StringBuilder boundaryBuilder = new StringBuilder();
		for (int i = 0; i < modelOrder; i++) boundaryBuilder.append(BOUNDARY_CHAR);
		final String boundary = boundaryBuilder.toString();
		
		final List<String> cleansedData = new ArrayList<String>(trainingData.size());
		trainingData.stream().forEach(x-> cleansedData.add(boundary + x.trim().toLowerCase()));
		return cleansedData;
	}
	
	/*
	 * 4th order model example for name ****Rebeccah*** 
	 * Decompose to {****, ***R, **Re, *Reb, Rebe, ebec, becc, ecca, ccah} 
	 * 
	 * For each decomposition, we do the following:
	 * 	(a) create a k (some string of decomposed characters)
	 * 	(b) if k is absent, 
	 * 		(b1) create a new PseudoRandomElementGenerator for v
	 * 		(b2) seed it with the next character in the name
	 * 		(b3) insert <k,v> pair 
	 * 	(c) if k is present,
	 * 		(c1) retrieve v
	 * 		(c2) seed it with the next character in the name 
	 */
	private void buildModel() {
		formattedData.stream().forEach(x -> {
			for (int index = 0; index + modelOrder <= x.length(); index++) {
				final String key = x.substring(index, index + modelOrder);
				
				if (index + modelOrder == x.length()) 
					model.put(key, PseudoRandomElementGeneratorFactory.getNewGenerator());
				else {
					final char lastChar = x.substring(index+modelOrder, index+modelOrder+1).charAt(0);
					if (model.containsKey(key) ) 
						model.get(key).addMember(lastChar);
					else {
						final PseudoRandomElementGenerator value = PseudoRandomElementGeneratorFactory.getNewGenerator();
						value.addMember(lastChar);
						model.put(key, value);
					}
				}
			}
		});
	}
	
	/*
	 * Generates *numRecords* of new text where text is gte *minRecordLength* and lte to *maxRecordLength* as follows:
	 * 	(1) create an initial key of modelOrder * BOUNDARY_CHAR
	 * 	(2) retrieve the pseduorandomelementgenerator associated with that key
	 * 	(3) append the random element returned from pseduorandomelementgenerator to the initial key
	 *  (4) if the text does not violate any constraints, add it to the result set
	 *  (5) repeat steps 1 - 4 with the last modelOrder chars of the current key
	 *  (6) return the results set once it has a size of numRecords
	 */
	@Override
	public List<String> generateText(final int numRecords, final int minRecordLength, final int maxRecordLength) {
		final List<String> results = new ArrayList<>();
		
		while (results.size() < numRecords) {
			// create an initial key of 
			// 	(1) length modelOrder+1 with modelOrder*BOUNDARY_CHAR characters appended with
			// 	(2) one random char
			StringBuilder keyBuilder = new StringBuilder();
			IntStream.rangeClosed(1, modelOrder).forEach(x-> keyBuilder.append(BOUNDARY_CHAR));
			PseudoRandomElementGenerator charGenerator = model.get(keyBuilder.toString());
			keyBuilder.append(Character.toString(charGenerator.getProbabilityWeightedPseudoRandomMember()));
			
			boolean stopSearching = false;
			while (!stopSearching) {
				
				// ensure the key is always of length modelOrder
				final String key = keyBuilder.substring(keyBuilder.length()-modelOrder, keyBuilder.length());
				
				// get a new char generator
				charGenerator = model.get(key);
				
				// get the next random char
				try {
					final char value = charGenerator.getProbabilityWeightedPseudoRandomMember();
					keyBuilder.append(value);
				} catch (IllegalStateException ise) {
					// an empty random element generator object signifies that this 
					// substring has no subsequent characters (i.e. occurs only at the end of names)
					stopSearching = true;
				} 
			}
			
			// format the text (output) to match the input
			String text = keyBuilder.toString();
			text = text.replace(Character.toString(BOUNDARY_CHAR), "");
			text = text.substring(0, 1).toUpperCase() + text.substring(1);
			
			// verify the random text violates no constraints
			final boolean notTooShort = text.length() >= minRecordLength;
			final boolean notTooLong = text.length() <= maxRecordLength;
			final boolean notInTrainingSet = !trainingData.contains(text);
			final boolean notInResultSet = !results.contains(text);
			final boolean validOutput = notTooShort && notTooLong && notInTrainingSet && notInResultSet;
			
			// add it to the result set if it is valid; else continue to search for more text
			if (validOutput) {
				results.add(text);
			}
		}
		
		return results;
	}
}