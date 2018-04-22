package com.kylenanakdewa.story.tags;

import java.util.Arrays;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;

import com.kylenanakdewa.core.common.Error;
import com.kylenanakdewa.core.common.Utils;
import com.kylenanakdewa.story.StoryPlugin;
import com.kylenanakdewa.story.tags.taggable.Taggable;
import com.kylenanakdewa.story.tags.taggable.TaggedEntity;
import com.kylenanakdewa.story.tags.taggable.TaggedNPC;

import net.citizensnpcs.api.CitizensAPI;
import net.citizensnpcs.api.npc.NPC;

public final class TagCommands implements TabExecutor {
    
    //// Commands
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args){
        // Check permissions
        if(!sender.hasPermission("story.tag")) return Error.NO_PERMISSION.displayChat(sender);
        if(args.length==0) return Error.INVALID_ARGS.displayActionBar(sender);

        // If first arg is info, just display tag info
        if(args[0].equalsIgnoreCase("info") && args.length==2){
            Tag.get(args[1]).displayInfo(sender);
            return true;
        }

        // First arg is the tag target - entity, player
        Taggable target;
        switch(args[0]){
            case "entity":
                if(args.length<2) return Error.INVALID_ARGS.displayActionBar(sender);
                // Target world is the world player is in, or default world if not a player
                World world = (sender instanceof Player) ? ((Player)sender).getWorld() : Bukkit.getWorlds().get(0);
            
                // Get entity by ID
                Entity entity = null;
                for(Entity e : world.getEntities()){
                    try{if(e.getEntityId()==Integer.parseInt(args[1])) entity = e;} catch(NumberFormatException exception){}
                }
                if(entity==null) return Error.ENTITY_NOT_FOUND.displayActionBar(sender);

                // Get TaggedEntity
                target = new TaggedEntity(entity);
                break;

            case "player":
                if(args.length<2) return Error.INVALID_ARGS.displayActionBar(sender);
                Player player = Utils.getPlayer(args[1]);
                if(player==null) return Error.PLAYER_NOT_FOUND.displayActionBar(sender);

                // Get TaggedPlayer
                target = new TaggedEntity(player);
                break;

            case "npc":
                if(StoryPlugin.citizensEnabled){
                    NPC npc = (args.length<2 || args[1].equalsIgnoreCase("sel")) ? CitizensAPI.getDefaultNPCSelector().getSelected(sender) : CitizensAPI.getNPCRegistry().getById(Integer.parseInt(args[1]));
                    target = npc.getTrait(TaggedNPC.class);
                    break;
                }


            default:
                return Error.INVALID_ARGS.displayActionBar(sender);
        }

        // Second arg is the action to take
        if(args.length<3 || args[2].equalsIgnoreCase("info")){
            target.displayInfo(sender);
        } else if(args.length==4 && args[2].equalsIgnoreCase("add")){
            target.addTag(Tag.get(args[3]));
            sender.sendMessage(Utils.messageText+"Added tag "+args[3].toLowerCase()+" to "+target);
        } else if(args.length==4 && args[2].equalsIgnoreCase("remove")){
            target.removeTag(Tag.get(args[3]));
            sender.sendMessage(Utils.messageText+"Removed tag "+args[3].toLowerCase()+" from "+target);
        } else if(args[2].equalsIgnoreCase("removeall")){
            target.removeAllTags();
            sender.sendMessage(Utils.messageText+"Removed all tags from "+target);
        } else if(args[2].equalsIgnoreCase("reload")){
            target.reload();
            sender.sendMessage(Utils.messageText+"Reloaded tags for "+target);
        }

        return true;
    }


    //// Tab completion
	@Override
	public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args){
        if(!sender.hasPermission("story.tag")) return Arrays.asList("");
        
        if(args.length==1) return Arrays.asList("info","entity","player","npc");
        if(args.length==2 && args[0].equalsIgnoreCase("player")) return null;
        if(args.length==3) return Arrays.asList("info","add","remove","removeall");

        return Arrays.asList("");
    }
}