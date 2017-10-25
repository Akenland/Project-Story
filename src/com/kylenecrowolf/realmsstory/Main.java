package com.kylenecrowolf.realmsstory;

import org.bukkit.Bukkit;
import org.bukkit.Particle;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import com.kylenecrowolf.realmsstory.tags.Condition;
import com.kylenecrowolf.realmsstory.tags.TagCommands;
import com.kylenecrowolf.realmsstory.tags.taggable.TaggedNPC;
import net.citizensnpcs.api.CitizensAPI;

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

			// NPC markers
			Bukkit.getScheduler().scheduleSyncRepeatingTask(plugin, () -> {
				for(Player player : Bukkit.getOnlinePlayers()){
					for(Entity entity : player.getNearbyEntities(30, 30, 30)){
						if(CitizensAPI.getNPCRegistry().isNPC(entity)){
							TaggedNPC npcTrait = CitizensAPI.getNPCRegistry().getNPC(entity).getTrait(TaggedNPC.class);
							if(npcTrait!=null && npcTrait.getTag()!=null && npcTrait.getTag().getMarkerConditions()!=null){
								for(Condition c : 
									npcTrait
										.getTag()
											.getMarkerConditions()){
									if(c.eval(player)){
										player.spawnParticle(Particle.NOTE, entity.getLocation().add(0, 3, 0), 1);
										break;
									}
								}
							}
						}
					}
				}
			}, 100, 60);
		}
	}
}
