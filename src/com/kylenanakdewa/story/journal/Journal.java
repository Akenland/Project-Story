package com.kylenanakdewa.story.journal;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import com.kylenanakdewa.core.characters.players.PlayerCharacter;
import com.kylenanakdewa.core.common.CommonColors;
import com.kylenanakdewa.core.common.Utils;
import com.kylenanakdewa.core.common.savedata.PlayerSaveDataSection;
import com.kylenanakdewa.story.StoryPlugin;
import com.kylenanakdewa.story.quests.objectives.Objective;
import com.kylenanakdewa.story.quests.objectives.ObjectiveStatusEvent;
import com.kylenanakdewa.story.quests.Quest;
import com.kylenanakdewa.story.tags.Interaction;
import com.kylenanakdewa.story.utils.Book;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Sound;
import org.bukkit.SoundCategory;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

/**
 * A journal for a Character, containing objectives that they have yet to complete.
 * @author Kyle Nanakdewa
 */
public class Journal extends PlayerSaveDataSection {

	/** The journals of logged in players. */
	static final Map<PlayerCharacter,Journal> playerJournals = new HashMap<PlayerCharacter,Journal>();


	/** The active quests in this journal. */
	final Set<Quest> activeQuests = new HashSet<Quest>();
	/** The discovered quests in this journal. */
	final Set<Quest> discoveredQuests = new HashSet<Quest>();
	/** The completed quests in this journal. */
	final Set<Quest> completedQuests = new HashSet<Quest>();

	/** The current active objectives in this journal. */
	private final Set<Objective> currentObjectives = new HashSet<Objective>();


	/**
	 * Gets a character's journal.
	 * @param character the character to get a journal for
	 */
	private Journal(PlayerCharacter character){
		super(character, StoryPlugin.plugin);

		// Load data from file
		if(data.contains("quests.active")){
			for(String key : data.getConfigurationSection("quests.active").getKeys(false)){
				ConfigurationSection questData = data.getConfigurationSection("quests.active."+key);
				activeQuests.add(Quest.retrieveSavedQuest(questData));
			}
		}
		if(data.contains("quests.discovered")){
			for(String key : data.getConfigurationSection("quests.discovered").getKeys(false)){
				ConfigurationSection questData = data.getConfigurationSection("quests.discovered."+key);
				discoveredQuests.add(Quest.retrieveSavedQuest(questData));
			}
		}
		if(data.contains("quests.completed")){
			for(String key : data.getConfigurationSection("quests.completed").getKeys(false)){
				ConfigurationSection questData = data.getConfigurationSection("quests.completed."+key);
				completedQuests.add(Quest.retrieveSavedQuest(questData));
			}
		}

		updateCurrentObjectives();

		//activeObjectives.add(new DummyObjective(null, "Talk to NPCs to find things to do"));
		//activeObjectives.add(new DummyObjective(null, "Explore the world"));
		//activeObjectives.add(new DummyObjective("talknpc_231", "Talk to the Greeter"));
		//if(activeObjectives.isEmpty()) activeObjectives.add(new NPCTalkObjective(CitizensAPI.getNPCRegistry().getById(231)));

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
		// Clear existing quest save data
		data.set("quests", null);

		int activeQuestsCount = 0;
		for(Quest quest : activeQuests){
			quest.saveQuestToConfiguration(data.createSection("quests.active."+activeQuestsCount));
			activeQuestsCount++;
		}

		int discoveredQuestsCount = 0;
		for(Quest quest : discoveredQuests){
			quest.saveQuestToConfiguration(data.createSection("quests.discovered."+discoveredQuestsCount));
			discoveredQuestsCount++;
		}

		int completedQuestsCount = 0;
		for(Quest quest : completedQuests){
			quest.saveQuestToConfiguration(data.createSection("quests.completed."+completedQuestsCount));
			completedQuestsCount++;
		}

		super.save();
	}

	/**
	 * Moves completed/failed quests to the correct set.
	 */
	private void updateCompletedQuests(){
		for(Quest quest : activeQuests){
			if(quest.isCompleted()){
				completeQuest(quest);
			} else {
				for (Objective objective : quest.getSubObjectives()) {
					if(objective.isFailed()) failQuest(quest);
				}
			}
		}

		for(Quest quest : discoveredQuests){
			if(quest.isCompleted()){
				completeQuest(quest);
			} else {
				for (Objective objective : quest.getSubObjectives()) {
					if(objective.isFailed()) failQuest(quest);
				}
			}
		}
	}

