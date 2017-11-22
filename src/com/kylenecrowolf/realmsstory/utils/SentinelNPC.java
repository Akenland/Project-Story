package com.kylenecrowolf.realmsstory.utils;

import org.bukkit.entity.LivingEntity;
import org.mcmonkey.sentinel.SentinelTrait;

import com.KyleNecrowolf.RealmsCore.Common.Utils;

import java.util.HashSet;
import java.util.List;
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
     * Gets a SentinelNPC for controlling Sentinel features for an {@link NPC}.
     */
    public SentinelNPC(NPC npc){
        this.npc = npc;
        this.trait = npc.getTrait(SentinelTrait.class);
    }


    /**
     * Tells the NPC to guard a {@link LivingEntity}.
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
     * Tells the NPC to attack a {@link LivingEntity}.
     * @param entity the entity to attack
     */
    public void attack(LivingEntity entity){
        trait.tryAttack(entity);
    }
    /**
     * Tells the NPC to stop attacking.
     */
    public void stopAttack(){
        trait.currentTargets.clear();
        trait.chasing = null;
    }


    /**
     * Gets the non-regex targets for this NPC.
     */
    public HashSet<String> getTargets(){
        return trait.targets;
    }
    /**
     * Sets the non-regex targets for this NPC.
     */
    public void setTargets(HashSet<String> newTargets){
        trait.targets = newTargets;
    }
    /**
     * Gets the non-regex ignores for this NPC.
     */
    public HashSet<String> getIgnores(){
        return trait.ignores;
    }
    /**
     * Sets the non-regex ignores for this NPC.
     */
    public void setIgnores(HashSet<String> newIgnores){
        trait.ignores = newIgnores;
    }

    /**
     * Gets the player regex targets for this NPC.
     */
    public List<String> getPlayerTargets(){
        return trait.playerNameTargets;
    }
    /**
     * Sets the player regex targets for this NPC.
     */
    public void setPlayerTargets(List<String> newTargets){
        trait.playerNameTargets = newTargets;
    }
    /**
     * Gets the player regex ignores for this NPC.
     */
    public List<String> getPlayerIgnores(){
        return trait.playerNameIgnores;
    }
    /**
     * Sets the player regex ignores for this NPC.
     */
    public void setPlayerIgnores(List<String> newIgnores){
        trait.playerNameIgnores = newIgnores;
    }

    /**
     * Gets the NPC regex targets for this NPC.
     */
    public List<String> getNPCTargets(){
        return trait.npcNameTargets;
    }
    /**
     * Sets the NPC regex targets for this NPC.
     */
    public void setNPCTargets(List<String> newTargets){
        trait.npcNameTargets = newTargets;
    }
    /**
     * Gets the NPC regex ignores for this NPC.
     */
    public List<String> getNPCIgnores(){
        return trait.npcNameIgnores;
    }
    /**
     * Sets the NPC regex ignores for this NPC.
     */
    public void setNPCIgnores(List<String> newIgnores){
        trait.npcNameIgnores = newIgnores;
    }

    /**
     * Gets the entity regex targets for this NPC.
     */
    public List<String> getEntityTargets(){
        return trait.entityNameTargets;
    }
    /**
     * Sets the entity regex targets for this NPC.
     */
    public void setEntityTargets(List<String> newTargets){
        trait.entityNameTargets = newTargets;
    }
    /**
     * Gets the entity regex ignores for this NPC.
     */
    public List<String> getEntityIgnores(){
        return trait.entityNameIgnores;
    }
    /**
     * Sets the entity regex ignores for this NPC.
     */
    public void setEntityIgnores(List<String> newIgnores){
        trait.entityNameIgnores = newIgnores;
    }

    /**
     * Gets the held item regex targets for this NPC.
     */
    public List<String> getHeldItemTargets(){
        return trait.heldItemTargets;
    }
    /**
     * Sets the held item regex targets for this NPC.
     */
    public void setHeldItemTargets(List<String> newTargets){
        trait.heldItemTargets = newTargets;
    }
    /**
     * Gets the held item regex ignores for this NPC.
     */
    public List<String> getHeldItemIgnores(){
        return trait.heldItemIgnores;
    }
    /**
     * Sets the held item regex ignores for this NPC.
     */
    public void setHeldItemIgnores(List<String> newIgnores){
        trait.heldItemIgnores = newIgnores;
    }

    /**
     * Gets the event targets for this NPC.
     */
    public List<String> getEventTargets(){
        return trait.eventTargets;
    }
    /**
     * Sets the event targets for this NPC.
     */
    public void setEventTargets(List<String> newTargets){
        trait.eventTargets = newTargets;
    }

    /**
     * Gets the other (including Tag condition) targets for this NPC.
     */
    public List<String> getOtherTargets(){
        return trait.otherTargets;
    }
    /**
     * Sets the other (including Tag condition) targets for this NPC.
     */
    public void setOtherTargets(List<String> newTargets){
        trait.otherTargets = newTargets;
    }
    /**
     * Gets the other (including Tag condition) ignores for this NPC.
     */
    public List<String> getOtherIgnores(){
        return trait.otherIgnores;
    }
    /**
     * Sets the other (including Tag condition) ignores for this NPC.
     */
    public void setOtherIgnores(List<String> newIgnores){
        trait.otherIgnores = newIgnores;
    }
}