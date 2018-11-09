package com.kylenanakdewa.story.quests.objectives;

import com.kylenanakdewa.story.tags.Interaction;
import com.kylenanakdewa.story.tags.Tag;

import org.bukkit.Location;

/**
 * An Objective to go to a specific location.
 * @author Kyle Nanakdewa
 */
public class GoToLocationObjective extends LocationObjective {

    public GoToLocationObjective(Location location, double radius, String locationName){
        super(location, radius, locationName);
    }
    public GoToLocationObjective(Location location, String locationName){
        super(location, locationName);
    }
    public GoToLocationObjective(Tag tag){
        super(tag);
    }

    @Override
    public String getIdentifier() {
        if(locationData!=null) return "gotoloc_"+locationData.getTag().getName();
        return "gotoloc_"+location.getWorld().getName()+" "+location.getBlockX()+" "+location.getBlockY()+" "+location.getBlockZ()+" "+radius+(locationName!=null?" "+locationName:"");
    }

    @Override
    public String getDescription() {
        return super.getDescription()==null ? "Go to "+locationName : super.getDescription();
    }

    @Override
    public Interaction getStartInteraction() {
        if(super.getStartInteraction()!=null) return super.getStartInteraction();

        Interaction interaction = new Interaction();
        String direction = (locationData!=null && !locationData.getDirectionalNames().isEmpty()) ? "It's "+locationData.getDirectionalName()+". " : "";
        interaction.addQuestion("You need to go to "+locationName+". "+direction);
        interaction.addQuestion("You need to pay a visit to "+locationName+". "+direction);
        interaction.addQuestion("Could you go to "+locationName+"? "+direction);
        interaction.addQuestion("I need you to go to "+locationName+". "+direction);
        interaction.addQuestion("Go to "+locationName+". "+direction);
        interaction.addQuestion("Visit "+locationName+". "+direction);
        interaction.addQuestion("I'd like you to go to "+locationName+". "+direction);
        interaction.addQuestion("I'd like you to visit "+locationName+". "+direction);
        interaction.addQuestion("Have you ever been to "+locationName+"? "+direction);
        interaction.addQuestion("Have you ever visited "+locationName+"? "+direction);
        interaction.addQuestion("You should visit "+locationName+"? "+direction);
        interaction.addQuestion("You should go to "+locationName+"? "+direction);
        interaction.setRandomQuestions(true);
        return interaction;
    }

}