	/**
	 * Updates the set of all current active objectives in this journal.
	 * This should be fired every time that quests are added, removed, or updated.
	 */
	private void updateCurrentObjectives(){
		updateCompletedQuests();

		currentObjectives.clear();

		for(Quest quest : activeQuests){
			currentObjectives.add(quest.getCurrentObjective());
		}
		for(Quest quest : discoveredQuests){
			currentObjectives.add(quest.getCurrentObjective());
		}
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

		// Active quests
		for(Quest quest : activeQuests){
			String page = ChatColor.DARK_GREEN + "Active Quests:\n\n" + ChatColor.RESET;
			page += ChatColor.getLastColors(quest.getName()) + ChatColor.BOLD + quest.getName() + ChatColor.RESET + "\n";
			page += ChatColor.ITALIC + quest.getDescription() + ChatColor.RESET + "\n";
			page += "- " + quest.getCurrentObjective().getDescription();

			book.addSimplePage(page);
		}

		// Discovered quests
		for(Quest quest : discoveredQuests){
			String page = ChatColor.DARK_AQUA + "Discoveries:\n\n" + ChatColor.RESET;
			page += ChatColor.getLastColors(quest.getName()) + ChatColor.BOLD + quest.getName() + ChatColor.RESET + "\n";
			page += ChatColor.ITALIC + quest.getDescription() + ChatColor.RESET + "\n";
			page += "- " + quest.getCurrentObjective().getDescription();

			book.addSimplePage(page);
		}

		// Completed quests
		for(Quest quest : completedQuests){
			String page = ChatColor.DARK_RED + "Completed Quests:\n\n" + ChatColor.RESET;
			page += ChatColor.getLastColors(quest.getName()) + ChatColor.BOLD + quest.getName() + ChatColor.RESET + "\n";
			page += ChatColor.ITALIC + quest.getDescription();

			book.addSimplePage(page);
		}

		return book.getItem();
	}


