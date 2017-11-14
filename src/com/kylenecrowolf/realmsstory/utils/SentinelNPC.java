package com.kylenecrowolf.realmsstory.utils;

import org.bukkit.entity.LivingEntity;
import org.mcmonkey.sentinel.SentinelTrait;

import net.citizensnpcs.api.npc.NPC;

/**
 * Represents a NPC that is a Sentinel. Essentially a wrapper class for SentinelTrait.
 */
public class SentinelNPC {

    /**
     * The NPC.
     */
    NPC npc;

    /**
     * The Sentinel trait for the NPC.
     */
    SentinelTrait trait;

    /**
     * Gets a SentinelNPC for controlling Sentinel features for an NPC.
     */
    public SentinelNPC(NPC npc){
        this.npc = npc;
        this.trait = npc.getTrait(SentinelTrait.class);
    }


    /**
     * Tells the NPC to guard an entity.
     * @param entity the entity to guard, or null to stop guarding
     */
    public void guard(LivingEntity entity){
        if(entity==null) trait.setGuarding(null);
        else trait.setGuarding(entity.getUniqueId());
    }


    /**
     * Sets the health of this NPC.
     * @param health the amount of health for the NPC, default 20 for players
     */
    public void setHealth(double health){
        trait.setHealth(health);
    }


    /**
     * Tells the NPC to attack an entity.
     */
    public void attack(LivingEntity entity){
        trait.tryAttack(entity);
    }
}