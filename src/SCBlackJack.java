import java.util.HashMap;

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

public class SCBlackJack {
	SignCasino SC;
	SCData SCD;
	HashMap<Sign, Player> PlayerPlaying;
	HashMap<Sign, Boolean> Hitting;
	HashMap<Player, BlackjackHand[]> Hands;
	HashMap<Sign, SCDeck> SignDeck;
	
	public SCBlackJack(SignCasino SC){
		this.SC = SC;
		SCD = SC.SCD;
		PlayerPlaying = new HashMap<Sign, Player>();
		Hands = new HashMap<Player, BlackjackHand[]>();
		Hitting = new HashMap<Sign, Boolean>();
		SignDeck = new HashMap<Sign, SCDeck>();
	}
	
	public boolean BlackJackSign(Player player, Sign sign){
		String line2string = null;
		double line2 = 0;
		if(sign.getText(3).equalsIgnoreCase("GLOBAL")){
			if(player.canUseCommand("/scplaceglobal") || player.canUseCommand("/scadmin")){
				sign.setText(3, "GLOBAL");
			}
			else{
				if(!SC.SCD.canPlaceSign(3, player.getName())){
					player.sendMessage("[§2Sign§4C§6a§bs§9i§dn§co§f]§c You have too many '§3BLACKJACK§c' signs");
					NoPlaceSign(sign);
					return false;
				}
				if(player.getName().length() > 15){
					SC.SCD.setNameFix(player.getName().substring(0, 15), player.getName());
					sign.setText(3, player.getName().substring(0, 15));
				}
				else{
					sign.setText(3, player.getName());
				}
			}
		}
		else{
			if(!SC.SCD.canPlaceSign(3, player.getName())){
				player.sendMessage("[§2Sign§4C§6a§bs§9i§dn§co§f]§c You have too many '§3BLACKJACK§c' signs");
				NoPlaceSign(sign);
				return false;
			}
			if(player.getName().length() > 15){
				SC.SCD.setNameFix(player.getName().substring(0, 15), player.getName());
				sign.setText(3, player.getName().substring(0, 15));
			}
			else{
				sign.setText(3, player.getName());
			}
		}
		sign.setText(0, "§3[BLACKJACK]");
		if (!sign.getText(1).equals("")) {
			try {
				line2string = sign.getText(1);
				line2 = Double.parseDouble(line2string);
			}catch (NumberFormatException NFE) {
				if(SCD.dCo){
					player.sendMessage("[§2Sign§4C§6a§bs§9i§dn§co§f]§4 Invalid '§6BET§4'. Auto-setting 'BET' to '§6"+SCD.priceForm(SCD.AB)+"§4'");
					sign.setText(1, "§6"+SCD.priceForm(SCD.AB));
				}
				else{
					player.sendMessage("[§2Sign§4C§6a§bs§9i§dn§co§f]§4 Invalid '§6BET§4'. Auto-setting 'BET' to '§6"+((int)SCD.AB)+"§4'");
					sign.setText(1, "§6"+((int)SCD.AB));
				}
			}
			if(SCD.dCo){
				if (line2 < 0.01){
					player.sendMessage("[§2Sign§4C§6a§bs§9i§dn§co§f]§4 Invalid '§6BET§4'. Auto-setting 'BET' to '§6"+SCD.priceForm(SCD.AB)+"§4'");
					sign.setText(1, "§6"+SCD.priceForm(SCD.AB));
				}
				else{
					sign.setText(1, "§6"+SCD.priceForm(line2));
				}
			}
			else{
				if(line2 < 1){
					player.sendMessage("[§2Sign§4C§6a§bs§9i§dn§co§f]§4 Invalid '§6BET§4'. Auto-setting 'BET' to '§6"+((int)SCD.AB)+"§4'");
					sign.setText(1, "§6"+((int)SCD.AB));
				}
				else{
					sign.setText(1, "§6"+String.valueOf((int)line2));
				}
			}
		}
		else{
			if(SCD.dCo){
				player.sendMessage("[§2Sign§4C§6a§bs§9i§dn§co§f]§4 '§6BET§4' not specified! Auto-setting 'BET' to '§6"+SCD.priceForm(SCD.AB)+"§4'");
				sign.setText(1, "§6"+SCD.priceForm(SCD.AB));
			}
			else{
				player.sendMessage("[§2Sign§4C§6a§bs§9i§dn§co§f]§4 '§6BET§4' not specified! Auto-setting 'BET' to '§6"+((int)SCD.AB)+"§4'");
				sign.setText(1, "§6"+String.valueOf((int)SCD.AB));
			}
		}
		player.sendMessage("[§2Sign§4C§6a§bs§9i§dn§co§f]§b '§3BLACKJACK§b' sign placed.");
		return false;
	}
	
