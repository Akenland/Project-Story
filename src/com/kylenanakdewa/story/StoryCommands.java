package com.kylenanakdewa.story;

import java.util.Arrays;
import java.util.List;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;

import com.kylenanakdewa.core.common.Error;
import com.kylenanakdewa.core.common.Utils;
import com.kylenanakdewa.core.Permissions.PermsUtils;
import com.kylenanakdewa.core.common.prompts.Prompt;
import com.kylenanakdewa.story.tags.Tag;

public final class StoryCommands implements TabExecutor {
    
    //// Commands
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args){
        // Version command
        if(args.length==0 || args[0].equalsIgnoreCase("version")){
            Prompt prompt = new Prompt();
            prompt.addQuestion("&6Project Story "+StoryPlugin.plugin.getDescription().getVersion()+" by Kyle Nanakdewa");
            prompt.addAnswer("Dynamic quests and characters, that are tailored to your world.","");
            prompt.addAnswer("Website: http://plugins.akenland.com", "url_http://plugins.akenland.com/");
            prompt.display(sender);
			return true;
        }

        // Reload command
        if(args[0].equalsIgnoreCase("reload")){
            // Check permissions
            if(!sender.hasPermission("story.reload") || !PermsUtils.isDoubleCheckedAdmin(sender)){
                Utils.notifyAdminsError(sender.getName()+Utils.errorText+" failed security check (reloading Story).");
                return Error.NO_PERMISSION.displayChat(sender);
            }

            //ConfigValues.reloadConfig();
            Tag.reloadAll();

            Utils.notifyAdmins(sender.getName()+Utils.messageText+" reloaded Story.");
            return true;
        }

        return Error.INVALID_ARGS.displayActionBar(sender);
    }


    //// Tab completion
	@Override
	public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args){
        if(args.length==1 && sender.hasPermission("story.reload")) return Arrays.asList("version","reload");

        return Arrays.asList("");
	}
}