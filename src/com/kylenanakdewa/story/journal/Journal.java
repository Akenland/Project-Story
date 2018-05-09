package com.kylenanakdewa.story.journal;

import java.util.HashSet;
import java.util.Set;

import com.kylenanakdewa.core.characters.Character;
import com.kylenanakdewa.story.quests.objectives.Objective;
import com.kylenanakdewa.story.utils.Book;

import org.bukkit.ChatColor;
import org.bukkit.inventory.ItemStack;

/**
 * A journal for a Character, containing objectives that they have yet to complete.
 * @author Kyle Nanakdewa
 */
public class Journal {

	/** The character that this journal is for. */
	private final Character character;

	/** The objectives in this journal. */
	private final Set<Objective> objectives;


	/**
	 * Gets a character's journal.
	 * @param character the character to get a journal for
	 */
	public Journal(Character character){
		this.character = character;
		objectives = new HashSet<Objective>();
	}


	/**
	 * Gets the journal item.
	 */
	public ItemStack getJournalItem(){
		Book book = new Book(ChatColor.DARK_PURPLE+"Journal", character.getName());
		for(Objective objective : objectives){
			book.addSimplePage(objective.getDescription());
		}
		book.addSimplePage(ChatColor.DARK_GRAY+"Talk to NPCs, read books, and explore the world, to find more things to do.");
		return book.getItem();
	}
}