import java.util.ArrayList;

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

public class SCListener extends PluginListener{
	SignCasino SC;
	SCData SCD;
	SCCraps SCC;
	SCSlotMachine SCSM;
	SCBlackJack SCBJ;
	SCBaccarat SCB;
	ArrayList<Sign> SignProtection;

	public SCListener(SignCasino SC){
		this.SC = SC;
		SCC = SC.SCC;
		SCSM = SC.SCSM;
		SCBJ = SC.SCBJ;
		SCB = SC.SCB;
		SCD = SC.SCD;
		SignProtection = new ArrayList<Sign>();
	}
	
	public boolean onSignChange(Player player, Sign sign){
		if(sign.getText(0).equalsIgnoreCase("[CRAPS]")){
			if((player.canUseCommand("/scplacecraps"))|| (player.canUseCommand("/scall") || (player.canUseCommand("/scadmin")))){
				return SCC.CrapsSign(player, sign);
			}
			else{
				NoPlaceSign(sign);
				player.sendMessage("[§2Sign§4C§6a§bs§9i§dn§co§f]§c You are not allowed to make '§4CRAPS§c' signs");
			}
		}
		else if(sign.getText(0).equalsIgnoreCase("[SLOTS]")){
			if((player.canUseCommand("/scplaceslots"))|| (player.canUseCommand("/scplaceall") || (player.canUseCommand("/scadmin")))){
				return SCSM.SlotsSign(player, sign);
			}
			else{
				NoPlaceSign(sign);
				player.sendMessage("[§2Sign§4C§6a§bs§9i§dn§co§f]§c You are not allowed to make '§2SLOTS§c' signs");
			}
		}
		else if(sign.getText(0).equalsIgnoreCase("[BLACKJACK]")){
			if((player.canUseCommand("/scplaceblackjack"))|| (player.canUseCommand("/scplaceall") || (player.canUseCommand("/scadmin")))){
				return SCBJ.BlackJackSign(player, sign);
			}
			else{
				NoPlaceSign(sign);
				player.sendMessage("[§2Sign§4C§6a§bs§9i§dn§co§f]§c You are not allowed to make '§3BLACKJACK§c' signs");
			}
		}
		else if(sign.getText(0).equalsIgnoreCase("[BACCARAT]")){
			if((player.canUseCommand("/scplacebaccarat"))|| (player.canUseCommand("/scplaceall") || (player.canUseCommand("/scadmin")))){
				return SCB.BaccaratSign(player, sign);
			}
			else{
				NoPlaceSign(sign);
				player.sendMessage("[§2Sign§4C§6a§bs§9i§dn§co§f]§c You are not allowed to make '§5BACCARAT§c' signs");
			}
		}
		return false;
	}
	
	public boolean onBlockRightClick(Player player, Block block, Item item){
		if(block.getType() == 63 || block.getType() == 68){
			Sign sign = (Sign)player.getWorld().getComplexBlock(block);
			if(sign != null){
				if(sign.getText(0).length() > 7){
					if(sign.getText(0).substring(2).equalsIgnoreCase("[CRAPS]")){
						if((player.canUseCommand("/scplaycraps")) || (player.canUseCommand("/scplayall") || (player.canUseCommand("/scadmin")))){
							onesignonly(player, sign);
							return SCC.RollingDice(player, sign);
						}
						else{
							player.sendMessage("[§2Sign§4C§6a§bs§9i§dn§co§f]§c You are not allowed to play '§4CRAPS§c' signs");
						}
					}
					else if(sign.getText(0).substring(2).equalsIgnoreCase("[SLOTS]")){
						if((player.canUseCommand("/scplayslots")) || (player.canUseCommand("/scplayall") || (player.canUseCommand("/scadmin")))){
							onesignonly(player, sign);
							return SCSM.SlotSignPlay(player, sign);
						}
						else{
							player.sendMessage("[§2Sign§4C§6a§bs§9i§dn§co§f]§c You are not allowed to play '§2SLOTS§c' signs");
						}
					}
					else if(sign.getText(0).substring(2).equalsIgnoreCase("[BLACKJACK]")){
						if((player.canUseCommand("/scplayblackjack")) || (player.canUseCommand("/scplayall") || (player.canUseCommand("/scadmin")))){
							onesignonly(player, sign);
							return SCBJ.PlayBlackJack(player, sign, false);
						}
						else{
							player.sendMessage("[§2Sign§4C§6a§bs§9i§dn§co§f]§c You are not allowed to play '§3BLACKJACK§c' signs");
						}
					}
					else if(sign.getText(0).substring(2).equalsIgnoreCase("[BACCARAT]")){
						if((player.canUseCommand("/scplaybaccarat")) || (player.canUseCommand("/scplayall") || (player.canUseCommand("/scadmin")))){
							onesignonly(player, sign);
							return SCB.PlayBaccarat(player, sign);
						}
						else{
							player.sendMessage("[§2Sign§4C§6a§bs§9i§dn§co§f]§c You are not allowed to play '§5BACCARAT§c' signs");
						}
					}
				}
			}
		}
		return false;
	}
	
