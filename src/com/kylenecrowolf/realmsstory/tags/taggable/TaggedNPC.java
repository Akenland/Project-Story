package com.kylenecrowolf.realmsstory.tags.taggable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.HumanEntity;
import org.bukkit.event.EventHandler;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import com.KyleNecrowolf.RealmsCore.Common.Utils;
import com.KyleNecrowolf.RealmsCore.Prompts.Prompt;
import com.KyleNecrowolf.RealmsCore.Prompts.PromptActionEvent;
import com.kylenecrowolf.realmsstory.tags.NPCTag;
import com.kylenecrowolf.realmsstory.tags.Tag;
import net.citizensnpcs.api.CitizensAPI;
import net.citizensnpcs.api.event.NPCRightClickEvent;
import net.citizensnpcs.api.persistence.Persist;
import net.citizensnpcs.api.trait.Trait;
import net.citizensnpcs.api.trait.TraitName;

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


    public List<Tag> getTags(){
        if(tags==null){
            tags = new ArrayList<Tag>();
            // Load tags from NPC saves
            if(savedTags!=null) for(String tag : savedTags) tags.addAll(new Tag(tag).getTotalInheritedTags());

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
    }


    public void displayInfo(CommandSender sender){
        Prompt prompt = new Prompt();
        prompt.addQuestion(Utils.infoText+"--- NPC: "+Utils.messageText+getNPC().getFullName()+Utils.infoText+" "+getNPC().getId()+" ---");
        prompt.addQuestion(Utils.infoText+"Has the following tags:");
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
        ((NPCTag)getTag()).displayConversation(event.getClicker(), event.getNPC());
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

            // Display prompt
            ((NPCTag)npc.getTag()).displayConversation(event.getPlayer(), npc.getNPC(), action[1]);

            //// Extra actions
            // Get equipment
            if(action[1].equalsIgnoreCase("equip")){
                equip();
            }
            // Sentinel actions
            if(action[1].startsWith("sentinel") && Bukkit.getPluginManager().isPluginEnabled("Sentinel")){
                event.getPlayer().sendMessage("Not yet implemented.");
            }
        }
    }

    /**
     * Equip this NPC on spawn.
     */
    @Override
    public void onSpawn(){
        equip();
    }


    /**
     * Tells this NPC to put on equipment from their equipment chest.
     */
    public void equip(){
        if(npc.getEntity() instanceof HumanEntity){
            HumanEntity entity = (HumanEntity)npc.getEntity();

            // Check equipment
            Inventory equipmentChest = getTag().getEquipmentChest();
            if(equipmentChest==null) return;

            Utils.notifyAdmins(npc.getFullName()+" found their equipment chest.");

            // Equip a helmet
            if(entity.getEquipment().getHelmet()==null){
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
                    entity.getEquipment().setHelmet(equipmentChest.getItem(helmetSlot));
                    equipmentChest.clear(helmetSlot);
                }
            }
            // Equip a chestplate
            if(entity.getEquipment().getChestplate()==null){
                // Find best chestplate
                int chestplateSlot = -1;
                if(chestplateSlot==-1) chestplateSlot = equipmentChest.first(Material.DIAMOND_CHESTPLATE);
                if(chestplateSlot==-1) chestplateSlot = equipmentChest.first(Material.IRON_CHESTPLATE);
                if(chestplateSlot==-1) chestplateSlot = equipmentChest.first(Material.CHAINMAIL_CHESTPLATE);
                if(chestplateSlot==-1) chestplateSlot = equipmentChest.first(Material.GOLD_CHESTPLATE);
                if(chestplateSlot==-1) chestplateSlot = equipmentChest.first(Material.LEATHER_CHESTPLATE);

                // Put on chestplate
                if(chestplateSlot!=-1){
                    entity.getEquipment().setChestplate(equipmentChest.getItem(chestplateSlot));
                    equipmentChest.clear(chestplateSlot);
                }
            }
            // Equip leggings
            if(entity.getEquipment().getLeggings()==null){
                // Find best leggings
                int leggingsSlot = -1;
                if(leggingsSlot==-1) leggingsSlot = equipmentChest.first(Material.DIAMOND_LEGGINGS);
                if(leggingsSlot==-1) leggingsSlot = equipmentChest.first(Material.IRON_LEGGINGS);
                if(leggingsSlot==-1) leggingsSlot = equipmentChest.first(Material.CHAINMAIL_LEGGINGS);
                if(leggingsSlot==-1) leggingsSlot = equipmentChest.first(Material.GOLD_LEGGINGS);
                if(leggingsSlot==-1) leggingsSlot = equipmentChest.first(Material.LEATHER_LEGGINGS);

                // Put on leggings
                if(leggingsSlot!=-1){
                    entity.getEquipment().setLeggings(equipmentChest.getItem(leggingsSlot));
                    equipmentChest.clear(leggingsSlot);
                }
            }
            // Equip boots
            if(entity.getEquipment().getBoots()==null){
                // Find best boots
                int bootsSlot = -1;
                if(bootsSlot==-1) bootsSlot = equipmentChest.first(Material.DIAMOND_BOOTS);
                if(bootsSlot==-1) bootsSlot = equipmentChest.first(Material.IRON_BOOTS);
                if(bootsSlot==-1) bootsSlot = equipmentChest.first(Material.CHAINMAIL_BOOTS);
                if(bootsSlot==-1) bootsSlot = equipmentChest.first(Material.GOLD_BOOTS);
                if(bootsSlot==-1) bootsSlot = equipmentChest.first(Material.LEATHER_BOOTS);

                // Put on boots
                if(bootsSlot!=-1){
                    entity.getEquipment().setBoots(equipmentChest.getItem(bootsSlot));
                    equipmentChest.clear(bootsSlot);
                }
            }
            // Equip weapon
            if(entity.getEquipment().getItemInMainHand()==null){
                // Find first weapon
                List<Material> weapons = Arrays.asList(
                    Material.DIAMOND_SWORD, Material.IRON_SWORD, Material.STONE_SWORD, Material.GOLD_SWORD, Material.WOOD_SWORD,
                    Material.DIAMOND_SPADE, Material.IRON_SPADE, Material.STONE_SPADE, Material.GOLD_SPADE, Material.WOOD_SPADE,
                    Material.DIAMOND_PICKAXE, Material.IRON_PICKAXE, Material.STONE_PICKAXE, Material.GOLD_PICKAXE, Material.WOOD_PICKAXE,
                    Material.DIAMOND_AXE, Material.IRON_AXE, Material.STONE_AXE, Material.GOLD_AXE, Material.WOOD_AXE,
                    Material.DIAMOND_HOE, Material.IRON_HOE, Material.STONE_HOE, Material.GOLD_HOE, Material.WOOD_HOE,
                    Material.BOW, Material.SPLASH_POTION, Material.LINGERING_POTION, Material.SNOW_BALL, Material.EGG
                );
                for(ItemStack item : equipmentChest){
                    if(weapons.contains(item.getType())){
                        entity.getEquipment().setItemInMainHand(item);
                        equipmentChest.removeItem(item);
                        break;
                    }
                }
            }
        }
    }


    /**
     * Gets the TaggedNPC {@link Trait} for an {@link net.citizensnpcs.api.npc.NPC}.
     * @param npcID the ID of the {@link net.citizensnpcs.api.npc.NPC}
     * @return the TaggedNPC trait
     */
    public static TaggedNPC getTaggedNPC(int npcID){
        return CitizensAPI.getNPCRegistry().getById(npcID).getTrait(TaggedNPC.class);
    }
}