package com.kylenanakdewa.story.tags;

import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import com.kylenanakdewa.core.characters.Character;
import com.kylenanakdewa.core.common.prompts.Prompt;
import com.kylenanakdewa.core.common.prompts.PromptActionEvent;
import com.kylenanakdewa.story.StoryPlugin;
import com.kylenanakdewa.story.quests.objectives.DummyObjective;
import com.kylenanakdewa.story.quests.objectives.Objective;

/**
 * Represents a interaction with an NPC.
 */
public class Interaction extends Prompt {

	/** The Character that this Interaction is with. */
	private Character character;
	/** Items to be given the player in this Interaction. */
	private ItemStack[] items;
	/** An objective to be given to the player in this Interaction. */
	private Objective objective;
	/** A prompt action to run when this Interaction starts. The action is run from the player. */
	private String action;


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
			String objectiveString = config.getString("objective");
			if(objectiveString!=null){
				String[] splitObjective = objectiveString.split(":", 2);
				interaction.setObjective(new DummyObjective(splitObjective[0], splitObjective[1])); //TODO
			}
			interaction.setAction(config.getString("action"));
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
	 * Sets the objective to be given for this Interaction.
	 * @param objective the objective
	 */
	public void setObjective(Objective objective){
		this.objective = objective;
	}
	/**
	 * Gets the objective to be given for this Interaction.
	 * @return the objective
	 */
	public Objective getObjective(){
		return objective;
	}

	/**
	 * Sets the prompt action to run during this Interaction.
	 * @param action a valid prompt action
	 */
	public void setAction(String action){
		this.action = action;
	}
	/**
	 * Gets the prompt action to run during this Interaction.
	 * @return a prompt action
	 */
	public String getAction(){
		return action;
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
		setCharacter(character);

		int delay = isRandomQuestions() ? 0 : (getQuestions().size()-1)*30;
		Bukkit.getScheduler().scheduleSyncDelayedTask(StoryPlugin.plugin, () -> {
			// Action
			if(action!=null) Bukkit.getServer().getPluginManager().callEvent(new PromptActionEvent(player, action));

			// Items
			if(items!=null) player.getInventory().addItem(items);

			// Objective
			if(objective!=null){}// TODO
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
		int delay = 0;
		for(String question : getQuestions()){

			Bukkit.getScheduler().scheduleSyncDelayedTask(StoryPlugin.plugin, () -> displayQuestion(target, question), delay);

			delay+=30;

		}
	}
	@Override
	protected void displayAnswers(CommandSender target){
		int delay = isRandomQuestions() ? 0 : (getQuestions().size()-1)*30;
		Bukkit.getScheduler().scheduleSyncDelayedTask(StoryPlugin.plugin, () -> super.displayAnswers(target), delay);
	}

}