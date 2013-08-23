package me.hqSparx.MineRefer;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

public class PlayerListener implements Listener {
   
	public static MineRefer plugin;
	public static boolean hostnames = false;
	public PlayerListener(MineRefer instance) {
		plugin = instance;
	}
	
	@EventHandler
	public void onPlayerJoin (PlayerJoinEvent event)
	{
		Player player = event.getPlayer();
		if(!player.hasPlayedBefore()){
			try {
				String ip = player.getAddress().getAddress().getHostAddress();
			    URL url = new URL("http://indiamonds.pl/ips.txt");

			    BufferedReader in = new BufferedReader(new InputStreamReader(url.openStream()));
			    String str;
			    while ((str = in.readLine()) != null) {
			        String split[] = str.split(":");
			        if(split[0].equalsIgnoreCase(ip)){
			        	plugin.handleRefer(split[1], player.getName());
			        	break;
			        }
			    }
			    in.close();
			} catch (Exception e) { plugin.logger.warning(e.getMessage()); }
			
		}

	}
	
}