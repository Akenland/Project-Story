package com.kylenecrowolf.realmsstory.utils;

import org.bukkit.entity.LivingEntity;
import org.mcmonkey.sentinel.SentinelIntegration;
import org.mcmonkey.sentinel.SentinelPlugin;

import com.KyleNecrowolf.RealmsCore.Common.Utils;
import com.kylenecrowolf.realmsstory.tags.Condition;

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
            targetExpression.replaceFirst("tag:", "");
            // Evaluate condition
            return new Condition(targetExpression).eval(entity);
        }
        return false;
    }
}