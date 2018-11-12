package com.kylenanakdewa.story.journal;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import com.kylenanakdewa.core.characters.players.PlayerCharacter;
import com.kylenanakdewa.core.common.Utils;
import com.kylenanakdewa.core.common.savedata.PlayerSaveDataSection;
import com.kylenanakdewa.story.StoryPlugin;
import com.kylenanakdewa.story.quests.objectives.CompoundObjective;
import com.kylenanakdewa.story.quests.objectives.NPCTalkObjective;
import com.kylenanakdewa.story.quests.objectives.Objective;
import com.kylenanakdewa.story.quests.objectives.ObjectiveStatusEvent;
import com.kylenanakdewa.story.quests.objectives.Quest;
import com.kylenanakdewa.story.quests.objectives.Objective.Status;
import com.kylenanakdewa.story.tags.Interaction;
import com.kylenanakdewa.story.utils.Book;

import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import net.citizensnpcs.api.CitizensAPI;

/**
 * A journal for a Character, containing objectives that they have yet to complete.
 * @author Kyle Nanakdewa
 */
public class Journal extends PlayerSaveDataSection {

	/** The journals of logged in players. */
	static final Map<PlayerCharacter,Journal> playerJournals = new HashMap<PlayerCharacter,Journal>();


	/** The character that this journal is for. */
	//private final PlayerCharacter character;

	/** The active objectives in this journal. */
	final Set<Objective> activeObjectives;
	/** The discovered objectives in this journal. */
	final Set<Objective> discoveredObjectives;
	/** The completed objectives in this journal. */
	final Set<Objective> completedObjectives;


	/**
	 * Gets a character's journal.
	 * @param character the character to get a journal for
	 */
	private Journal(PlayerCharacter character){
		super(character, StoryPlugin.plugin);
		activeObjectives = new HashSet<Objective>();
		discoveredObjectives = new HashSet<Objective>();
		completedObjectives = new HashSet<Objective>();

		//ConfigurationSection data = character.getData(StoryPlugin.plugin).getData();

		//activeObjectives.add(new DummyObjective(null, "Talk to NPCs to find things to do"));
		//activeObjectives.add(new DummyObjective(null, "Explore the world"));
		//activeObjectives.add(new DummyObjective("talknpc_231", "Talk to the Greeter"));
		activeObjectives.add(new NPCTalkObjective(CitizensAPI.getNPCRegistry().getById(231)));

		//discoveredObjectives.add(new DummyObjective("library-aunix", "Learn about the Aunix"));
		//discoveredObjectives.add(new DummyObjective("talknpc_278", "Talk to the Waramon researcher"));

		//completedObjectives.add(new DummyObjective(null, "View your journal"));
	}
	/**
	 * Gets a character's journal.
	 * @param character the character to get a journal for
	 */
	public static Journal get(PlayerCharacter character){
		Journal journal = playerJournals.get(character);
		if(journal==null){
			journal = new Journal(character);
			playerJournals.put(character, journal);
		}
		return journal;
	}
	/**
	 * Refreshes the journal list.
	 */
	static void updateJournals(){
		playerJournals.clear();
	}


	/**
	 * Gets the character that owns this Journal.
	 * @return the player character
	 */
	public PlayerCharacter getCharacter(){
		return character;
	}

	/**
	 * Gets the journal item.
	 * @return the journal item
	 */
	public ItemStack getJournalItem(){
		Book book = new Book(ChatColor.DARK_PURPLE+"Journal", character.getName());

		String activePage = ChatColor.DARK_GREEN+"Active Objectives:\n";
		for(Objective objective : activeObjectives){
			activePage += ChatColor.RESET+"- "+objective.getDescription()+"\n";
		}
		book.addSimplePage(activePage);

		String discoveredPage = ChatColor.DARK_PURPLE+"Discoveries:\n";
		for(Objective objective : discoveredObjectives){
			discoveredPage += ChatColor.RESET+"- "+objective.getDescription()+"\n";
		}
		book.addSimplePage(discoveredPage);

		String completedPage = ChatColor.DARK_RED+"Completed Objectives:\n";
		for(Objective objective : completedObjectives){
			completedPage += ChatColor.RESET+"- "+objective.getDescription()+"\n";
		}
		book.addSimplePage(completedPage);
		//book.addSimplePage(ChatColor.DARK_GRAY+"Talk to NPCs, read books, and explore the world, to find more things to do.");
		return book.getItem();
	}


