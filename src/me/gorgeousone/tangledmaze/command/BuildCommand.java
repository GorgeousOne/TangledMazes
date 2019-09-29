package me.gorgeousone.tangledmaze.command;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import me.gorgeousone.tangledmaze.generation.WallGenerator;
import me.gorgeousone.tangledmaze.generation.FloorGenerator;
import me.gorgeousone.tangledmaze.generation.PathGenerator;

import org.bukkit.Material;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import me.gorgeousone.tangledmaze.command.api.argument.ArgType;
import me.gorgeousone.tangledmaze.command.api.argument.ArgValue;
import me.gorgeousone.tangledmaze.command.api.argument.Argument;
import me.gorgeousone.tangledmaze.command.api.command.ArgCommand;
import me.gorgeousone.tangledmaze.core.Maze;
import me.gorgeousone.tangledmaze.data.Messages;
import me.gorgeousone.tangledmaze.handler.BuildHandler;
import me.gorgeousone.tangledmaze.handler.Renderer;
import me.gorgeousone.tangledmaze.handler.ToolHandler;
import me.gorgeousone.tangledmaze.mapmaking.TerrainEditor;
import me.gorgeousone.tangledmaze.util.PlaceHolder;
import me.gorgeousone.tangledmaze.util.TextException;

public class BuildCommand extends ArgCommand {

	private PathGenerator pathGenerator;
	private TerrainEditor terrainEditor;
	private WallGenerator wallGenerator;
	private FloorGenerator floorGenerator;
	
	public BuildCommand(MazeCommand mazeCommand) {
		super("build", null, mazeCommand);
		
		addArg(new Argument("part", ArgType.STRING, "maze", "floor", "ceiling"));
		addArg(new Argument("blocks...", ArgType.STRING));

		pathGenerator = new PathGenerator();
		terrainEditor = new TerrainEditor();
		wallGenerator = new WallGenerator();
		floorGenerator = new FloorGenerator();
	}

	@Override
	protected boolean onExecute(CommandSender sender, ArgValue[] args) {
		
		Player player = (Player) sender;
		Maze maze = getStartedMaze(player, true, false);
		
		if(maze == null)
			return false;
		
		String mazePart = args[0].getString();
		
		List<Material> blockMaterials;
		
		try {
			blockMaterials = getWallMaterials(Arrays.copyOfRange(args, 1, args.length));
			
		} catch (TextException ex) {
			ex.sendTextTo(player);
			return false;
		}

		switch (mazePart) {
		
		case "floor":
			
			if(!maze.isConstructed()) {
				Messages.ERROR_MAZE_NOT_BUILT.sendTo(player);
				return false;
			}
		
			buildFloor(maze, blockMaterials);
			break;
		
		case "ceiling":

			if(!maze.isConstructed()) {
				Messages.ERROR_MAZE_NOT_BUILT.sendTo(player);
				return false;
			}
			
			break;
			
		case "maze":
			
			if(maze.isConstructed()) {
				Messages.ERROR_MAZE_ALREADY_BUILT.sendTo(player);
				return false;
			}
			
			buildMaze(maze, blockMaterials);
			break;

		default:
			Messages.ERROR_INVALID_MAZE_PART.sendTo(player, new PlaceHolder("mazepart", mazePart));
			break;
		}
		
		return true;
	}
	
	private void buildMaze(Maze maze, List<Material> blockMaterials) {
		
		Renderer.hideMaze(maze);
		BuildHandler.buildMaze(maze, blockMaterials, pathGenerator, terrainEditor, wallGenerator);
		
		Messages.MESSAGE_MAZE_BUILDING_STARTED.sendTo(maze.getPlayer());
		ToolHandler.resetToDefaultTool(maze.getPlayer());
	}

	private void buildFloor(Maze maze, List<Material> blockMaterials) {
		
		BuildHandler.buildMazeFloor(maze, blockMaterials, floorGenerator);
	}

	private List<Material> getWallMaterials(ArgValue[] serializedMaterials) throws TextException {
		
		List<Material> wallMaterials = new ArrayList<>();
		
		for(ArgValue materialValue : serializedMaterials) {
			
			String materialString = materialValue.getString();
			Material material = Material.matchMaterial(materialString);
			
			if(material == null || !material.isBlock())
				throw new TextException(Messages.ERROR_NO_MATCHING_BLOCK_TYPE, new PlaceHolder("block", materialString));
			else
				wallMaterials.add(material);
		}
		
		return wallMaterials;
	}
}