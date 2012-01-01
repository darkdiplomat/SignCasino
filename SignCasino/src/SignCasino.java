import java.util.logging.Logger;

public class SignCasino extends Plugin{
	Logger log = Logger.getLogger("Minecraft");
	String name = "SignCasino";
	String version = "1.0.1";
	String author = "darkdiplomat";	
	
	SCData SCD;
	SCCraps SCC;
	SCSlotMachine SCSM;
	SCBlackJack SCBJ;
	SCBaccarat SCB;
	SCListener SCL;
	
	
	public void enable(){
		this.
		
		log.info("[SignCasino] Version "+version+" by darkdiplomat enabled!");
	}
	
	public void disable(){
		if(SCD.dCo){
			etc.getInstance().removeCommand("/scshare");
		}
		else if(SCD.item){
			etc.getInstance().removeCommand("/scchest");
		}
		log.info("[SignCasino] Version "+version+" disabled!");
	}
	
	public void initialize(){
		SCD = new SCData(this);
		SCSM = new SCSlotMachine(this);
		SCC = new SCCraps(this);
		SCBJ = new SCBlackJack(this);
		SCB = new SCBaccarat(this);
		SCL = new SCListener(this);
		if(SCD.dCo){
			etc.getInstance().addCommand("/scshare", " <jointaccountname> - then left click sign to set shared account");
		}
		else if(SCD.item){
			etc.getInstance().addCommand("/scchest", " left click sign then left click a chest set a chest for the sign");
		}
		etc.getLoader().addListener(PluginLoader.Hook.COMMAND, SCL, this, PluginListener.Priority.MEDIUM);
		etc.getLoader().addListener(PluginLoader.Hook.BLOCK_RIGHTCLICKED, SCL, this, PluginListener.Priority.MEDIUM);
		etc.getLoader().addListener(PluginLoader.Hook.SIGN_CHANGE, SCL, this, PluginListener.Priority.MEDIUM);
		etc.getLoader().addListener(PluginLoader.Hook.BLOCK_DESTROYED, SCL, this, PluginListener.Priority.MEDIUM);
		etc.getLoader().addListener(PluginLoader.Hook.BLOCK_BROKEN, SCL, this, PluginListener.Priority.MEDIUM);
		etc.getLoader().addListener(PluginLoader.Hook.PLAYER_MOVE, SCL, this, PluginListener.Priority.MEDIUM);
	}
}
