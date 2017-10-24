package com.kylenecrowolf.realmsstory.tags;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.bukkit.entity.Entity;

import com.KyleNecrowolf.RealmsCore.Common.Utils;
import com.kylenecrowolf.realmsstory.tags.taggable.Taggable;
import com.kylenecrowolf.realmsstory.tags.taggable.TaggedEntity;

/**
 * A tag condition that can be evaluated.
 */
public class Condition {

    /**
     * The expression to evaluate.
     */
    private final List<String> expressions;

    /**
     * Creates a new condition. The expression must be tags. <p>
     * Tags seperated by commas are matched as OR (any tag must be present).
     * Tags seperated by && are matched as AND (all tags must be present).
     */
    public Condition(String expression){
        expressions = new ArrayList<String>(Arrays.asList(expression.split(",")));

        // Remove spaces and convert to lower case
        for(String e : expressions){
            expressions.remove(e);
            expressions.add(e.toLowerCase().replace(" ", ""));
        }
    }


    /**
     * Checks if a {@link Taggable} meets this Condition.
     * @param target the Taggable to check
     * @return true if the target has all of the tags as specified by this Condition
     */
    public boolean eval(Taggable target){
        Utils.notifyAdmins("Evaulating condition on "+target);
        for(String expression : expressions){
            Utils.notifyAdmins("Checking "+expression);
            // Convert expression to array of tags
            List<Tag> tagList = new ArrayList<Tag>();
            for(String exp : expression.split("&&")){
                tagList.add(new Tag(exp));
            }
            Tag tagArray[] = tagList.toArray(new Tag[tagList.size()]);
            Utils.notifyAdmins("AKA "+tagArray);

            // If Tag has this tag, return true
            if(target.hasTag(tagArray)){
                Utils.notifyAdmins("Match found!");
                return true;
            }
        }

        Utils.notifyAdmins("No match found.");
        return false;
    }
    /**
     * Checks if an {@link Entity} meets this Condition.
     * @param target the Entity to check
     * @return true if the target has all of the tags as specified by this Condition
     */
    public boolean eval(Entity target){
        return eval(new TaggedEntity(target));
    }
}