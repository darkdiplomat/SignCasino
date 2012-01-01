import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.logging.Logger;

public class SCData {
	SignCasino SC;
	PluginLoader loader;
	Logger log = Logger.getLogger("Minecraft");
	
	//File names and directory
	String SCDir = "plugins/config/SignCasino/";
	String SCP = "SCProperties.ini";
	String SCNF = "SCNameFixer.txt";
	String SCS = "SCSignCounts.list";
	String SCSS = "SCSharedSigns.list";
	String SCIC = "SCItemChests.list";
	//Properties Files
	PropertiesFile SCProps;
	PropertiesFile SCNames;
	PropertiesFile SCSigns;
	PropertiesFile SCShared;
	PropertiesFile SCItemChests;
	//Regular File Constructors
	File SCPF;
	File SCD;
	File SCSF;
	File SCNFF;
	File SCSignC;
	File SCShare;
	File SCICF;
	
	//Settings stuff
	boolean UEP, dCo, iCo, item, Debug, Tweet, UMS, UMCS, UMSS, UMBJS, UMBS;
	int MCS, MSS, MBJS, MBS, MSCS, WJPR, ItemID;
	String SA = "N/A";
	double AB;
	
	//Values Holders
	HashMap<Sign, String> SharedOwnerSign;
	HashMap<String, String> NameFix;
	HashMap<Player, String> SettingAccount;
	HashMap<String, Integer> AllSignCount;
	HashMap<String, Integer> SlotsSignCount;
	HashMap<String, Integer> CrapsSignCount;
	HashMap<String, Integer> BlackJackSignCount;
	HashMap<String, Integer> BaccaratSignCount;
	HashMap<Sign, Location[]> ItemChests;
	HashMap<Player, Sign> SettingItemChests;
	ArrayList<Player> PreSetItemChests;
	
	public SCData(SignCasino SC){
		this.SC = SC;
		loader = etc.getLoader();
		SharedOwnerSign = new HashMap<Sign, String>();
		NameFix = new HashMap<String, String>();
		SettingAccount = new HashMap<Player, String>();
		AllSignCount = new HashMap<String, Integer>();
		SlotsSignCount = new HashMap<String, Integer>();
		CrapsSignCount = new HashMap<String, Integer>();
		BlackJackSignCount= new HashMap<String, Integer>();
		BaccaratSignCount= new HashMap<String, Integer>();
		ItemChests = new HashMap<Sign, Location[]>();
		SettingItemChests = new HashMap<Player, Sign>();
		PreSetItemChests = new ArrayList<Player>();
		load();
	}
	
	private void load(){
		SCD = new File(SCDir);
		if(!SCD.exists()){
			SCD.mkdirs();
		}
		MakeProps();
		getNameFixer();
		getSignCounts();
		checkCurrencyPlugins();
		checkTweet();
		checkMaxSigns();
		checkServerAccount();
		getSharedSigns();
		getItemChests();
		//And the last few bits
		Debug = LoadBooleanCheck(false, "DEBUG");
		WJPR = LoadINTCheck(100, "WinJackpotRate");
		AB= LoadDOUBLECheck(1.25, "Auto-Bet");
		
	}
	
	private void MakeProps(){
		SCPF = new File(SCDir+SCP);
		if(!SCPF.exists()){
			try{
				File outputFile = new File(SCDir+SCP);
				InputStream in = getClass().getClassLoader().getResourceAsStream("DefaultProp.ini");
				FileWriter out = new FileWriter(outputFile);
				int c;
				while ((c = in.read()) != -1){
					out.write(c);
				}
				in.close();
				out.close();
			} 
			catch (IOException e){
				log.severe("[SignCasino] Unable to create properties file!");
			}
		}
		SCProps = new PropertiesFile(SCDir+SCP);
	}	
	
	private void checkCurrencyPlugins(){
		UEP = LoadBooleanCheck(true, "useEconomyPlugin");
		if(UEP){
			if(loader.getPlugin("dConomy") != null && loader.getPlugin("dConomy").isEnabled()){
				dCo = true;
			}
			else if(loader.getPlugin("iConomy") != null && loader.getPlugin("iConomy").isEnabled()){
				iCo = true;
			}
			if(!dCo && !iCo){
				log.warning("[SignCasino] No sutible Economy Plugin found! Defaulting to Item Currency");
				item = true;
			}
		}
		else{
			item = true;
		}
		if(item){
			ItemID = LoadINTCheck(371, "CurrencyItemID");
		}
	}
	
	private void checkTweet(){
		Tweet = LoadBooleanCheck(false, "Use-TwitterEvents");
		if(Tweet){
			if(!(loader.getPlugin("TwitterEvents") != null) || (!loader.getPlugin("TwitterEvents").isEnabled())){
				log.warning("[SignCasino] TwitterEvents not found! Disabling TwitterEvents use!");
				Tweet = false;
			}
		}
	}
	
