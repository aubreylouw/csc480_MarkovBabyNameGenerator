package alouw.csc480.markovmodel.interfaces;

/*
 * Generates a pesudo random char based on that character's probability distribution within an internally managed dataset of characters. 
 * EXAMPLE: given an internal population of 4 characters ['A','A','A','B'], 
 * 	the ProbableCharGenerator will produce an 'A' 75% of the time and B 25% of the time.
 * An "empty" ProbableCharGenerator generates throws an illegalstateexception
 * 
 */
public interface PseudoRandomElementGenerator {
	public void addMember(final char member);
	public char getProbabilityWeightedPseudoRandomMember() throws IllegalStateException;
}