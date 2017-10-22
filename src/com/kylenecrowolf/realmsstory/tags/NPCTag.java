package com.kylenecrowolf.realmsstory.tags;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.bukkit.ChatColor;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;

import com.KyleNecrowolf.RealmsCore.Player.PlayerData;
import com.KyleNecrowolf.RealmsCore.Prompts.Prompt;

import net.citizensnpcs.api.npc.NPC;

/**
 * A tag that contains behaviors and conversations for an {@link NPC}.
 */
public class NPCTag extends Tag {

    // Whether this tag's data has been loaded from file.
    private boolean loaded;

    //// CONVERSATIONS
    Map<String,Prompt> prompts = new HashMap<String,Prompt>();
    
    //// REALMSCORE DATA
    // The title of NPCs with this tag
    private String title;


    public NPCTag(String name){
        super(name);
    }

    public NPCTag(List<Tag> tags){
        super(tags);
    }

    private void load(){
        // If already loaded, return
        if(loaded) return;

        // Iterate through tags 
        for(Tag tag : getTotalInheritedTags()){

            // Prompts - load per condition
            ConfigurationSection promptFile = tag.getData().getConfigurationSection("prompts");
            if(promptFile!=null){
                for(String key : promptFile.getKeys(false)){
                    Prompt prompt = new Prompt(promptFile.getConfigurationSection(key));
                    prompts.putIfAbsent(key, prompt);
                }
            }

            // Title
            if(title==null || title.isEmpty()) title = tag.getData().getString("realmscore.title");
        }

        loaded = true;
    }


    /**
     * Display the appropriate conversation to a {@link Player}.
     * @param player the {@link Player} that sees the conversation
     * @param npc the {@link NPC} that says the conversation
     * @param promptName the prompt to display, or null to display onInteract prompt
     */
    public void displayConversation(Player player, NPC npc, String promptName){
        load();

        if(prompts.isEmpty()) return;

        // Get the appropriate prompt
        promptName = (promptName!=null) ? promptName : "onInteract";
        // TODO check conditions
        Prompt conversation = prompts.get(promptName);
        if(conversation==null) return;


        // Format all strings with player and NPC name
        PlayerData data = new PlayerData(player);
        String playerName = player.getDisplayName();
        String playerTitle = data.getTitle(); playerTitle = playerTitle.length()<2 ? "explorer" : playerTitle;
        String playerRealm = (data.getRealm()!=null && data.getRealm().exists()) ? data.getRealm().getFullName() : "Akenland";
        String npcName = npc.getFullName();
        String npcTitle = title!=null ? title : "citizen";
        String npcRealm = (getRealm()!=null && getRealm().exists()) ? getRealm().getFullName() : "Akenland";

        // Format questions
        List<String> questions = conversation.getQuestions();
        for(int i=0; i<questions.size(); i++){
            String question = questions.get(i);
            questions.remove(i);
            question = question
                .replace("PLAYER_NAME", playerName + ChatColor.WHITE)
                .replace("PLAYER_TITLE", playerTitle + ChatColor.WHITE)
                .replace("PLAYER_REALM", playerRealm + ChatColor.WHITE)
                .replace("NPC_NAME", npcName + ChatColor.WHITE)
                .replace("NPC_TITLE", npcTitle + ChatColor.WHITE)
                .replace("NPC_REALM", npcRealm + ChatColor.WHITE);
            questions.add(i, ChatColor.WHITE + question);
        }
        conversation.setQuestions(questions);
        // Format answers
        Map<String,String> promptAnswers = conversation.getAnswers();
        List<String> answers = new ArrayList<String>(promptAnswers.keySet());
        List<String> actions = new ArrayList<String>(promptAnswers.values());
        for(int i=0; i<answers.size(); i++){
            String answer = answers.get(i);
            answers.remove(i);
            answer = answer
                .replace("PLAYER_NAME", playerName + ChatColor.GRAY)
                .replace("PLAYER_TITLE", playerTitle + ChatColor.GRAY)
                .replace("PLAYER_REALM", playerRealm + ChatColor.GRAY)
                .replace("NPC_NAME", npcName + ChatColor.GRAY)
                .replace("NPC_TITLE", npcTitle + ChatColor.GRAY)
                .replace("NPC_REALM", npcRealm + ChatColor.GRAY);
            answers.add(i, answer);

            // Replace THISNPC with npc_ID in all actions
            String action = actions.get(i).toLowerCase();
            actions.remove(i);
            action = action.replace("thisnpc", "npc_"+npc.getId());
            actions.add(i, action);
        }
        conversation.setAnswers(answers, actions);

        // Format NPC name
        ChatColor realmColor = getRealm()!=null ? getRealm().getColor() : ChatColor.GRAY;
        ChatColor topRealmColor = getRealm()!=null ? getRealm().getTopParent().getColor() : realmColor;
        String formattedTitle = title!=null && !title.isEmpty() ? title+" " : "";
        String formattedNPCName = topRealmColor+"<"+realmColor+formattedTitle+npcName+topRealmColor+"> ";

        // Prepare the prompt
        conversation.display(player, formattedNPCName);
    }
    /**
     * Display the appropriate conversation to a {@link Player}.
     * @param player the {@link Player} that sees the conversation
     * @param npc the {@link NPC} that says the conversation
     */
    public void displayConversation(Player player, NPC npc){
        displayConversation(player, npc, null);
    }
}