	/**
	 * Shows the quest started message.
	 */
	private void showQuestStartedMessage(Quest quest) {
		if(!character.isOnline()) return;

		Player player = (Player)character.getPlayer();

		player.playSound(player.getLocation(), Sound.UI_TOAST_CHALLENGE_COMPLETE, SoundCategory.MASTER, 1000, 0.1f);

		player.sendTitle(ChatColor.DARK_PURPLE+"!", "Quest Started", 10, 20, 10);
		Bukkit.getScheduler().scheduleSyncDelayedTask(StoryPlugin.plugin, () -> {
			player.sendTitle(ChatColor.DARK_PURPLE+"!", ChatColor.getLastColors(quest.getName()) + ChatColor.BOLD + quest.getName(), 10, 70, 20);
		}, 30);
		Bukkit.getScheduler().scheduleSyncDelayedTask(StoryPlugin.plugin, () -> {
			player.sendTitle(ChatColor.DARK_PURPLE+"!", quest.getCurrentObjective().getDescription(), 10, 70, 20);
		}, 110);

		Utils.sendActionBar(player, "Added to Journal!");
	}
	/**
	 * Shows the quest progress message.
	 */
	private void showQuestProgressMessage(Quest quest) {
		if(!character.isOnline()) return;

		Player player = (Player)character.getPlayer();

		player.playSound(player.getLocation(), Sound.BLOCK_ENCHANTMENT_TABLE_USE, SoundCategory.MASTER, 1000, 1.6f);

		player.sendTitle(ChatColor.GRAY+"!", ChatColor.getLastColors(quest.getName()) + ChatColor.BOLD + quest.getName(), 10, 40, 10);
		Bukkit.getScheduler().scheduleSyncDelayedTask(StoryPlugin.plugin, () -> {
			player.sendTitle(ChatColor.GRAY+"!", quest.getCurrentObjective().getDescription(), 10, 70, 20);
		}, 50);

		Utils.sendActionBar(player, "Journal updated.");
	}
	/**
	 * Shows the quest discovered message.
	 */
	private void showQuestDiscoveryMessage(Quest quest) {
		if(!character.isOnline()) return;

		Player player = (Player)character.getPlayer();

		player.playSound(player.getLocation(), Sound.BLOCK_ENCHANTMENT_TABLE_USE, SoundCategory.MASTER, 1000, 0.6f);

		player.sendTitle(ChatColor.DARK_PURPLE+"?", ChatColor.getLastColors(quest.getName()) + ChatColor.BOLD + quest.getName(), 10, 70, 20);
		Bukkit.getScheduler().scheduleSyncDelayedTask(StoryPlugin.plugin, () -> {
			player.sendTitle(ChatColor.DARK_PURPLE+"?", quest.getCurrentObjective().getDescription(), 10, 70, 20);
		}, 80);

		Utils.sendActionBar(player, "Added to Journal!");
	}
	/**
	 * Shows the quest completed message.
	 */
	private void showQuestCompletedMessage(Quest quest) {
		if(!character.isOnline()) return;

		Player player = (Player)character.getPlayer();

		player.playSound(player.getLocation(), Sound.UI_TOAST_CHALLENGE_COMPLETE, SoundCategory.MASTER, 1000, 1.6f);

		player.sendTitle(ChatColor.GREEN+"\u2713", "Quest Completed", 10, 20, 10);
		Bukkit.getScheduler().scheduleSyncDelayedTask(StoryPlugin.plugin, () -> {
			player.sendTitle(ChatColor.GREEN+"\u2713", ChatColor.getLastColors(quest.getName()) + ChatColor.BOLD + quest.getName(), 10, 70, 20);
		}, 30);

		Utils.sendActionBar(player, "Journal updated.");
	}
	/**
	 * Shows the quest failed message.
	 */
	private void showQuestFailedMessage(Quest quest) {
		if(!character.isOnline()) return;

		Player player = (Player)character.getPlayer();

		player.playSound(player.getLocation(), Sound.BLOCK_ENCHANTMENT_TABLE_USE, SoundCategory.MASTER, 1000, 1.1f);

		player.sendTitle(ChatColor.RED+"x", "Quest Failed", 10, 20, 10);
		Bukkit.getScheduler().scheduleSyncDelayedTask(StoryPlugin.plugin, () -> {
			player.sendTitle(ChatColor.RED+"x", ChatColor.getLastColors(quest.getName()) + ChatColor.BOLD + quest.getName(), 10, 70, 20);
		}, 30);

		Utils.sendActionBar(player, "Journal updated.");
	}


	/**
	 * Adds a quest to this Journal.
	 */
	public void addQuest(Quest quest) {
		addQuest(quest, false);
	}
	/**
	 * Adds a discovered quest to this Journal.
	 */
	public void addDiscoveredQuest(Quest quest) {
		addQuest(quest, true);
	}
	/**
	 * Adds a quest to this Journal.
	 * @param quest the quest to add
	 * @param discovery true for a normal quest, false for a discovery
	 */
	private void addQuest(Quest quest, boolean discovery){
		// If Journal already contains this quest, ignore
		if(hasQuestFromTemplate(quest.getTemplateName())){
			Utils.notifyAdminsError("Journal for "+character.getName()+CommonColors.ERROR+" already contains quest from template "+quest.getTemplateName());
			return;
		}

		// Add quest to journal, and play the appropriate message
		if(discovery){
			discoveredQuests.add(quest);
			showQuestDiscoveryMessage(quest);
		} else {
			activeQuests.add(quest);
			showQuestStartedMessage(quest);
		}

		// Display the start interaction
		Interaction startInteraction = quest.getCurrentObjective().getStartInteraction();
		if(startInteraction!=null) startInteraction.start((Player)character.getPlayer(), quest.getQuestGiver());

		updateCurrentObjectives();
	}

	/**
	 * Marks a quest as complete.
	 */
	private void completeQuest(Quest quest) {
		activeQuests.remove(quest);
		discoveredQuests.remove(quest);

		completedQuests.add(quest);

		showQuestCompletedMessage(quest);
	}
	/**
	 * Marks a quest as failed.
	 */
	private void failQuest(Quest quest) {
		activeQuests.remove(quest);
		discoveredQuests.remove(quest);
		completedQuests.remove(quest);

		showQuestFailedMessage(quest);
	}


