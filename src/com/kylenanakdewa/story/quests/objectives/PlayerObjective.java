package com.kylenanakdewa.story.quests.objectives;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import org.bukkit.entity.Player;

/**
 * Represents an Objective that must be completed by a Player, or group of players.
 * @author Kyle Nanakdewa
 */
public abstract class PlayerObjective extends Objective {

    /** The players who can complete this objective. */
    private Set<Player> players;


    public PlayerObjective(Player... players) {
        this(new HashSet<Player>(Arrays.asList(players)));
    }
    public PlayerObjective(Set<Player> players) {
        this.players = players;
    }


	/**
     * Gets the set of Players that can complete this objective.
	 * @return the players
	 */
	public Set<Player> getPlayers() {
		return players;
	}

}