	private void checkMaxSigns(){
		UMS = LoadBooleanCheck(false, "Use-MaxSignCasinoSigns");
		if(UMS){
			MSCS = LoadINTCheck(50, "MaxSignCasinoSigns");
		}
		else{
			UMSS = LoadBooleanCheck(false, "Use-MaxSLOTSSigns");
			if(UMSS){
				MSS = LoadINTCheck(10, "MaxSLOTSSigns");
			}
			UMCS = LoadBooleanCheck(false, "Use-MaxCRAPSSigns");
			if(UMCS){
				MCS = LoadINTCheck(10, "MaxCRAPSSigns");
			}
			UMBJS = LoadBooleanCheck(false, "Use-MaxBLACKJACKSigns");
			if(UMBJS){
				MBJS = LoadINTCheck(10, "MaxBLACKJACKSigns");
			}
			UMBS = LoadBooleanCheck(false, "Use-MaxBACCARATSigns");
			if(UMBS){
				MBS = LoadINTCheck(10, "MaxBACCARATSigns");
			}
		}
	}
	
	private void checkServerAccount(){
		SA = LoadStringCheck("N/A", "ServerGobalAccount");
		double bal = 0;
		if(!SA.equalsIgnoreCase("N/A")){
			if(dCo){
				bal = (Double) loader.callCustomHook("dCBalance", new Object[]{"Joint-Balance-NC", SA});
				if(bal == -1){
					SA = "N/A";
					log.warning("[SignCasino] ServerGobalAccount is not a dConomy Joint Account! Defaulting to N/A");
				}
			}
			else if(iCo){
				bal = (Integer) loader.callCustomHook("iBalance", new Object[]{"balance", SA});
				if(bal < 0){
					log.warning("[SignCasino] ServerGobalAccount is not a iConomy Account! Defaulting to N/A");
				}
			}
			else{
				Chest chest = null;
				String[] chests = SA.split(",");
				for(int i = 0; i < chests.length; i++){
					String[] coords = chests[i].split("-");
					int w = 0, x = 0, y = 0, z = 0;
					int set = 0;
					try{
						w = Integer.parseInt(coords[0]);
						x = Integer.parseInt(coords[1]);
						y = Integer.parseInt(coords[2]);
						z = Integer.parseInt(coords[3]);
					}catch(NumberFormatException NFE){
						log.warning("[SignCasino] ServerGobalAccount had a set of bad Coordinates at set: "+set);
						set++;
						continue;
					}catch(ArrayIndexOutOfBoundsException AIOOBE){
						log.warning("[SignCasino] ServerGobalAccount had a set of bad Coordinates at set: "+set);
						set++;
						continue;
					}
					set++;
					if(!etc.getServer().getWorld(w).getChunk(x, y, z).isLoaded()){
						etc.getServer().getWorld(w).loadChunk(x, y, z);
					}
					Block block = etc.getServer().getWorld(w).getBlockAt(x, y, z);
					if(block.getType() == 54){
						chest = (Chest)etc.getServer().getWorld(w).getOnlyComplexBlock(block);
					}
				}
				if(chest == null){
					SA = "N/A";
					log.warning("[SignCasino] ServerGobalAccount didn't have Chest Coordinates! Defaulting to N/A");
				}
			}
		}
	}
	
	private void getNameFixer(){
		SCNFF = new File(SCDir+SCNF);
		if(SCNFF.exists()){
			try{
				BufferedReader in = new BufferedReader(new FileReader(SCDir+SCNF));
				String str;
				int line = 0;
				while ((str = in.readLine()) != null) {
					if(!str.contains("#")){
						try{
							String[] namefix = str.split("=");
							NameFix.put(namefix[0], namefix[1]);
						}catch(ArrayIndexOutOfBoundsException AIOOBE){
							log.warning("[SignCasino] (NameFix) There was an issue with Name at line: "+line);
						}
						line++;
					}
					else{
						line++;
					}
				}
				in.close();
			}catch(IOException IOE){
				log.warning("[SignCasino] Failed to load Name Fix file.");
			}
		}
		SCNames = new PropertiesFile(SCDir+SCNF);
	}
	
	private void getSignCounts(){
		SCSignC = new File(SCDir+SCS);
		int count, amount;
		if(SCSignC.exists()){
			try{
				BufferedReader in = new BufferedReader(new FileReader(SCDir+SCS));
				String str;
				int line = 0;
				while ((str = in.readLine()) != null) {
					if(!str.contains("#")){
						try{
							String[] SC = str.split("=");
							String[] NST = SC[0].split("-");
							try{
								amount = Integer.parseInt(SC[1]);
							}catch(NumberFormatException NFE){
								log.warning("[SignCasino] (SignCounts) There was an issue reading amount at line: "+line);
								line++;
								continue;
							}catch(ArrayIndexOutOfBoundsException AIOOBE){
								log.warning("[SignCasino] (SignCounts) There was an issue reading amount at line: "+line);
								line++;
								continue;
							}
							
							if(AllSignCount.containsKey(NST[0])){
								count = AllSignCount.get(NST)+amount;
								AllSignCount.put(NST[0], count);
							}
							else{
								AllSignCount.put(NST[0], amount);
							}
							if(NST[1].equals("Slots")){
								SlotsSignCount.put(NST[0], amount);
							}
							else if(NST[1].equals("Craps")){
								CrapsSignCount.put(NST[0], amount);
							}
							else if(NST[1].equals("BlackJack")){
								BlackJackSignCount.put(NST[0], amount);
							}
							else if(NST[1].equals("Baccarat")){
								BaccaratSignCount.put(NST[0], amount);
							}
						}catch(ArrayIndexOutOfBoundsException AIOOBE){
							log.warning("[SignCasino] (SignCounts) There was an issue reading the Line at line: "+line);
							line++;
							continue;
						}
					}
					else{
						line++;
					}
				}
				in.close();
			}catch(IOException IOE){
				log.warning("[SignCasino] Failed to load Sign Count file.");
			}
		}
		SCSigns = new PropertiesFile(SCDir+SCS);
	}
	