	/**
	 * Gets a Quest that was created from the specified template, if one exists in this Journal, otherwise returns null.
	 */
	public Quest getQuestFromTemplate(String templateName){
		if(templateName==null) return null;
		for(Quest quest : activeQuests){
			if(templateName.equalsIgnoreCase(quest.getTemplateName())) return quest;
		}
		for(Quest quest : discoveredQuests){
			if(templateName.equalsIgnoreCase(quest.getTemplateName())) return quest;
		}
		for(Quest quest : completedQuests){
			if(templateName.equalsIgnoreCase(quest.getTemplateName())) return quest;
		}
		return null;
	}
	/**
	 * Checks if this Journal contains a quest created from the specified template.
	 */
	public boolean hasQuestFromTemplate(String templateName){
		return getQuestFromTemplate(templateName) != null;
	}


	/**
	 * Gets all current active objectives in this Journal.
	 * @return all current objectives
	 */
	public Set<Objective> getCurrentObjectives(){
		return currentObjectives;
	}
	/**
	 * Gets a current objective with the specified identifier.
	 * @return the objective, or null if not found
	 */
	public Objective getCurrentObjective(String identifier){
		for(Objective objective : getCurrentObjectives()){
			if(identifier.equalsIgnoreCase(objective.getIdentifier())){
				return objective;
			}
		}
		return null;
	}
	/**
	 * Gets all current objective with the specified identifier type.
	 * @return all current objectives
	 */
	public Set<Objective> getCurrentObjectivesByType(String identifierType){
		Set<Objective> objectives = new HashSet<Objective>(getCurrentObjectives());
		objectives.removeIf(objective -> !objective.getIdentifier().split("_",2)[0].equalsIgnoreCase(identifierType));
		return objectives;
	}

	/**
	 * Gets the active quest that contains the specified objective.
	 * @return the quest, or null if not found
	 */
	public Quest getQuestByObjective(Objective objective){
		for(Quest quest : activeQuests){
			if(quest.getCurrentObjective().equals(objective)){
				return quest;
			}
		}
		for(Quest quest : discoveredQuests){
			if(quest.getCurrentObjective().equals(objective)){
				return quest;
			}
		}
		return null;
	}


	// /**
	//  * Adds an objective to the journal, and marks it active.
	//  * @param objective the objective to add
	//  */
	// @Deprecated
	// public void addObjective(Objective objective){
	// 	if(objective==null || hasObjective(objective)) return;

	// 	activeObjectives.add(objective);
	// 	if(character.isOnline()){
	// 		Player player = (Player)character.getPlayer();
	// 		player.sendTitle(ChatColor.DARK_PURPLE+"!", objective.getDescription(), -1, -1, -1);
	// 		Utils.sendActionBar(player, "Added to Journal");
	// 		Interaction startInteraction = objective.getStartInteraction();
	// 		if(startInteraction!=null) startInteraction.display(player);
	// 	}
	// 	Utils.notifyAdmins(character.getName()+CommonColors.INFO+" started an objective: "+objective.getIdentifier()+" - "+objective.getDescription());
	// }
	// /**
	//  * Adds an objective to the journal, as a discovery.
	//  * @param objective the objective to add
	//  */
	// @Deprecated
	// public void addDiscoveredObjective(Objective objective){
	// 	if(objective==null) return;
	// 	// Auto-complete objectives that have already been completed
	// 	if(hasObjective(objective)){
	// 		for(Objective completedObjective : completedObjectives){
	// 			if(completedObjective.getIdentifier().equalsIgnoreCase(objective.getIdentifier())){
	// 				objective.setCompleted();
	// 				return;
	// 			}
	// 		}
	// 		return;
	// 	}

	// 	discoveredObjectives.add(objective);
	// 	if(character.isOnline()){
	// 		Player player = (Player)character.getPlayer();
	// 		player.sendTitle(ChatColor.DARK_PURPLE+"?", objective.getDescription(), -1, -1, -1);
	// 		Utils.sendActionBar(player, "Added to Journal");
	// 		Interaction startInteraction = objective.getStartInteraction();
	// 		if(startInteraction!=null) startInteraction.display(player);
	// 	}
	// 	//Utils.notifyAdmins(character.getName()+CommonColors.INFO+" discovered an objective: "+objective.getIdentifier()+" - "+objective.getDescription());
	// }

