import java.util.HashMap;
import java.util.Random;


public class SCSlotMachine {
	SignCasino SC;
	SCData SCD;
	HashMap<Sign, Player> PlayerPlaying;
	
	public SCSlotMachine(SignCasino SC){
		this.SC = SC;
		SCD = SC.SCD;
		PlayerPlaying = new HashMap<Sign, Player>();
	}
	
	public boolean SlotsSign(Player player, Sign sign){
		String line2string = null;
		double line2 = 0;
		if(sign.getText(3).equalsIgnoreCase("GLOBAL")){
			if(player.canUseCommand("/scplaceglobal") || player.canUseCommand("/scadmin")){
				sign.setText(3, "GLOBAL");
			}
			else{
				if(!SC.SCD.canPlaceSign(1, player.getName())){
					player.sendMessage("[§2Sign§4C§6a§bs§9i§dn§co§f]§c You have too many '§2SLOTS§c' signs");
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
			if(!SC.SCD.canPlaceSign(1, player.getName())){
				player.sendMessage("[§2Sign§4C§6a§bs§9i§dn§co§f]§c You have too many '§2SLOTS§c' signs");
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
		sign.setText(0, "§2[SLOTS]");
		sign.setText(2, "\\o/ \\o/ \\o/");
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
		player.sendMessage("[§2Sign§4C§6a§bs§9i§dn§co§f]§b '§2SLOTS§b' sign placed.");
		return false;
	}
	
	private void NoPlaceSign(Sign sign){
		Location loc = new Location(sign.getWorld(), sign.getX(), sign.getY(), sign.getZ());
		sign.getWorld().dropItem(loc, 323);
		sign.getWorld().setBlockAt(0, sign.getX(), sign.getY(), sign.getZ());
	}
	
	public boolean SlotSignPlay(Player player, Sign sign){
		if(!sign.getText(0).equals("§2[SLOTS]")){
			sign.setText(0, "§2[SLOTS]");
			sign.update();
		}
		if(sign.getText(3).startsWith("§")){
			sign.setText(3, sign.getText(3).substring(2));
			sign.update();
		}
		if(alreadyplaying(sign, player)){ 
			player.sendMessage("[§2Sign§4C§6a§bs§9i§dn§co§f]§c There is already a player playing this sign!");
			return true;
		}
		double bet = Double.valueOf(sign.getText(1).substring(2));
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
			
			
			if(!SCD.hasMoney(owner, (bet*7), (SCD.ItemChests.containsKey(sign) && SCD.item), owner, sign)){
				player.sendMessage("[§2Sign§4C§6a§bs§9i§dn§co§f]§c The owner does not have the money to pay out.");
				return true;
			}
		}
		
		
		int[] slots = SpinSlots(sign);
		String Display = SlotSymbol(slots[0])+" "+SlotSymbol(slots[1])+" "+SlotSymbol(slots[2]);
		sign.setText(2, Display);
		sign.update();
		if((slots[0] == 1 || slots[0] == 3)&&(slots[1] == 1 || (slots[1] == 3 && slots[0] != 3))&&(slots[2] == 1 || (slots[2] == 3 && slots[0] != 3 && slots[1] != 3))){
			player.sendMessage("[§2Sign§4C§6a§bs§9i§dn§co§f]§a YOU WON!");
			win(player, sign, bet);
		}
		else if((slots[0] == 2 || slots[0] == 3)&&(slots[1] == 2 || (slots[1] == 3 && slots[0] != 3))&&(slots[2] == 2 || (slots[2] == 3 && slots[0] != 3 && slots[1] != 3))){
			player.sendMessage("[§2Sign§4C§6a§bs§9i§dn§co§f]§a YOU WON!");
			win(player, sign, bet);
		}
		else if((slots[0] == 4 || slots[0] == 3)&&(slots[1] == 4 || (slots[1] == 3 && slots[0] != 3)) && (slots[2] == 4 || (slots[2] == 3 && slots[0] != 3 && slots[1] != 3))){
			player.sendMessage("[§2Sign§4C§6a§bs§9i§dn§co§f]§a YOU WON!");
			win(player, sign, bet);
		}
		else if((slots[0] == 3)&&(slots[1] == 3)&&(slots[2] == 3)){
			player.sendMessage("[§2Sign§4C§6a§bs§9i§dn§co§f]§a YOU WON!");
			win(player, sign, (bet*2));
		}
		else if((slots[0] == 5 || slots[0] == 3)&&(slots[1] == 5 || (slots[1] == 3 && slots[0] != 3))&&(slots[2] == 5 || (slots[2] == 3 && slots[0] != 3 && slots[1] != 3))){
			player.sendMessage("[§2Sign§4C§6a§bs§9i§dn§co§f]§a YOU WON!");
			win(player, sign, (bet*2));
		}
		else if((slots[0] == 6) && (slots[1] == 6)&&(slots[2] == 6)){
			player.sendMessage("[§2Sign§4C§6a§bs§9i§dn§co§f]§a JACKPOT!");
			if(SCD.Tweet){
				etc.getLoader().callCustomHook("tweet", new Object[] {player.getName() + " Won the Jackpot in the SlotMachine on SignCasino for CanaryMod"});
			}
			win(player, sign, (bet*5));
		}
		else{
			player.sendMessage("[§2Sign§4C§6a§bs§9i§dn§co§f]§c You lost.");
			lose(player, sign, bet);
		}
		return true;
	}
	
	public int[] SpinSlots(Sign sign){
		Random rand = new Random();
		int slot1 = rand.nextInt(6)+1, slot2 = rand.nextInt(6)+1, slot3 = rand.nextInt(6)+1;
		return new int[]{slot1, slot2, slot3};
	}
	
	public String SlotSymbol(int slot){
		switch(slot){
		case 1: return ":P";
		case 2: return ":)";
		case 3: return "<3";
		case 4: return ":D";
		case 5: return ":3";
		default: return "\\o/";
		}
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
}