	public boolean onBlockDestroy(Player player, Block block){
		if(block.getType() == 63 || block.getType() == 68){
			Sign sign = (Sign)player.getWorld().getComplexBlock(block);
			if(sign != null){
				if(sign.getText(0).length() > 7){
					String Type = sign.getText(0).substring(2);
					if(Type.equals("[BLACKJACK]") && (SCBJ.Hitting.containsKey(sign) && SCBJ.Hitting.get(sign))){
						SCBJ.PlayBlackJack(player, sign, true);
						SignProtection.add(sign);
					}
					else if(Type.equalsIgnoreCase("[SLOTS]") || Type.equals("[CRAPS]") || Type.equals("[BLACKJACK]") || Type.equals("[BACCARAT]")){
						if(SCD.SettingAccount.containsKey(player)){
							SCD.setSharedOwnerSign(sign, player);
							SignProtection.add(sign);
						}
						else if(SCD.PreSetItemChests.contains(player)){
							SignProtection.add(sign);
							SCD.SettingItemChests.put(player, sign);
							SCD.PreSetItemChests.remove(player);
							player.sendMessage("[§2Sign§4C§6a§bs§9i§dn§co§f]§b Left Click a Chest to complete the Link.");
						}
					}
					else{
						if(SignProtection.contains(sign)){
							SignProtection.remove(sign);
						}
					}
				}
			}
		}
		else if(block.getType() == 54){
			if(SCD.SettingItemChests.containsKey(player)){
				return SCD.SetItemChest(player, block);
			}
		}
		return false;
	}
	
	public boolean onCommand(Player player, String[] cmd){
		if(cmd[0].equals("/scshare")){
			if(cmd.length > 1){
				if(SCD.dCo){
					if(player.canUseCommand("/scshare")){
						return SCD.presetSharedOwnerSign(player, cmd[1]);
					}
				}
				else{
					player.sendMessage("[§2Sign§4C§6a§bs§9i§dn§co§f]§c dConomy not used! Unable to set Shared Sign!");
					return true;
				}
			}
		}
		else if(cmd[0].equals("/scchest")){
			if(SCD.item){
				if(player.canUseCommand("/scchest")){
					SCD.PreSetItemChests.add(player);
					player.sendMessage("[§2Sign§4C§6a§bs§9i§dn§co§f]§b Left Click a Sign to Link to a chest.");
					return true;
				}
			}
			else{
				player.sendMessage("[§2Sign§4C§6a§bs§9i§dn§co§f]§c Item Currency not used! Unable to set Chest Linked Sign!");
				return true;
			}
		}
		return false;
	}
	
