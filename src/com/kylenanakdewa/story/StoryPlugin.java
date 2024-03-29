package com.kylenanakdewa.story;

import org.bukkit.Bukkit;
import org.bukkit.Particle;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import com.kylenanakdewa.story.journal.JournalCommands;
import com.kylenanakdewa.story.journal.JournalListener;
import com.kylenanakdewa.story.tags.Condition;
import com.kylenanakdewa.story.tags.TagCommands;
import com.kylenanakdewa.story.tags.taggable.TaggedNPC;
import com.kylenanakdewa.story.utils.RealmsStorySentinelIntegration;
import net.citizensnpcs.api.CitizensAPI;

public final class StoryPlugin extends JavaPlugin {

	//@Deprecated
	public static StoryPlugin plugin;

	/**
	 * Whether the Citizens plugin is enabled.
	 */
	public static boolean citizensEnabled = false;
	/**
	 * Whether the Sentinel plugin is enabled.
	 */
	public static boolean sentinelEnabled = false;


	@Override
	public void onEnable(){
		plugin = this;

		// Version command
		this.getCommand("story").setExecutor(new StoryCommands());

		// Tag command
		this.getCommand("tag").setExecutor(new TagCommands());

		// Journal command
		this.getCommand("journal").setExecutor(new JournalCommands());


		// Register listener
		getServer().getPluginManager().registerEvents(new JournalListener(), this);


		// Citizens NPCs integration
		if(getServer().getPluginManager().getPlugin("Citizens")!=null){
			getLogger().info("Enabling Citizens integration!");
			citizensEnabled = true;

			// Register TaggedNPC trait
			net.citizensnpcs.api.CitizensAPI.getTraitFactory().registerTrait(net.citizensnpcs.api.trait.TraitInfo.create(com.kylenanakdewa.story.tags.taggable.TaggedNPC.class));

			// NPC markers
			Bukkit.getScheduler().scheduleSyncRepeatingTask(plugin, () -> {
				for(Player player : Bukkit.getOnlinePlayers()){
					for(Entity entity : player.getNearbyEntities(30, 30, 30)){
						if(CitizensAPI.getNPCRegistry().isNPC(entity)){
							TaggedNPC npcTrait = CitizensAPI.getNPCRegistry().getNPC(entity).getTrait(TaggedNPC.class);
							if(npcTrait!=null && npcTrait.getTag()!=null && npcTrait.getTag().getMarkerConditions()!=null){
								for(Condition c : npcTrait.getTag().getMarkerConditions()){
									if(c.eval(player)){
										player.spawnParticle(Particle.NOTE, entity.getLocation().add(0, 2.75, 0), 1);
										break;
									}
								}
							}
						}
					}
				}
			}, 100, 40);


			// Sentinel integration
			if(getServer().getPluginManager().getPlugin("Sentinel")!=null){
				sentinelEnabled = RealmsStorySentinelIntegration.addIntegration();
			}
		}
	}
}
