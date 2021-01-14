package zerrium.Commands;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.scheduler.BukkitRunnable;
import zerrium.ZLocation;

import java.util.ArrayList;

public class Locato implements CommandExecutor {
    private CommandSender cs;

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        cs = sender;
        final String msg = ChatColor.GOLD+"[Locato] " + ChatColor.RESET + "usage:\n" +
                "/locato <remove/status> <place_name>\n" +
                "/locato <add/edit> <place_name> (optional for add: <chunk1 pos x> <chunk1 pos y> <chunk1 pos z> <chunk2 pos x> <chunk2 pos y> <chunk2 pos z> <dimension>)" +
                "/locato <search> <keyword>";
        switch(args.length){
            case 2:
                switch(args[0].toLowerCase()){
                    case "add":
                        doAdd();
                        return true;
                    case "remove":
                        doRemove();
                        return true;
                    case "search":
                        doSearch(args[1]);
                        return true;
                    case "status":
                        doStatus();
                        return true;
                    default:
                        sender.sendMessage(msg);
                }
            case 9:
                switch (args[0].toLowerCase()){
                    case "add":
                        doAddEdit("add");
                        return true;
                    case "edit":
                        doAddEdit("edit");
                        return true;
                    default:
                        sender.sendMessage(msg);
                }
            default:
                sender.sendMessage(msg);
        }
        return false;
    }

    private void doSearch(String keyword){
        BukkitRunnable r = new BukkitRunnable() { //asynchronous because it might disturb main thread performance if the player is doing enormous search
            @Override
            public void run() {
                if(!cs.hasPermission("Locato.search")){
                    cs.sendMessage(ChatColor.GOLD+"[Locato] " + ChatColor.RESET + "Sorry you do not have permission to perform this command. Ask your admin for more info.");
                    return;
                }
                ArrayList<String> result = new ArrayList<>();
                for(ZLocation zl :zerrium.Locato.zLocations){
                    if(zl.getPlaceId().contains(keyword)) result.add(zl.getPlaceId());
                }
                if(result.isEmpty()){
                    cs.sendMessage(ChatColor.GOLD+"[Locato] " + ChatColor.RESET + "No registered places found with \"" + keyword + "\" keyword.");
                }else{
                    cs.sendMessage(ChatColor.GOLD+"[Locato] " + ChatColor.RESET + "Found "+ result.size() +" place(s) with \"" + keyword + "\" keyword:");
                    StringBuilder result2 = new StringBuilder();
                    for(int i=0; i<result.size(); i++){
                        result2.append(result.get(i)).append(i != (result.size()-1) ? ", " : ".");
                    }
                    cs.sendMessage(result2.toString());
                }
            }
        };
        r.runTaskAsynchronously(zerrium.Locato.getPlugin(zerrium.Locato.class));
    }

    private void doStatus(){

    }

    private void doRemove(){

    }

    private void doAdd(){

    }

    private void doAddEdit(String add_edit){

    }
}
