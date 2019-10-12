package com.kylenanakdewa.story.tags;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import com.kylenanakdewa.core.characters.Character;
import com.kylenanakdewa.core.characters.players.PlayerCharacter;
import com.kylenanakdewa.core.common.CommonColors;
import com.kylenanakdewa.core.common.Utils;
import com.kylenanakdewa.core.common.prompts.Prompt;
import com.kylenanakdewa.core.common.prompts.PromptActionEvent;
import com.kylenanakdewa.story.StoryPlugin;
import com.kylenanakdewa.story.journal.Journal;
import com.kylenanakdewa.story.quests.Quest;
import com.kylenanakdewa.story.tags.taggable.TempNPC;

/**
 * Represents a interaction with an NPC.
 */
public class Interaction extends Prompt {

	/** The Character that this Interaction is with. */
	private Character character;
	/** Items to be given the player in this Interaction. */
	private ItemStack[] items;
	/** Quests to be given to the player in this Interaction. */
	private Collection<Quest> quests;
	/** Discovery quests to be given to the player in this Interaction. */
	private Collection<Quest> discoveries;
	/** Prompt actions to run when this Interaction starts. The action is run from the player. */
	private Collection<String> actions;


	/**
	 * Creates a blank interaction.
	 */
	public Interaction(){
		super();
	}

	/**
	 * Retrieves a interaction from a Tag.
	 * @param tag the tag to retrive a interaction from
	 * @param name the name of the interaction to retrieve
	 */
	public static Interaction getFromTag(NPCTag tag, String name){
		//TODO
		return null;
	}

	/**
	 * Retrieves a interaction from a ConfigurationSection.
	 * @param config the ConfigurationSection to retrieve from
	 */
	public static Interaction getFromConfig(ConfigurationSection config){
		Interaction interaction = new Interaction();

		if(config!=null){
			// Load the data from file
			interaction.setQuestions(config.getStringList("questions"));
			interaction.setRandomQuestions(config.getBoolean("randomQuestions"));

			interaction.setAnswers(config.getStringList("answers"), config.getStringList("actions"), config.getStringList("conditions"));

			List<String> questList = config.getStringList("quests");
			if(questList!=null && !questList.isEmpty()){
				interaction.setQuests(new ArrayList<Quest>());
				questList.forEach(questString -> interaction.getQuests().add(Quest.generateFromTemplate(questString, interaction.getCharacter())));
			} else {
				String questString = config.getString("quest");
				if(questString!=null) interaction.setQuest(Quest.generateFromTemplate(questString, interaction.getCharacter()));
			}

			List<String> discoveryList = config.getStringList("discoveries");
			if(discoveryList!=null && !discoveryList.isEmpty()){
				interaction.setDiscoveries(new ArrayList<Quest>());
				discoveryList.forEach(questString -> interaction.getDiscoveries().add(Quest.generateFromTemplate(questString, interaction.getCharacter())));
			} else {
				String questString = config.getString("discovery");
				if(questString!=null) interaction.setDiscovery(Quest.generateFromTemplate(questString, interaction.getCharacter()));
			}

			List<String> actionList = config.getStringList("runActions");
			if(actionList!=null && !actionList.isEmpty()){
				interaction.setActions(actionList);
			} else {
				interaction.setAction(config.getString("action"));
			}

			interaction.setItems(config.getItemStack("item"));
		} else return null;

		return interaction;
	}


	/**
	 * Sets the Character for this Interaction.
	 * The Character is the one who asks the questions and responds to answers.
	 * @param character the character
	 */
	public void setCharacter(Character character){
		this.character = character;
	}
	/**
	 * Gets the Character involved in this Interaction.
	 * The Character is the one who asks the questions and responds to answers.
	 * @return the character
	 */
	public Character getCharacter(){
		return character;
	}

