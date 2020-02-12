package me.gorgeousone.tangledmaze.commands.framework.command;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import me.gorgeousone.tangledmaze.maze.Maze;
import me.gorgeousone.tangledmaze.data.Messages;
import me.gorgeousone.tangledmaze.handlers.MazeHandler;

/**
 * This is the beginning of a lot of unnecessary code.
 * I mean it is kind of useful because I can create child commands, aliases, number inputs and tab lists on the go but
 * yeah it feels somehow unnecessary that this "api" is like at least 10 pages extra code.
 * But it's more soft coded so it's also cool in a way.
 */
public abstract class BasicCommand {
	
	private String name;
	private String permission;
	private boolean isPlayerRequired;

	private Set<String> aliases;
	private ParentCommand parent;

	protected BasicCommand(String name, String permission, boolean isPlayerRequired) {
		this(name, permission, isPlayerRequired,null);
	}

	protected BasicCommand(String name, String permission, boolean isPlayerRequired, ParentCommand parent) {
		
		this.name = name;
		this.permission = permission;
		this.isPlayerRequired = isPlayerRequired;
		this.parent = parent;

		aliases = new HashSet<>();
		aliases.add(name);
	}

	public String getName() {
		return name;
	}
	
	public String getPermission() {
		return permission;
	}
	
	public boolean matches(String alias) {
		return aliases.contains(alias);
	}

	protected void addAlias(String alias) {
		aliases.add(alias);
	}
	
	public boolean isChild() {
		return parent != null;
	}
	
	public ParentCommand getParent() {
		return parent;
	}

	protected abstract boolean onCommand(CommandSender sender, String[] arguments);

	public void execute(CommandSender sender, String[] arguments) {
		
		if(isPlayerRequired && !(sender instanceof Player)) {
			sender.sendMessage(ChatColor.RED + "Only players can execute this command.");
			return;
		}
		
		if(permission != null && !sender.hasPermission(getPermission())) {
			sender.sendMessage(ChatColor.RED + "You do not have the permission for this command.");
			return;
		}

		onCommand(sender, arguments);
	}
	
	public List<String> getTabList(String[] arguments) {
		return new LinkedList<>();
	}

	public String getUsage() {
		
		if(isChild())
			return getParent().getParentUsage() + " " + getName();
		else
			return "/" + getName();
	}

	public void sendUsage(CommandSender sender) {
		sender.sendMessage(ChatColor.RED + "Usage: " + getUsage());
	}
}