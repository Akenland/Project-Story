package com.kylenanakdewa.story.tags;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.block.BlockState;
import org.bukkit.block.Container;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;

import com.kylenanakdewa.core.characters.players.PlayerCharacter;
import com.kylenanakdewa.core.common.Utils;
import com.kylenanakdewa.story.StoryPlugin;
import com.kylenanakdewa.story.tags.taggable.TaggedEntity;
import com.kylenanakdewa.story.tags.taggable.TempNPC;

import net.citizensnpcs.api.npc.NPC;

/**
 * A tag that contains behaviors and conversations for an {@link NPC}.
 */
public class NPCTag extends Tag {

    // Whether this tag's data has been loaded from file.
    private boolean loaded;

    //// CONVERSATIONS
    private Map<String,Interaction> prompts = new LinkedHashMap<String,Interaction>();

    //// REALMSCORE DATA
    // The title of NPCs with this tag
    private String title;

    //// NPC DATA
    // Vulnerability
    private boolean invulnerable;
    // Equipment chest
    private Inventory equipmentChest;
    // Skin username
    private String skinName;

    //// SENTINEL DATA
    // Non-regex targets
    private HashSet<String> targets;
    private HashSet<String> ignores;
    // Held item regex targets
    private List<String> heldItemTargets;
    private List<String> heldItemIgnores;
    // Event targets
    private List<String> eventTargets;
    // Other/Tag targets
    private List<String> otherTargets;
    private List<String> otherIgnores;


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
                    Interaction prompt = Interaction.getFromConfig(promptFile.getConfigurationSection(key));
                    prompts.putIfAbsent(key, prompt);
                }
            }

            // Title
            if(title==null || title.isEmpty()) title = tag.getData().getString("data.title");

            // Invulnerability
            if(tag.getData().isBoolean("invulnerable")){
                invulnerable = tag.getData().getBoolean("invulnerable");
            }

            // Equipment chest
            if(equipmentChest==null){
                String unsplitLocString = tag.getData().getString("equipment");
                if(unsplitLocString!=null){
                    String[] locString = unsplitLocString.split(" ");
                    if(locString.length==4){
                        BlockState chest = new Location(Bukkit.getWorld(locString[0]), Double.parseDouble(locString[1]), Double.parseDouble(locString[2]), Double.parseDouble(locString[3])).getBlock().getState();
                        if(chest instanceof Container) equipmentChest = ((Container)chest).getInventory();
                        else Utils.notifyAdmins("Tag "+tag.getName()+" equipment chest: Expected container, found "+chest.getType());
                    }
                }
            }

            // Skin
            if(skinName==null) skinName = tag.getData().getString("skin");

            // Sentinel targets
            ConfigurationSection sentinelFile = tag.getData().getConfigurationSection("sentinel");
            if(sentinelFile!=null){
                if(sentinelFile.contains("targets")){
                    if(targets==null) targets = new HashSet<String>();
                    targets.addAll(sentinelFile.getStringList("targets"));
                }
                if(sentinelFile.contains("ignores")){
                    if(ignores==null) ignores = new HashSet<String>();
                    ignores.addAll(sentinelFile.getStringList("ignores"));
                }
                if(sentinelFile.contains("heldItemTargets")){
                    if(heldItemTargets==null) heldItemTargets = new ArrayList<String>();
                    heldItemTargets.addAll(sentinelFile.getStringList("heldItemTargets"));
                }
                if(sentinelFile.contains("heldItemIgnores")){
                    if(heldItemIgnores==null) heldItemIgnores = new ArrayList<String>();
                    heldItemIgnores.addAll(sentinelFile.getStringList("heldItemIgnores"));
                }
                if(sentinelFile.contains("eventTargets")){
                    if(eventTargets==null) eventTargets = new ArrayList<String>();
                    eventTargets.addAll(sentinelFile.getStringList("eventTargets"));
                }
                if(sentinelFile.contains("otherTargets")){
                    if(otherTargets==null) otherTargets = new ArrayList<String>();
                    otherTargets.addAll(sentinelFile.getStringList("otherTargets"));
                }
                if(sentinelFile.contains("otherIgnores")){
                    if(otherIgnores==null) otherIgnores = new ArrayList<String>();
                    otherIgnores.addAll(sentinelFile.getStringList("otherIgnores"));
                }
                if(sentinelFile.contains("tagTargets")){
                    if(otherTargets==null) otherTargets = new ArrayList<String>();
                    for(String target : sentinelFile.getStringList("tagTargets")){
                        target = target.replace("NPC_REALM", getRealm()!=null ? getRealm().getName() : "norealm");
                        otherTargets.add("tag:"+target);
                    }
                }
                if(sentinelFile.contains("tagIgnores")){
                    if(otherIgnores==null) otherIgnores = new ArrayList<String>();
                    for(String target : sentinelFile.getStringList("tagIgnores")){
                        target = target.replace("NPC_REALM", getRealm()!=null ? getRealm().getName() : "norealm");
                        otherIgnores.add("tag:"+target);
                    }
                }
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
        Map<String,Interaction> availablePrompts = new LinkedHashMap<String,Interaction>();
        for(Map.Entry<String,Interaction> p : prompts.entrySet()){
            String s[] = p.getKey().split("cond:", 2);
            String newPromptName = s[0].trim();

            // Replace variables in condition
            if(s.length==2){
                s[1] = s[1].replace("NPC_REALM", getRealm()!=null ? getRealm().getIdentifier() : "norealm");
                Utils.notifyAdmins("Fixed condition: "+s[1]);
            }

            // If condition is missing, or evaluates to true, add prompt to availablePrompts
            if(s.length==1 || new Condition(s[1]).eval(new TaggedEntity(player)))
                availablePrompts.putIfAbsent(newPromptName, p.getValue());
        }

        Interaction conversation = availablePrompts.get(promptName);
        if(conversation==null) return;


        // Format all strings with player and NPC name
        PlayerCharacter character = PlayerCharacter.getCharacter(player);
        String playerName = character.getName();
        String playerTitle = character.getTitle().length()<2 ? "explorer" : character.getTitle();
        String playerRealm = character.getRealm()!=null ? character.getRealm().getName() : "Akenland";
        String npcName = npc.getFullName();
        String npcTitle = title!=null ? (title.length()>2 ? title : title+"citizen" ) : "citizen";
        String npcRealm = getRealm()!=null ? getRealm().getName() : "Akenland";
        String npcLoc = !getLocationData().getDisplayNames().isEmpty() ? getLocationData().getDisplayName() : "the "+npcRealm;
        String npcLocDirection = !getLocationData().getDirectionalNames().isEmpty() ? getLocationData().getDirectionalName() : "in the "+npcRealm;

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
                .replace("NPC_LOCATION", npcLoc + ChatColor.WHITE)
                .replace("NPC_LOCATION_DESCRIPTION", npcLocDirection + ChatColor.WHITE);
            questions.add(i, ChatColor.WHITE + question);
        }
        conversation.setQuestions(questions);
        // Format answers
        conversation.getAnswers().forEach(answer -> {
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
                .replaceAction("thisnpc", "npc_"+npc.getId())
                .replaceAction("thisNPC", "npc_"+npc.getId())
                .replaceAction("THISNPC", "npc_"+npc.getId())
                .replaceAction("PLAYER_USERNAME", player.getName());
        });

        // Prepare the prompt
        conversation.start(player, new TempNPC(npc));

        // Pause the NPC's movement
        if(!npc.getNavigator().isPaused()){
            npc.getNavigator().setPaused(true);
            int delay = conversation.isRandomQuestions() ? 30 : (conversation.getQuestions().size())*30;
            Bukkit.getScheduler().scheduleSyncDelayedTask(StoryPlugin.plugin, () -> npc.getNavigator().setPaused(false), delay);
        }
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
     * Gets whether this NPC is invulnerable/protected.
     */
    public boolean isInvulnerable(){
        load();
        return invulnerable;
    }

    /**
     * Gets the equipment chest that NPCs with this tag can take from.
     * @return the {@link Inventory} that NPCs with this tag can take items from, or null if not defined
     */
    public Inventory getEquipmentChest(){
        load();
        return equipmentChest;
    }

    /**
     * Gets the username for a skin.
     * @return the username of the player whose skin should be used on NPCs with this tag
     */
    public String getSkin(){
        load();
        return skinName;
    }


    /**
     * Gets the Sentinel targets for NPCs with this tag.
     */
    public HashSet<String> getTargets(){
        load();
        return targets;
    }
    /**
     * Gets the Sentinel ignores for NPCs with this tag.
     */
    public HashSet<String> getIgnores(){
        load();
        return ignores;
    }
    /**
     * Gets the Sentinel heldItem targets for NPCs with this tag.
     */
    public List<String> getHeldItemTargets(){
        load();
        return heldItemTargets;
    }
    /**
     * Gets the Sentinel heldItem ignores for NPCs with this tag.
     */
    public List<String> getHeldItemIgnores(){
        load();
        return heldItemIgnores;
    }
    /**
     * Gets the Sentinel event targets for NPCs with this tag.
     */
    public List<String> getEventTargets(){
        load();
        return eventTargets;
    }
    /**
     * Gets the Sentinel tag targets for NPCs with this tag.
     */
    public List<String> getTagTargets(){
        load();
        return otherTargets;
    }
    /**
     * Gets the Sentinel tag ignores for NPCs with this tag.
     */
    public List<String> getTagIgnores(){
        load();
        return otherIgnores;
    }


    public String getTitle(){
        return title;
    }
}