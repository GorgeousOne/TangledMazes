package me.gorgeousone.tangledmaze.listeners;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Effect;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerItemDamageEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;

import me.gorgeousone.tangledmaze.core.Constants;
import me.gorgeousone.tangledmaze.core.TangledMain;
import me.gorgeousone.tangledmaze.selections.SelectionHandler;

public class WandListener implements Listener{
	
	private TangledMain plugin;
	
	public WandListener(TangledMain plugin) {
		this.plugin = plugin;
	}
	
	@EventHandler
	public void onItemDamage(PlayerItemDamageEvent e) {
		if(plugin.isSelectionWand(e.getItem()))
			e.setCancelled(true);
	}
	
	@EventHandler
	public void onBlockClick(PlayerInteractEvent e) {
		
		Action a = e.getAction();
		
		if(a != Action.LEFT_CLICK_BLOCK &&
		   a != Action.RIGHT_CLICK_BLOCK)
			return;
		
		try {
			if(e.getHand() != EquipmentSlot.HAND)
				return;
		} catch (NoSuchMethodError err) {}
		
		
		if(!plugin.isSelectionWand(e.getItem()))
			return;
		
		e.setCancelled(true);
		
		Player p = e.getPlayer();
		ItemStack wand = e.getItem();
		
		if(!p.hasPermission(Constants.buildPerm)) {
			destroyTool(p, wand);
			return;
		}
		
		SelectionHandler.getSelection(p).interact(e.getClickedBlock(), a);
	}
	
	@SuppressWarnings("deprecation")
	private void destroyTool(Player p, ItemStack wand) {
		
		p.getInventory().remove(wand);
		p.sendMessage(ChatColor.GRAY + "" + ChatColor.ITALIC + "It seems like you are unworthy to use such mighty tool... it broke apart.");

		p.damage(0);
		p.getWorld().playEffect(p.getLocation().add(0, 1, 0), Effect.EXPLOSION_HUGE, 0);
		
		if(Bukkit.getVersion().contains("1.8"))
			p.getWorld().playSound(p.getEyeLocation(), Sound.valueOf("ITEM_BREAK"), 1f, 1f);
		else
			p.getWorld().playSound(p.getEyeLocation(), Sound.valueOf("ENTITY_ITEM_BREAK"), 1f, 1f);
	}

}
