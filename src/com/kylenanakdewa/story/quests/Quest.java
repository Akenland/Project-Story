package com.kylenanakdewa.story.quests;

import java.util.ArrayList;
import java.util.List;

import com.kylenanakdewa.core.characters.Character;
import com.kylenanakdewa.core.common.ConfigAccessor;

import com.kylenanakdewa.story.StoryPlugin;
import com.kylenanakdewa.story.quests.objectives.Objective;
import com.kylenanakdewa.story.quests.objectives.Objective.Status;
import com.kylenanakdewa.story.tags.taggable.TempNPC;

import org.bukkit.configuration.ConfigurationSection;

import net.citizensnpcs.api.CitizensAPI;

/**
 * An quest, made up of several sub-objectives.
 * The quest is complete when all of its sub-objectives are completed, in order.
 * @author Kyle Nanakdewa
 */
public class Quest {

    /** The quest template name, if this quest came from a template, otherwise null. */
    private String templateName;

    /** The quest name. */
    protected String name;
    /** The quest description. */
    protected String description;
    /** The quest-giver. */
    private Character questGiver;

    /** The sub-objectives. */
    protected List<Objective> subObjectives;
    /** Progress through the quest. */
    private int objectivesComplete;

    /** The prerequisites for this quest. */
    //private Condition prerequisites;


    /** Creates a quest from a ConfigurationSection. */
    public static Quest generateFromConfiguration(ConfigurationSection data, Character questGiver){
        Quest quest = new Quest();

        quest.name = data.getString("name");
        quest.description = data.getString("description");
        quest.questGiver = questGiver;

        quest.subObjectives = new ArrayList<Objective>();
        for(String objName : data.getConfigurationSection("objectives").getKeys(false)){
            quest.subObjectives.add(Objective.getFromConfig(data.getConfigurationSection("objectives."+objName)));
        }
        quest.subObjectives.forEach(objective -> objective.setStatus(Status.NOT_READY));
        quest.subObjectives.get(0).setStatus(Status.ACTIVE);

        return quest;
    }

    /** Creates a quest from a template. */
    public static Quest generateFromTemplate(String templateName, Character questGiver){
        ConfigurationSection data = new ConfigAccessor("quests\\"+templateName+".yml", StoryPlugin.plugin).getConfig();
        Quest quest = generateFromConfiguration(data, questGiver);
        quest.templateName = templateName;
        return quest;
    }


    /** Retrieves an in-progress quest from a ConfigurationSection. */
    public static Quest retrieveSavedQuest(ConfigurationSection data){
        Quest quest;

        String templateName = data.getString("template");

        int questGiverNpcId = data.getInt("quest-giver-npc");
        Character questGiver = new TempNPC(CitizensAPI.getNPCRegistry().getById(questGiverNpcId));

        if(templateName!=null){
            quest = generateFromTemplate(templateName, questGiver);
        } else {
            quest = generateFromConfiguration(data, questGiver);
        }

        // Set all objectives statuses correctly
        quest.objectivesComplete = data.getInt("objectives-complete");
        quest.subObjectives.subList(0, quest.objectivesComplete).forEach(objective -> objective.setStatus(Status.COMPLETED));
        if(!quest.isCompleted()){
            quest.getCurrentObjective().setStatus(Status.ACTIVE);
            quest.subObjectives.subList(quest.objectivesComplete + 1, quest.subObjectives.size()).forEach(objective -> objective.setStatus(Status.NOT_READY));
        }

        return quest;
    }


    /**
     * Creates a Quest.
     * @param subObjectives a list of sub-objectives
     */
    /*@Deprecated
    public Quest(List<Objective> subObjectives) {
        //setSubObjectives(subObjectives);
    }*/
    /**
     * Creates a Quest.
     * @param subObjectives an array of sub-objectives
     */
    /*@Deprecated
    public Quest(Objective... subObjectives) {
        this(new ArrayList<Objective>(Arrays.asList(subObjectives)));
    }*/

    /**
     * Creates a quest.
     * @param name the display name of the quest
     * @param description the journal description of the quest, use null to assume from objectives
     * @param questGiver the character that gave out this quest
     * @param subObjectives the objectives in this quest
     */
    public Quest(String name, String description, Character questGiver, List<Objective> subObjectives) {
        this.name = name;
        this.description = description;
        this.questGiver = questGiver;
        this.subObjectives = subObjectives;
    }

    private Quest(){

    }


    /** Saves an in-progress quest to a ConfigurationSection. */
    public void saveQuestToConfiguration(ConfigurationSection data){
        if(templateName!=null){
            data.set("template", templateName);
        } else {
            data.set("name", name);
            data.set("description", description);
            // TODO - save the objectives!
        }

        data.set("quest-giver-npc", ((TempNPC)questGiver).getNPC().getId());
        data.set("objectives-complete", getObjectivesComplete());
    }


