package me.gorgeousone.tangledmaze.handler;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

import me.gorgeousone.tangledmaze.command.HelpCommand;
import me.gorgeousone.tangledmaze.command.MazeCommand;
import me.gorgeousone.tangledmaze.data.Constants;
import me.gorgeousone.tangledmaze.data.Messages;

public class CommandHandler implements CommandExecutor {

	private List<MazeCommand> mazeCommands;

	public CommandHandler() {
		mazeCommands = new ArrayList<>();
	}
	
	@Override
	public boolean onCommand(CommandSender sender, Command command, String label, String[] arguments) {
		
		if(!sender.hasPermission(Constants.BUILD_PERM))
			Messages.ERROR_NO_BUILD_PERMISSION.send(sender);
		
		if(arguments.length < 1) {
			
			HelpCommand.sendHelpPage(sender, 1);
			return true;
		}
		
		String subCommandName = arguments[0];
		
		for(MazeCommand mazeCommand : mazeCommands) {
			
			if(mazeCommand.getName().equalsIgnoreCase(subCommandName)) {
				mazeCommand.execute(sender, getSubArguents(arguments));
				return true;
			}
		}
		
		return false;
	}
	
	public List<MazeCommand> getCommands() {
		return mazeCommands;
	}

	public void registerCommand(MazeCommand command) {
		mazeCommands.add(command);
	}
	
	private String[] getSubArguents(String[] arguments) {
		
		if(arguments.length < 2)
			return new String[] {};
		
		return Arrays.copyOfRange(arguments, 1, arguments.length);
	}
}