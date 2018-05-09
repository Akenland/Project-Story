package com.kylenanakdewa.story.journal;

import com.kylenanakdewa.core.characters.players.PlayerCharacter;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;

/**
 * JournalListener
 */
public final class JournalListener implements Listener {

	/**
	 * Checks if an item is a Journal.
	 * @return true if item is a journal
	 */
	public static boolean isJournal(ItemStack item){
		return item!=null && item.getType().equals(Material.WRITTEN_BOOK) && item.getItemMeta().getDisplayName().equals(ChatColor.DARK_PURPLE+"Journal");
	}


	public void onJournalOpen(PlayerInteractEvent event){
		ItemStack item = event.getItem();
		if(isJournal(item))
			item = new Journal(PlayerCharacter.getCharacter(event.getPlayer())).getJournalItem();
	}

}