	private void getSharedSigns(){
		if(dCo){
			SCShare = new File(SCDir+SCSS);
			ArrayList<String> RemoveKey = new ArrayList<String>();
			if(SCShare.exists()){
				try{
					BufferedReader in = new BufferedReader(new FileReader(SCDir+SCSS));
					String str;
					int line = 0;
					while ((str = in.readLine()) != null) {
						if(!str.contains("#")){
							String[] account = str.split("=");
							try{
								String[] coords = account[0].split("-");
								int w = Integer.parseInt(coords[0]);
								int x = Integer.parseInt(coords[1]);
								int y = Integer.parseInt(coords[2]);
								int z = Integer.parseInt(coords[3]);
								if(!etc.getServer().getWorld(w).getChunk(x, y, z).isLoaded()){
									etc.getServer().getWorld(w).loadChunk(x, y, z);
								}
								Block block = etc.getServer().getWorld(w).getBlockAt(x, y, z);
								if(block.getType() == 63 || block.getType() == 68){
									Sign sign = (Sign) etc.getServer().getWorld(w).getComplexBlock(block);
									if(sign != null){
										SharedOwnerSign.put(sign, account[1]);
									}
									else{
										RemoveKey.add(account[0]);
									}
								}
							}catch(NumberFormatException NFE){
								log.warning("[SignCasino] There was an issue reading the Line at line: "+line);
								line++;
								continue;
							}catch(ArrayIndexOutOfBoundsException AIOOBE){
								log.warning("[SignCasino] There was an issue reading the Line at line: "+line);
								line++;
								continue;
							}
						}
						else{
							line++;
						}
					}
				}catch(IOException IOE){
					log.warning("[SignCasino] Failed to load Shared Account Signs file.");
				}
			}
			SCShared = new PropertiesFile(SCDir+SCSS);
			if(!RemoveKey.isEmpty()){
				for(int i = 0; i < RemoveKey.size(); i++){
					SCItemChests.removeKey(RemoveKey.get(i));
				}
			}
		}
	}
	
	private void getItemChests(){
		if(item){
			SCICF = new File(SCDir+SCIC);
			ArrayList<String> RemoveKey = new ArrayList<String>();
			HashMap<String, String[]> RemoveChest = new HashMap<String, String[]>();
			if(SCICF.exists()){
				try{
					BufferedReader in = new BufferedReader(new FileReader(SCDir+SCIC));
					String str;
					while ((str = in.readLine()) != null) {
						if(!str.contains("#")){
							String[] SignLoc = str.split("=");
							try{
								String[] Signcoords = SignLoc[0].split("-");
								String[] ChestLocs = SignLoc[1].split(",");
								int w = Integer.parseInt(Signcoords[0]);
								int x = Integer.parseInt(Signcoords[1]);
								int y = Integer.parseInt(Signcoords[2]);
								int z = Integer.parseInt(Signcoords[3]);
								if(!etc.getServer().getWorld(w).getChunk(x, y, z).isLoaded()){
									etc.getServer().getWorld(w).loadChunk(x, y, z);
								}
								Block block = etc.getServer().getWorld(w).getBlockAt(x, y, z);
								if(block.getType() == 63 || block.getType() == 68){
									Sign sign = (Sign) etc.getServer().getWorld(w).getComplexBlock(block);
									if(sign != null){
										Location[] locs = new Location[ChestLocs.length];
										for(int i = 0; i < ChestLocs.length; i++){
											String[] Chestcoords = ChestLocs[i].split("-");
											w = Integer.parseInt(Chestcoords[0]);
											x = Integer.parseInt(Chestcoords[1]);
											y = Integer.parseInt(Chestcoords[2]);
											z = Integer.parseInt(Chestcoords[3]);
											if(!etc.getServer().getWorld(w).getChunk(x, y, z).isLoaded()){
												etc.getServer().getWorld(w).loadChunk(x, y, z);
											}
											block = etc.getServer().getWorld(w).getBlockAt(x, y, z);
											World world = etc.getServer().getWorld(w);
											if(block.getType() == 54){
												locs[i] = new Location(world, x, y, z);
											}
											else{
												if(RemoveChest.containsKey(SignLoc[0])){
													String[] remove = RemoveChest.get(SignLoc[0]);
													String[] newremove = new String[remove.length+1];
													int j;
													for(j = 0; i < remove.length; j++){
														newremove[j] = remove[j];
													}
													newremove[j] = ChestLocs[i];
												}
												else{
													RemoveChest.put(SignLoc[0], new String[]{ChestLocs[i]});
												}
											}
										}
										if(locs[0] != null){
											ItemChests.put(sign, locs);
										}
										else{
											RemoveKey.add(SignLoc[0]);
										}
									}
									else{
										RemoveKey.add(SignLoc[0]);
									}
								}
							}catch(NumberFormatException NFE){
								continue;
							}catch(ArrayIndexOutOfBoundsException AIOOBE){
								continue;
							}
						}
					}
				}catch(IOException IOE){
					log.warning("[SignCasino] Failed to load Item Chests file.");
				}
			}
			SCItemChests = new PropertiesFile(SCDir+SCIC);
			if(!RemoveKey.isEmpty()){
				for(int i = 0; i < RemoveKey.size(); i++){
					SCItemChests.removeKey(RemoveKey.get(i));
					if(RemoveChest.containsKey(RemoveKey.get(i))){
						RemoveChest.remove(RemoveKey.get(i));
					}
				}
			}
			if(!RemoveChest.isEmpty()){
				ArrayList<String> newChests = new ArrayList<String>();
				for(String key : RemoveChest.keySet()){
					if(SCItemChests.containsKey(key)){
						String Value = SCItemChests.getString(key);
						String[] ChestLocs = Value.split(",");
						String[] removeLocs = RemoveChest.get(key);
						for(int i = 0; i < ChestLocs.length; i++){
							for(int j = 0; j < removeLocs.length; j++){
								if(!ChestLocs[i].equals(removeLocs[j])){
									if(!newChests.contains(ChestLocs[i])){
										newChests.add(ChestLocs[i]);
									}
								}
							}
						}
					}
					if(!newChests.isEmpty()){
						String newVal = newChests.toString().replace("[", "").replace("]", "").replace(" ", "");
						SCItemChests.setString(key, newVal);
						newChests.clear();
					}
				}
			}
		}
	}
	
