package me.tangledmaze.gorgeousone.mazes;

import java.util.ArrayList;

import java.util.Arrays;
import java.util.HashMap;

import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

import me.tangledmaze.gorgeousone.main.Constants;
import me.tangledmaze.gorgeousone.main.Utils;
import me.tangledmaze.gorgeousone.shapes.Shape;
public class Maze {
	
	private Player p;
	private HashMap<Chunk, ArrayList<Location>> fillChunks, borderChunks;
	private ArrayList<Shape> shapes;
	private int size;
	
	private boolean isVisible;
	
	public Maze(Shape borderShape, Player editor) {
		p = editor;
		
		fillChunks = new HashMap<>();
		borderChunks = new HashMap<>();
		
		shapes = new ArrayList<>();
		shapes.add(borderShape);
		
		add(borderShape);
	}
	
	public int getSize() {
		return size;
	}
	
	public ArrayList<Location> getFill() {
		ArrayList<Location> fill = new ArrayList<>();
		
		for(ArrayList<Location> chunk : fillChunks.values())
			fill.addAll(chunk);
		
		return fill;
	}
	
	public ArrayList<Location> getMainBorder() {
		ArrayList<Location> border = new ArrayList<>();
		
		for(ArrayList<Location> chunk : borderChunks.values())
			border.addAll(chunk);
		
		return border;
	}
	
	@SuppressWarnings("deprecation")
	public void add(Shape s) {
		ArrayList<Chunk> newShapeChunks = new ArrayList<>(),
						 overlappingBorderChunks = new ArrayList<>();

		ArrayList<Location> newBorder  = new ArrayList<>(),
							newFill    = new ArrayList<>();
		
		//check for new border blocks
		//save chunks with new or overlapping border to show/refresh them later
		for(Chunk c : s.getBorder().keySet()) {
			for(Location point : s.getBorder().get(c)) {

				if(!contains(point)) {
					newBorder.add(point);
					
					if(!newShapeChunks.contains(c))
						newShapeChunks.add(c);
				}
				if(borderContains(point) && !overlappingBorderChunks.contains(c))
					overlappingBorderChunks.add(c);
			}
		}

		//return if the shapes is totally covered by the maze
		if(newShapeChunks.isEmpty()) {
			update(overlappingBorderChunks);
			return;
		}

		//check for new fill blocks
		for(Chunk c : s.getFill().keySet())
			for(Location point : s.getFill().get(c)) {
				if(!contains(point)) {
					newFill.add(point);
					
					if(!newShapeChunks.contains(c))
						newShapeChunks.add(c);
				}
			}
	
		//check for existing removable border blocks in the chunks of new shape
		for(Chunk c : newShapeChunks) {
			if(!borderChunks.containsKey(c))
				continue;

			ArrayList<Location> currentChunk = borderChunks.get(c);
			
			for(int i = currentChunk.size()-1; i >= 0; i--) {
				Location point = currentChunk.get(i);
				
				if(s.contains(point) && !s.borderContains(point)) {
					if(isVisible)
						p.sendBlockChange(point, point.getBlock().getType(), point.getBlock().getData());
					currentChunk.remove(point);
				}
			}
		}

		//add all new blocks
		for(Location point : newFill)
			addFill(point);
		for(Location point : newBorder)
			addBorder(point);

		//show the new border and re-show overlapping border parts (they may be hidden with the shape)
		update(newShapeChunks);
		update(overlappingBorderChunks);
	}
	
	@SuppressWarnings("deprecation")
	public void subtract(Shape s) {
		ArrayList<Chunk> newVoidChunks = new ArrayList<>();
		ArrayList<Location> newBorder  = new ArrayList<>();
		
		//get new border points where shape is cutting into maze
		for(Chunk c : s.getBorder().keySet()) {
			for(Location point : s.getBorder().get(c))

				if(contains(point) && !borderContains(point)) {
					newBorder.add(point);

					if(!newVoidChunks.contains(c))
						newVoidChunks.add(c);
				}
		}
		
		if(newVoidChunks.isEmpty())
			return;

		//remove all overlapping fill
		for(Chunk c : s.getFill().keySet())
			for(Location point : s.getFill().get(c))
				
				if(contains(point)) {
					removeFill(point);
					
					if(!newVoidChunks.contains(c))
						newVoidChunks.add(c);
				}

		//remove all maze border inside the shape
		for(Chunk c : newVoidChunks) {
			if(!borderChunks.containsKey(c))
				continue;

			ArrayList<Location> current = borderChunks.get(c);
			
			for(int i = current.size()-1; i >= 0; i--) {
				Location point = current.get(i);
				
				if(s.contains(point) && !s.borderContains(point)) {
					current.remove(point);
					
					if(isVisible)
						p.sendBlockChange(point, point.getBlock().getType(), point.getBlock().getData());
				}
			}
		}
		
		//add all new border blocks
		for(Location point : newBorder)
			addBorder(point);

		//refresh changed chunks
		update(newVoidChunks);
	}
	
