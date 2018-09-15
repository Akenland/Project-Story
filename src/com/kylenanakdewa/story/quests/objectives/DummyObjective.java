package com.kylenanakdewa.story.quests.objectives;

import java.util.Map;

import com.kylenanakdewa.story.tags.Condition;
import com.kylenanakdewa.story.tags.Interaction;

/**
 * DummyObjective
 */
public class DummyObjective extends Objective {

	private String identifier;
    private String description;

    public DummyObjective(String identifier, String description) {
		this.identifier = identifier;
        this.description = description;
	}
	

	@Override
	public String getIdentifier() {
		return identifier;
	}


	@Override
	public String getDescription() {
		return description;
	}

	@Override
	public void setDescription(String description) {
		this.description = description;
    }


	@Override
	public Map<Condition, Interaction> getInteractions() {
		return null;
	}


	@Override
	public void setInteractions(Map<Condition, Interaction> interactions) {
		
	}

}