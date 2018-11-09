package com.kylenanakdewa.story.tags.taggable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Entity;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import com.kylenanakdewa.core.characters.players.PlayerCharacter;
import com.kylenanakdewa.core.common.CommonColors;
import com.kylenanakdewa.core.common.Utils;
import com.kylenanakdewa.core.common.prompts.Prompt;
import com.kylenanakdewa.core.common.prompts.PromptActionEvent;
import com.kylenanakdewa.story.StoryPlugin;
import com.kylenanakdewa.story.journal.Journal;
import com.kylenanakdewa.story.quests.objectives.AutoQuest;
import com.kylenanakdewa.story.quests.objectives.NPCTalkObjective;
import com.kylenanakdewa.story.quests.objectives.Objective;
import com.kylenanakdewa.story.tags.Condition;
import com.kylenanakdewa.story.tags.NPCTag;
import com.kylenanakdewa.story.tags.Tag;
import com.kylenanakdewa.story.utils.SentinelNPC;

import net.citizensnpcs.api.CitizensAPI;
import net.citizensnpcs.api.event.DespawnReason;
import net.citizensnpcs.api.event.NPCDamageByEntityEvent;
import net.citizensnpcs.api.event.NPCDespawnEvent;
import net.citizensnpcs.api.event.NPCRightClickEvent;
import net.citizensnpcs.api.npc.NPC;
import net.citizensnpcs.api.persistence.Persist;
import net.citizensnpcs.api.trait.Trait;
import net.citizensnpcs.api.trait.TraitName;
import net.citizensnpcs.api.trait.trait.Equipment;
import net.citizensnpcs.api.trait.trait.Equipment.EquipmentSlot;

/**
 * Represents a Citizens NPC Trait with RealmsStory Tags.
 */
@TraitName("tags")
public class TaggedNPC extends Trait implements Taggable {

    /**
     * All tags applied to this NPC
     */
    private List<Tag> tags;

    /**
     * Tags saved to this NPC in the saves file
     */
    @Persist("tags") private List<String> savedTags;


    public TaggedNPC(){
        super("tags");
    }


    /**
     * Gets a random NPC that meets the specified condition.
     */
    public static TaggedNPC getRandomNPC(Condition condition){
        List<NPC> npcs = new ArrayList<NPC>();
        CitizensAPI.getNPCRegistry().forEach(npc -> {
            if(npc.hasTrait(TaggedNPC.class) && condition.eval(npc.getTrait(TaggedNPC.class))) npcs.add(npc);
        });

        return npcs.get(new Random().nextInt(npcs.size())).getTrait(TaggedNPC.class);
    }


    public List<Tag> getTags(){
        if(tags==null){
            tags = new ArrayList<Tag>();
            // Load tags from NPC saves
            if(savedTags!=null) for(String tag : savedTags) tags.addAll(Tag.get(tag).getTotalInheritedTags());

            // Load other traits
            //for(Trait trait : getNPC().getTraits()) tags.addAll(new Tag(trait.getName()).getTotalInheritedTags());
        }
        return tags;       
    }

    public NPCTag getTag(){
        return new NPCTag(getTags());
    }

    public boolean hasTag(Tag... tags){
        // Get a list of the names of all tags
        List<String> tagNames = new ArrayList<String>();
        for(Tag t : getTags()) tagNames.add(t.getName());
        List<String> checkTagNames = new ArrayList<String>();
        for(Tag t : tags) checkTagNames.add(t.getName());

        // Return true if all tag names are found
        return tagNames.containsAll(checkTagNames);
    }

    public void addTag(Tag tag){
        if(savedTags==null) savedTags = new ArrayList<String>();
        savedTags.add(0, tag.getName());
        reload();
    }

    /**
     * Removes a tag from this object.
     * This method will only remove tags saved to this NPC; it will NOT remove inherited tags.
     * @param tag the tag to be removed from this object
     */
    public void removeTag(Tag tag){
        savedTags.remove(tag.getName());
        reload();
    }

    public void removeAllTags(){
        savedTags.clear();
        reload();
    }

    public void reload(){
        tags = null;

        // Respawn the NPC
        npc.despawn();
        npc.spawn(npc.getStoredLocation());
    }


    public void displayInfo(CommandSender sender){
        Prompt prompt = new Prompt();
        prompt.addQuestion(CommonColors.INFO+"--- NPC: "+CommonColors.MESSAGE+getNPC().getFullName()+CommonColors.INFO+" "+getNPC().getId()+" ---");
        prompt.addQuestion(CommonColors.INFO+"Has the following tags:");
        for(Tag tag : getTags()){
            prompt.addAnswer(tag.getName(),"");
        }
        prompt.display(sender);
    }