	private String LoadStringCheck(String defaultvalue, String Property){
		String value;
		if(SCProps.containsKey(Property)){
			value = SCProps.getString(Property);
		}else{
			log.warning("[SignCasino] Value: "+Property+" not found! Using default of "+String.valueOf(defaultvalue));
			value = defaultvalue;
		}
		return value;
	}
	
	private boolean LoadBooleanCheck(boolean defaultvalue, String Property){
		boolean value;
		if(SCProps.containsKey(Property)){
			value = SCProps.getBoolean(Property);
		}else{
			log.warning("[SignCasino] Value: "+Property+" not found! Using default of "+String.valueOf(defaultvalue));
			value = defaultvalue;
		}
		return value;
	}
	
	private int LoadINTCheck(int defaultvalue, String Property){
		int value;
		if(SCProps.containsKey(Property)){
			try{
				value = SCProps.getInt(Property);
			}catch(NumberFormatException NFE){
				log.warning("[SignCasino] Bad Value at "+Property+" Using default of "+String.valueOf(defaultvalue));
				value = defaultvalue;
			}
		}
		else{
			log.warning("[SignCasino] Value: "+Property+" not found! Using default of "+String.valueOf(defaultvalue));
			value = defaultvalue;
		}
		return value;
	}
	
	private double LoadDOUBLECheck(double defaultvalue, String Property){
		double value;
		if(SCProps.containsKey(Property)){
			try{
				value = SCProps.getDouble(Property);
			}catch(NumberFormatException NFE){
				log.warning("[SignCasino] Bad Value at "+Property+" Using default of "+String.valueOf(defaultvalue));
				value = defaultvalue;
			}
		}
		else{
			log.warning("[SignCasino] Value: "+Property+" not found! Using default of "+String.valueOf(defaultvalue));
			value = defaultvalue;
		}
		return value;
	}
	
	public String getAB(){
		if(dCo){
			return priceForm(AB);
		}
		return String.valueOf(((int)AB));
	}
	
	public String priceForm(double price){
		String newprice = String.valueOf(price);
		String[] form = newprice.split("\\.");
		if(form[1].length() == 1){
			newprice += "0";
		}
		else{
			newprice = form[0] + "." + form[1].substring(0, 2);
		}
		return newprice;
	}
	
	public boolean isSharedSign(Sign sign){
		if(SharedOwnerSign.containsKey(sign)){
			String account = SharedOwnerSign.get(sign);
			String owner = sign.getText(3);
			if(isNameFixed(owner)){
				owner = getNameFix(owner);
			}
			double bal = (Double) loader.callCustomHook("dCBalance", new Object[]{"Joint-Balance", owner, account});
			if(bal == -1 || bal == -2){
				SharedOwnerSign.remove(sign);
				return false;
			}
			else{
				return true;
			}
		}
		return false;
	}
	
	public String getSharedAccount(Sign sign){
		return SharedOwnerSign.get(sign);
	}
	
	public boolean presetSharedOwnerSign(Player player, String Account){
		double bal = (Double) loader.callCustomHook("dCBalance", new Object[]{"Joint-Balance", player.getName(), Account});
		if(bal == -1){
			player.sendMessage("[§2Sign§4C§6a§bs§9i§dn§co§f]§c Account not found.");
		}
		else if(bal == -2){
			player.sendMessage("[§2Sign§4C§6a§bs§9i§dn§co§f]§c You don't have rights to that Account.");
		}
		else{
			player.sendMessage("[§2Sign§4C§6a§bs§9i§dn§co§f]§b Left Click sign to set account for.");
			SettingAccount.put(player, Account);
		}
		return true;
	}
	