	@SuppressWarnings("deprecation")
	public void brush(Block b) {
		Location point = b.getLocation();
		
		if(!borderContains(point) || !isHighlighted(b))
			return;
		
		ArrayList<Location> neighbors = new ArrayList<>();
		ArrayList<Chunk> changedChunks = new ArrayList<>();
		
		boolean isExternalBorder = false,
				isSealing = false;

		//check what kind of border this block is
		//is it at the outside of the shape? does it close the shape somehow?
		for(Vector dir : Utils.getDirs()) {
			Location point2 = point.clone().add(dir);
			
			if(!contains(point2)) {
				isExternalBorder = true;
				
			}else if(!borderContains(point2)) {
				isSealing = true;
				neighbors.add(point2);

				if(!changedChunks.contains(point2.getChunk()))
					changedChunks.add(point2.getChunk());
			}
		}

		//if it is at the outside remove the fill there as well 
		if(isExternalBorder) {
			removeFill(point);
			
			//if it seals the shape not-border, neighbor blocks have to replace it
			if(isSealing)
				for(Location point2 : neighbors) {
					Location surface = Utils.getNearestSurface(point2);
					addBorder(surface);
					
					if(isVisible)
						p.sendBlockChange(surface, Constants.MAZE_BORDER, (byte) 0);
				}
		}

		borderChunks.get(b.getChunk()).remove(point);
	}
	
	private void addFill(Location point) {
		Chunk c = point.getChunk();
		
		if(fillChunks.containsKey(c))
			fillChunks.get(c).add(point);
		else
			fillChunks.put(c, new ArrayList<>(Arrays.asList(point)));

		size++;
	}

	private void addBorder(Location point) {
		Chunk c = point.getChunk();
		
		if(borderChunks.containsKey(c))
			borderChunks.get(c).add(Utils.getNearestSurface(point));
		else
			borderChunks.put(c, new ArrayList<>(Arrays.asList(Utils.getNearestSurface(point))));
	}
	
	private void removeFill(Location point) {
		Chunk c = point.getChunk();
		
		for(Location point2 : fillChunks.get(c))

			if(point2.getBlockX() == point.getBlockX() &&
			   point2.getBlockZ() == point.getBlockZ()) {
				
				fillChunks.get(c).remove(point2);
				size--;
				break;
			}
	}
	
	private void removeBorder(Location point) {
		Chunk c = point.getChunk();
		
		for(Location point2 : borderChunks.get(c))

			if(point2.getBlockX() == point.getBlockX() &&
			   point2.getBlockZ() == point.getBlockZ()) {
				
				borderChunks.get(c).remove(point2);
				break;
			}
	}
	
	public boolean contains(Location point) {
		Chunk c = point.getChunk();
		
		if(!fillChunks.containsKey(c))
			return false;
		
		for(Location point2 : fillChunks.get(c)) {
			if(point2.getBlockX() == point.getBlockX() &&
			   point2.getBlockZ() == point.getBlockZ())
				return true;
		}
		return false;
	}
	
	public boolean borderContains(Location point) {
		Chunk c = point.getChunk();
		
		if(!borderChunks.containsKey(c))
			return false;
		
		for(Location point2 : borderChunks.get(c))
			if(point2.getBlockX() == point.getBlockX() &&
			   point2.getBlockZ() == point.getBlockZ())
				return true;
		
		return false;
	}
	
	public boolean isHighlighted(Block b) {
		Chunk c = b.getChunk();
		
		if(!borderChunks.containsKey(c))
			return false;
		
		for(Location point : borderChunks.get(c))
			if(point.getBlock().equals(b))
				return true;
		
		return false;
	}
	
	public void recalc(Location point) {
		if(!contains(point))
			return;
		
			hide();
		Location newPoint = Utils.getNearestSurface(point);
		
		p.sendMessage("mip m�p m�p mip m�p");
		removeFill(point);
		addFill(newPoint);
		
		if(borderContains(point)) {
			removeBorder(point);
			addBorder(newPoint);
		}
	}
	
	@SuppressWarnings("deprecation")
	public void show() {
		if(isVisible || p == null)
			return;
		
		isVisible = true;

		for(Chunk c : fillChunks.keySet())
			if(borderChunks.containsKey(c))
				for(Location point : borderChunks.get(c))
					p.sendBlockChange(point, Constants.MAZE_BORDER, (byte) 0);
	}
	
	@SuppressWarnings("deprecation")
	public void hide() {
		if(!isVisible || p == null)
			return;
		
		isVisible = false;
		
		for(Chunk c : borderChunks.keySet())
			for(Location point : borderChunks.get(c))
				p.sendBlockChange(point, point.getBlock().getType(), point.getBlock().getData());
	}
	
	@SuppressWarnings("deprecation")
	public void update(ArrayList<Chunk> changedChunks) {
		if(p == null)
			return;
		
		for(Chunk c : changedChunks)
			if(borderChunks.containsKey(c))
				for(Location point : borderChunks.get(c))
					p.sendBlockChange(point, Constants.MAZE_BORDER, (byte) 0);
	}
}