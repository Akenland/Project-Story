package com.kylenanakdewa.story.journal;

import com.kylenanakdewa.core.characters.players.PlayerCharacter;
import com.kylenanakdewa.core.common.Utils;

import org.bukkit.ChatColor;
import org.bukkit.Material;
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


	public void onJournalOpen(PlayerInteractEvent event){
		ItemStack item = event.getItem();
		if(isJournal(item)){

			Utils.notifyAdmins("Found valid journal");

			ItemStack journal = new Journal(PlayerCharacter.getCharacter(event.getPlayer())).getJournalItem();

			if(event.getHand().equals(EquipmentSlot.HAND))
				event.getPlayer().getEquipment().setItemInMainHand(journal);
			else if(event.getHand().equals(EquipmentSlot.OFF_HAND))
				event.getPlayer().getEquipment().setItemInOffHand(journal);

		}

	}

}