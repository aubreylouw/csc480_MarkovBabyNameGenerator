package alouw.csc480.markovmodel.implementations;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import alouw.csc480.markovmodel.interfaces.MarkovTextGenerator;

public class BabyNameMain {
	
	enum NameGender {MALE, FEMALE};
	
	private static NameGender nameGender;
	private static int minNameLength;
	private static int maxNameLength;
	private static int numNames;
	private static int orderMarkovModel;
	
	/*
	 * Loops until valid user input is obtained for the following questions:
	 * 	(a) Male / Female name
	 * 	(b) Minimum name length (gte 1)
	 * 	(c) Maximum name length (gte min name length)
	 * 	(d) Markov model order
	 * 	(e) Number of names to generate
	 */
	public static void getUserInput() {
		System.out.println("Welcome to the N-Order Markov Baby Name Generator!");
		
		final Scanner scanner = new Scanner(System.in);
		boolean inputValid = false;
		System.out.print("Generate a Male [M] or Female [F] name? ");
		do {
			String input = scanner.nextLine();
			if (!(input.toUpperCase().equals("M") || input.toUpperCase().equals("F"))) { 
				System.out.println("Invalid input!");
				System.out.print("Enter [M] for a Male name or [F] for a female name: ");
				continue;
			} 
			
			inputValid = true;
			if (input.toUpperCase().equals("M")) nameGender = NameGender.MALE;
			else if (input.toUpperCase().equals("F")) nameGender = NameGender.FEMALE;
			else assert false;
				
		} while (!inputValid);
		
		inputValid = false;
		System.out.print("Enter a minimum name length: ");
		do {
			try {
				int input = Integer.parseInt(scanner.nextLine());
				if (input <= 0) throw new NumberFormatException();
				minNameLength = input;
				inputValid = true;
			} catch (NumberFormatException nfe) {
				System.out.println("Invalid input!");
				System.out.print("Enter a minimum name length greater than zero: ");
			} 	
		} while (!inputValid);
		
		inputValid = false;
		System.out.print("Enter a maximum name length: ");
		do {
			try {
				int input = Integer.parseInt(scanner.nextLine());
				if (input <= 0) throw new NumberFormatException();
				maxNameLength = input;
				inputValid = true;
			} catch (NumberFormatException nfe) {
				System.out.println("Invalid input!");
				System.out.print("Enter a maximum name length greater than " + minNameLength + ": ");
			} 	
		} while (!inputValid);
		
		inputValid = false;
		System.out.print("Specify the number of names to generate: ");
		do {
			try {
				int input = Integer.parseInt(scanner.nextLine());
				if (input <= 0) throw new NumberFormatException();
				numNames = input;
				inputValid = true;
			} catch (NumberFormatException nfe) {
				System.out.println("Invalid input!");
				System.out.print("Specify the number of names to generate: ");
			} 	
		} while (!inputValid);
		
		inputValid = false;
		System.out.print("Specify the order of the model: ");
		do {
			try {
				int input = Integer.parseInt(scanner.nextLine());
				if (input <= 0) throw new NumberFormatException();
				orderMarkovModel = input;
				inputValid = true;
			} catch (NumberFormatException nfe) {
				System.out.println("Invalid input!");
				System.out.print("Specify the order of the model: ");
			} 	
		} while (!inputValid);
		
		scanner.close();
	}
	
	/*
	 * Gets user input
	 * Loads a file with training data 
	 * 
	 */
	public static void main (String[] args) {
		// obtain user input necessary to create a model
		getUserInput();
		
		// determine the correct training data set
		String filename = null;
		switch (nameGender) {
			case MALE: filename = "namesBoys.txt"; 
				break;
			case FEMALE: filename = "namesGirls.txt";
				break;
		}
		
		// load the identified file
		List<String> trainingData = new ArrayList<String>();
		try {
			Scanner fileScanner = new Scanner(new FileReader(filename));
			while (fileScanner.hasNextLine())
				trainingData.add(fileScanner.nextLine());
			fileScanner.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		
		// get a markov text generator for the specific model and dataset
		MarkovTextGenerator textGenerator = MarkovTextGeneratorFactory.getNewModel(orderMarkovModel, trainingData);
		
		// generate text
		final List<String> results = textGenerator.generateText(numNames, minNameLength, maxNameLength);
		
		// show output to user
		if (results.isEmpty()) {
			System.out.println("No output for the specified constraints. Relax the model constraints, and try again.");
		} else {
			System.out.println("Random " + nameGender + " names that satisfy the model constraints:");
			results.stream().forEach(x->System.out.println(x));
		}
		
	}
}