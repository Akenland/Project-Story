package com.kylenanakdewa.story.tags;

import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.ConfigurationSection;

import com.kylenanakdewa.core.characters.Character;
import com.kylenanakdewa.core.common.prompts.Prompt;
import com.kylenanakdewa.story.StoryPlugin;

/**
 * Represents a interaction with an NPC.
 */
public class Interaction extends Prompt {

    /** The Character that this Interaction is with. */
    private Character character;

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

            delay+=20;

        }
    }
    @Override
    protected void displayAnswers(CommandSender target){
        int delay = isRandomQuestions() ? 0 : (getQuestions().size()-1)*20;
        Bukkit.getScheduler().scheduleSyncDelayedTask(StoryPlugin.plugin, () -> super.displayAnswers(target), delay);
    }

}