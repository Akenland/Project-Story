package com.kylenanakdewa.story.quests.objectives;

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

}