	private void NoPlaceSign(Sign sign){
		Location loc = new Location(sign.getWorld(), sign.getX(), sign.getY(), sign.getZ());
		sign.getWorld().dropItem(loc, 323);
		sign.getWorld().setBlockAt(0, sign.getX(), sign.getY(), sign.getZ());
	}
	
	public boolean PlayBlackJack(Player player, Sign sign, boolean LeftClickCall){
		if(!sign.getText(0).equals("§3[BLACKJACK]")){
			sign.setText(0, "§3[BLACKJACK]");
			sign.update();
		}
		if(sign.getText(3).startsWith("§")){
			sign.setText(3, sign.getText(3).substring(2));
			sign.update();
		}
		if(!sign.getText(2).equals("")){
			sign.setText(2, "");
			sign.update();
		}
		double bet = Double.valueOf(sign.getText(1).substring(2));
		if(alreadyplaying(sign, player)){ 
			player.sendMessage("[§2Sign§4C§6a§bs§9i§dn§co§f]§c There is already a player playing this sign!");
			return true;
		}
		if(LeftClickCall){
			Hitting.put(sign, false);
		}
		SCDeck deck;
		if(SignDeck.containsKey(sign)){
			deck = SignDeck.get(sign);
		}
		else{
			deck = new SCDeck(5);
			deck.shuffle();
		}
		if(Hitting.containsKey(sign) && (Hands.containsKey(player))){
			if(Hitting.get(sign)){
				//User Hits
				BlackjackHand userHand = Hands.get(player)[0];
				BlackjackHand dealerHand = Hands.get(player)[1];
				
				SCCard newCard = deck.dealCard();
				userHand.addCard(newCard);
				player.sendMessage("[§2Sign§4C§6a§bs§9i§dn§co§f]§a HIT!§b Your card is the§6 " + newCard);
				if (userHand.getBlackjackValue() > 21) {
					player.sendMessage("[§2Sign§4C§6a§bs§9i§dn§co§f]§c You busted by going over §621.  You lose.");
					player.sendMessage("[§2Sign§4C§6a§bs§9i§dn§co§f]§b Dealer's other card was the §6" + dealerHand.getCard(1));
					lose(player, sign, bet);
					Hitting.remove(sign);
					Hands.remove(player);
				}
				else{
					player.sendMessage("[§2Sign§4C§6a§bs§9i§dn§co§f]§b Your total is now §6" + userHand.getBlackjackValue());
					Hands.put(player, new BlackjackHand[]{userHand, dealerHand});
				}
			}
			else{
				/* If we get to this point, the user has Stood with 21 or less.  Now, it's
					the dealer's chance to draw.  Dealer draws cards until the dealer's total is > 16.
				 */	
				BlackjackHand dealerHand = Hands.get(player)[1];
				BlackjackHand userHand = Hands.get(player)[0];
				player.sendMessage("[§2Sign§4C§6a§bs§9i§dn§co§f]§b You stand.");
				
				player.sendMessage("[§2Sign§4C§6a§bs§9i§dn§co§f]§b Dealer's cards are §6" + dealerHand.getCard(0) + "§b and §6" + dealerHand.getCard(1) );
				while (dealerHand.getBlackjackValue() <= 15) {
					SCCard newCard = deck.dealCard();
					double nerf = Math.random(); //Nerf Player winning too much
					if((newCard.getValue()+dealerHand.getBlackjackValue() > 21) && (nerf > 0 && nerf < 0.33)){ //NERF IT
						while(newCard.getValue()+dealerHand.getBlackjackValue() > 21){
							newCard = deck.dealCard();
						}
					}
					player.sendMessage("[§2Sign§4C§6a§bs§9i§dn§co§f]§b Dealer hits and gets the §6" + newCard);
					dealerHand.addCard(newCard);
				}
				
				/* Now, the winner can be declared. */
				if (dealerHand.getBlackjackValue() > 21) {
					player.sendMessage("[§2Sign§4C§6a§bs§9i§dn§co§f]§b Dealer busted by going over 21.  §aYOU WON!");
					win(player, sign, bet);
				}
				else if (dealerHand.getBlackjackValue() == userHand.getBlackjackValue()) {
					player.sendMessage("[§2Sign§4C§6a§bs§9i§dn§co§f]§c Dealer wins on a tie.  You lose.");
					lose(player, sign, bet);
				}
				else if (dealerHand.getBlackjackValue() > userHand.getBlackjackValue()) {
					player.sendMessage("[§2Sign§4C§6a§bs§9i§dn§co§f]§c Dealer wins, §6" + dealerHand.getBlackjackValue() + "§c points to §6" + userHand.getBlackjackValue() + "§c.");
					lose(player, sign, bet);
				}
				else{
					player.sendMessage("[§2Sign§4C§6a§bs§9i§dn§co§f]§a YOU WON§b, §6" + userHand.getBlackjackValue() + "§b points to§6 " + dealerHand.getBlackjackValue() + "§b.");
					win(player, sign, (bet));
				}
				Hitting.remove(sign);
				Hands.remove(player);
			}
		}
		else{
			String owner = sign.getText(3);
			if(SCD.isSharedSign(sign) && SCD.dCo){
				String account = SCD.getSharedAccount(sign);
				if((SCD.isSharedOwner(account, player.getName()) || (owner.equals(player.getName())))  && (!SCD.Debug)){
					player.sendMessage("[§2Sign§4C§6a§bs§9i§dn§co§f]§c You may not play your own signs or signs shared to you.");
					return true;
				}
				
				if(!SCD.hasMoney(player.getName(), bet, false, owner, sign)){
					player.sendMessage("[§2Sign§4C§6a§bs§9i§dn§co§f]§c You do not have the money to play.");
					return true;
				}
				
				if(!SCD.hasMoney(account, (bet*7), true, owner, sign)){
					player.sendMessage("[§2Sign§4C§6a§bs§9i§dn§co§f]§c The owner does not have the money to pay out.");
					return true;
				}
			}
			else{
				if(SCD.isNameFixed(owner)){
					owner = SCD.getNameFix(owner);
				}
			
				if((owner.equals(player.getName())) && (!SCD.Debug)){
					player.sendMessage("[§2Sign§4C§6a§bs§9i§dn§co§f]§c You may not play your own signs.");
					return true;
				}
				
				if(!SCD.hasMoney(player.getName(), bet, false, owner, sign)){
					player.sendMessage("[§2Sign§4C§6a§bs§9i§dn§co§f]§c You do not have the money to play.");
					return true;
				}
				
				
				if(!SCD.hasMoney(owner, (bet*4), (SCD.ItemChests.containsKey(sign) && SCD.item), owner, sign)){
					player.sendMessage("[§2Sign§4C§6a§bs§9i§dn§co§f]§c The owner does not have the money to pay out.");
					return true;
				}
			}
			
			//LETS PLAY SOME BLACKJACK :3
			BlackjackHand dealerHand = new BlackjackHand();
			BlackjackHand userHand = new BlackjackHand();

			/* Deal two cards to each player. */
			userHand.addCard( deck.dealCard() );
			dealerHand.addCard( deck.dealCard() );
			userHand.addCard( deck.dealCard() );
			dealerHand.addCard( deck.dealCard() );

			/* Check if one of the players has Blackjack (two cards totaling to 21).
   				The player with Blackjack wins the game.  Dealer wins ties.*/

			if (dealerHand.getBlackjackValue() == 21) {
				player.sendMessage("[§2Sign§4C§6a§bs§9i§dn§co§f]§c Dealer has Blackjack.  Dealer wins.");
				lose(player, sign, bet);
				return true;
			}

			if (userHand.getBlackjackValue() == 21) {
				player.sendMessage("[§2Sign§4C§6a§bs§9i§dn§co§f]§a YOU HAVE BLACKJACK! YOU WON!");
				if(SCD.Tweet){
					etc.getLoader().callCustomHook("tweet", new Object[] {player.getName() + " Got BlackJack(21) in BlackJack on SignCasino for CanaryMod"});
				}
				win(player, sign, bet);
				return true;
			}
			
			//If neither player has Blackjack, play the game.  The user gets a chance to draw cards (i.e., to "Hit").
			player.sendMessage("[§2Sign§4C§6a§bs§9i§dn§co§f]§b Your cards are: §6"+ userHand.getCard(0) + "§b and §6" + userHand.getCard(1));
			player.sendMessage("[§2Sign§4C§6a§bs§9i§dn§co§f]§b Dealer is showing the §6" + dealerHand.getCard(0));
			player.sendMessage("[§2Sign§4C§6a§bs§9i§dn§co§f]§b HIT (RIGHT-CLICK) or STAY (LEFT-CLICK)");
			Hitting.put(sign, true);
			Hands.put(player, new BlackjackHand[]{userHand, dealerHand});
		}
		return true;
	}
	
