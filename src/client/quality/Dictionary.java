package client.quality;

// Comes from Project 2's Spell Checker
// Aside from some minor changes - I didn't modify it, or add extra comments.
public class Dictionary{
	
	public class Node{
		
		char _letter;
		Node[] _nodes;
		
		public int count;
		
		public Node(char letter){
			_letter = letter;
			_nodes = new Node[26];
			count = 0;
		}
		
		// Returns a new Node if it needs to.
		public Node get(char letter){
			return this.get(Dictionary.hash(letter));
		}
		
		public Node get(int index){
			return _nodes[index];
		}
		
		public Node add(char letter){
			return _nodes[Dictionary.hash(letter)] = new Node(letter);
		}
		public char getLetter(){
			return _letter;
		}
		
		public int getValue() {
			return count;
		}
	}
	
	Node _root;
	int _nodeCount;
	int _wordCount;
	
	public Dictionary(){
		_root = new Node(' ');
		_nodeCount = 1;
		_wordCount = 0;
	}

	public void add(String word) {
		Node currentNode = _root;
		for(int i = 0; i < word.length(); i++){
			if(currentNode.get(word.charAt(i)) == null){
				currentNode = currentNode.add(word.charAt(i));
				_nodeCount++;
			}else{
				currentNode = currentNode.get(word.charAt(i));
			}
		}
		
		currentNode.count++;
		
		if(currentNode.getValue() == 1){
			_wordCount++;
		}
	}

	public Node find(String word) {
		word = word.toLowerCase();
		Node currentNode = _root;
		for(int i = 0; i < word.length(); i++){
			if(currentNode.get(word.charAt(i)) == null){
				return null;
			}else{
				currentNode = currentNode.get(word.charAt(i));
			}
		}
		
		// We may have found an interm node - make sure it's end-of-word before sending
		if(currentNode.getValue() > 0)
			return currentNode;
		else
			return null;
	}	
	
	public boolean exists(String word){		
		if(this.find(word) == null)
			return false;
		else
			return true;
	}

	public int getWordCount() {
		return _wordCount;
	}

	public int getNodeCount() {
		return _nodeCount;
	}

	static public int hash(char letter){
		try{
			if(letter - 'a' > 25 || letter - 'a' < 0)
				throw new Exception("Letter " + letter + " is out of bounds for hashing function");
			
			return letter - 'a';
		}catch(Exception e){
			return 0;
		}
	}
	

	public String toString(){
		return this.print(_root, "");
	}
	
	private String print(Node node, String prepend){
		String returnString = "";
		prepend += node.getLetter();
		
		if(node.getValue() > 0)
			returnString += prepend + " " + node.getValue() + "\n";
		
		for(int i = 0; i < 26; i++){
			if(node.get(i) != null)
				returnString += this.print(node.get(i), prepend);
		}
		
		return returnString;
		
	}
	
	public int hashCode(){
		String representation = this.toString();
		int sum = 0;
		for(int i = 0; i < representation.length(); i++){
			sum += representation.charAt(i);
		}
		
		return sum % 10000000;
	}

	public boolean equals(Dictionary that){
		if(this.getWordCount() == that.getWordCount()){
			return this.toString() == that.toString();
		}else{
			return false;
		}
	}
	
}