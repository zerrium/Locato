package zerrium.Commands;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import zerrium.Locato;
import zerrium.ZLocation;

public class Whereis implements CommandExecutor {
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if(args.length == 0){
            sender.sendMessage(ChatColor.GOLD+"[Locato] " + "Usage: /whereis <place_name>");
            return false;
        }else{
            if(Locato.zLocations.contains(new ZLocation(args[0].toLowerCase()))){
                ZLocation zl = Locato.zLocations.get(Locato.zLocations.indexOf(new ZLocation(args[0].toLowerCase())));
                int[] chunk1 = zl.getChunk1().getCoord();
                int[] chunk2 = zl.getChunk2().getCoord();
                World w = Bukkit.getWorld(zl.getDimension());
                Location block1 = w.getChunkAt(chunk1[0], chunk1[1]).getBlock(0, chunk1[2], 0).getLocation();
                Location block2 = w.getChunkAt(chunk2[0], chunk2[1]).getBlock(15, chunk2[2], 15).getLocation();
                Location loc = new Location(w,
                        Math.round((block1.getX()+block2.getX())/2),
                        Math.min(block1.getY(), block2.getY()),
                        Math.round((block1.getZ()+block2.getZ())/2));
                sender.sendMessage(ChatColor.GOLD+"[Locato] " + ChatColor.RESET + zl.getPlaceId() + " is located at: " +
                        loc.getX() + ", " + loc.getY() + ", " + loc.getZ() + " in " +
                        loc.getWorld().getEnvironment().toString().toLowerCase().replaceAll("_", " ") + " dimension.");
            }else{
                sender.sendMessage(ChatColor.GOLD+"[Locato]" + ChatColor.RESET + args[0] + " is not found on the server record.");
            }
        }
        return true;
    }
}