	public boolean setSharedOwnerSign(Sign sign, Player player){
		String owner = sign.getText(3);
		if(isNameFixed(owner)){
			owner = getNameFix(owner);
		}
		if(!owner.equalsIgnoreCase(player.getName())){
			player.sendMessage("[§2Sign§4C§6a§bs§9i§dn§co§f]§c You don't own this sign.");
			SettingAccount.remove(player);
		}
		else{
			String account = SettingAccount.get(player);
			SharedOwnerSign.put(sign, account);
			SettingAccount.remove(player);
			int w = sign.getWorld().getType().getId(), x = sign.getX(), y = sign.getY(), z = sign.getZ();
			SCShared.setString(w+"-"+x+"-"+y+"-"+z, account);
			player.sendMessage("[§2Sign§4C§6a§bs§9i§dn§co§f]§b SharedAccount set to: "+account);
		}
		return true;
	}
	
	public boolean isSharedOwner(String Account, String Name){
		double bal = (Double) loader.callCustomHook("dCBalance", new Object[]{"Joint-Balance", Name, Account});
		if(bal == -2 || bal == -1){
			return false;
		}
		else{
			return true;
		}
	}
	
	public boolean isNameFixed(String name){
		return NameFix.containsKey(name);
	}
	
	public String getNameFix(String name){
		return NameFix.get(name);
	}
	
	public void setNameFix(String fix, String name){
		NameFix.put(fix, name);
		SCNames.setString(fix, name);
	}
	
	public boolean canPlaceSign(int Type, String name){
		int count = 1;
		if(UMS){
			if(AllSignCount.containsKey(name)){
				count = AllSignCount.get(name)+1;
				if(count > MSCS){
					return false;
				}
			}
		}
		else{
			switch(Type){
			case 1:
				if(UMSS){
					if(SlotsSignCount.containsKey(name)){
						count = SlotsSignCount.get(name)+1;
						if(count > MSS){
							return false;
						}
					}
				}
			case 2:
				if(UMCS){
					if(CrapsSignCount.containsKey(name)){
						count = CrapsSignCount.get(name)+1;
						if(count > MCS){
							return false;
						}
					}
				}
			case 3:
				if(UMBJS){
					if(BlackJackSignCount.containsKey(name)){
						count = BlackJackSignCount.get(name)+1;
						if(count > MBJS){
							return false;
						}
					}
				}
			case 4:
				if(UMBS){
					if(BaccaratSignCount.containsKey(name)){
						count = BaccaratSignCount.get(name)+1;
						if(count > MBS){
							return false;
						}
					}
				}
			}
		}
		addALLSignCount(name);
		switch(Type){
		case 1: addSlotSignCount(name); break;
		case 2: addCrapsSignCount(name); break;
		case 3: addBlackJackSignCount(name); break;
		case 4: addBaccaratSignCount(name); break;
		}		
		return true;
	}
	
	private void addSlotSignCount(String name){
		int count = 1;
		if(SlotsSignCount.containsKey(name)){
			count = SlotsSignCount.get(name)+1;
			SlotsSignCount.put(name, count);
			SCSigns.setInt(name+"-Slots", count);
		}
		else{
			SlotsSignCount.put(name, count);
			SCSigns.setInt(name+"-Slots", count);
		}
	}
	
	private void addCrapsSignCount(String name){
		int count = 1;
		if(CrapsSignCount.containsKey(name)){
			count = CrapsSignCount.get(name)+1;
			CrapsSignCount.put(name, count);
			SCSigns.setInt(name+"-Craps", count);
		}
		else{
			CrapsSignCount.put(name, count);
			SCSigns.setInt(name+"-Craps", count);
		}
	}
	
	private void addBlackJackSignCount(String name){
		int count = 1;
		if(BlackJackSignCount.containsKey(name)){
			count = BlackJackSignCount.get(name)+1;
			BlackJackSignCount.put(name, count);
			SCSigns.setInt(name+"-BlackJack", count);
		}
		else{
			BlackJackSignCount.put(name, count);
			SCSigns.setInt(name+"-BlackJack", count);
		}
	}
	
	private void addBaccaratSignCount(String name){
		int count = 1;
		if(BaccaratSignCount.containsKey(name)){
			count = BaccaratSignCount.get(name)+1;
			BaccaratSignCount.put(name, count);
			SCSigns.setInt(name+"-Baccarat", count);
		}
		else{
			BaccaratSignCount.put(name, count);
			SCSigns.setInt(name+"-Baccarat", count);
		}
	}
	
	private void addALLSignCount(String name){
		int count = 1;
		if(AllSignCount.containsKey(name)){
			count = AllSignCount.get(name)+1;
			AllSignCount.put(name, count);
		}
		else{
			AllSignCount.put(name, count);
		}
	}
	
