
/**
* SignCasino v1.x
* Copyright (C) 2012 Visual Illusions Entertainment
* @author darkdiplomat <darkdiplomat@visualillusionsent.net>
* 
* This file is part of SignCasino
* 
* This program is free software: you can redistribute it and/or modify
* it under the terms of the GNU General Public License as published by
* the Free Software Foundation, either version 3 of the License, or
* (at your option) any later version.
*
* This program is distributed in the hope that it will be useful,
* but WITHOUT ANY WARRANTY; without even the implied warranty of
* MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
* GNU General Public License for more details.
*
* You should have received a copy of the GNU General Public License
* along with this program.  If not, see http://www.gnu.org/copyleft/gpl.html.
*/

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