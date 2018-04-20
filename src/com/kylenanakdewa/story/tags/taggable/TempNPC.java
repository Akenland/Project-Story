package com.kylenanakdewa.story.tags.taggable;

import com.kylenanakdewa.core.characters.Character;
import com.kylenanakdewa.core.realms.Realm;
import com.kylenanakdewa.story.tags.NPCTag;

import org.bukkit.ChatColor;

import net.citizensnpcs.api.npc.NPC;

/**
 * TempNPC
 */
public class TempNPC implements Character {

    NPC npc;
    NPCTag tag;

    public TempNPC(NPC npc) {
        this.npc = npc;
        tag = TaggedNPC.getTaggedNPC(npc).getTag();
    }

	@Override
	public Realm getRealm() {
		return tag.getRealm();
	}

	@Override
	public void setRealm(Realm realm) {
		
	}

	@Override
	public boolean isRealmOfficer() {
		return false;
	}

	@Override
	public String getName() {
		return ChatColor.getLastColors(getTitle()) + npc.getName();
	}

	@Override
	public void setName(String name) {
		
	}

	@Override
	public String getTitle() {
		return (getRealm()!=null ? getRealm().getColor() : "") + (tag.getTitle()!=null ? tag.getTitle() : "");
	}

	@Override
	public void setTitle(String title) {
		
	}

	@Override
	public String getFormattedName() {
		return getTitle() + (ChatColor.stripColor(getTitle()).length()>0 ? " " : "") + getName();
	}

	@Override
	public String getChatFormat() {
		ChatColor realmColor = getRealm()!=null ? getRealm().getColor() : ChatColor.GRAY;
		ChatColor topParentRealmColor = getRealm()!=null && getRealm().getTopParentRealm()!=null ? getRealm().getTopParentRealm().getColor() : realmColor;
		String spacedTitle = getTitle() + (ChatColor.stripColor(getTitle()).length()>0 ? " " : "");

		return topParentRealmColor+"<" +ChatColor.GRAY + spacedTitle+"%s" + topParentRealmColor+"> " + ChatColor.RESET+"%s";

	}

    
}