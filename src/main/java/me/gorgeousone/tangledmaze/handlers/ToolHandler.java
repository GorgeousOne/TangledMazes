package me.gorgeousone.tangledmaze.handlers;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import me.gorgeousone.tangledmaze.TangledMain;
import me.gorgeousone.tangledmaze.clip.ClipShape;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;

import me.gorgeousone.tangledmaze.data.Constants;
import me.gorgeousone.tangledmaze.tools.ClipTool;
import me.gorgeousone.tangledmaze.tools.Tool;
import org.bukkit.event.block.Action;

/**
 * This class stores tools in relation to players.
 * Listeners and commands can get information about what kind of tool a player is using.
 * There is an unsafe shortcut method for getting clipboards :)
 */
//TODO make a singelton out of this? Create one instance handed around by TangledMain
public class ToolHandler {

	private Map<UUID, Tool> tools = new HashMap<>();

//	public boolean hasClipboard(Player player) {
//		return
//			tools.containsKey(player.getUniqueId()) &&
//			tools.get(player.getUniqueId()) instanceof ClipTool;
//	}
	
	public Tool getTool(Player player) {
		
		if(!player.hasPermission(Constants.BUILD_PERM))
			return null;
		
		UUID uuid = player.getUniqueId();
		
		if(!tools.containsKey(uuid)) {
			//TODO check if the way of storing tools and cliptools can be organized more clearly (maybe exclude cliptools completely from this class)
			ClipTool clipTool = new ClipTool(player, ClipShape.RECTANGLE);
			tools.put(uuid, clipTool);
			TangledMain.clipHandler.setClipTool(player, clipTool);
		}

		return tools.get(player.getUniqueId());
	}
	
	public Collection<Tool> getPlayersTools() {
		return tools.values();
	}
	
//	public ClipTool getClipboard(Player p) {
//		Tool clipboard = tools.get(p.getUniqueId());
//		return clipboard instanceof ClipTool ? (ClipTool) clipboard : null;
//	}
	
	public void setTool(Player p, Tool tool) {
		tools.put(p.getUniqueId(), tool);
		
		if(tool instanceof ClipTool)
			Renderer.registerClip((ClipTool) tool);
	}

//	TODO check how to replace resetToDefaultTool correctly

	public void resetToDefaultTool(Player player) {
		
//		if(hasClipboard(player)) {
//
//			ClipTool clipboard = getClipboard(player);
//			Renderer.hideClipboard(clipboard, true);
			TangledMain.clipHandler.removeClipTool(player);
//
//		}else {
			setTool(player, new ClipTool(player, ClipShape.RECTANGLE));
//		}
	}
	
	public void removeTool(Player player) {

//		if(hasClipboard(player))
//			Renderer.unregisterShape(getClipboard(player));

		tools.remove(player.getUniqueId());
	}

	public void handleToolInteraction(Player player, Block clickedBlock, Action action) {

		Tool tool = getTool(player);

		//TODO handle player interaction in this class, not in the tool classes separately.
		if(TangledMain.clipHandler.hasClipTool(player))
			TangledMain.clipHandler.handleClipInteraction(player, clickedBlock);
		else
			tool.interact(clickedBlock, action);
	}
}