	/**
	 * Adds an objective to the journal, and marks it active.
	 * @param objective the objective to add
	 */
	public void addObjective(Objective objective){
		if(objective==null || hasObjective(objective)) return;

		activeObjectives.add(objective);
		if(character.isOnline()){
			Player player = (Player)character.getPlayer();
			player.sendTitle(ChatColor.DARK_PURPLE+"!", objective.getDescription(), -1, -1, -1);
			Utils.sendActionBar(player, "Added to Journal");
			Interaction startInteraction = objective.getStartInteraction();
			if(startInteraction!=null) startInteraction.display(player);
		}
	}
	/**
	 * Adds an objective to the journal, as a discovery.
	 * @param objective the objective to add
	 */
	public void addDiscoveredObjective(Objective objective){
		if(objective==null || hasObjective(objective)) return;

		discoveredObjectives.add(objective);
		if(character.isOnline()){
			Player player = (Player)character.getPlayer();
			player.sendTitle(ChatColor.DARK_PURPLE+"?", objective.getDescription(), -1, -1, -1);
			Utils.sendActionBar(player, "Added to Journal");
			Interaction startInteraction = objective.getStartInteraction();
			if(startInteraction!=null) startInteraction.display(player);
		}
	}

	/**
	 * Gets all active objectives in this Journal.
	 * @return all active and discovered objectives
	 */
	public Set<Objective> getActiveObjectives(){
		Set<Objective> objectives = new HashSet<Objective>();
		objectives.addAll(activeObjectives);
		objectives.addAll(discoveredObjectives);
		// Add sub-objectives
		Set<Objective> subObjectives = new HashSet<Objective>();
		objectives.forEach(objective -> {
			if(objective instanceof Quest) ((Quest)objective).getSubObjectives().forEach(subObjective -> {
				if(subObjective.getStatus().equals(Status.ACTIVE)) subObjectives.add(subObjective);
			});
			if(objective instanceof CompoundObjective) ((CompoundObjective)objective).getSubObjectives().forEach(subObjective -> {
				if(subObjective.getStatus().equals(Status.ACTIVE)) subObjectives.add(subObjective);
			});
		});
		objectives.addAll(subObjectives);
		objectives.removeIf(objective -> objective==null);
		return objectives;
	}
	/**
	 * Gets an active objective with the specified identifier.
	 * @return the objective, or null if not found
	 */
	public Objective getActiveObjective(String identifier){
		Objective objective = null;
		for(Objective i : getActiveObjectives()){
			if(i.getIdentifier()!=null && i.getIdentifier().equalsIgnoreCase(identifier)){
				objective = i;
				break;
			}
		}
		return objective;
	}
	/**
	 * Gets all active objective with the specified identifier type.
	 * @return all active and discovered objectives
	 */
	public Set<Objective> getActiveTypeObjectives(String identifierType){
		Set<Objective> objectives = new HashSet<Objective>(getActiveObjectives());
		objectives.removeIf(objective -> !objective.getIdentifier().split("_",2)[0].equalsIgnoreCase(identifierType));
		return objectives;
	}
	/**
	 * Checks if this Journal contains the specified Objective.
	 * @return true if this Journal contains the specified objective, otherwise false
	 */
	public boolean hasObjective(Objective objective){
		if(activeObjectives.contains(objective) || discoveredObjectives.contains(objective) || completedObjectives.contains(objective)) return true;

		return getActiveObjective(objective.getIdentifier())!=null;
	}

	/**
	 * Marks an objective as completed.
	 * @param objective the objective to mark as completed
	 */
	public void completeObjective(Objective objective){
		objective.setCompleted();
	}

	/**
	 * Update with an objective status change.
	 */
	void objectiveStatusUpdate(ObjectiveStatusEvent event){
		if(!character.isOnline() || !hasObjective(event.getObjective())) return;
		Player player = (Player)character.getPlayer();

		if(event.getNewStatus().equals(Status.COMPLETED)){
			player.sendTitle(ChatColor.DARK_GREEN+"\u2713", event.getObjective().getDescription(), -1, -1, -1);
			Interaction completionInteraction = event.getObjective().getCompletionInteraction();
			if(completionInteraction!=null) completionInteraction.display(player);
			completedObjectives.add(event.getObjective());
		}
		else if(event.getNewStatus().equals(Status.FAILED)){
			player.sendTitle(ChatColor.DARK_RED+"x", event.getObjective().getDescription(), -1, -1, -1);
			Interaction failInteraction = event.getObjective().getFailInteraction();
			if(failInteraction!=null) failInteraction.display(player);
		}

		activeObjectives.remove(event.getObjective());
		discoveredObjectives.remove(event.getObjective());
	}
}