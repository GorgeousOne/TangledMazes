package me.tangledmaze.gorgeousone.selections;

import java.util.HashMap;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerItemDamageEvent;
import org.bukkit.event.player.PlayerItemHeldEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;

import me.tangledmaze.gorgeousone.main.TangledMain;
import me.tangledmaze.gorgeousone.mazes.MazeHandler;

public class ToolListener implements Listener {
	
	private SelectionHandler sHandler;
	private MazeHandler mHandler;
	
	private HashMap<Player, Long> times;
	private BukkitRunnable timer;
	private static final int expiration = 10*1000;
	
	public ToolListener() {
		sHandler = TangledMain.getPlugin().getSelectionHandler();
		mHandler = TangledMain.getPlugin().getMazeHandler();
		
		times = new HashMap<>();
		
		timer = new BukkitRunnable() {
			@Override
			public void run() {
				for(Player p : times.keySet())
					
					if(System.currentTimeMillis() - times.get(p) >= expiration) {
						times.remove(p);
								
						if(mHandler.hasMaze(p))
							mHandler.getMaze(p).hide();
						if(sHandler.hasSelection(p))
							sHandler.getSelection(p).hide();
					}
			}
		};
		timer.runTaskTimer(TangledMain.getPlugin(), 0, 1*20);
	}
	
	
	@EventHandler
	public void onSlotSwitch(PlayerItemHeldEvent e) {
		Player p = e.getPlayer();
		Inventory i = p.getInventory();
		
		ItemStack previousItem = i.getItem(e.getPreviousSlot()),
				  newItem      = i.getItem(e.getNewSlot());
		
		if(TangledMain.isSelectionWand(newItem)) {
			times.remove(p);
			
			if(mHandler.hasMaze(p))
				mHandler.getMaze(p).show();
			if(sHandler.hasSelection(p))
				sHandler.getSelection(p).show();
		
		}else if(TangledMain.isSelectionWand(previousItem))			
			times.put(p, System.currentTimeMillis());
	}
	
	@EventHandler
	public void onItemDamage(PlayerItemDamageEvent e) {
		if(TangledMain.isSelectionWand(e.getItem()))
			e.setCancelled(true);
	}
}