	public void removeCount(int Type, String name){
		int count;
		switch(Type){
		case 1:
			if(SlotsSignCount.containsKey(name)){
				count = SlotsSignCount.get(name)-1;
				if(count > -1){
					SlotsSignCount.put(name, count);
				}
			}
			break;
		case 2:
			if(CrapsSignCount.containsKey(name)){
				count = CrapsSignCount.get(name)-1;
				if(count > -1){
					CrapsSignCount.put(name, count);
				}
			}
			break;
		case 3:
			if(BlackJackSignCount.containsKey(name)){
				count = BlackJackSignCount.get(name)-1;
				if(count > -1){
					BlackJackSignCount.put(name, count);
				}
			}
		case 4:
			if(BaccaratSignCount.containsKey(name)){
				count = BaccaratSignCount.get(name)-1;
				if(count > -1){
					BaccaratSignCount.put(name, count);
				}
			}
		}
		if(AllSignCount.containsKey(name)){
			count = AllSignCount.get(name)-1;
			if(count > -1){
				SlotsSignCount.put(name, count);
			}
		}
	}
	
	public boolean SetItemChest(Player player, Block chest){
		StringBuffer build = new StringBuffer();
		Sign sign = SettingItemChests.get(player);
		Location loc = new Location(player.getWorld(), chest.getX(), chest.getY(), chest.getZ());
		Location[] exist = null;
		Location[] add = new Location[]{loc};
		String SignLoc = sign.getWorld().getType().getId()+"-"+sign.getX()+"-"+sign.getY()+"-"+sign.getZ();
 		if(ItemChests.containsKey(sign)){
			exist = ItemChests.get(sign);
			add = new Location[exist.length+1];
			for(int i = 0; i < exist.length; i++){
				if(!(loc == exist[i])){
					add[i] = exist[i];
					build.append(player.getLocation().dimension+"-"+((int)exist[i].x)+"-"+((int)exist[i].y)+"-"+((int)exist[i].z)+",");
				}
			}
			add[exist.length] = loc;
		}
 		build.append(loc.dimension+"-"+((int)loc.x)+"-"+((int)loc.y)+"-"+((int)loc.z)+",");
 		ItemChests.put(sign, add);
 		SCItemChests.setString(SignLoc, build.toString());
 		player.sendMessage("[§2Sign§4C§6a§bs§9i§dn§co§f]§b Link Completed!");
 		SettingItemChests.remove(player);
		return true;
	}
	
	public boolean hasMoney(String name, double bet, boolean shared, String revertto, Sign sign){
		double bal = 0;
		if(dCo){
			if(!shared){
				if(!name.equals("GLOBAL")){
					bal = (Double) loader.callCustomHook("dCBalance", new Object[]{"Player-Balance", name});
					if(bal >= bet){
						return true;
					}
				}
				else{
					if(!SA.equals("N/A")){
						bal = (Double) loader.callCustomHook("dCBalance", new Object[]{"Joint-Balance-NC", SA});
						if(bal < 0){
							SA = "N/A";
							return true;
						}
						else if(bal >= bet){
							return true;
						}
					}
					else{
						return true;
					}
				}
			}
			else{
				bal = (Double) loader.callCustomHook("dCBalance", new Object[]{"Joint-Balance-NC", name});
				if(bal == -1){
					//Joint Account no longer there so revert ownership
					SharedOwnerSign.remove(sign);
					bal = (Double) loader.callCustomHook("dCBalance", new Object[]{"Player-Balance", revertto});
					if(bal >= bet){
						return true;
					}
				}
				if(bal >= bet){
					return true;
				}
			}
		}
		else if(iCo){
			if(!name.equals("GLOBAL")){
				bal = (Integer) loader.callCustomHook("iBalance", new Object[]{"balance", name});
				if(bal >= bet){
					return true;
				}
			}
			else if(!SA.equals("N/A")){
				bal = (Integer) loader.callCustomHook("iBalance", new Object[]{"balance", SA});
				if(bal >= bet){
					return true;
				}
			}
			else{
				return true;
			}
		}
		else{
			if(!name.equals("GLOBAL")){
				if(!shared){
					Player player = etc.getServer().getPlayer(name);
					if(player != null && player.isConnected()){
						if(player.getInventory().hasItem(ItemID, (int)bet)){
							return true;
						}
					}
				}
				else{
					Chest chest = null;
					Location[] locs = ItemChests.get(sign);
					for(int i = 0; i < locs.length; i++){
						int w = sign.getWorld().getType().getId();
						int x = (int)locs[i].x;
						int y = (int)locs[i].y;
						int z = (int)locs[i].z;
						
						if(!etc.getServer().getWorld(w).getChunk(x, y, z).isLoaded()){
							etc.getServer().getWorld(w).loadChunk(x, y, z);
						}
						
						Block block = etc.getServer().getWorld(w).getBlockAt(x, y, z);
						if(block.getType() == 54){
							chest = (Chest)block.getWorld().getComplexBlock(block);
							if(chest.findAttachedChest() != null){
								DoubleChest dc = chest.findAttachedChest();
								if(dc.hasItem(ItemID, (int)bet)){
									return true;
								}
							}
							else if(chest.hasItem(ItemID, (int)bet)){
								return true;
							}
						}
					}
					if(chest == null){
						ItemChests.remove(sign);
						Player player = etc.getServer().getPlayer(name);
						if(player != null && player.isConnected()){
							if(player.getInventory().hasItem(ItemID, (int)bet)){
								return true;
							}
						}
					}
				}
			}
			else if(!SA.equalsIgnoreCase("N/A")){
				Chest chest = null;
				String[] chests = SA.split(",");
				for(int i = 0; i < chests.length; i++){
					String[] coords = chests[i].split("-");
					int w = 0, x = 0, y = 0, z = 0;
					try{
						w = Integer.parseInt(coords[0]);
						x = Integer.parseInt(coords[1]);
						y = Integer.parseInt(coords[2]);
						z = Integer.parseInt(coords[3]);
					}catch(NumberFormatException NFE){
						//whoops no go
						continue;
					}catch(StringIndexOutOfBoundsException SIOOBE){
						//whoops no go
						continue;
					}
					if(!etc.getServer().getWorld(w).getChunk(x, y, z).isLoaded()){
						etc.getServer().getWorld(w).loadChunk(x, y, z);
					}
					Block block = etc.getServer().getWorld(w).getBlockAt(x, y, z);
					if(block.getType() == 54){
						chest = (Chest)etc.getServer().getWorld(w).getOnlyComplexBlock(block);
						if(chest.findAttachedChest() != null){
							DoubleChest dc = chest.findAttachedChest();
							if(dc.hasItem(ItemID, (int)bet)){
								return true;
							}
						}
						else if(chest.hasItem(ItemID, (int)bet)){
							return true;
						}
					}
				}
				if(chest == null){
					SA = "N/A";
					return true;
				}
			}
			else{
				return true;
			}
		}
		return false;
	}
	
