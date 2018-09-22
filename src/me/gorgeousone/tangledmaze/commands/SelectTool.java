package me.gorgeousone.tangledmaze.commands;

import org.bukkit.entity.Player;

import me.gorgeousone.tangledmaze.mazes.MazeHandler;
import me.gorgeousone.tangledmaze.selections.*;
import me.gorgeousone.tangledmaze.shapes.*;
import me.gorgeousone.tangledmaze.utils.Constants;

public class SelectTool {

	public void execute(Player p, String selectionType) {
		
		if(!p.hasPermission(Constants.buildPerm)) {
			p.sendMessage(Constants.insufficientPerms);
			return;
		}
		
		switch (selectionType.toLowerCase()) {
		case "rectangle":
		case "rect":
		case "square":
			
			if(SelectionHandler.hasShapeSel(p)) {
				ShapeSelection selection = SelectionHandler.getShapeSel(p);
				
				if(selection.getType() instanceof Rectangle)
					break;
				else
					selection.setType(Shape.RECT);
			
			}else 
				SelectionHandler.setSelection(p, new ShapeSelection(p, Shape.RECT));
			
			p.sendMessage(Constants.prefix + "Changed selection type to rectangular.");
			break;
			
		case "ellipse":
		case "circle":
			if(SelectionHandler.hasShapeSel(p)) {
				ShapeSelection selection = SelectionHandler.getShapeSel(p);
				
				if(selection.getType() instanceof Ellipse)
					break;
				else
					selection.setType(Shape.ELLIPSE);
			
			}else 
				SelectionHandler.setSelection(p, new ShapeSelection(p, Shape.ELLIPSE));
			
			p.sendMessage(Constants.prefix + "Changed selection type to elliptical.");

			break;
		
		case "brush":
			
			if(!MazeHandler.getMaze(p).isStarted()) {
				p.sendMessage(Constants.prefix + "The brush tool can only be used on mazes.");
				p.sendMessage("/tangledmaze start");
				return;
			
			}else if(SelectionHandler.hasShapeSel(p))
					SelectionHandler.getShapeSel(p).reset();
			
			if(SelectionHandler.getSelection(p) instanceof BrushSelection)
				break;
			
			SelectionHandler.setSelection(p, new BrushSelection(p));
			p.sendMessage(Constants.prefix + "Changed selection type to brush.");
			
			break;
			
		case "exit":
			
			if(!MazeHandler.getMaze(p).isStarted()) {
				p.sendMessage(Constants.prefix + "The brush tool can only be used on mazes.");
				p.sendMessage("/tangledmaze start");
				return;
			}
			
			if(SelectionHandler.getSelection(p) instanceof ExitSetter)
				break;
			
			if(MazeHandler.getMaze(p).isStarted()) {
				
				if(SelectionHandler.hasShapeSel(p))
					SelectionHandler.getShapeSel(p).reset();
				
				SelectionHandler.setSelection(p, new ExitSetter(p));
				p.sendMessage(Constants.prefix + "Changed selection type to exit setter.");
			
			}else {
				p.sendMessage(Constants.prefix + "The exit setting tool can only be used on mazes.");
				p.sendMessage("/tangledmaze start");
			}
			break;
			
		default:
			p.sendMessage("/tangledmaze help 5");
			break;
		}
	}
}