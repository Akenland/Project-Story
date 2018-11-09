package com.kylenanakdewa.story.journal;

import com.kylenanakdewa.core.characters.players.PlayerCharacter;
import com.kylenanakdewa.core.common.prompts.PromptActionEvent;
import com.kylenanakdewa.story.quests.objectives.GoToLocationObjective;
import com.kylenanakdewa.story.quests.objectives.Objective;
import com.kylenanakdewa.story.quests.objectives.ObjectiveStatusEvent;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BookMeta;

/**
 * JournalListener
 */
public final class JournalListener implements Listener {

	/**
	 * Checks if an item is a Journal.
	 * @return true if item is a journal
	 */
	public static boolean isJournal(ItemStack item){
		return item!=null && item.getType().equals(Material.WRITTEN_BOOK) && ((BookMeta)item.getItemMeta()).getTitle().equals(ChatColor.DARK_PURPLE+"Journal");
	}


	@EventHandler
	public void onJournalOpen(PlayerInteractEvent event){
		ItemStack item = event.getItem();
		if(isJournal(item)){

			ItemStack journal = Journal.get(PlayerCharacter.getCharacter(event.getPlayer())).getJournalItem();

			if(event.getHand().equals(EquipmentSlot.HAND))
				event.getPlayer().getEquipment().setItemInMainHand(journal);
			else if(event.getHand().equals(EquipmentSlot.OFF_HAND))
				event.getPlayer().getEquipment().setItemInOffHand(journal);

		}

	}


	@EventHandler(priority = EventPriority.LOW)
	public void onServerEmpty(PlayerQuitEvent event){
		if(Bukkit.getOnlinePlayers().isEmpty()) Journal.updateJournals();
	}


	@EventHandler
	public void onObjectiveStatusChange(ObjectiveStatusEvent event){
		Journal.playerJournals.values().forEach(journal -> {
			journal.objectiveStatusUpdate(event);
		});
	}


	@EventHandler
	public void onPromptAction(PromptActionEvent event){
		if(event.isType("completeObjective")){
			Journal journal = Journal.get(PlayerCharacter.getCharacter(event.getPlayer()));
			Objective objective = journal.getActiveObjective(event.getAction());
			if(objective!=null) journal.completeObjective(objective);
		}
	}


	@EventHandler
	public void onPlayerLocation(PlayerMoveEvent event){
		if(event.getFrom().distanceSquared(event.getTo()) < 0.02) return;

		PlayerCharacter player = PlayerCharacter.getCharacter(event.getPlayer());

		for(Objective objective : Journal.get(player).getActiveTypeObjectives("gotoloc")){
			if(!(objective instanceof GoToLocationObjective)) objective = Objective.loadObjective(objective.getIdentifier());
			if(((GoToLocationObjective)objective).isWithinLocation(event.getTo())){
				objective.setCompleted();
			}
		}
	}
}