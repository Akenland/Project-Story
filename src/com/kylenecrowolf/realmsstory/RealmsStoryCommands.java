package com.kylenecrowolf.realmsstory;

import java.util.Arrays;
import java.util.List;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;

import com.KyleNecrowolf.RealmsCore.Common.Error;
import com.KyleNecrowolf.RealmsCore.Common.Utils;
import com.KyleNecrowolf.RealmsCore.Permissions.PermsUtils;
import com.KyleNecrowolf.RealmsCore.Prompts.Prompt;

public final class RealmsStoryCommands implements TabExecutor {
    
    //// Commands
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args){
        // Version command
        if(args.length==0 || args[0].equalsIgnoreCase("version")){
            Prompt prompt = new Prompt();
            prompt.addQuestion("RealmsStory "+Main.plugin.getDescription().getVersion()+" by Kyle Necrowolf");
            prompt.addAnswer("Dynamic quests and characters, that are tailored to your world.","");
            prompt.addAnswer("Website: http://WolfiaMC.com/plugins", "url_http://WolfiaMC.com/plugins");
            prompt.display(sender);
			return true;
        }

        // Reload command
        if(args[0].equalsIgnoreCase("reload")){
            // Check permissions
            if(!sender.hasPermission("story.reload") || !PermsUtils.isDoubleCheckedAdmin(sender)){
                Utils.notifyAdminsError(sender.getName()+Utils.errorText+" failed security check (reloading RealmsStory config).");
                return Error.NO_PERMISSION.displayChat(sender);
            }

            //ConfigValues.reloadConfig();
            Utils.notifyAdmins(sender.getName()+Utils.messageText+" reloaded the RealmsStory config.");
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