	private boolean alreadyplaying(Sign sign, Player player){
		if(PlayerPlaying.containsKey(sign)){
			if((PlayerPlaying.get(sign).isConnected()) && (PlayerPlaying.get(sign) != player)){
				return true;
			}
			else if(PlayerPlaying.get(sign) != player){
				Player other = PlayerPlaying.get(sign);
				if(Hands.containsKey(other)){
					Hands.remove(other);
				}
				Hitting.put(sign, false);
				PlayerPlaying.put(sign, player);
			}
		}
		else{
			PlayerPlaying.put(sign, player);
		}
		return false;
	}
	
	private void lose(Player player, Sign sign, double bet){
		if(!SCD.Debug){
			String owner = sign.getText(3);
			if(SCD.isNameFixed(owner)){
				owner = SCD.getNameFix(owner);
			}
			if(SCD.isSharedSign(sign) && (SCD.dCo)){
				String account = SCD.getSharedAccount(sign);
				SCD.payup(player.getName(), account, bet, true, false, sign);
			}
			else if(SCD.ItemChests.containsKey(sign) && (SCD.item)){
				SCD.payup(player.getName(), owner, bet, true, false, sign);
			}
			else{
				SCD.payup(player.getName(), owner, bet, false, false, sign);
			}
		}
	}
	