    /**
     * Player chat event. Looks for the most significant conversation in tags, and displays it to player.
     */
    @EventHandler
    public void onPlayerInteract(NPCRightClickEvent event){
        if(event.getNPC() != this.getNPC()) return;

        PlayerCharacter player = PlayerCharacter.getCharacter(event.getClicker());
        
        // NPC should face the player
        npc.faceLocation(event.getClicker().getEyeLocation());

        // Clear the objective
        Objective objective = Journal.get(player).getActiveObjective("talknpc_" + npc.getId());
        if(!(objective instanceof NPCTalkObjective)) objective = Objective.loadObjective(objective.getIdentifier());
        if(objective!=null){
            // Set the NPC for the completion interaction
            if(objective.getCompletionInteraction()!=null) objective.getCompletionInteraction().setCharacter(new TempNPC(npc));
            objective.setCompleted();
            return;
        }

        // Display the conversation
        getTag().displayConversation(event.getClicker(), event.getNPC());
    }
    /**
     * Player chat response event. Displays the requested prompt when player chooses a response to a previous conversation.
     */
    @EventHandler
    public void onPlayerPromptResponse(PromptActionEvent event){
        if(event.isType("npc")){
            // Get target NPC
            String action[] = event.getAction().split("\\.", 2);
            TaggedNPC npc = getTaggedNPC(Integer.parseInt(action[0]));
            if(npc.getNPC() != this.getNPC()) return;
            String npcAction = action[1];

            // Display prompt
            ((NPCTag)npc.getTag()).displayConversation(event.getPlayer(), npc.getNPC(), npcAction);

            //// Extra actions
            // AutoQuest
            if(npcAction.equalsIgnoreCase("autoquest")){
                Journal.get(PlayerCharacter.getCharacter(event.getPlayer())).addObjective(new AutoQuest(getTags()));
            }
            // Get equipment
            if(npcAction.equalsIgnoreCase("equip")){
                equip();
            }
            if(npcAction.equalsIgnoreCase("unequip")){
                unequip();
            }
            // Sentinel actions
            if(npcAction.startsWith("sentinel") && StoryPlugin.sentinelEnabled){
                String sentinelAction = npcAction.split("_", 2)[1];
                SentinelNPC sentinel = new SentinelNPC(getNPC());

                // Guard player
                if(sentinelAction.equalsIgnoreCase("guardPlayer")){
                    sentinel.guard(event.getPlayer());
                }
                // Guard nearest player (except the one who chose this option)
                if(sentinelAction.equalsIgnoreCase("guardNearest")){
                    // Get entities in 3 radius
                    List<Entity> near = getNPC().getEntity().getNearbyEntities(3, 3, 3);
                    // Only look at players, and exclude sending player
                    near.retainAll(Bukkit.getOnlinePlayers());
                    near.remove(event.getPlayer());
                    // If none found, guard spot
                    if(!near.isEmpty()) sentinel.guard((LivingEntity)near.get(0));
                    else sentinel.guard(null);
                }
                // Guard current location
                if(sentinelAction.equalsIgnoreCase("guardSpot")){
                    sentinel.guard(null);
                }

                // Attack nearest LivingEntity that isn't the player
                if(sentinelAction.equalsIgnoreCase("attackNearest")){
                    // Get entities in 40 radius
                    List<Entity> near = getNPC().getEntity().getNearbyEntities(20, 20, 20);
                    // Exclude sending player
                    near.remove(event.getPlayer());
                    for(Entity e:near){
                        // Attack first entity found
                        if(e instanceof LivingEntity){
                            sentinel.attack((LivingEntity) e);
                            break;
                        }
                    }
                }
                // Stop attacking
                if(sentinelAction.equalsIgnoreCase("stopAttack")){
                    sentinel.stopAttack();
                }
            }
        }
    }

    /**
     * Runs when this NPC is attacked.
     */
    @EventHandler
    public void onAttacked(NPCDamageByEntityEvent event){
        if(event.getNPC() != this.getNPC()) return;

        // Show a conversation
        if(event.getDamager() instanceof Player){
            getTag().displayConversation((Player)event.getDamager(), npc, "onAttacked");
        }
    }

    /**
     * Runs when this NPC dies.
     */
    @EventHandler
    public void onDeath(NPCDespawnEvent event){
        if(event.getNPC()!=this.getNPC() || event.getReason()!=DespawnReason.DEATH) return;

        // If this NPC has an equipment chest, drop equipment on death
        if(getTag().getEquipmentChest()!=null){
            Utils.notifyAdmins("NPC "+npc.getFullName()+" is dropping equipment on death!");
            unequip();
        } else Utils.notifyAdmins("NPC "+npc.getFullName()+" didn't have an equipment chest, Citizens will handle drops.");
    }

