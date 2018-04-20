package com.kylenanakdewa.story.quests.objectives;

import java.util.Set;

import org.bukkit.entity.Player;

/**
 * Represents an Objective that is completed by Player Events.
 */
public abstract class PlayerEventObjective extends PlayerObjective {

	public PlayerEventObjective(Player players) {
		super(players);
	}
	public PlayerEventObjective(Set<Player> players) {
		super(players);
	}

}