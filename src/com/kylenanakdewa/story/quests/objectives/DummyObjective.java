package com.kylenanakdewa.story.quests.objectives;

/**
 * DummyObjective
 */
public class DummyObjective extends Objective {

    private String description;

    public DummyObjective(String description) {
        this.description = description;
    }

	@Override
	public boolean isCompleted() {
		return false;
	}

	@Override
	public boolean isFailed() {
		return false;
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