    /**
     * Runs when this NPC is spawned.
     */
    @Override
    public void onSpawn(){
        switchSkin();
        if(!npc.isProtected()) npc.setProtected(getTag().isInvulnerable());
        equip();
        setSentinelTargets();
    }


    /**
     * Tells this NPC to put on equipment from their equipment chest.
     */
    public void equip(){
        if(npc.getEntity() instanceof HumanEntity){
            // Check equipment
            final Inventory equipmentChest = getTag().getEquipmentChest();
            if(equipmentChest==null) return;

            final Equipment equipment = npc.getTrait(Equipment.class);

            // Equip a helmet
            if(equipment.get(EquipmentSlot.HELMET)==null){
                // Find best helmet
                int helmetSlot = -1;
                if(helmetSlot==-1) helmetSlot = equipmentChest.first(Material.DIAMOND_HELMET);
                if(helmetSlot==-1) helmetSlot = equipmentChest.first(Material.IRON_HELMET);
                if(helmetSlot==-1) helmetSlot = equipmentChest.first(Material.CHAINMAIL_HELMET);
                if(helmetSlot==-1) helmetSlot = equipmentChest.first(Material.GOLD_HELMET);
                if(helmetSlot==-1) helmetSlot = equipmentChest.first(Material.LEATHER_HELMET);
                if(helmetSlot==-1) helmetSlot = equipmentChest.first(Material.PUMPKIN);
                if(helmetSlot==-1) helmetSlot = equipmentChest.first(Material.END_ROD);

                // Put on helmet
                if(helmetSlot!=-1){
                    equipment.set(EquipmentSlot.HELMET, equipmentChest.getItem(helmetSlot));
                    equipmentChest.clear(helmetSlot);
                }
            }
            // Equip a chestplate
            if(equipment.get(EquipmentSlot.CHESTPLATE)==null){
                // Find best chestplate
                int chestplateSlot = -1;
                if(chestplateSlot==-1) chestplateSlot = equipmentChest.first(Material.DIAMOND_CHESTPLATE);
                if(chestplateSlot==-1) chestplateSlot = equipmentChest.first(Material.IRON_CHESTPLATE);
                if(chestplateSlot==-1) chestplateSlot = equipmentChest.first(Material.CHAINMAIL_CHESTPLATE);
                if(chestplateSlot==-1) chestplateSlot = equipmentChest.first(Material.GOLD_CHESTPLATE);
                if(chestplateSlot==-1) chestplateSlot = equipmentChest.first(Material.LEATHER_CHESTPLATE);

                // Put on chestplate
                if(chestplateSlot!=-1){
                    equipment.set(EquipmentSlot.CHESTPLATE, equipmentChest.getItem(chestplateSlot));
                    equipmentChest.clear(chestplateSlot);
                }
            }
            // Equip leggings
            if(equipment.get(EquipmentSlot.LEGGINGS)==null){
                // Find best leggings
                int leggingsSlot = -1;
                if(leggingsSlot==-1) leggingsSlot = equipmentChest.first(Material.DIAMOND_LEGGINGS);
                if(leggingsSlot==-1) leggingsSlot = equipmentChest.first(Material.IRON_LEGGINGS);
                if(leggingsSlot==-1) leggingsSlot = equipmentChest.first(Material.CHAINMAIL_LEGGINGS);
                if(leggingsSlot==-1) leggingsSlot = equipmentChest.first(Material.GOLD_LEGGINGS);
                if(leggingsSlot==-1) leggingsSlot = equipmentChest.first(Material.LEATHER_LEGGINGS);

                // Put on leggings
                if(leggingsSlot!=-1){
                    equipment.set(EquipmentSlot.LEGGINGS, equipmentChest.getItem(leggingsSlot));
                    equipmentChest.clear(leggingsSlot);
                }
            }
            // Equip boots
            if(equipment.get(EquipmentSlot.BOOTS)==null){
                // Find best boots
                int bootsSlot = -1;
                if(bootsSlot==-1) bootsSlot = equipmentChest.first(Material.DIAMOND_BOOTS);
                if(bootsSlot==-1) bootsSlot = equipmentChest.first(Material.IRON_BOOTS);
                if(bootsSlot==-1) bootsSlot = equipmentChest.first(Material.CHAINMAIL_BOOTS);
                if(bootsSlot==-1) bootsSlot = equipmentChest.first(Material.GOLD_BOOTS);
                if(bootsSlot==-1) bootsSlot = equipmentChest.first(Material.LEATHER_BOOTS);

                // Put on boots
                if(bootsSlot!=-1){
                    equipment.set(EquipmentSlot.BOOTS, equipmentChest.getItem(bootsSlot));
                    equipmentChest.clear(bootsSlot);
                }
            }
            // Equip weapon
            if(equipment.get(EquipmentSlot.HAND)==null){
                // Find first weapon
                final List<Material> weapons = Arrays.asList(
                    Material.DIAMOND_SWORD, Material.IRON_SWORD, Material.STONE_SWORD, Material.GOLD_SWORD, Material.WOOD_SWORD,
                    Material.DIAMOND_SPADE, Material.IRON_SPADE, Material.STONE_SPADE, Material.GOLD_SPADE, Material.WOOD_SPADE,
                    Material.DIAMOND_PICKAXE, Material.IRON_PICKAXE, Material.STONE_PICKAXE, Material.GOLD_PICKAXE, Material.WOOD_PICKAXE,
                    Material.DIAMOND_AXE, Material.IRON_AXE, Material.STONE_AXE, Material.GOLD_AXE, Material.WOOD_AXE,
                    Material.DIAMOND_HOE, Material.IRON_HOE, Material.STONE_HOE, Material.GOLD_HOE, Material.WOOD_HOE,
                    Material.BOW, Material.SPLASH_POTION, Material.LINGERING_POTION, Material.SNOW_BALL, Material.EGG
                );
                for(ItemStack item : equipmentChest){
                    if(item!=null && weapons.contains(item.getType())){
                        equipment.set(EquipmentSlot.HAND, item);
                        equipmentChest.removeItem(item);
                        break;
                    }
                }
            }
            // Equip shield
            if(equipment.get(EquipmentSlot.OFF_HAND)==null){
                // Find first shield or torch
                final List<Material> offHandItems = Arrays.asList(Material.SHIELD, Material.TORCH);

                // Equip in off-hand
                for(ItemStack item : equipmentChest){
                    if(item!=null && offHandItems.contains(item.getType())){
                        equipment.set(EquipmentSlot.OFF_HAND, item);
                        equipmentChest.removeItem(item);
                        break;
                    }
                }
            }
        }
    }
    /**
     * Tells this NPC to drop their equipment on the ground.
     */
    public void unequip(){
        if(!npc.isSpawned()){
            Utils.notifyAdmins("NPC "+npc.getFullName()+" could not drop equipment because they are not spawned!");
            return;
        }

        final Equipment equipment = npc.getTrait(Equipment.class);

        // Drop every item this NPC has equipped
        for(ItemStack item : equipment.getEquipment()){
            if(item!=null) npc.getEntity().getWorld().dropItemNaturally(npc.getEntity().getLocation(), item);
        }

        // Remove all equipment from NPC
        for(EquipmentSlot slot : EquipmentSlot.values()){
            equipment.set(slot, null);
        }
    }

