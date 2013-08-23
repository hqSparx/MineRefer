package me.hqSparx.MineRefer;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.PluginDescriptionFile;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

public class MineRefer extends JavaPlugin {

	public static MineRefer plugin;
	public static int creditsPerPlayer = 100;
	public final Logger logger = Logger.getLogger("Minecraft");
	public final PlayerListener listener = new PlayerListener(this);

	public static List<Referer> referers = new ArrayList<Referer>(1024);
	
	public void onDisable() {
		PluginDescriptionFile pdfFile = this.getDescription();
		this.logger.info(pdfFile.getName() + " is now disabled.");
	}
	
	public void onEnable() {
		PluginManager pm = getServer().getPluginManager();
	    pm.registerEvents(this.listener, this);
		PluginDescriptionFile pdfFile = this.getDescription();
		this.logger.info(pdfFile.getName() + " version " + pdfFile.getVersion() + " is now enabled.");
		loadReferers();
	}
	

    public void give(Player player, int item, int type, int amount) {
    	HashMap<Integer, ItemStack> leftover = new HashMap<Integer, ItemStack>();
        if (type == -1) {
        	leftover = player.getInventory().addItem(new ItemStack(item, amount));
        } else {
        	leftover = player.getInventory().addItem(new ItemStack(item, amount, (byte) type));
        }
        //drop leftover
        if(!leftover.isEmpty()){
        	Iterator it = leftover.entrySet().iterator();
            while (it.hasNext()) {
                Map.Entry pairs = (Map.Entry)it.next();
                ItemStack stack = (ItemStack) pairs.getValue();
                player.getWorld().dropItemNaturally(player.getLocation(), stack);
                it.remove(); // avoids a ConcurrentModificationException
            }
        }
    }

    public void reward(Player player, int price, int id, int data){
    	Referer referer = findReferer(player.getName());
    	if(referer == null) return;
    	
    	if(referer.credits < price){
    		player.sendMessage(colorizeString("&bNiewystarczajaca ilosc kredytow!"));
    		return;
    	}
    	
    	referer.credits -= price;
    	saveReferers();
    	give(player, id, data, 1);
    	log(player.getName() + " bought " + id + ":" + data + " for " + price + " credits.");
    	player.sendMessage(colorizeString("&aNagroda przyznana. &7Pozostala ilosc kredytow: &a" + referer.credits));
    }
    
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
		//sender, cmd = link/stats/reward
		String command = cmd.getLabel();
		String nickname = sender.getName();
		if(command.equalsIgnoreCase("link")){
			sender.sendMessage(colorizeString("&7Twoj link do zapraszania graczy:"));
			sender.sendMessage(colorizeString("&ahttp://indiamonds.pl/?ref=" + nickname));
			return true;
		}
		if(command.equalsIgnoreCase("stats")){
			sender.sendMessage(colorizeString("&7Twoj link do zapraszania graczy:"));
			sender.sendMessage(colorizeString("&ahttp://indiamonds.pl/?ref=" + nickname));
			Referer ref = findReferer(nickname);
			if(ref == null){
				sender.sendMessage(colorizeString("&bZrekrutowani gracze: &c" + 0 +
						". &bKredyty: &c" + 0 + "."));
				sender.sendMessage(colorizeString("&7Aby wymienic kredyty na nagrode, wpisz &a/reward"));
				return true;
			}
			else{
			sender.sendMessage(colorizeString("&7Zrekrutowani gracze: &a" + ref.players.size() +
					". &7Kredyty: &a" + ref.credits + "."));
			for(int i = 0; i < ref.players.size(); i++)
				sender.sendMessage(colorizeString("&7#" + (i+1) + " &f" + ref.players.get(i)));
			}
			sender.sendMessage(colorizeString("&7Aby wymienic kredyty na nagrode, wpisz &a/reward"));
		}
		if(command.equalsIgnoreCase("reward")){
			if(args.length == 0) {
				sender.sendMessage(colorizeString("&aHV Solar Array &7- 300 kredytow - &a/reward hv"));
				sender.sendMessage(colorizeString("&aMV Solar Array &7- 50 kredytow - &a/reward mv"));
				sender.sendMessage(colorizeString("&aAdjustable Electric Engine &7- 30 kredytow - &a/reward adjust"));
				sender.sendMessage(colorizeString("&aMFSU &7- 25 kredytow - &a/reward mfsu"));
				sender.sendMessage(colorizeString("&aLV Solar Array &7- 8 kredytow - &a/reward lv"));
				sender.sendMessage(colorizeString("&aSlimeball &7- 4 kredyty - &a/reward slime"));
				sender.sendMessage(colorizeString("&aEnder Pearl &7- 2 kredyty - &a/reward perl"));
			}
			else {
				String arg = args[0];
				for(int i = 1; i < args.length; i++){
					arg += " " + args[i];
				}
				
				if(arg.equalsIgnoreCase("hv")) reward((Player)sender, 300, 183, 2);
				else if(arg.equalsIgnoreCase("mv")) reward((Player)sender, 50, 183, 1);
				else if(arg.equalsIgnoreCase("adjust")) reward((Player)sender, 30, 200, 3);
				else if(arg.equalsIgnoreCase("mfsu")) reward((Player)sender, 25, 227, 2);
				else if(arg.equalsIgnoreCase("lv")) reward((Player)sender, 8, 183, -1);
				else if(arg.equalsIgnoreCase("slime")) reward((Player)sender, 4, 341, -1);
				else if(arg.equalsIgnoreCase("perl")) reward((Player)sender, 2, 368, -1);
				else sender.sendMessage(colorizeString("&bNie rozpoznano nagrody"));
			}
		}
		
