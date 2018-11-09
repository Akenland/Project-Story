package com.kylenanakdewa.story.tags;

import com.kylenanakdewa.core.common.savedata.SaveDataSection;
import com.kylenanakdewa.story.tags.Tag;

import org.bukkit.plugin.Plugin;

/**
 * Represents plugin save data that can be stored with a Tag.
 * <p>
 * Extend this class with your own plugin's methods, for easy saving and loading
 * of data for Tags.
 */
public abstract class TagDataSection extends SaveDataSection {

    /** The Tag that this save data is for. */
    protected final Tag tag;

    /**
     * Creates or retrieves a SaveDataSection for the specified Tag and plugin.
     * @param tag the Tag to save data for
     * @param plugin the plugin that is saving data
     */
    public TagDataSection(Tag tag, Plugin plugin){
        super(tag.getData(plugin));
        this.tag = tag;
    }


    /**
     * Gets the Tag that this data is saved for.
     * @return the Tag where this data is stored
     */
    public Tag getTag(){
        return tag;
    }

}