package com.kylenanakdewa.story.journal;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import com.kylenanakdewa.core.characters.players.PlayerCharacter;
import com.kylenanakdewa.core.common.CommonColors;
import com.kylenanakdewa.core.common.Error;
import com.kylenanakdewa.core.common.Utils;
import com.kylenanakdewa.core.common.prompts.Prompt;
import com.kylenanakdewa.story.quests.objectives.Objective;

import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Player;

/**
 * Journal Commands
 * @author Kyle Nanakdewa
 */
public class JournalCommands implements TabExecutor {

    private enum JournalSection {ALL, ACTIVE, DISCOVERED, COMPLETED;}

	@Override
	public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        // Convert args to list
        List<String> argsList = new ArrayList<String>(Arrays.asList(args));

        // Filter out flags
        JournalSection targetSection = JournalSection.ALL;
        OfflinePlayer targetPlayer = null;
        if(sender.hasPermission("story.journal")){
            if(argsList.contains("-active") || argsList.contains("-a")) targetSection = JournalSection.ACTIVE;
            else if(argsList.contains("-discovered") || argsList.contains("-d")) targetSection = JournalSection.DISCOVERED;
            else if(argsList.contains("-completed") || argsList.contains("-c")) targetSection = JournalSection.COMPLETED;
            argsList.removeAll(Arrays.asList("-active", "-a", "-discovered", "-d", "-completed", "-c"));

            int pIndex = argsList.indexOf("-player")+argsList.indexOf("-p")+1;
            if(pIndex>=0 && pIndex<argsList.size()){
                argsList.remove(pIndex);
                targetPlayer = Utils.getPlayer(argsList.get(pIndex), true);
                if(targetPlayer==null) return Error.PLAYER_NOT_FOUND.displayActionBar(sender);
                argsList.remove(pIndex);
            }
        }
        if(targetPlayer==null){
            if(sender instanceof Player) targetPlayer = (OfflinePlayer)sender;
            else return Error.PLAYER_NOT_FOUND.displayActionBar(sender);
        }

        PlayerCharacter character = PlayerCharacter.getCharacter(targetPlayer);
        Journal journal = Journal.get(character);

        // If no args, give the player their journal
        if(argsList.size()==0){
            if(targetPlayer.isOnline()){
                targetPlayer.getPlayer().getInventory().addItem(journal.getJournalItem());
                return true;
            } else return Error.PLAYER_NOT_FOUND.displayActionBar(sender);
        }

        // Check permissions
        if(!sender.hasPermission("story.journal")) return Error.NO_PERMISSION.displayChat(sender);


        // List command
        if(argsList.get(0).equalsIgnoreCase("list")){
            Prompt prompt = new Prompt();
            prompt.addQuestion(CommonColors.INFO+"--- "+CommonColors.MESSAGE+"Journal for "+character.getName()+CommonColors.INFO+" ---");
            Collection<Objective> objectives = null;
            switch(targetSection){
                case ALL:
                    prompt.addQuestion(CommonColors.INFO+"All active objectives:");
                    objectives = journal.getActiveObjectives();
                    break;
                case ACTIVE:
                    prompt.addQuestion(CommonColors.INFO+"Listed active objectives:");
                    objectives = journal.activeObjectives;
                    break;
                case DISCOVERED:
                    prompt.addQuestion(CommonColors.INFO+"Discovered objectives:");
                    objectives = journal.discoveredObjectives;
                    break;
                case COMPLETED:
                    prompt.addQuestion(CommonColors.INFO+"Completed objectives:");
                    objectives = journal.completedObjectives;
                    break;
            }
            objectives.forEach(objective -> prompt.addAnswer(objective.getIdentifier()+CommonColors.INFO+" - "+objective.getStatus()+CommonColors.INFO+" - "+CommonColors.MESSAGE+objective.getDescription(), ""));
            prompt.display(sender);
            return true;
        }

        // Add command
        if(argsList.get(0).equalsIgnoreCase("add")){
            argsList.remove(0);
            String objectiveId = String.join(" ", argsList);
            Objective objective = Objective.loadObjective(objectiveId);

            switch(targetSection){
                case ALL: case ACTIVE:
                    journal.addObjective(objective);
                    break;
                case DISCOVERED:
                    journal.addDiscoveredObjective(objective);
                    break;
                case COMPLETED:
                    journal.completeObjective(objective);
                    journal.completedObjectives.add(objective);
                    break;
            }

            sender.sendMessage(CommonColors.MESSAGE+"Added to Journal: "+objectiveId);
            return true;
        }

        // Remove command
        if(argsList.get(0).equalsIgnoreCase("remove")){
            argsList.remove(0);
            String objectiveId = String.join(" ", argsList);
            Objective objective = journal.getActiveObjective(objectiveId);
            if(objective!=null){
                journal.activeObjectives.remove(objective);
                journal.discoveredObjectives.remove(objective);
                journal.completedObjectives.remove(objective);
                sender.sendMessage(CommonColors.MESSAGE+"Removed from Journal: "+objectiveId);
                return true;
            } else {
                sender.sendMessage(CommonColors.ERROR+"Journal does not contain objective: "+objectiveId);
                return false;
            }
        }

        // Reset command
        if(argsList.get(0).equalsIgnoreCase("reset")){
            Journal.playerJournals.remove(character);
            sender.sendMessage(CommonColors.MESSAGE+"Journal reset for "+character.getName());
            return true;
        }


        return Error.INVALID_ARGS.displayActionBar(sender);
	}

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if(args.length>1 && args[args.length-2].startsWith("-p")){
            return null;
        }

        return Arrays.asList("list", "add", "remove", "reset", "-player", "-active", "-discovered", "-completed");
    }

}