		return false;
	}
	
	public Referer findReferer(String nick){
		for(int i = 0; i < referers.size(); i++){
			if(referers.get(i).nickname.equalsIgnoreCase(nick)) return referers.get(i);
		}
		return null;
	}
	
	public boolean handleRefer(String referer, String nick){
		int size = referers.size();
		String msg = colorizeString("&dZaproszony przez " + referer + ". Nagroda przyznana.");
		this.getServer().broadcastMessage(msg);
		
		Referer ref = findReferer(referer);
		if(ref != null) {
				ref.addPlayer(nick);
				ref.credits += creditsPerPlayer;
				saveReferers();
				return true;
		}
		referers.add(new Referer(referer, creditsPerPlayer));
		referers.get(size).addPlayer(nick);
		saveReferers();
		return true;
	}
	
	public void log(String text){
		try{
    		String filename = this.getDataFolder() + "/log.txt";
    	    FileWriter fw = new FileWriter(filename,true); //the true will append the new data
    	    fw.write(text + "\n");//appends the string to the file
    	    fw.close();
    	} catch(IOException e) { e.printStackTrace(); }
	}
	
	public void saveReferers() {
    	File ReferersFile = new File(this.getDataFolder() + "/referers.txt");
    	if(!this.getDataFolder().exists()) this.getDataFolder().mkdir();
    	
    	try {
    		BufferedWriter output =  new BufferedWriter(new FileWriter(ReferersFile));
    		try {
    			List<String> written = new ArrayList<String>(1024);
    			for (int i = 0; i < referers.size(); i++) {
    				boolean dont = false;
    				for (int j = 0; j < written.size(); j++) {
		    		   if (written.get(j).contentEquals(referers.get(i).nickname)) dont = true;
    				}
					if(!dont) {
					output.write(referers.get(i).toString() + "\n");
					written.add(referers.get(i).nickname);
					}
    			}
    		} finally {
    			output.close();
		    }
    	} catch (Exception e) {}
    }
	
	public void loadReferers() {
    	File ReferersFile = new File(this.getDataFolder() + "/referers.txt");

    	try {
    		BufferedReader input =  new BufferedReader(new FileReader(ReferersFile));
	    	try {
		        String line;
		        while ((line = input.readLine()) != null) {
		        	line = line.trim();
			        if (line.length() > 0) {
			        	try {
			        	load(line);
			        	} catch(Exception e) {}
			        }
			     }
	    	 } finally {
	    		 input.close();
	    	 		}
    			} catch (Exception e) { logger.info("[MineRefer] Cant load referers.txt");
	    	}
	    }
	
	public void load(String line) {
			int i = referers.size();
			String[] params = line.split(":");
			try{
				referers.add(new Referer(params[0], Integer.parseInt(params[1])));
				if(params[2] != null){
					String[] players = params[2].split("\\.");
					for(int j = 0; j < players.length; j++) 
						referers.get(i).addPlayer(players[j]);
		   		}
			} catch (Exception e) {}
		}
	
	public static String colorizeString(String toColor) {
        if (toColor != null)
            return toColor.replaceAll("&([0-9a-f])", "\u00A7$1");
        else
            return "";
    }

}