	/**
	 * Sets a single quest to be given for this Interaction.
	 * This overwrites all existing quests.
	 * @param quest the quest
	 */
	public void setQuest(Quest quest){
		quests = Arrays.asList(quest);
	}
	/**
	 * Sets the quests to be given for this Interaction.
	 * This overwrites all existing quests.
	 * @param quests the quests
	 */
	public void setQuests(Collection<Quest> quests){
		this.quests = quests;
	}
	/**
	 * Gets the quests to be given for this Interaction.
	 * @return the quests
	 */
	public Collection<Quest> getQuests(){
		return quests;
	}
	/**
	 * Sets a single quest to be given for this Interaction.
	 * This overwrites all existing quests.
	 * @param quest the quest
	 */
	public void setDiscovery(Quest quest){
		discoveries = Arrays.asList(quest);
	}
	/**
	 * Sets the quests to be given for this Interaction.
	 * This overwrites all existing quests.
	 * @param quests the quests
	 */
	public void setDiscoveries(Collection<Quest> quests){
		this.discoveries = quests;
	}
	/**
	 * Gets the quests to be given for this Interaction.
	 * @return the quests
	 */
	public Collection<Quest> getDiscoveries(){
		return discoveries;
	}

	/**
	 * Sets a single prompt action to run during this Interaction.
	 * This overwrites all existing actions.
	 * @param action a valid prompt action
	 */
	public void setAction(String action){
		actions = action!=null ? Arrays.asList(action) : actions;
	}
	/**
	 * Sets the prompt actions to run during this Interaction.
	 * This overwrites all existing objectives.
	 * @param actions valid prompt actions
	 */
	public void setActions(Collection<String> actions){
		this.actions = actions;
		if(actions!=null) actions.removeIf(action -> action==null);
	}
	/**
	 * Gets the prompt actions to run during this Interaction.
	 * @return the prompt actions
	 */
	public Collection<String> getActions(){
		return actions;
	}

	/**
	 * Sets the items to give during this Interaction.
	 * @param items the ItemStack(s) to give to the player
	 */
	public void setItems(ItemStack... items){
		this.items = items;
	}
	/**
	 * Gets the items to give during this Interaction.
	 * @return the ItemStack(s) to give to the player
	 */
	public ItemStack[] getItems(){
		return items;
	}


