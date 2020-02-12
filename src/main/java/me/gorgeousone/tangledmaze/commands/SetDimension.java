package me.gorgeousone.tangledmaze.commands;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import me.gorgeousone.tangledmaze.commands.framework.argument.ArgType;
import me.gorgeousone.tangledmaze.commands.framework.argument.ArgValue;
import me.gorgeousone.tangledmaze.commands.framework.argument.Argument;
import me.gorgeousone.tangledmaze.commands.framework.command.ArgCommand;
import me.gorgeousone.tangledmaze.maze.Maze;
import me.gorgeousone.tangledmaze.data.Messages;
import me.gorgeousone.tangledmaze.handlers.MazeHandler;
import me.gorgeousone.tangledmaze.maze.MazeDimension;
import me.gorgeousone.tangledmaze.utils.PlaceHolder;

public class SetDimension extends ArgCommand {

	private MazeHandler mazeHandler;

	public SetDimension(MazeCommand mazeCommand, MazeHandler mazeHandler) {
		super("set", null, true, mazeCommand);

		addArg(new Argument("dimension", ArgType.STRING, MazeDimension.getCommandNames()));
		addArg(new Argument("integer", ArgType.INTEGER));

		this.mazeHandler = mazeHandler;
	}
	
	@Override
	protected boolean onCommand(CommandSender sender, ArgValue[] arguments) {
		
		Player player = (Player) sender;
		MazeDimension dimension = MazeDimension.match(arguments[0].getString());
		
		if(dimension == null) {
			Messages.ERROR_INVALID_DIMENSION.sendTo(player, new PlaceHolder("dimension", arguments[0].toString()));
			return false;
		}
		
		int newDimValue = arguments[1].getInt();
		Maze maze = mazeHandler.getMaze(player);
		
		if(maze.getDimension(dimension) != newDimValue) {
			maze.setDimension(dimension, newDimValue);
		
			Messages.MESSAGE_DIMENSION_CHANGED.sendTo(
					player,
					new PlaceHolder("dimension", dimension.toString()), 
					new PlaceHolder("number", newDimValue));
		}
		
		return true;
	}
}