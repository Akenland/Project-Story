package com.kylenecrowolf.realmsstory.tags;

import java.util.List;

import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

import com.KyleNecrowolf.RealmsCore.Player.PlayerData;
import com.KyleNecrowolf.RealmsCore.Prompts.Prompt;

/**
 * A tag that contains behaviors and conversations for NPCs.
 */
public class NPCTag extends Tag {

    // Whether this tag's data has been loaded from file.
    private boolean loaded;

    //// CONVERSATIONS
    // The initial question(s) for the NPC to ask, and the conditions for displaying those questions
    private List<String> questions;
    private List<String> questionConditions;
    // Whether to display a single question (chosen at random), instead of all questions in order
    private boolean randomizeQuestions;
    // Answers and actions
	private List<String> answers;
	private List<String> actions;
    private List<String> answerConditions;
    
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
            
            // Conversations
            if(questions==null || questions.isEmpty()){
                questions = tag.getData().getStringList("conversations.questions");
                questionConditions = tag.getData().getStringList("conversations.question-conditions");
                randomizeQuestions = tag.getData().getBoolean("conversations.random-question");
            }
            if(answers==null || answers.isEmpty()){
                answers = tag.getData().getStringList("conversations.answers");
                actions = tag.getData().getStringList("conversations.actions");
                answerConditions = tag.getData().getStringList("conversations.answer-conditions");
            }

            // Title
            if(title==null || title.isEmpty()) title = tag.getData().getString("realmscore.title");
        }
        

        loaded = true;
    }


    /**
     * Display the appropriate conversation to a player.
     * @param player the player that sees the conversation
     * @param npcName the name of the NPC, shown as a prefix for conversation messages
     */
    public void displayConversation(Player player, String npcName){
        load();

        if(questions==null || answers==null) return;

        // Format all strings with player and NPC name
        PlayerData data = new PlayerData(player);
        String playerName = player.getDisplayName();
        String playerTitle = data.getTitle(); playerTitle = playerTitle.length()<2 ? "explorer" : playerTitle;
        String playerRealm = (data.getRealm()!=null && data.getRealm().exists()) ? data.getRealm().getFullName() : "Akenland";
        String npcTitle = title!=null ? title : "citizen";
        String npcRealm = (getRealm()!=null && getRealm().exists()) ? getRealm().getFullName() : "Akenland";
        for(int i=0; i<questions.size(); i++){
            String question = questions.get(i);
            questions.remove(i);
            question = question.replace("PLAYER_NAME", playerName);
            question = question.replace("PLAYER_TITLE", playerTitle);
            question = question.replace("PLAYER_REALM", playerRealm);
            question = question.replace("NPC_NAME", npcName);
            question = question.replace("NPC_TITLE", npcTitle);
            question = question.replace("NPC_REALM", npcRealm);
            questions.add(i, question);
        }
        for(int i=0; i<answers.size(); i++){
            String answer = answers.get(i);
            answers.remove(i);
            answer = answer.replace("PLAYER_NAME", playerName);
            answer = answer.replace("PLAYER_TITLE", playerTitle);
            answer = answer.replace("PLAYER_REALM", playerRealm);
            answer = answer.replace("NPC_NAME", npcName);
            answer = answer.replace("NPC_TITLE", npcTitle);
            answer = answer.replace("NPC_REALM", npcRealm);
            answers.add(i, answer);
        }
        /*for(String question : questions){
            question = question.replace("PLAYER_NAME", playerName);
            question = question.replace("PLAYER_TITLE", playerTitle);
            question = question.replace("PLAYER_REALM", playerRealm);
            question = question.replace("NPC_NAME", npcName);
        }
        for(String answer : answers){
            answer = answer.replace("PLAYER_NAME", playerName);
            answer = answer.replace("PLAYER_TITLE", playerTitle);
            answer = answer.replace("PLAYER_REALM", playerRealm);
            answer = answer.replace("NPC_NAME", npcName);
        }*/

        // Format NPC name
        ChatColor realmColor = getRealm()!=null ? getRealm().getColor() : ChatColor.GRAY;
        ChatColor topRealmColor = getRealm()!=null ? getRealm().getTopParent().getColor() : realmColor;
        String formattedTitle = title!=null && !title.isEmpty() ? title+" " : "";
        String formattedNPCName = topRealmColor+"<"+realmColor+formattedTitle+npcName+topRealmColor+"> ";

        // Prepare the prompt
        new Prompt(questions, questionConditions, randomizeQuestions, answers, actions, answerConditions).display(player, formattedNPCName);
    }
}