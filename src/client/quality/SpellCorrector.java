package client.quality;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.Scanner;

// Comes from Project 2's Spell Checker
// added getSuggestedWords function and no more - no extra comments were added
public class SpellCorrector{
	
	private class SimilarWord implements Comparable<SimilarWord>{
		String word;
		int count;
		int priority;
		
		public SimilarWord(String _word, int _count, int _distance){
			priority = _distance;
			word = _word;
			count = _count;
		}

		public int compareTo(SimilarWord that) {
			
			// Priority high to low (inverted editDistance)
			if(this.priority > that.priority) return -1;
			if(this.priority < that.priority) return 1;
			
			// Count high to low
			if(this.count > that.count) return -1;
			if(this.count < that.count) return 1;
			
			// Alphabetically
			if(this.word.compareToIgnoreCase(that.word) > 0) return 1;
			if(this.word.compareToIgnoreCase(that.word) < 0) return -1;
			
			return 0;
		}
	}
	
	Dictionary _dictionary;
	
	public SpellCorrector(){
		
	}
	
	public SpellCorrector(String filename){
		
		System.out.println("loading " + filename);
		
		
		try {
			this.useDictionary(filename);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public void useDictionary(String url) throws IOException {
		_dictionary = new Dictionary();
		
		BufferedReader reader = new BufferedReader(new InputStreamReader(new URL(url).openStream()));
		Scanner scanner = new Scanner(reader);

		scanner.useDelimiter("[^a-zA-Z]");
		while(scanner.hasNext()){
			this.addWord(scanner.next());
		}
		
		scanner.close();
		reader.close();
	}

	public void addWord(String word){
		_dictionary.add(word.toLowerCase());
	}
	
	private ArrayList<SimilarWord> testAndGenerateModifiedWords(String modifiedWord, int editDistance){
		ArrayList<SimilarWord> results = new ArrayList<SimilarWord>();
		Dictionary.Node found = _dictionary.find(modifiedWord);
		
		if(found != null){
			results.add(new SimilarWord(modifiedWord, found.getValue(), editDistance));
			//System.out.println("- " + modifiedWord + " " + found.getValue() + " " + editDistance);
		}
		
		if(editDistance > 1)
			results.addAll(this.editDistance(modifiedWord, editDistance - 1));
		
		return results;
	}
	
	// Edit Distance Functions
	// Question : Edit distance 2, does that mean 2 letters anywhere, or 2 letters juxtaposed?
	public ArrayList<SimilarWord> deletionDistance(String word, int editDistance){
		
		ArrayList<SimilarWord> results = new ArrayList<SimilarWord>();
		String modifiedWord;
		
		for(int i = 0; i < word.length(); i++){
			modifiedWord = word.substring(0, i) + word.substring(i + 1, word.length());
			// System.out.println(" - " + modifiedWord);
			results.addAll(this.testAndGenerateModifiedWords(modifiedWord, editDistance));
			
		}
		
		return results;
	}
	
	// Two letters switched
	public ArrayList<SimilarWord> transpositionDistance(String word, int editDistance){
		ArrayList<SimilarWord> results = new ArrayList<SimilarWord>();
		String modifiedWord;
		
		for(int i = 0; i < word.length() - 1; i++){
			modifiedWord = word.substring(0, i) + word.charAt(i + 1) + word.charAt(i) + word.substring(i + 2, word.length());
			// System.out.println(" - " + modifiedWord);
			results.addAll(this.testAndGenerateModifiedWords(modifiedWord, editDistance));
		}
		
		return results;
	}
	
	// Replaced letter
	// Letter replaced
	public ArrayList<SimilarWord> alterationDistance(String word, int editDistance){
		ArrayList<SimilarWord> results = new ArrayList<SimilarWord>();
		String modifiedWord;
		
		for(int i = 0; i < word.length(); i++){
			for(int j = 97; j < 97 + 26; j++){
				if(word.charAt(i) != (char) j){
					modifiedWord = word.substring(0, i) + (char) j + word.substring(i + 1, word.length());
					// System.out.println(" - " + modifiedWord);
					results.addAll(this.testAndGenerateModifiedWords(modifiedWord, editDistance));
				}
			}
		}
		
		return results;
	}
	
	// Letter inserted
	
	// Inserted letter
	public ArrayList<SimilarWord> insertionDistance(String word, int editDistance){
		ArrayList<SimilarWord> results = new ArrayList<SimilarWord>();
		String modifiedWord;
		for(int i = -1; i <= word.length(); i++){
			for(int j = 97; j < 97 + 26; j++){
				modifiedWord = (i > -1 ? word.substring(0, i) : "") + (char) j + word.substring((i > -1 ? i : 0), word.length());
				//System.out.println(" - " + modifiedWord);
				results.addAll(this.testAndGenerateModifiedWords(modifiedWord, editDistance));
			}
		}
		
		return results;
	}
	
	
	// All 4 edit
	public ArrayList<SimilarWord> editDistance(String word, int editDistance){
		ArrayList<SimilarWord> similarWords = new ArrayList<SimilarWord>();
	
		similarWords.addAll(this.deletionDistance(word, editDistance));
		similarWords.addAll(this.transpositionDistance(word, editDistance));
		similarWords.addAll(this.alterationDistance(word, editDistance));
		similarWords.addAll(this.insertionDistance(word, editDistance));
		
		return similarWords;
	}
	
	public boolean contains(String word){
		return _dictionary.exists(word);
	}
	
	public HashSet<String> getSuggestedWords(String word){
		HashSet<String> responseWords = new HashSet<String>();
		ArrayList<SimilarWord> similarWords;
		
		similarWords = this.editDistance(word, 2);
		
		for(SimilarWord similarWord : similarWords){
			responseWords.add(similarWord.word);
		}
		
		return responseWords;
	}

	// Similar Word Function
	public String suggestSimilarWord(String word){
		
		ArrayList<SimilarWord> similarWords = new ArrayList<SimilarWord>();
		
		word = word.toLowerCase();
		
		if(_dictionary.exists(word))
			return word;
		else{
			
			similarWords = this.editDistance(word, 2);
			
			if(similarWords.size() > 0){
				Collections.sort(similarWords);
				return similarWords.get(0).word;
			}else{
				return "";
			}
		}
		
	}
}