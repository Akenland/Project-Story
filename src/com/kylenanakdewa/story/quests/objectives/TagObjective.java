package com.kylenanakdewa.story.quests.objectives;

import java.util.Map;
import java.util.List;

import com.kylenanakdewa.story.tags.Condition;
import com.kylenanakdewa.story.tags.Interaction;
import com.kylenanakdewa.story.tags.Tag;

import org.bukkit.configuration.ConfigurationSection;

/**
 * An objective loaded from a Tag.
 */
public class TagObjective extends CompoundObjective {

	/** The name (short description) of this objective. If unset, defaults to the goal. */
	private String name;

	/** The interactions that apply to characters with this objective. */
	private Map<Condition,Interaction> interactions;

	/** Tags to apply during the objective. */
	private List<Tag> progressTags;

	public static TagObjective getFromConfig(ConfigurationSection config){
		String name = config.getString("name");
		
		return null; // TODO
	}
}