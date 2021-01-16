package zerrium.Commands;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Objects;

public class ShareLoc implements CommandExecutor {
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if(sender instanceof Player){
            Player p = (Player) sender;
            Location l = p.getLocation();
            final String a = ChatColor.GOLD+"[Locato] " + ChatColor.RESET + p.getDisplayName() + " shared his current location";
            final String b = ChatColor.GOLD+"[Locato] " + ChatColor.RESET + p.getDisplayName() + " is at ";
            final String c = l.getBlockX() + ", " + l.getBlockY() + ", " + l.getBlockZ() + " in " + Objects.requireNonNull(l.getWorld()).getEnvironment().toString().toLowerCase().replaceAll("_", " ") + " dimension.";
            if(args.length == 0){
                Bukkit.broadcastMessage(a + ".");
                Bukkit.broadcastMessage(b + c);
            }else{
                Player p2 = Bukkit.getPlayer(args[0]);
                if(p2 == null){
                    sender.sendMessage(ChatColor.GOLD+"[Locato] " + "Player " + args[0] + " is not found or offline.");
                }else{
                    sender.sendMessage(ChatColor.GOLD+"[Locato] " + ChatColor.RESET + "You shared your current location to " + p2.getDisplayName() + ": " + c);
                    p2.sendMessage(a + "to you.");
                    p2.sendMessage(b + c);
                }
            }
            return true;
        }else{
            sender.sendMessage(ChatColor.GOLD+"[Locato]" + ChatColor.RESET + "You must be a player to execute this command!");
        }
        return false;
    }
}