	public void payup(String charge, String pay, double amount, boolean payshared, boolean chargeshared, Sign sign){
		if(dCo){
			if(chargeshared){
				loader.callCustomHook("dCBalance", new Object[]{"Joint-Withdraw-NC", charge, amount});
				loader.callCustomHook("dCBalance", new Object[]{"Player-Pay", pay, amount});
			}
			else if(payshared){
				loader.callCustomHook("dCBalance", new Object[]{"Joint-Deposit-NC", pay, amount});
				loader.callCustomHook("dCBalance", new Object[]{"Player-Charge", charge, amount});
			}
			else{
				loader.callCustomHook("dCBalance", new Object[]{"Player-Pay", pay, amount});
				loader.callCustomHook("dCBalance", new Object[]{"Player-Charge", charge, amount});
			}
		}
		else if(iCo){
			if(!charge.equals("GLOBAL")){
				loader.callCustomHook("iBalance", new Object[]{"withdraw", charge, (int)amount});
			}
			else if(!SA.equalsIgnoreCase("N/A")){
				loader.callCustomHook("iBalance", new Object[]{"withdraw", SA, (int)amount});
			}
			if(!pay.equals("GLOBAL")){
				loader.callCustomHook("iBalance", new Object[]{"deposit", pay, (int)amount});
			}
			else if(!SA.equalsIgnoreCase("N/A")){
				loader.callCustomHook("iBalance", new Object[]{"deposit", SA, (int)amount});
			}
		}
		else{
			if(!charge.equals("GLOBAL")){
				if(!chargeshared){
					Player charging = etc.getServer().getPlayer(charge);
					if((charging != null) && (charging.isConnected())){
						if(charging.getInventory().hasItem(ItemID, (int)amount)){
							charging.getInventory().removeItem(ItemID, (int)amount);
						}
					}
				}
				else{
					Chest chest = null;
					Location[] locs = ItemChests.get(sign);
					if(locs.length > 0){
						for(int i = 0; i < locs.length; i++){
							int w = locs[i].dimension;
							int x = (int)locs[i].x;
							int y = (int)locs[i].y;
							int z = (int)locs[i].z;
						
							if(!etc.getServer().getWorld(w).getChunk(x, y, z).isLoaded()){
									etc.getServer().getWorld(w).loadChunk(x, y, z);
							}
						
							Block block = etc.getServer().getWorld(w).getBlockAt(x, y, z);
							if(block.getType() == 54){
								chest = (Chest)block.getWorld().getOnlyComplexBlock(block);
								if(chest.findAttachedChest() != null){
									DoubleChest dc = chest.findAttachedChest();
									if(dc.hasItem(ItemID, (int)amount)){
										dc.removeItem(ItemID, (int)amount);
									}
								}
								else if(chest.hasItem(ItemID, (int)amount)){
									chest.removeItem(ItemID, (int)amount);
								}
							}
						}
					}
					if(chest == null){
						ItemChests.remove(sign);
						SCItemChests.removeKey(sign.getWorld().getType().getId()+"-"+sign.getX()+"-"+sign.getY()+"-"+sign.getZ());
						Player charging = etc.getServer().getPlayer(charge);
						if((charging != null) && (charging.isConnected())){
							if(charging.getInventory().hasItem(ItemID, (int)amount)){
								charging.getInventory().removeItem(ItemID, (int)amount);
							}
						}
					}
				}
			}
			else if(!SA.equalsIgnoreCase("N/A")){
				Chest chest = null;
				String[] chests = SA.split(",");
				for(int i = 0; i < chests.length; i++){
					String[] coords = chests[i].split("-");
					int w = 0, x = 0, y = 0, z = 0;
					try{
						w = Integer.parseInt(coords[0]);
						x = Integer.parseInt(coords[1]);
						y = Integer.parseInt(coords[2]);
						z = Integer.parseInt(coords[3]);
					}catch(NumberFormatException NFE){
						//whoops no go
						continue;
					}catch(ArrayIndexOutOfBoundsException SIOOBE){
						//whoops no go
						continue;
					}
					if(!etc.getServer().getWorld(w).getChunk(x, y, z).isLoaded()){
						etc.getServer().getWorld(w).loadChunk(x, y, z);
					}
					Block block = etc.getServer().getWorld(w).getBlockAt(x, y, z);
					if(block.getType() == 54){
						chest = (Chest)etc.getServer().getWorld(w).getOnlyComplexBlock(block);
						if(chest.findAttachedChest() != null){
							DoubleChest dc = chest.findAttachedChest();
							if(dc.hasItem(ItemID, (int)amount)){
								dc.removeItem(ItemID, (int)amount);
							}
						}
						if(chest.hasItem(ItemID, (int)amount)){
							chest.removeItem(ItemID, (int)amount);
							break;
						}
					}
				}
				if(chest == null){
					SA = "N/A";
				}
			}
			//PAY
			if(!pay.equals("GLOBAL")){
				if(!payshared){
					Player paying = etc.getServer().getPlayer(pay);
					if(paying != null && paying.isConnected()){
						addItem(paying.getInventory(), ItemID, (int)amount);
					}
				}
				else{
					Chest chest = null;
					Location[] locs = ItemChests.get(sign);
					for(int i = 0; i < locs.length; i++){
						int w = locs[i].dimension;
						int x = (int)locs[i].x;
						int y = (int)locs[i].y;
						int z = (int)locs[i].z;
						
						if(!etc.getServer().getWorld(w).getChunk(x, y, z).isLoaded()){
							etc.getServer().getWorld(w).loadChunk(x, y, z);
						}
						
						Block block = etc.getServer().getWorld(w).getBlockAt(x, y, z);
						if(block.getType() == 54){
							chest = (Chest)block.getWorld().getComplexBlock(block);
							if(chest.findAttachedChest() != null){
								DoubleChest dc = chest.findAttachedChest();
								if(hasRoom(dc, ItemID, (int)amount)){
									addItem(dc, ItemID, (int)amount);
								}
							}
							else if(hasRoom(chest, ItemID, (int)amount)){
								addItem(chest, ItemID, (int)amount);
								break;
							}
						}
					}
					if(chest == null){
						ItemChests.remove(sign);
						SCItemChests.removeKey(sign.getWorld().getType().getId()+"-"+sign.getX()+"-"+sign.getY()+"-"+sign.getZ());
						Player paying = etc.getServer().getPlayer(pay);
						if(paying != null && paying.isConnected()){
							addItem(paying.getInventory(), ItemID, (int)amount);
						}
					}
				}
			}
			else if(!SA.equalsIgnoreCase("N/A")){
				Chest chest = null;
				String[] chests = SA.split(",");
				for(int i = 0; i < chests.length; i++){
					String[] coords = chests[i].split("-");
					int w = 0, x = 0, y = 0, z = 0;
					try{
						w = Integer.parseInt(coords[0]);
						x = Integer.parseInt(coords[1]);
						y = Integer.parseInt(coords[2]);
						z = Integer.parseInt(coords[3]);
					}catch(NumberFormatException NFE){
						//whoops no go
						continue;
					}catch(StringIndexOutOfBoundsException SIOOBE){
						//whoops no go
						continue;
					}
					if(!etc.getServer().getWorld(w).getChunk(x, y, z).isLoaded()){
						etc.getServer().getWorld(w).loadChunk(x, y, z);
					}
					Block block = etc.getServer().getWorld(w).getBlockAt(x, y, z);
					if(block.getType() == 54){
						chest = (Chest)etc.getServer().getWorld(w).getOnlyComplexBlock(block);
						if(chest.findAttachedChest() != null){
							DoubleChest dc = chest.findAttachedChest();
							if(hasRoom(dc, ItemID, (int)amount)){
								addItem(dc, ItemID, (int)amount);
							}
						}
						if(hasRoom(chest, ItemID, (int)amount)){
							addItem(chest, ItemID, (int)amount);
							break;
						}
					}
				}
				if(chest == null){
					SA = "N/A";
				}
			}
		}
	}
	
	private boolean hasRoom(Inventory inv, int ID, int amount){
		int room = 0;
		for (int i = 0; i < inv.getContentsSize(); i++){
			Item item = inv.getItemFromSlot(i);
			if (item != null){
				if (item.getItemId() == ID){
					int am = 64 - item.getAmount();
					if (am > 0){
						room += am;
					}
				}
			}
			else{
				room += 64;
			}
		}
		if(room >= amount){
			return true;
		}
		return false;
	}
	
	private void addItem(Inventory inv, int ID, int amount){
		for (int i = 0; i < inv.getContentsSize(); i++){
			if (amount > 0){
				Item item = inv.getItemFromSlot(i);
				if (item != null){
					if (item.getItemId() == ID){
						if(item.getAmount() < 64){
							int ia = item.getAmount();
							item.setAmount(64);
							inv.update();
							amount -= ia;
						}
					}
				}
				else{
					if (amount > 64){
						inv.setSlot(ID, 64, i);
						inv.update();
						amount -= 64;
					}
					else{
						inv.setSlot(ID, amount, i);
						inv.update();
						break;
					}
				}
			}
			else{
				break;
			}
		}
	}
}
