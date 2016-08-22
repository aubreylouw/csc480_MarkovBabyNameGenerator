package alouw.csc480.markovmodel.implementations;

import java.util.ArrayList;
import java.util.List;

import alouw.csc480.markovmodel.interfaces.PseudoRandomElementGenerator;

/*
 * A factory class for generating random element generators
 */
public class PseudoRandomElementGeneratorFactory {
	public static PseudoRandomElementGenerator getNewGenerator() {
		return new PseudoRandomElementGeneratorImpl();
	}
}

/*
 * Encapsulates the logic needed to generate a random element based on some internal dataset.
 * The logic is very simple: the generator allows clients to add members to its internal dataset.
 * The dataset permits duplicates and does not count how many of each element exists.
 * To generate a random element, a random element in the dataset is retrieved and returned to the user.
 * This element's randomness is weighted by the probability distribution of each element since elements
 * that predominate in the dataset are likely to be retrived using a random key more often
 * 
 * Note: this implementation relies entirely on the randomness of Math.Random for its correctness.
 */
class PseudoRandomElementGeneratorImpl implements PseudoRandomElementGenerator {
	private final List<Character> dataset = new ArrayList<>();
	private boolean initialized = false;
	
	PseudoRandomElementGeneratorImpl() {}
	
	@Override
	public void addMember(char member) {
		initialized = true;
		dataset.add(Character.toLowerCase(member));
	}

	@Override
	public char getProbabilityWeightedPseudoRandomMember() {
		if (!initialized)
			throw new IllegalStateException("Dataset is empty");
		final int key = (int)(Math.random() * dataset.size());
		return dataset.get(key).charValue();
	}
}