    /*@Deprecated
    protected void setSubObjectives(List<Objective> subObjectives){
        this.subObjectives = subObjectives;

        // Add the following objective to the completion interaction of the preceding action
        Iterator<Objective> iterator = subObjectives.iterator();
        Objective previous = null;
        while(iterator.hasNext()){
            Objective current = iterator.next();
            // Add previous objective to end of previous objective
            if(previous!=null){
                Interaction completion = current.getCompletionInteraction();
                if(completion==null){
                    completion = new Interaction();
                    current.setCompletionInteraction(completion);
                }
                Collection<Objective> objectives = completion.getObjectives();
                if(objectives==null) completion.setObjective(previous);
                else objectives.add(previous);
            }
            // Start the first sub-objective at the same time this objective is started
            else {
                Interaction start = getStartInteraction();
                if(start==null){
                    start = new Interaction();
                    setStartInteraction(start);
                }
                Collection<Objective> objectives = start.getObjectives();
                if(objectives==null) start.setObjective(current);
                else objectives.add(current);
            }
            previous = current;
        }
        // Complete the quest at the end of the final objective
        if(!subObjectives.isEmpty()){
            Objective finalObjective = subObjectives.get(subObjectives.size()-1);
            Interaction completion = finalObjective.getCompletionInteraction();
            if(completion==null){
                completion = new Interaction();
                finalObjective.setCompletionInteraction(completion);
            }
            Collection<String> actions = completion.getActions();
            if(actions==null) completion.setAction("completeObjective_"+getIdentifier());
            else actions.add("completeObjective_"+getIdentifier());
        } else {
            setCompleted();
            Utils.notifyAdmins(CommonColors.INFO+"Quest ("+getIdentifier()+" - "+getDescription()+") had no objectives and was marked as complete.");
        }
    }*/
    /**
     * Gets a list of the sub-objectives in this quest, in the order they must be completed in.
     * @return the sub-objectives
     */
    public List<Objective> getSubObjectives(){
        return subObjectives;
    }


	/**
     * Returns true if this quest is complete.
     * @return
     */
	public boolean isCompleted() {
        return getObjectivesComplete() >= subObjectives.size();
	}

	/*@Override
	public boolean isFailed() {
        if(super.isFailed()) return true;
        // Return true if any sub-objective is failed
		for(Objective objective : subObjectives){
            if(objective.isFailed()) return true;
        }
        return false;
    }*/


    /*@Override
    public String getIdentifier() {
        String identifier = "quest[";
        for(Objective objective : subObjectives) identifier += objective.getIdentifier()+",";
        if(identifier.endsWith(",")) identifier = identifier.substring(0, identifier.length());
        identifier += "]";
        return identifier;
    }*/


    /**
     * Gets the name of this Quest.
     */
    public String getName() {
        return name;
    }

    /**
     * Gets the description of this Quest.
     */
	public String getDescription() {
        if(description!=null) return description;

        // Return first incomplete objective description
        /*for(Objective objective : subObjectives){
            if(!objective.isCompleted()){
                return objective.getDescription();
            }
        }*/
        return subObjectives.get(getObjectivesComplete()).getDescription();

        // If all objectives are complete, return the last objective's description, so it shows on the title at the end
        //return !subObjectives.isEmpty() ? subObjectives.get(subObjectives.size()-1).getDescription() : "[Empty Quest]";
    }

    /**
     * Gets the Character that gave out this Quest.
     */
    public Character getQuestGiver() {
        return questGiver;
    }
    /**
     * Sets the Character that gave out this Quest.
     */
    public void setQuestGiver(Character character) {
        questGiver = character;
    }


    /**
     * Gets the current objective in this Quest.
     * @return the current objective, or null if this quest is already complete
     */
    public Objective getCurrentObjective() {
        return isCompleted() ? null : subObjectives.get(getObjectivesComplete());
    }

    /**
     * Gets the number of objectives that have been completed in this Quest.
     */
    private int getObjectivesComplete() {
        updateObjectivesCompletedCount();
        return objectivesComplete;
    }

    /**
     * Updates the count of completed objectives.
     */
    private void updateObjectivesCompletedCount() {
        for (Objective objective : subObjectives) {
            if(objective.isActive()){
                objectivesComplete = subObjectives.indexOf(objective);
                return;
            }
        }
    }


    /**
     * Gets the name of the template that this Quest was created from, or null if this Quests was not created with a template.
     */
    public String getTemplateName() {
        return templateName;
    }


    /*@Override
    public Interaction getStartInteraction() {
        if((super.getStartInteraction()!=null && super.getStartInteraction().getQuestions()!=null && !super.getStartInteraction().getQuestions().isEmpty()) || subObjectives==null || subObjectives.isEmpty()) return super.getStartInteraction();

        Interaction interaction = new Interaction();

        List<String> objectiveStrings = new ArrayList<String>();
        for(Objective objective : subObjectives){
            objectiveStrings.add(RandomDialogue.lowerFirstLetter(objective.getDescription()) + ChatColor.RESET);
        }

        String firstLine = RandomDialogue.getRandomLine(RandomDialogue.objectiveStarters)+objectiveStrings.get(0);
        firstLine += objectiveStrings.size()>=2 ? ", and "+objectiveStrings.get(1)+". " : ".";
        interaction.addQuestion(firstLine);

        if(objectiveStrings.size()>=3){
            String secondLine = RandomDialogue.lowerFirstLetter(RandomDialogue.getRandomLine(RandomDialogue.objectiveStarters));
            secondLine += objectiveStrings.get(2);
            interaction.addQuestion("Then, "+secondLine+".");
        }

        for(int i=3; i<objectiveStrings.size()-2; i++){
            String additionalLines = RandomDialogue.lowerFirstLetter(RandomDialogue.getRandomLine(RandomDialogue.objectiveStarters));
            additionalLines += objectiveStrings.get(2);
            interaction.addQuestion("Then, "+additionalLines+".");
        }

        if(objectiveStrings.size()>=4){
            String finalLine = RandomDialogue.lowerFirstLetter(RandomDialogue.getRandomLine(RandomDialogue.objectiveStarters));
            String finalObjective = objectiveStrings.get(objectiveStrings.size()-1);
            finalLine += finalObjective.startsWith("return to") ? "return to me." : finalObjective;
            interaction.addQuestion("Finally, "+finalLine+".");
        }

        return interaction;
    }*/
}