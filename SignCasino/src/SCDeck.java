public class SCDeck {

		private SCCard[] deck;   // An array of 52 Cards, representing the deck.
	    private int cardsUsed; // How many cards have been dealt from the deck.
	    private int size;
	    
	    public SCDeck(int decksize) {
	    	size = decksize*52;
	           // Create an unshuffled deck of cards.
	       deck = new SCCard[size];
	       int cardCt = 0; // How many cards have been created so far.
	       while(cardCt < (size)){
	    	   for ( int suit = 0; suit <= 3; suit++ ) {
	    		   for ( int value = 1; value <= 13; value++ ) {
	    			   deck[cardCt] = new SCCard(value,suit);
	    			   cardCt++;
	    		   }
	    	   }
	       }
	       cardsUsed = 0;
	    }
	    
	    public void shuffle() {
	          // Put all the used cards back into the deck, and shuffle it into
	          // a random order.
	        for ( int i = (size-1); i > 0; i-- ) {
	            int rand = (int)(Math.random()*(i+1));
	            SCCard temp = deck[i];
	            deck[i] = deck[rand];
	            deck[rand] = temp;
	        }
	        cardsUsed = 0;
	    }
	    
	    public int cardsLeft() {
	          // As cards are dealt from the deck, the number of cards left
	          // decreases.  This function returns the number of cards that
	          // are still left in the deck.
	        return size - cardsUsed;
	    }
	    
	    public SCCard dealCard() {
	          // Deals one card from the deck and returns it.
	        if (cardsUsed == size)
	           shuffle();
	        cardsUsed++;
	        return deck[cardsUsed - 1];
	    }

	} // end class Deck