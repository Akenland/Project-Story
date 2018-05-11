package com.kylenanakdewa.story.journal;

import com.kylenanakdewa.core.characters.players.PlayerCharacter;
import com.kylenanakdewa.story.quests.objectives.ObjectiveStatusEvent;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEvent;
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

			ItemStack journal = new Journal(PlayerCharacter.getCharacter(event.getPlayer())).getJournalItem();

			if(event.getHand().equals(EquipmentSlot.HAND))
				event.getPlayer().getEquipment().setItemInMainHand(journal);
			else if(event.getHand().equals(EquipmentSlot.OFF_HAND))
				event.getPlayer().getEquipment().setItemInOffHand(journal);

		}

	}


	@EventHandler
	public void onObjectiveStatusChange(ObjectiveStatusEvent event){
		
	}

}