	// /**
	//  * Gets all active objectives in this Journal.
	//  * @return all active and discovered objectives
	//  */
	// @Deprecated
	// public Set<Objective> getActiveObjectives(){
	// 	Set<Objective> objectives = new HashSet<Objective>();
	// 	objectives.addAll(activeObjectives);
	// 	objectives.addAll(discoveredObjectives);
	// 	// Add sub-objectives
	// 	Set<Objective> subObjectives = new HashSet<Objective>();
	// 	objectives.forEach(objective -> {
	// 		if(objective instanceof Quest){
	// 			boolean latestActiveObjAdded = false;
	// 			for(Objective subObjective : ((Quest)objective).getSubObjectives()){
	// 				if(subObjective.isCompleted() || !latestActiveObjAdded){
	// 					subObjectives.add(subObjective);
	// 					if(!subObjective.isCompleted()) latestActiveObjAdded = true;
	// 				}
	// 			}
	// 		}
	// 		if(objective instanceof CompoundObjective) ((CompoundObjective)objective).getSubObjectives().forEach(subObjective -> subObjectives.add(subObjective));
	// 	});
	// 	objectives.addAll(subObjectives);
	// 	objectives.removeIf(objective -> objective==null);
	// 	return objectives;
	// }
	// /**
	//  * Gets an active objective with the specified identifier.
	//  * @return the objective, or null if not found
	//  */
	// @Deprecated
	// public Objective getActiveObjective(String identifier){
	// 	for(Objective objective : getActiveObjectives()){
	// 		if(objective.getIdentifier()!=null && objective.getIdentifier().equalsIgnoreCase(identifier)){
	// 			return objective;
	// 		}
	// 	}
	// 	return null;
	// }
	// /**
	//  * Gets all active objective with the specified identifier type.
	//  * @return all active and discovered objectives
	//  */
	// @Deprecated
	// public Set<Objective> getActiveTypeObjectives(String identifierType){
	// 	Set<Objective> objectives = new HashSet<Objective>(getActiveObjectives());
	// 	objectives.removeIf(objective -> !objective.getIdentifier().split("_",2)[0].equalsIgnoreCase(identifierType));
	// 	return objectives;
	// }
	// /**
	//  * Checks if this Journal fConfigurationSectiontquestDataains the specified Objective.
	//  * @return true if this Journal contains the specified objective, otherwise false
	//  */
	// @Deprecated
	// public boolean hasObjective(Objective objective){
	// 	if(activeObjectives.contains(objective) || discoveredObjectives.contains(objective) || completedObjectives.contains(objective)) return true;

	// 	return getActiveObjective(objective.getIdentifier())!=null;
	// }

	// /**
	//  * Marks an objective as completed.
	//  * @param objective the objective to mark as completed
	//  */
	// @Deprecated
	// public void completeObjective(Objective objective){
	// 	objective.setCompleted();
	// 	//Utils.notifyAdmins(character.getName()+CommonColors.INFO+" completed an objective: "+objective.getIdentifier()+" - "+objective.getDescription());
	// 	if(completedObjectives.size()>1) save();
	// }


	/**
	 * Update with an objective status change.
	 */
	void objectiveStatusUpdate(ObjectiveStatusEvent event){
		if(!character.isOnline() || !getCurrentObjectives().contains(event.getObjective())) return;
		Player player = (Player)character.getPlayer();
		Quest quest = getQuestByObjective(event.getObjective());

		switch (event.getNewStatus()) {
			case ACTIVE:
				if(quest!=null) showQuestProgressMessage(quest);

				Interaction startInteraction = event.getObjective().getStartInteraction();
				if(startInteraction!=null) startInteraction.display(player);

				break;

			case COMPLETED:
				Interaction completionInteraction = event.getObjective().getCompletionInteraction();
				if(completionInteraction!=null) completionInteraction.display(player);

				break;

			case FAILED:
				Interaction failInteraction = event.getObjective().getFailInteraction();
				if(failInteraction!=null) failInteraction.display(player);

				break;

			case NOT_READY:
				break;

		}

		updateCurrentObjectives();
	}
}