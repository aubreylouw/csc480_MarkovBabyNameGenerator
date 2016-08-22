package alouw.csc480.markovmodel.interfaces;

import java.util.List;

public interface MarkovTextGenerator {
	public List<String> generateText(final int numRecords, final int minRecordLength, final int maxRecordLength);
}