package com.kylenecrowolf.realmsstory;

import org.bukkit.plugin.java.JavaPlugin;

import com.kylenecrowolf.realmsstory.tags.TagCommands;

public final class Main extends JavaPlugin {

	public static JavaPlugin plugin;

	/**
	 * Whether the Citizens plugin is enabled.
	 */
	public static boolean citizensEnabled = false;
	
	@Override
	public void onEnable(){
		plugin = this;

		// Version command
		this.getCommand("realmsstory").setExecutor(new RealmsStoryCommands());

		// Tag command
		this.getCommand("tag").setExecutor(new TagCommands());

		// Citizens NPCs integration
		if(getServer().getPluginManager().getPlugin("Citizens")!=null){
			getLogger().info("Enabling Citizens integration!");
			net.citizensnpcs.api.CitizensAPI.getTraitFactory().registerTrait(net.citizensnpcs.api.trait.TraitInfo.create(com.kylenecrowolf.realmsstory.tags.taggable.TaggedNPC.class));
			citizensEnabled = true;
		}
	}
}
