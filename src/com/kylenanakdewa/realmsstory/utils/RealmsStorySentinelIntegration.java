package com.kylenanakdewa.realmsstory.utils;

import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.mcmonkey.sentinel.SentinelIntegration;
import org.mcmonkey.sentinel.SentinelPlugin;
import org.mcmonkey.sentinel.SentinelTrait;

import com.KyleNecrowolf.RealmsCore.Common.Utils;
import com.kylenanakdewa.realmsstory.tags.Condition;
import com.kylenanakdewa.realmsstory.tags.taggable.TaggedNPC;
import java.util.concurrent.ThreadLocalRandom;
import net.citizensnpcs.api.npc.NPC;

/**
 * A SentinelIntegration that allows Sentinel NPCs to target entities based on their tags.
 */
public class RealmsStorySentinelIntegration extends SentinelIntegration {

    /**
     * Adds this integration to the Sentinel plugin.
     */
    public static boolean addIntegration(){
        if(SentinelPlugin.integrations.add(new RealmsStorySentinelIntegration())){
            Utils.notifyAdmins("RealmsStory has integrated with Sentinel, allowing NPCs to target entities based on tags.");
            return true;
        }
        Utils.notifyAdminsError("RealmsStory failed to integrate with Sentinel.");
        return false;
    }

    /**
     * Gets the help string for this target.
     */
    @Override
    public String getTargetHelp(){
        return "tag:TAG_CONDITION";
    }

    /**
     * Checks if a living entity is a target, based on the target expression provided.
     */
    @Override
    public boolean isTarget(LivingEntity entity, String targetExpression){
        if(targetExpression.startsWith("tag:")){
            targetExpression = targetExpression.replaceFirst("tag:", "");
            // Evaluate condition
            return new Condition(targetExpression).eval(entity);
        }
        return false;
    }

    /**
     * Runs when a NPC attacks something.
     */
    @Override
    public boolean tryAttack(SentinelTrait sentinel, LivingEntity entity){
        // Show conversation to attacked player, 20% of times
        if(entity instanceof Player && ThreadLocalRandom.current().nextInt(10)<2){
            NPC npc = sentinel.getNPC();
            npc.getTrait(TaggedNPC.class).getTag().displayConversation((Player)entity, npc, "onAttack");
        }

        // Returning false tells Sentinel to proceed with normal attacks (true = cancel Sentinel's attack)
        return false;
    }
}