	/**
	 * Starts this Interaction between the specified Player and Character.
	 */
	public void start(Player player, Character character){
		if(character==null){
			character = PlayerCharacter.getCharacter(player);
		}
		setCharacter(character);
		if(character==null){
			Utils.notifyAdminsError("[Story] Interaction for "+player.getDisplayName()+CommonColors.ERROR+" failed because there is no character.");
			return;
		}

		// Format all strings with player and NPC name
		PlayerCharacter playerCharacter = PlayerCharacter.getCharacter(player);
		String playerName = playerCharacter.getName();
		String playerTitle = playerCharacter.getTitle().length()<2 ? "explorer" : playerCharacter.getTitle();
		String playerRealm = playerCharacter.getRealm()!=null ? playerCharacter.getRealm().getName() : "Akenland";
		String npcName = character.getFormattedName();
		String npcTitle = character.getTitle()!=null ? (character.getTitle().length()>2 ? character.getTitle() : character.getTitle()+"citizen" ) : "citizen";
		String npcRealm = character.getRealm()!=null ? character.getRealm().getName() : "Akenland";
		String npcLoc = !((TempNPC)character).getTag().getLocationData().getDisplayNames().isEmpty() ? ((TempNPC)character).getTag().getLocationData().getDisplayName() : "the "+npcRealm;
		String npcLocDirection = !((TempNPC)character).getTag().getLocationData().getDirectionalNames().isEmpty() ? ((TempNPC)character).getTag().getLocationData().getDirectionalName() : "in the "+npcRealm;

		// Format questions
		List<String> questions = getQuestions();
		if(questions!=null){
			for(int i=0; i<questions.size(); i++){
        	    String question = questions.get(i);
        	    questions.remove(i);
        	    question = question
        	        .replace("PLAYER_NAME", playerName + ChatColor.WHITE)
        	        .replace("PLAYER_TITLE", playerTitle + ChatColor.WHITE)
        	        .replace("PLAYER_REALM", playerRealm + ChatColor.WHITE)
        	        .replace("NPC_NAME", npcName + ChatColor.WHITE)
        	        .replace("NPC_TITLE", npcTitle + ChatColor.WHITE)
        	        .replace("NPC_REALM", npcRealm + ChatColor.WHITE)
        	        .replace("NPC_LOCATION", npcLoc + ChatColor.WHITE)
        	        .replace("NPC_LOCATION_DESCRIPTION", npcLocDirection + ChatColor.WHITE);
        	    questions.add(i, ChatColor.WHITE + question);
        	}
        	setQuestions(questions);
		}
		// Format answers
		if(getAnswers()!=null){
			for(Answer answer : getAnswers()){
            	answer
                .replaceText("PLAYER_NAME", playerName + ChatColor.GRAY)
                .replaceText("PLAYER_TITLE", playerTitle + ChatColor.GRAY)
                .replaceText("PLAYER_REALM", playerRealm + ChatColor.GRAY)
                .replaceText("NPC_NAME", npcName + ChatColor.GRAY)
                .replaceText("NPC_TITLE", npcTitle + ChatColor.GRAY)
                .replaceText("NPC_REALM", npcRealm + ChatColor.GRAY)
                .replaceText("NPC_LOCATION", npcLoc + ChatColor.GRAY)
                .replaceText("NPC_LOCATION_DESCRIPTION", npcLocDirection + ChatColor.GRAY)

                // Replace THISNPC with npc_ID in all actions
                .replaceAction("thisnpc", "npc_"+((TempNPC)character).getNPC().getId())
                .replaceAction("thisNPC", "npc_"+((TempNPC)character).getNPC().getId())
				.replaceAction("THISNPC", "npc_"+((TempNPC)character).getNPC().getId())

                .replaceAction("PLAYER_USERNAME", player.getName())
                .replaceAction("PLAYER_CO-ORDS", player.getLocation().getX()+" "+player.getLocation().getY()+" "+player.getLocation().getZ());
        	}
		}

		/** TODO - this is a dirty solution to make the character effectively final, need to fix this later! */
		Character finalCharacter = character;
		int delay = isRandomQuestions() || getQuestions()==null ? 0 : (getQuestions().size()-1)*30;
		Bukkit.getScheduler().scheduleSyncDelayedTask(StoryPlugin.plugin, () -> {

			// Actions
			if(getActions()!=null){
				for(String action : getActions()){
					action.replace("thisnpc", "npc_"+((TempNPC)finalCharacter).getNPC().getId());
					action.replace("thisNPC", "npc_"+((TempNPC)finalCharacter).getNPC().getId());
					action.replace("THISNPC", "npc_"+((TempNPC)finalCharacter).getNPC().getId());
					action.replace("PLAYER_USERNAME", player.getName());
					action.replace("PLAYER_CO-ORDS", player.getLocation().getX()+" "+player.getLocation().getY()+" "+player.getLocation().getZ());

					Bukkit.getServer().getPluginManager().callEvent(new PromptActionEvent(player, action));
				}
			}

			// Items
			if(getItems()!=null && getItems().length>0){
				try{player.getInventory().addItem(getItems());}
				catch(IllegalArgumentException e){}
			}

			// Quests
			if(getQuests()!=null){
				int questDelay = 30;
				for(Quest quest : getQuests()){
					quest.setQuestGiver(finalCharacter);
					Bukkit.getScheduler().scheduleSyncDelayedTask(StoryPlugin.plugin, () -> Journal.get(PlayerCharacter.getCharacter(player)).addQuest(quest), questDelay);
					questDelay += 200;
				}
			}
			// Discoveries
			if(getDiscoveries()!=null){
				int questDelay = 30 + (getQuests()!=null ? 30 + (getQuests().size() * 200) : 0);
				for(Quest quest : getDiscoveries()){
					quest.setQuestGiver(finalCharacter);
					Bukkit.getScheduler().scheduleSyncDelayedTask(StoryPlugin.plugin, () -> Journal.get(PlayerCharacter.getCharacter(player)).addDiscoveredQuest(quest), questDelay);
					questDelay += 200;
				}
			}

		}, delay);

		super.display(player);
	}
	@Override
	public void display(CommandSender target){
		if(target instanceof Player) start((Player)target, character);
	}


	@Override
	protected void displayQuestion(CommandSender target, String question){
		// Format with character's name
		target.sendMessage(String.format(character.getChatFormat(), character.getName(), question));
	}
	@Override
	protected void displayAllQuestions(CommandSender target){
		if(getQuestions()==null) return;
		int delay = 0;
		for(String question : getQuestions()){

			Bukkit.getScheduler().scheduleSyncDelayedTask(StoryPlugin.plugin, () -> displayQuestion(target, question), delay);

			delay+=30;

		}
	}
	@Override
	protected void displayAnswers(CommandSender target){
		int delay = isRandomQuestions() || getQuestions()==null ? 0 : (getQuestions().size()-1)*30;
		Bukkit.getScheduler().scheduleSyncDelayedTask(StoryPlugin.plugin, () -> super.displayAnswers(target), delay);
	}

}