	public boolean onBlockBreak(Player player, Block block){
		if(block.getType() == 63 || block.getType() == 68){
			Sign sign = (Sign)player.getWorld().getComplexBlock(block);
			if(sign != null){
				if(sign.getText(0).length() > 7){
					String Type = sign.getText(0).substring(2);
					if(Type.equalsIgnoreCase("[SLOTS]") || Type.equals("[CRAPS]") || Type.equals("[BLACKJACK]") || Type.equals("[BACCARAT]")){
						String Owner = sign.getText(3);
						if(SCD.isNameFixed(Owner)){
							Owner = SCD.getNameFix(Owner);
						}
						if(SignProtection.contains(sign)){
							ResetSign(sign);
							SignProtection.remove(sign);
							return true;
						}
						else if(Owner.equals("GLOBAL") && ((!player.canUseCommand("/scadmin")) && (!player.canUseCommand("/scplaceglobal")))){
							ResetSign(sign);
							player.sendMessage("[§2Sign§4C§6a§bs§9i§dn§co§f]§c You cannot destory a GLOBAL sign!");
							return true;
						}
						else if((!Owner.equals(player.getName())) && (!player.canUseCommand("/scadmin"))){
							ResetSign(sign);
							player.sendMessage("[§2Sign§4C§6a§bs§9i§dn§co§f]§c You cannot destory a sign not owned by you!");
							return true;
						}
						else{
							dataDump(player, sign, Owner);
						}
					}
				}
			}
		}
		return false;
	}
	
	public void onPlayerMove(Player player, Location from, Location to){
		if(SCSM.PlayerPlaying.containsValue(player)){
			for(Sign sign : SCSM.PlayerPlaying.keySet()){
				if(SCSM.PlayerPlaying.get(sign) == player){
					for(int x = (int)player.getX()-4; x <= (int)player.getX()+4; x++){
						for(int z = (int)player.getZ()-4; z <= (int)player.getZ()+4; z++){
							for(int y = (int)player.getY(); y <= (int)player.getY()+4; y++){
								if(sign.getX() == x && sign.getZ() == z && sign.getY() == y){
									return;
								}
							}
						}
					}
					SCSM.PlayerPlaying.remove(sign);
				}
			}
		}
		else if(SCC.PlayerPlaying.containsValue(player)){
			for(Sign sign : SCC.PlayerPlaying.keySet()){
				if(SCC.PlayerPlaying.get(sign) == player){
					for(int x = (int)player.getX()-4; x <= (int)player.getX()+4; x++){
						for(int z = (int)player.getZ()-4; z <= (int)player.getZ()+4; z++){
							for(int y = (int)player.getY(); y <= (int)player.getY()+4; y++){
								if(sign.getX() == x && sign.getZ() == z && sign.getY() == y){
									return;
								}
							}
						}
					}
					SCC.PlayerPlaying.remove(sign);
				}
			}
		}
		else if(SCBJ.PlayerPlaying.containsValue(player)){
			for(Sign sign : SCBJ.PlayerPlaying.keySet()){
				if(SCBJ.PlayerPlaying.get(sign) == player){
					for(int x = (int)player.getX()-4; x <= (int)player.getX()+4; x++){
						for(int z = (int)player.getZ()-4; z <= (int)player.getZ()+4; z++){
							for(int y = (int)player.getY(); y <= (int)player.getY()+4; y++){
								if(sign.getX() == x && sign.getZ() == z && sign.getY() == y){
									return;
								}
							}
						}
					}
					SCBJ.PlayerPlaying.remove(sign);
					SCBJ.Hitting.put(sign, false);
				}
			}
		}
		else if(SCB.PlayerPlaying.containsValue(player)){
			for(Sign sign : SCB.PlayerPlaying.keySet()){
				if(SCB.PlayerPlaying.get(sign) == player){
					for(int x = (int)player.getX()-4; x <= (int)player.getX()+4; x++){
						for(int z = (int)player.getZ()-4; z <= (int)player.getZ()+4; z++){
							for(int y = (int)player.getY(); y <= (int)player.getY()+4; y++){
								if(sign.getX() == x && sign.getZ() == z && sign.getY() == y){
									return;
								}
							}
						}
					}
					SCB.PlayerPlaying.remove(sign);
				}
			}
		}
	}
	
	private void NoPlaceSign(Sign sign){
		Location loc = new Location(sign.getWorld(), sign.getX(), sign.getY(), sign.getZ());
		sign.getWorld().dropItem(loc, 323);
		sign.getWorld().setBlockAt(0, sign.getX(), sign.getY(), sign.getZ());
	}
	
