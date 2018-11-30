package com.kylenanakdewa.story.journal;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.kylenanakdewa.core.characters.players.PlayerCharacter;
import com.kylenanakdewa.core.common.CommonColors;
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
		data.getStringList("objectives.active").forEach(objString -> {
			if(!objString.startsWith("autoquest")) activeObjectives.add(Objective.loadObjective(objString));
		});
		data.getStringList("objectives.discovered").forEach(objString -> {
			if(!objString.startsWith("autoquest")) activeObjectives.add(Objective.loadObjective(objString));
		});
		data.getStringList("objectives.completed").forEach(objString -> {
			if(!objString.startsWith("autoquest")) activeObjectives.add(Objective.loadObjective(objString));
		});

		//activeObjectives.add(new DummyObjective(null, "Talk to NPCs to find things to do"));
		//activeObjectives.add(new DummyObjective(null, "Explore the world"));
		//activeObjectives.add(new DummyObjective("talknpc_231", "Talk to the Greeter"));
		if(activeObjectives.isEmpty()) activeObjectives.add(new NPCTalkObjective(CitizensAPI.getNPCRegistry().getById(231)));

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
	 * Saves this Journal to the character's save data.
	 */
	@Override
	public void save(){
		List<String> activeList = new ArrayList<String>();
		activeObjectives.forEach(obj -> {
			if(!obj.getIdentifier().startsWith("autoquest")) activeList.add(obj.getIdentifier());
		});
		if(!activeList.isEmpty()) data.set("objectives.active", activeList);

		List<String> discoveredList = new ArrayList<String>();
		discoveredObjectives.forEach(obj -> {
			if(!obj.getIdentifier().startsWith("autoquest")) discoveredList.add(obj.getIdentifier());
		});
		if(!discoveredList.isEmpty()) data.set("objectives.discovered", discoveredList);

		List<String> completedList = new ArrayList<String>();
		completedObjectives.forEach(obj -> {
			if(!obj.getIdentifier().startsWith("autoquest")) completedList.add(obj.getIdentifier());
		});
		if(!completedList.isEmpty()) data.set("objectives.completed", completedList);

		super.save();
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

		String discoveredPage = ChatColor.DARK_AQUA+"Discoveries:\n";
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
		Utils.notifyAdmins(character.getName()+CommonColors.INFO+" started an objective: "+objective.getIdentifier()+" - "+objective.getDescription());
	}
	/**
	 * Adds an objective to the journal, as a discovery.
	 * @param objective the objective to add
	 */
	public void addDiscoveredObjective(Objective objective){
		if(objective==null) return;
		// Auto-complete objectives that have already been completed
		if(hasObjective(objective)){
			for(Objective completedObjective : completedObjectives){
				if(completedObjective.getIdentifier().equalsIgnoreCase(objective.getIdentifier())){
					objective.setCompleted();
					return;
				}
			}
			return;
		}

		discoveredObjectives.add(objective);
		if(character.isOnline()){
			Player player = (Player)character.getPlayer();
			player.sendTitle(ChatColor.DARK_PURPLE+"?", objective.getDescription(), -1, -1, -1);
			Utils.sendActionBar(player, "Added to Journal");
			Interaction startInteraction = objective.getStartInteraction();
			if(startInteraction!=null) startInteraction.display(player);
		}
		//Utils.notifyAdmins(character.getName()+CommonColors.INFO+" discovered an objective: "+objective.getIdentifier()+" - "+objective.getDescription());
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
			if(objective instanceof Quest){
				boolean latestActiveObjAdded = false;
				for(Objective subObjective : ((Quest)objective).getSubObjectives()){
					if(subObjective.isCompleted() || !latestActiveObjAdded){
						subObjectives.add(subObjective);
						if(!subObjective.isCompleted()) latestActiveObjAdded = true;
					}
				}
			}
			if(objective instanceof CompoundObjective) ((CompoundObjective)objective).getSubObjectives().forEach(subObjective -> subObjectives.add(subObjective));
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
		for(Objective objective : getActiveObjectives()){
			if(objective.getIdentifier()!=null && objective.getIdentifier().equalsIgnoreCase(identifier)){
				return objective;
			}
		}
		return null;
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
		//Utils.notifyAdmins(character.getName()+CommonColors.INFO+" completed an objective: "+objective.getIdentifier()+" - "+objective.getDescription());
		if(completedObjectives.size()>1) save();
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