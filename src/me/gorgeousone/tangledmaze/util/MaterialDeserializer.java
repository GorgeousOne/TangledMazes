package me.gorgeousone.tangledmaze.util;


import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.material.MaterialData;

public class MaterialDeserializer {

	@SuppressWarnings("deprecation")
	public static MaterialData deserializeMaterialData(String materialData) {
		
		Material type;
		byte data;
		String typeString, dataString;
		
		if(materialData.contains(":")) {
			typeString = materialData.split(":")[0];
			dataString = materialData.split(":")[1];
			
		}else {
			typeString = materialData;
			dataString = "0";
		}
		
		if(Bukkit.getServer().getVersion().contains("1.13")) {
			type = Material.valueOf(typeString.toUpperCase());
		
		}else {
			type = ReflectionMaterials.getMaterial(typeString);
		}
		
		if(type == null || type == Material.AIR && !typeString.equalsIgnoreCase("AIR")) {
			throw new IllegalArgumentException(ChatColor.RED + "\"" + typeString + "\" does not match any block type.");
		}
		
		try {
			data = Byte.parseByte(dataString);
		} catch (Exception e) {
			throw new IllegalArgumentException(ChatColor.RED + "\"" + dataString + "\" is not a valid number.");
		}
		
		return new MaterialData(type, data);
	}
}