	private void onesignonly(Player player, Sign sign){
		if(SCC.PlayerPlaying.containsValue(player)){
			for(Sign othersign : SCC.PlayerPlaying.keySet()){
				if(SCC.PlayerPlaying.get(othersign) == player){
					if(othersign.getX() != sign.getX() && othersign.getY() != sign.getY() && othersign.getZ() != sign.getZ() && othersign.getWorld() != sign.getWorld()){
						SCC.PlayerPlaying.remove(othersign);
						break;
					}
				}
			}
		}
		else if(SCSM.PlayerPlaying.containsValue(player)){
			for(Sign othersign : SCSM.PlayerPlaying.keySet()){
				if(SCSM.PlayerPlaying.get(othersign) == player){
					if(othersign.getX() != sign.getX() && othersign.getY() != sign.getY() && othersign.getZ() != sign.getZ() && othersign.getWorld() != sign.getWorld()){
						SCSM.PlayerPlaying.remove(sign);
						break;
					}
				}
			}
		}
		else if(SCBJ.PlayerPlaying.containsValue(player)){
			for(Sign othersign : SCBJ.PlayerPlaying.keySet()){
				if(SCBJ.PlayerPlaying.get(othersign) == player){
					if(othersign.getX() != sign.getX() && othersign.getY() != sign.getY() && othersign.getZ() != sign.getZ() && othersign.getWorld() != sign.getWorld()){
						SCBJ.PlayerPlaying.remove(sign);
						SCBJ.Hitting.put(sign, false);
						break;
					}
				}
			}
		}
		else if(SCB.PlayerPlaying.containsValue(player)){
			for(Sign othersign : SCB.PlayerPlaying.keySet()){
				if(SCB.PlayerPlaying.get(othersign) == player){
					if(othersign.getX() != sign.getX() && othersign.getY() != sign.getY() && othersign.getZ() != sign.getZ() && othersign.getWorld() != sign.getWorld()){
						SCB.PlayerPlaying.remove(sign);
						break;
					}
				}
			}
		}
	}
	
	private void ResetSign(Sign sign){
		sign.setText(0, sign.getText(0));
		sign.setText(1, sign.getText(1));
		sign.setText(2, sign.getText(2));
		sign.setText(3, sign.getText(3));
		sign.update();
	}
	
	private void dataDump(Player player, Sign sign, String Owner){
		String Type = sign.getText(0).substring(2);
		if(SCSM.PlayerPlaying.containsKey(sign)){
			SCSM.PlayerPlaying.remove(sign);
		}
		else if(SCC.PlayerPlaying.containsKey(sign)){
			SCC.PlayerPlaying.remove(sign);
		}
		else if(SCBJ.PlayerPlaying.containsKey(sign)){
			SCBJ.PlayerPlaying.remove(sign);
		}
		else if(SCB.PlayerPlaying.containsKey(sign)){
			SCB.PlayerPlaying.remove(sign);
		}
		if(SCD.dCo){
			if(SCD.SharedOwnerSign.containsKey(sign)){
				SCD.SharedOwnerSign.remove(sign);
				int w = sign.getWorld().getType().getId(), x = sign.getX(), y = sign.getY(), z = sign.getZ();
				SCD.SCShared.removeKey(w+"-"+x+"-"+y+"-"+z);
			}
		}
		else if(SCD.item){
			if(SCD.ItemChests.containsKey(sign)){
				SCD.ItemChests.remove(sign);
				int w = sign.getWorld().getType().getId(), x = sign.getX(), y = sign.getY(), z = sign.getZ();
				SCD.SCItemChests.removeKey(w+"-"+x+"-"+y+"-"+z);
			}
		}
		int signtype = 0;
		if(Type.equals("[SLOTS]")){
			signtype = 1;
		}
		else if(Type.equals("[CRAPS]")){
			signtype = 2;
		}
		else if(Type.equals("[BLACKJACK]")){
			if(SCBJ.SignDeck.containsKey(sign)){
				SCBJ.SignDeck.remove(sign);
			}
			if(SCBJ.Hitting.containsKey(sign)){
				SCBJ.Hitting.remove(sign);
			}
			signtype = 3;
		}
		else if(Type.equals("[BACCARAT]")){
			if(SCB.SignDeck.containsKey(sign)){
				SCB.SignDeck.remove(sign);
			}
			signtype = 4;
		}
		SCD.removeCount(signtype, Owner);
		player.sendMessage("[§2Sign§4C§6a§bs§9i§dn§co§f]§c Sign Successfully Destroyed!");
	}
}
