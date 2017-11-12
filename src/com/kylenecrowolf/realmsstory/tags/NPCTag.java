package com.kylenecrowolf.realmsstory.tags;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.block.BlockState;
import org.bukkit.block.Container;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;

import com.KyleNecrowolf.RealmsCore.Player.PlayerData;
import com.KyleNecrowolf.RealmsCore.Prompts.Prompt;
import com.kylenecrowolf.realmsstory.tags.taggable.TaggedEntity;
import net.citizensnpcs.api.npc.NPC;

/**
 * A tag that contains behaviors and conversations for an {@link NPC}.
 */
public class NPCTag extends Tag {

    // Whether this tag's data has been loaded from file.
    private boolean loaded;

    //// CONVERSATIONS
    Map<String,Prompt> prompts = new LinkedHashMap<String,Prompt>();
    
    //// REALMSCORE DATA
    // The title of NPCs with this tag
    private String title;

    //// NPC DATA
    // Equipment chest
    Inventory equipmentChest;
    // Skin UUID
    UUID skinUUID;


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
            if(title==null || title.isEmpty()) title = tag.getData().getString("data.title");

            // Equipment chest
            if(equipmentChest==null){
                String unsplitLocString = tag.getData().getString("equipment");
                if(unsplitLocString!=null){
                    String[] locString = unsplitLocString.split(" ");
                    if(locString.length==4){
                        BlockState chest = new Location(Bukkit.getWorld(locString[0]), Double.parseDouble(locString[1]), Double.parseDouble(locString[2]), Double.parseDouble(locString[3])).getBlock().getState();
                        if(chest instanceof Container) equipmentChest = ((Container)chest).getInventory();
                    }
                }
            }

            // Skin UUID
            if(skinUUID==null){
                String uuidString = tag.getData().getString("skin");
                if(uuidString!=null) skinUUID = UUID.fromString(uuidString);
            }
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

        // Get a list of available prompts, based on conditions
        Map<String,Prompt> availablePrompts = new LinkedHashMap<String,Prompt>();
        for(Map.Entry<String,Prompt> p : prompts.entrySet()){
            String s[] = p.getKey().split("cond:", 2);
            String newPromptName = s[0].trim();
            // If condition is missing, or evaluates to true, add prompt to availablePrompts
            if(s.length==1 || new Condition(s[1]).eval(new TaggedEntity(player)))
                availablePrompts.putIfAbsent(newPromptName, p.getValue());
        }

        Prompt conversation = availablePrompts.get(promptName);
        if(conversation==null) return;


        // Format all strings with player and NPC name
        PlayerData data = new PlayerData(player);
        String playerName = player.getDisplayName();
        String playerTitle = data.getTitle(); playerTitle = playerTitle.length()<2 ? "explorer" : playerTitle;
        String playerRealm = (data.getRealm()!=null && data.getRealm().exists()) ? data.getRealm().getFullName() : "Akenland";
        String npcName = npc.getFullName();
        String npcTitle = title!=null ? (title.length()>2 ? title : title+"citizen" ) : "citizen";
        String npcRealm = (getRealm()!=null && getRealm().exists()) ? getRealm().getFullName() : "Akenland";
        String npcLoc = getLocationName()!=null ? getLocationName() : npcRealm;

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
                .replace("NPC_REALM", npcRealm + ChatColor.WHITE)
                .replace("NPC_LOCATION", npcLoc + ChatColor.WHITE);
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
                .replace("NPC_REALM", npcRealm + ChatColor.GRAY)
                .replace("NPC_LOCATION", npcLoc + ChatColor.GRAY);
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
        String formattedTitle = title!=null ? (title.length()>2 ? title+" " : title) : "";
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


    /**
     * Gets the equipment chest that NPCs with this tag can take from.
     * @return the {@link Inventory} that NPCs with this tag can take items from
     */
    public Inventory getEquipmentChest(){
        return equipmentChest;
    }

    /**
     * Gets the UUID for a skin.
     * @return the {@link UUID} of the player whose skin should be used on NPCs with this tag
     */
    public UUID getSkin(){
        return skinUUID;
    }

}