    /**
     * Switches to the skin saved in the tags.
     */
    private void switchSkin(){
        // Don't switch skins if this NPC already has a skin.
        if(npc.data().has(NPC.PLAYER_SKIN_UUID_METADATA)) return;

        String skinUUID = getTag().getSkin();
        if(skinUUID!=null){
            npc.data().set(NPC.PLAYER_SKIN_UUID_METADATA, skinUUID);
            // Respawn the NPC to apply the skin
            npc.despawn();
            npc.spawn(npc.getStoredLocation());
        }
    }

    /**
     * Set Sentinel targets from tags.
     */
    private void setSentinelTargets(){
        // If Sentinel is enabled, get the SentinelNPC
        if(StoryPlugin.sentinelEnabled){
            SentinelNPC sentinel = new SentinelNPC(npc);

            // Set targets
            if(getTag().getTargets()!=null) sentinel.setTargets(getTag().getTargets());
            if(getTag().getIgnores()!=null) sentinel.setIgnores(getTag().getIgnores());
            if(getTag().getHeldItemTargets()!=null) sentinel.setHeldItemTargets(getTag().getHeldItemTargets());
            if(getTag().getHeldItemIgnores()!=null) sentinel.setHeldItemIgnores(getTag().getHeldItemIgnores());
            if(getTag().getEventTargets()!=null) sentinel.setEventTargets(getTag().getEventTargets());
            if(getTag().getTagTargets()!=null) sentinel.setOtherTargets(getTag().getTagTargets());
            if(getTag().getTagIgnores()!=null) sentinel.setOtherIgnores(getTag().getTagIgnores());
        }
    }

    /**
     * Gets the TaggedNPC {@link Trait} for an {@link NPC}.
     * @param npcID the {@link NPC}
     * @return the TaggedNPC trait
     */
    public static TaggedNPC getTaggedNPC(NPC npc){
        return npc.getTrait(TaggedNPC.class);
    }
    /**
     * Gets the TaggedNPC {@link Trait} for an {@link NPC}.
     * @param npcID the ID of the {@link NPC}
     * @return the TaggedNPC trait
     */
    public static TaggedNPC getTaggedNPC(int npcID){
        return CitizensAPI.getNPCRegistry().getById(npcID).getTrait(TaggedNPC.class);
    }

}