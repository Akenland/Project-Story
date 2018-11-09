package com.kylenanakdewa.story.quests.objectives;

/**
 * DummyObjective
 */
public class DummyObjective extends Objective {

	private String identifier;

    public DummyObjective(String identifier, String description) {
		this.identifier = identifier;
        this.description = description;
	}
	

	@Override
	public String getIdentifier() {
		return identifier;
	}

}