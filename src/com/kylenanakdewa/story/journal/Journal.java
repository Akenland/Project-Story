package com.kylenanakdewa.story.journal;

import java.util.HashSet;
import java.util.Set;

import com.kylenanakdewa.core.characters.Character;
import com.kylenanakdewa.story.quests.objectives.DummyObjective;
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

	/** The active objectives in this journal. */
	private final Set<Objective> activeObjectives;
	/** The discovered objectives in this journal. */
	private final Set<Objective> discoveredObjectives;
	/** The completed objectives in this journal. */
	private final Set<Objective> completedObjectives;


	/**
	 * Gets a character's journal.
	 * @param character the character to get a journal for
	 */
	public Journal(Character character){
		this.character = character;
		activeObjectives = new HashSet<Objective>();
		discoveredObjectives = new HashSet<Objective>();
		completedObjectives = new HashSet<Objective>();

		activeObjectives.add(new DummyObjective("Talk to NPCs to find things to do"));
		activeObjectives.add(new DummyObjective("Explore the world"));

		discoveredObjectives.add(new DummyObjective("Learn about the Aunix"));
		discoveredObjectives.add(new DummyObjective("Talk to the Waramon researcher"));

		completedObjectives.add(new DummyObjective("View your journal"));
		completedObjectives.add(new DummyObjective("Talk to the Greeter"));
	}


	/**
	 * Gets the journal item.
	 */
	public ItemStack getJournalItem(){
		Book book = new Book(ChatColor.DARK_PURPLE+"Journal", character.getName());

		String activePage = ChatColor.DARK_GREEN+"Active Objectives:\n";
		for(Objective objective : activeObjectives){
			activePage += "- "+objective.getDescription()+"\n";
		}
		book.addSimplePage(activePage);

		String discoveredPage = ChatColor.DARK_PURPLE+"Discoveries:\n";
		for(Objective objective : discoveredObjectives){
			discoveredPage += "- "+objective.getDescription()+"\n";
		}
		book.addSimplePage(discoveredPage);

		String completedPage = ChatColor.DARK_RED+"Completed Objectives:\n";
		for(Objective objective : completedObjectives){
			completedPage += "- "+objective.getDescription()+"\n";
		}
		book.addSimplePage(completedPage);
		//book.addSimplePage(ChatColor.DARK_GRAY+"Talk to NPCs, read books, and explore the world, to find more things to do.");
		return book.getItem();
	}
}