	private void win(Player player, Sign sign, double bet){
		if(!SCD.Debug){
			String owner = sign.getText(3);
			if(SCD.isNameFixed(owner)){
				owner = SCD.getNameFix(owner);
			}
			if(SCD.isSharedSign(sign) && (SCD.dCo)){
				String account = SCD.getSharedAccount(sign);
				SCD.payup(account, player.getName(), bet, false, true, sign);
			}
			else if(SCD.ItemChests.containsKey(sign) && (SCD.item)){
				SCD.payup(owner, player.getName(), bet, false, true, sign);
			}
			else{
				SCD.payup(owner, player.getName(), bet, false, false, sign);
			}
		}
	}
		
	private class BlackjackHand extends SCHand {
		public int getBlackjackValue() {
			// Returns the value of this hand for the
			// game of Blackjack.

		int val;      // The value computed for the hand.
		boolean ace;  // This will be set to true if the hand contains an ace.
		int cards;    // Number of cards in the hand.

		val = 0;
		ace = false;
		cards = getCardCount();

		for ( int i = 0;  i < cards;  i++ ) {
			// Add the value of the i-th card in the hand.
			SCCard card;    // The i-th card; 
			int cardVal;  // The blackjack value of the i-th card.
			card = getCard(i);
			cardVal = card.getValue();  // The normal value, 1 to 13.
			if (cardVal > 10) {
				cardVal = 10;   // For a Jack, Queen, or King.
			}
			if (cardVal == 1) {
				ace = true;     // There is at least one ace.
			}
			val = val + cardVal;
		}

		//Now, val is the value of the hand, counting any ace as 1.
		//  If there is an ace, and if changing its value from 1 to 11 would leave the score less than or equal to 21,then do so by adding the extra 10 points to val.
		if (ace == true  &&  val + 10 <= 21 )
			val = val + 10;

		return val;
		}  // end getBlackjackValue()
	}//End Special BlackJackHand Class
}
