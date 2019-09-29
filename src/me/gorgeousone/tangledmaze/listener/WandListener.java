package me.gorgeousone.tangledmaze.listener;

import org.bukkit.ChatColor;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerItemDamageEvent;
import org.bukkit.event.player.PlayerItemHeldEvent;
import org.bukkit.event.player.PlayerPickupItemEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;

import me.gorgeousone.tangledmaze.core.Maze;
import me.gorgeousone.tangledmaze.data.Constants;
import me.gorgeousone.tangledmaze.handler.MazeHandler;
import me.gorgeousone.tangledmaze.handler.Renderer;
import me.gorgeousone.tangledmaze.handler.ToolHandler;
import me.gorgeousone.tangledmaze.tool.ClippingTool;
import me.gorgeousone.tangledmaze.util.Utils;

@SuppressWarnings("deprecation")
public class WandListener implements Listener{
	
	@EventHandler
	public void onItemDamage(PlayerItemDamageEvent e) {

		if(Utils.isMazeWand(e.getItem())) {
			e.setCancelled(true);
			e.getPlayer().updateInventory();
		}
	}
	
	@EventHandler
	public void onBlockClick(PlayerInteractEvent event) {
		
		Action action = event.getAction();
		
		if(action != Action.LEFT_CLICK_BLOCK &&
		   action != Action.RIGHT_CLICK_BLOCK ||
		   event.getHand() != EquipmentSlot.HAND)
			return;
		
		Player player = event.getPlayer();
		Block block = event.getClickedBlock();
		
		if(Utils.isMazeWand(event.getItem())) {
		
			event.setCancelled(true);
			
			if(!player.hasPermission(Constants.BUILD_PERM)) {
				destroyMazeWand(player, event.getItem());
				return;
			}
			
			ToolHandler.getTool(player).interact(block, action);
		
		}else if(player.hasPermission(Constants.BUILD_PERM)) {
			
			Maze maze = MazeHandler.getMaze(player);

			if(maze.isStarted() && Renderer.isMazeVisible(maze) && maze.getClip().isBorderBlock(block))
				Renderer.hideMaze(maze);
			
			if(!ToolHandler.hasClipboard(player))
				return;
				
			ClippingTool clipboard = ToolHandler.getClipboard(player);
			
			if(Renderer.isClipboardVisible(clipboard) && (clipboard.isVertex(block) || clipboard.getClip().isBorderBlock(block)))
				Renderer.hideClipboard(clipboard, true);
		}
	}
	
	@EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
	public void onSlotSwitch(PlayerItemHeldEvent event) {
		
		Player player = event.getPlayer();
		
		if(!player.hasPermission(Constants.BUILD_PERM))
			return;
		
		ItemStack newItem = player.getInventory().getItem(event.getNewSlot());
		
		if(!Utils.isMazeWand(newItem))
			return;
				
		Maze maze = MazeHandler.getMaze(player);
			
		if(maze.isStarted() && !maze.isConstructed() && !Renderer.isMazeVisible(maze))
			Renderer.displayMaze(MazeHandler.getMaze(player));
		
		if(ToolHandler.hasClipboard(player) && !Renderer.isClipboardVisible(ToolHandler.getClipboard(player)))
			Renderer.displayClipboard(ToolHandler.getClipboard(player));
	}
	
	@EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
	public void onPickUp(PlayerPickupItemEvent e) {

		if(Utils.isMazeWand(e.getItem().getItemStack())) {
			
			Player player = e.getPlayer();
			
			if(MazeHandler.hasMaze(player) && !Renderer.isMazeVisible(MazeHandler.getMaze(player)))
				Renderer.displayMaze(MazeHandler.getMaze(player));
			
			if(ToolHandler.hasClipboard(player) && !Renderer.isClipboardVisible(ToolHandler.getClipboard(player)))
				Renderer.displayClipboard(ToolHandler.getClipboard(player));
		}
	}
	
//	@EventHandler
//	public void onChunkLoad(ChunkLoadEvent e) {
//		Renderer.updateChunk(e.getChunk());
//	}
	
	private void destroyMazeWand(Player p, ItemStack wand) {
		
		p.getInventory().remove(wand);
		p.sendMessage(ChatColor.GRAY + "" + ChatColor.ITALIC + "It seems like you are unworthy to use such mighty tool, it broke apart.");
		p.damage(0);
		
		p.getWorld().playSound(p.getEyeLocation(), Sound.ENTITY_ITEM_BREAK, 1f, 1f);
		p.getWorld().spawnParticle(Particle.EXPLOSION_HUGE, p.getLocation(), 1);
	}
}