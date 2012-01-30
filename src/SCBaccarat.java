import java.util.HashMap;
import java.util.Random;

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

public class SCBaccarat {
	SignCasino SC;
	SCData SCD;
	HashMap<Sign, Player> PlayerPlaying;
	HashMap<Sign, SCDeck> SignDeck;
	
	public SCBaccarat(SignCasino SC){
		this.SC = SC;
		SCD = SC.SCD;
		PlayerPlaying = new HashMap<Sign, Player>();
		SignDeck = new HashMap<Sign, SCDeck>();
	}
	
	public boolean BaccaratSign(Player player, Sign sign){
		String line2string = null;
		double line2 = 0;
		if(sign.getText(3).equalsIgnoreCase("GLOBAL")){
			if(player.canUseCommand("/scplaceglobal") || player.canUseCommand("/scadmin")){
				sign.setText(3, "GLOBAL");
			}
			else{
				if(!SC.SCD.canPlaceSign(4, player.getName())){
					player.sendMessage("[§2Sign§4C§6a§bs§9i§dn§co§f]§c You have too many '§5BACCARAT§c' signs");
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
			if(!SC.SCD.canPlaceSign(4, player.getName())){
				player.sendMessage("[§2Sign§4C§6a§bs§9i§dn§co§f]§c You have too many '§5BACCARAT§c' signs");
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
		sign.setText(0, "§5[BACCARAT]");
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
		player.sendMessage("[§2Sign§4C§6a§bs§9i§dn§co§f]§b '§5BACCARAT§b' sign placed.");
		return false;
	}
	
	private void NoPlaceSign(Sign sign){
		Location loc = new Location(sign.getWorld(), sign.getX(), sign.getY(), sign.getZ());
		sign.getWorld().dropItem(loc, 323);
		sign.getWorld().setBlockAt(0, sign.getX(), sign.getY(), sign.getZ());
	}
	
	public boolean PlayBaccarat(Player player, Sign sign){
		
		double bet = Double.valueOf(sign.getText(1).substring(2));
		if(alreadyplaying(sign, player)){ 
			player.sendMessage("[§2Sign§4C§6a§bs§9i§dn§co§f]§c There is already a player playing this sign!");
			return true;
		}
		
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
		
		SCDeck deck; //Deck to use for game
		if(!SignDeck.containsKey(sign)){
			deck = new SCDeck(6); //Make a new 6 deck stack for Baccarat
			deck.shuffle(); //Shuffle Deck for first Game
		}
		else{
			deck = SignDeck.get(sign); //Get Deck
		}
		
		BaccaratHand BancoHand = new BaccaratHand();
		BaccaratHand PuntoHand = new BaccaratHand();
		
		PuntoHand.addCard( deck.dealCard() ); //Deal Player 1 card
		BancoHand.addCard( deck.dealCard() ); //Deal Banker 1 card
		PuntoHand.addCard( deck.dealCard() ); //Deal Player 1 card
		BancoHand.addCard( deck.dealCard() ); //Deal Banker 1 Card
		
		int phvalue = PuntoHand.getBaccaratValue(); //Check Player Hand Values
		int bhvalue = BancoHand.getBaccaratValue(); //Check Banker Hand Values
		int phnew = 0;
		boolean pdrew = false;
		double nerf = Math.random(); //Nerf Player winning too much
		
		if(phvalue > 7 || bhvalue > 7){ //There was a natural, find winner
			
			//Help give the House some edge over the Player
			if((phvalue > bhvalue) && (nerf > 0 && nerf < 0.33)){ //NERF IT
				bhvalue = phvalue;
			}
			else if(phvalue > bhvalue && (nerf > 0.33 && nerf < 0.66)){ //NERF IT WORSE!
				Random rand = new Random();
				int bankedge = rand.nextInt(2)+7;
				bhvalue = bankedge;
			}
			
			if(phvalue > bhvalue){ //Player Wins
				player.sendMessage("[§2Sign§4C§6a§bs§9i§dn§co§f]§b Your total is §e"+phvalue+"§b and Banker's total is §6"+bhvalue+"§b.");
				player.sendMessage("[§2Sign§4C§6a§bs§9i§dn§co§f]§a YOU WON!");
				if(SCD.Tweet){
					etc.getLoader().callCustomHook("tweet", new Object[] {player.getName() + " Got a Natural "+phvalue+" in Baccarat on SignCasino for CanaryMod"});
				}
				win(player, sign, bet);
				SignDeck.put(sign, deck); //Put deck away
				return true;
			}
			else if(bhvalue > phvalue){ //Bank Wins
				player.sendMessage("[§2Sign§4C§6a§bs§9i§dn§co§f]§b Your total is §e"+phvalue+"§b and Banker's total is §6"+bhvalue+"§b.");
				player.sendMessage("[§2Sign§4C§6a§bs§9i§dn§co§f]§c You lost.");
				lose(player, sign, bet);
				SignDeck.put(sign, deck); //Put deck away
				return true;
			}
			else{ //No Winner
				player.sendMessage("[§2Sign§4C§6a§bs§9i§dn§co§f]§b Your total is §e"+phvalue+"§b and Banker's total is §6"+bhvalue+"§b.");
				player.sendMessage("[§2Sign§4C§6a§bs§9i§dn§co§f]§b TIE No winners.");
				SignDeck.put(sign, deck); //Put deck away
				return true;
			}
		}
		
		if(phvalue > 5){ //If player has a value of 6 or more don't draw
			player.sendMessage("[§2Sign§4C§6a§bs§9i§dn§co§f]§b Your total is: §e"+phvalue);
		}
		else{ //Draw third card
			player.sendMessage("[§2Sign§4C§6a§bs§9i§dn§co§f]§b Your total is: §e"+phvalue+"§b. You draw 1 card.");
			SCCard newcard = deck.dealCard();
			PuntoHand.addCard( newcard );
			phnew = newcard.getValue();
			//check card value and prepare for Bank draws
			if(phnew > 9){
				phnew = 0;
			}
			
			player.sendMessage("[§2Sign§4C§6a§bs§9i§dn§co§f]§b Your new total is: §e"+PuntoHand.getBaccaratValue()+"§b.");
			pdrew = true;
		}
		
		if(pdrew){ //Player drew so use special Bank Rules
			if(phnew == 0 || phnew == 1){ //Player drew Ace, 9, 10, King, Queen, Jack as card 3
				if(bhvalue < 4){ //If Bank hand is below 4, Bank draws
					player.sendMessage("[§2Sign§4C§6a§bs§9i§dn§co§f]§b Banker draws 1 card.");
					BancoHand.addCard( deck.dealCard() );
				}
			}
			else if(phnew == 2 || phnew == 3){ //Player drew 2 or 3 as card 3
				if(bhvalue < 5){ //If Bank hand is below 5, Bank draws
					player.sendMessage("[§2Sign§4C§6a§bs§9i§dn§co§f]§b Banker draws 1 card.");
					BancoHand.addCard( deck.dealCard() );
				}
			}
			else if(phnew == 4 || phnew == 5){ //Player drew 4 or 5 as card 3
				if(bhvalue < 6){ //If Bank hand is below 6, Bank draws
					player.sendMessage("[§2Sign§4C§6a§bs§9i§dn§co§f]§b Banker draws 1 card.");
					BancoHand.addCard( deck.dealCard() );
				}
			}
			else if(phnew == 6 || phnew == 7){ //Player drew 6 or 7 as card 3
				if(bhvalue < 7){ //If Bank hand is below 7, Bank draws
					player.sendMessage("[§2Sign§4C§6a§bs§9i§dn§co§f]§b Banker draws 1 card.");
					BancoHand.addCard( deck.dealCard() );
				}
			}
			else if(phnew == 8){ //Player drew 8 as card 3
				if(bhvalue < 3){ //If Bank hand is below 3, Bank draws
					player.sendMessage("[§2Sign§4C§6a§bs§9i§dn§co§f]§b Banker draws 1 card.");
					BancoHand.addCard( deck.dealCard() );
				}
			}
		}
		else if (bhvalue < 6){ //Player didn't draw and Bank has a value of 5 or less, Bank Draws
			player.sendMessage("[§2Sign§4C§6a§bs§9i§dn§co§f]§b Banker draws 1 card.");
			BancoHand.addCard( deck.dealCard() );
		}
		
		//Re-get totals
		phvalue = PuntoHand.getBaccaratValue();
		bhvalue = BancoHand.getBaccaratValue();
		
		//And initiate HouseEdge
		if((phvalue > bhvalue) && (nerf > 0 && nerf < 0.33)){ //NERF IT
			bhvalue = phvalue;
		}
		else if(phvalue > bhvalue && (nerf > 0.33 && nerf < 0.66)){ //NERF IT WORSE!
			Random rand = new Random();
			int bankedge = rand.nextInt(2)+7;
			bhvalue = bankedge;
		}
		
		//Find Winner
		if(phvalue > bhvalue){ //Player Wins
			player.sendMessage("[§2Sign§4C§6a§bs§9i§dn§co§f]§b Your total is §e"+phvalue+"§b and Banker's total is §6"+bhvalue+"§b.");
			player.sendMessage("[§2Sign§4C§6a§bs§9i§dn§co§f]§a YOU WON!");
			win(player, sign, bet);
		}
		else if(bhvalue > phvalue){ //Bank Wins
			player.sendMessage("[§2Sign§4C§6a§bs§9i§dn§co§f]§b Your total is §e"+phvalue+"§b and Banker's total is §6"+bhvalue+"§b.");
			player.sendMessage("[§2Sign§4C§6a§bs§9i§dn§co§f]§c You lost.");
			lose(player, sign, bet);
		}
		else{ //No Winner
			player.sendMessage("[§2Sign§4C§6a§bs§9i§dn§co§f]§b Your total is §e"+phvalue+"§b and Banker's total is §6"+bhvalue+"§b.");
			player.sendMessage("[§2Sign§4C§6a§bs§9i§dn§co§f]§b TIE No winners.");
		}
		SignDeck.put(sign, deck); //Put deck away
		return true;
	}
	
	//Check if this sign is already in use
	private boolean alreadyplaying(Sign sign, Player player){
		if(PlayerPlaying.containsKey(sign)){
			if((PlayerPlaying.get(sign).isConnected()) && (PlayerPlaying.get(sign) != player)){
				return true;
			}
			else{
				PlayerPlaying.put(sign, player);
			}
		}
		else{
			PlayerPlaying.put(sign, player);
		}
		return false;
	}
	
	//Player lost
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
	
	private class BaccaratHand extends SCHand {
		public int getBaccaratValue() {
			// Returns the value of this hand for the
			// game of Baccarat.

		int val;      // The value computed for the hand.
		int cards;    // Number of cards in the hand.

		val = 0;
		cards = getCardCount();

		for ( int i = 0;  i < cards;  i++ ) {
			// Add the value of the i-th card in the hand.
			SCCard card;    // The i-th card; 
			int cardVal;  // The baccarat value of the i-th card.
			card = getCard(i);
			cardVal = card.getValue();  // The normal value, 1 to 13.
			if (cardVal > 9) {
				cardVal = 0;   // For a 10, Jack, Queen, or King.
			}
			val = val + cardVal;
		}
		while (val > 9){ //Baccarat hands are maxed at 9 so deduct 10 till its less than 9
			val -= 10;
		}
		return val;
		}  // end getBaccaratValue()
	}//end special Baccarat Hand Class

}
