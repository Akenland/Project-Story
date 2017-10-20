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
 * A tag that contains behaviors and conversations for NPCs.
 */
public class NPCTag extends Tag {

    // Whether this tag's data has been loaded from file.
    private boolean loaded;

    //// CONVERSATIONS
    Map<String,Prompt> prompts = new HashMap<String,Prompt>();
    /*// The initial question(s) for the NPC to ask, and the conditions for displaying those questions
    private List<String> questions;
    private List<String> questionConditions;
    // Whether to display a single question (chosen at random), instead of all questions in order
    private boolean randomizeQuestions;
    // Answers and actions
	private List<String> answers;
	private List<String> actions;
    private List<String> answerConditions;*/
    
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
            
            //// Conversations
            // Prompts - load per condition
            ConfigurationSection promptFile = tag.getData().getConfigurationSection("prompts");
            if(promptFile!=null){
                for(String key : promptFile.getKeys(false)){
                    Prompt prompt = new Prompt(promptFile.getConfigurationSection(key));
                    prompts.putIfAbsent(key, prompt);
                }
            }

            /*/ Conversations
            if(questions==null || questions.isEmpty()){
                questions = tag.getData().getStringList("conversations.questions");
                questionConditions = tag.getData().getStringList("conversations.question-conditions");
                randomizeQuestions = tag.getData().getBoolean("conversations.random-question");
            }
            if(answers==null || answers.isEmpty()){
                answers = tag.getData().getStringList("conversations.answers");
                actions = tag.getData().getStringList("conversations.actions");
                answerConditions = tag.getData().getStringList("conversations.answer-conditions");
            }*/

            // Title
            if(title==null || title.isEmpty()) title = tag.getData().getString("realmscore.title");
        }
        

        loaded = true;
    }


    /**
     * Display the appropriate conversation to a player.
     * @param player the player that sees the conversation
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
                .replace("PLAYER_NAME", playerName)
                .replace("PLAYER_TITLE", playerTitle)
                .replace("PLAYER_REALM", playerRealm)
                .replace("NPC_NAME", npcName)
                .replace("NPC_TITLE", npcTitle)
                .replace("NPC_REALM", npcRealm);
            questions.add(i, question);
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
                .replace("PLAYER_NAME", playerName)
                .replace("PLAYER_TITLE", playerTitle)
                .replace("PLAYER_REALM", playerRealm)
                .replace("NPC_NAME", npcName)
                .replace("NPC_TITLE", npcTitle)
                .replace("NPC_REALM", npcRealm);
            answers.add(i, answer);

            // Replace THISNPC with npc_ID in all actions
            String action = actions.get(i);
            actions.remove(i);
            action = action.replace("NPC_PROMPTS", "npc_"+npc.getId());
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
     * Display the appropriate conversation to a player.
     * @param player the player that sees the conversation
     * @param npc the {@link NPC} that says the conversation
     */
    public void displayConversation(Player player, NPC npc){
        displayConversation(player, npc, null);
    }
}