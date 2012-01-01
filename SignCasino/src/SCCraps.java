import java.util.HashMap;
import java.util.Random;

public class SCCraps{
	SignCasino SC;
	SCData SCD;
	HashMap<Sign, int[]> WinRate;
	HashMap<Sign, Player> PlayerPlaying;
	
	public SCCraps(SignCasino SC){
		this.SC = SC;
		SCD = SC.SCD;
		WinRate = new HashMap<Sign, int[]>();
		PlayerPlaying = new HashMap<Sign, Player>();
	}
	
	public boolean CrapsSign(Player player, Sign sign){
		String line2string = null;
		double line2 = 0;
		if(sign.getText(3).equalsIgnoreCase("GLOBAL")){
			if(player.canUseCommand("/scplaceglobal") || player.canUseCommand("/scadmin")){
				sign.setText(3, "GLOBAL");
			}
			else{
				if(!SC.SCD.canPlaceSign(2, player.getName())){
					player.sendMessage("[§2Sign§4C§6a§bs§9i§dn§co§f]§c You have too many '§4CRAPS§c' signs");
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
			if(!SC.SCD.canPlaceSign(2, player.getName())){
				player.sendMessage("[§2Sign§4C§6a§bs§9i§dn§co§f]§c You have too many '§4CRAPS§c' signs");
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
		sign.setText(0, "§4[CRAPS]");
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
		player.sendMessage("[§2Sign§4C§6a§bs§9i§dn§co§f]§b '§4CRAPS§b' sign placed.");
		return false;
	}
	
	private void NoPlaceSign(Sign sign){
		Location loc = new Location(sign.getWorld(), sign.getX(), sign.getY(), sign.getZ());
		sign.getWorld().dropItem(loc, 323);
		sign.getWorld().setBlockAt(0, sign.getX(), sign.getY(), sign.getZ());
	}
	
	public boolean RollingDice(Player player, Sign sign){
		if(!sign.getText(0).equals("§4[CRAPS]")){
			sign.setText(0, "§4[CRAPS]");
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
			
			if(!SCD.hasMoney(account, (bet*4), true, owner, sign)){
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
		int[] dice = rollDice(sign);
		int die1 = dice[0];
		int die2 = dice[1];
		int die3 = dice[2];
		int die4 = dice[3];
		String signtext="R1: "+die1 + " " + die2 + " R2: " + "_" + " " + "_";
		if((die1+die2) == 2){
			player.sendMessage("[§2Sign§4C§6a§bs§9i§dn§co§f]§cYou rolled SNAKE EYES, you loose.");
			lose(player, sign, bet);
		}
		else if((die1+die2) == 12){
			player.sendMessage("[§2Sign§4C§6a§bs§9i§dn§co§f]§c You rolled BOXCARS, you loose.");
			lose(player, sign, bet);
		}
		else if((die1+die2) == 7){
			player.sendMessage("[§2Sign§4C§6a§bs§9i§dn§co§f]§a YOU ROLLED 7! YOU WIN!");
			if(SCD.Tweet){
				etc.getLoader().callCustomHook("tweet", new Object[] {player.getName() + " Rolled 7 in Craps on SignCasino for CanaryMod"});
			}
			win(player, sign, bet);
		}
		else if((die1+die2) == 11){
			player.sendMessage("[§2Sign§4C§6a§bs§9i§dn§co§f]§a YOU ROLLED 11! YOU WIN!");
			win(player, sign, bet);
		}
		else{
			signtext = "R1: "+die1 + " " + die2 + " R2: " + die3 + " " + die4;
			if((die3+die4) == 7){
				player.sendMessage("[§2Sign§4C§6a§bs§9i§dn§co§f]§a YOU ROLLED 7! YOU WIN!");
				win(player, sign, bet);
			}
			else if(diceMatch(dice)){
				player.sendMessage("[§2Sign§4C§6a§bs§9i§dn§co§f]§a YOU ROLLED POINT! YOU WIN!");
				win(player, sign, bet);
			}
			else{
				player.sendMessage("[§2Sign§4C§6a§bs§9i§dn§co§f]§c You didn't meet point, you loose.");
				lose(player, sign, bet);
			}
		}
		sign.setText(2, signtext);
		sign.update();
		return true;
	}
	
	private boolean diceMatch(int[] dice){
		boolean d1d3 = false, d1d4 = false, d2d3 = false, d2d4 = false;
		int die1 = dice[0], die2 = dice[1], die3 = dice[2], die4 = dice[3];
		if(die1 == die3){
			d1d3 = true;
		}
		else if(die1 == die4){
			d1d4 = true;
		}
		if((die2 == die3) && (!d1d3)){
			d2d3 = true;
		}
		if((die2 == die4) && (!d1d4)){
			d2d4 = true;
		}
		return ((d1d3 && d2d4) || (d1d4 && d2d3));
	}
	
	private int[] rollDice(Sign sign){
		Random rand = new Random();
		int die1 = rand.nextInt(6)+1, die2 = rand.nextInt(6)+1, die3 = rand.nextInt(6)+1, die4 = rand.nextInt(6)+1, WinSet = rand.nextInt(SCD.WJPR)+1;
		int kickoutofloop = 25; //For some reason it seems that my while statements want to crash...
		if(WinRate.containsKey(sign)){
			int[] winning = WinRate.get(sign);
			WinSet = winning[0];
			int WinNum = winning[1];
			if(WinNum > SCD.WJPR){
				WinSet = rand.nextInt(SCD.WJPR)+1;
				WinRate.put(sign, new int[]{WinSet,0});
			}
			else{
				WinRate.put(sign, new int[]{WinSet, (WinNum+1)});
			}
			if(WinSet == WinNum){
				int pick = rand.nextInt(1);
				if(pick == 0){
					while(((die1+die2) != 7) && (kickoutofloop > 0)){
						die1 = rand.nextInt(6)+1;
						die2 = rand.nextInt(6)+1;
						kickoutofloop--;
					}
				}
				else{
					while (((die3+die4) != 7) && (kickoutofloop > 0)){
						die3 = rand.nextInt(6)+1;
						die4 = rand.nextInt(6)+1;
						kickoutofloop--;
					}
				}
			}
			else{
				while(((die1+die2) == 7)  && (kickoutofloop > 0)){
					die1 = rand.nextInt(6)+1;
					die2 = rand.nextInt(6)+1;
					kickoutofloop--;
				}
				while (((die3+die4) == 7)  && (kickoutofloop > 0)){
					die3 = rand.nextInt(6)+1;
					die4 = rand.nextInt(6)+1;
					kickoutofloop--;
				}
			}
		}
		else{
			WinRate.put(sign, new int[]{WinSet,0});
			while(((die1+die2) == 7) && (kickoutofloop > 0)){
				die1 = rand.nextInt(6)+1;
				die2 = rand.nextInt(6)+1;
				kickoutofloop--;
			}
			while (((die3+die4) == 7) && (kickoutofloop > 0)){
				die3 = rand.nextInt(6)+1;
				die4 = rand.nextInt(6)+1;
				kickoutofloop--;
			}
		}
		return new int[]{die1, die2, die3, die4};
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
