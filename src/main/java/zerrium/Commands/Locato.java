package zerrium.Commands;

import org.bukkit.*;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import zerrium.SqlCon;
import zerrium.ZChunk;
import zerrium.ZLocation;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;

public class Locato implements CommandExecutor {
    private CommandSender cs;
    private final HashMap<String, Location> hm = new HashMap<>();

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        cs = sender;
        final String msg = ChatColor.GOLD+"[Locato] " + ChatColor.RESET + "usage:\n" +
                "/locato <remove/status> <place_name>\n" +
                "/locato <add/edit> <place_name> (optional for add and player: <chunk1 pos x> <chunk1 pos y> <chunk1 pos z> <chunk2 pos x> <chunk2 pos y> <chunk2 pos z> <dimension: NORMAL or NETHER or THE_END>)" +
                "/locato <search> <keyword>";
        switch(args.length){
            case 2:
                switch(args[0].toLowerCase()){
                    case "add":
                        doAdd(args[1].toLowerCase());
                        return true;
                    case "remove":
                        doRemove(args[1].toLowerCase());
                        return true;
                    case "search":
                        doSearch(args[1].toLowerCase());
                        return true;
                    case "status":
                        doStatus(args[1].toLowerCase());
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

    private void doStatus(String place){
        BukkitRunnable r = new BukkitRunnable() {
            @Override
            public void run() {
                if(!cs.hasPermission("Locato.status")){
                    cs.sendMessage(ChatColor.GOLD+"[Locato] " + ChatColor.RESET + "Sorry you do not have permission to perform this command. Ask your admin for more info.");
                    return;
                }
                int index = zerrium.Locato.zLocations.indexOf(new ZLocation(place));
                if(index == -1){
                    cs.sendMessage(ChatColor.GOLD+"[Locato] " + ChatColor.RESET + "No registered places found with \"" + place + "\" name.");
                }else{
                    ZLocation zl = zerrium.Locato.zLocations.get(index);
                    int[] chunk1 = zl.getChunk1().getCoord();
                    int[] chunk2 = zl.getChunk2().getCoord();
                    World w = Bukkit.getWorld(zl.getDimension());
                    ArrayList<Player> p = new ArrayList<>();

                    for(int i=Math.min(chunk1[0], chunk2[0]); i<=Math.max(chunk1[0], chunk2[0]); i++){
                        for(int j=Math.min(chunk1[1], chunk2[1]); j<=Math.max(chunk1[1], chunk2[1]); j++){
                            Chunk c = w.getChunkAt(i, j);
                            for(Entity e:c.getEntities()){
                                if(e.getType() == EntityType.PLAYER){
                                    Player temp = Bukkit.getPlayer(e.getUniqueId());
                                    int y = (int) temp.getLocation().getY();
                                    if(zerrium.Locato.debug) System.out.println("Found a player with uuid of " + temp.getUniqueId() + ", name: " + temp.getDisplayName());
                                    if((Math.min(chunk1[2], chunk2[2])-2) <= y && (Math.max(chunk1[2], chunk2[2])+2) >= y) p.add(temp);
                                }
                            }
                        }
                    }

                    if(p.isEmpty()){
                        cs.sendMessage(ChatColor.GOLD+"[Locato] " + ChatColor.RESET + "No one is at " + place + ".");
                    }else{
                        StringBuilder result = new StringBuilder();
                        int i;
                        for(i=0; i<p.size(); i++){
                            p.get(i).sendMessage(ChatColor.GOLD+"[Locato] " + ChatColor.RESET + (cs instanceof Player ? ((Player) cs).getDisplayName() : "Server admin") + " checked this place status.");
                            result.append(p.get(i).getDisplayName()).append(i != (p.size()-1) ? ", " : ".");
                        }
                        cs.sendMessage(ChatColor.GOLD+"[Locato] " + ChatColor.RESET + "There are " + i + " player(s) at " + place + ":");
                        cs.sendMessage(result.toString());
                    }
                }
            }
        };
        r.runTaskAsynchronously(zerrium.Locato.getPlugin(zerrium.Locato.class));
    }

    private void doRemove(String place){
        BukkitRunnable r = new BukkitRunnable() { //asynchronous because it might disturb main thread performance if the player is doing enormous search
            @Override
            public void run() {
                if(!cs.hasPermission("Locato.delete")){
                    cs.sendMessage(ChatColor.GOLD+"[Locato] " + ChatColor.RESET + "Sorry you do not have permission to perform this command. Ask your admin for more info.");
                    return;
                }
                int index = zerrium.Locato.zLocations.indexOf(new ZLocation(place));
                if(index == -1){
                    cs.sendMessage(ChatColor.GOLD+"[Locato] " + ChatColor.RESET + "No registered places found with \"" + place + "\" name.");
                }else{
                    SQL_delete(place, index);
                }
            }
        };
        r.runTaskAsynchronously(zerrium.Locato.getPlugin(zerrium.Locato.class));
    }

    private void doAdd(String place){
        BukkitRunnable r = new BukkitRunnable() {
            @Override
            public void run() {
                if(!(cs instanceof Player)){
                    cs.sendMessage(ChatColor.GOLD+"[Locato] " + ChatColor.RESET + "usage:\n" +
                            "/locato <add> <place_name> <chunk1 pos x> <chunk1 pos y> <chunk1 pos z> <chunk2 pos x> <chunk2 pos y> <chunk2 pos z> <dimension: NORMAL or NETHER or THE_END>");
                    return;
                }
                if(!cs.hasPermission("Locato.add")){
                    cs.sendMessage(ChatColor.GOLD+"[Locato] " + ChatColor.RESET + "Sorry you do not have permission to perform this command. Ask your admin for more info.");
                    return;
                }
                if(hm.get(place) == null){
                    if(zerrium.Locato.zLocations.contains(new ZLocation(place))){
                        cs.sendMessage(ChatColor.GOLD+"[Locato] " + ChatColor.RESET + "Place of \"" + place + "\" is already recorded on database. Try again using another name!");
                    }else{
                        hm.put(place, ((Player) cs).getLocation());
                        cs.sendMessage(ChatColor.GOLD+"[Locato] " + ChatColor.RESET + "First position recorded. Go to second position and perform \"/locato add " + place + "\" again to save this place.");
                    }
                }else{
                    Location lo1 = hm.get(place);
                    Location lo2 = ((Player) cs).getLocation();
                    SQL_add(place, lo1.getWorld().getEnvironment().toString(), (int) lo1.getX(), (int) lo1.getZ(), (int) lo1.getY(), (int) lo2.getX(), (int) lo2.getZ(), (int) lo2.getY());
                }
            }
        };
        r.runTaskAsynchronously(zerrium.Locato.getPlugin(zerrium.Locato.class));
    }

    private void doAddEdit(String add_edit){

    }

    private void SQL_add(String place, String dimension, int chunk1_x, int chunk1_z, int elevation1, int chunk2_x, int chunk2_z, int elevation2){
        System.out.println(ChatColor.YELLOW + "[Locato]" + ChatColor.RESET + " Adding place: " + place + " to database...");
        PreparedStatement pss = null;
        Connection con = null;
        try {
            con = SqlCon.openConnection();
            pss = con.prepareStatement("insert into locato(place_id, dimension, chunk1_x, chunk1_z, elevation1, chunk2_x, chunk2_z, elevation2) values (?, ?, ?, ?, ?, ?, ?, ?");
            pss.setString(1, place);
            pss.setString(2, dimension);
            pss.setInt(3, chunk1_x);
            pss.setInt(4, chunk1_z);
            pss.setInt(5, elevation1);
            pss.setInt(6, chunk2_x);
            pss.setInt(7, chunk2_z);
            pss.setInt(8, elevation2);
            pss.executeUpdate();
            System.out.println(ChatColor.YELLOW + "[Locato]" + ChatColor.RESET + " Added place: " + place + " to the database.");
            zerrium.Locato.zLocations.add(new ZLocation(place, dimension, new ZChunk(chunk1_x, chunk1_z, elevation1), new ZChunk(chunk2_x, chunk2_z, elevation2)));
            cs.sendMessage(ChatColor.GOLD+"[Locato] " + ChatColor.RESET + "Added place: \"" + place + "\" to database record.");
        } catch (SQLException throwables) {
            throwables.printStackTrace();
            cs.sendMessage(ChatColor.GOLD+"[Locato] " + ChatColor.RESET + "Failed to add place: \"" + place + "\" from database record. Check server console for more info.");
        }finally {
            try {
                assert pss != null;
                pss.close();
                con.close();
            } catch (SQLException throwables) {
                if(zerrium.Locato.debug) throwables.printStackTrace();
            }
        }
    }

    private void SQL_delete(String place, int index){
        System.out.println(ChatColor.YELLOW + "[Locato]" + ChatColor.RESET + " Deleting place: " + place + " from database...");
        PreparedStatement pss = null;
        Connection con = null;
        try {
            con = SqlCon.openConnection();
            pss = con.prepareStatement("delete from locato where place_id=?");
            pss.setString(1, place);
            int row = pss.executeUpdate();
            System.out.println(ChatColor.YELLOW + "[Locato]" + ChatColor.RESET +
                    (row > 0 ? " Deleted place: " + place + " from database." : " No place of " + place + " found on the database. No rows affected."));
            zerrium.Locato.zLocations.remove(index);
            cs.sendMessage(ChatColor.GOLD+"[Locato] " + ChatColor.RESET + "Deleted place: \"" + place + "\" from database record.");
        } catch (SQLException throwables) {
            throwables.printStackTrace();
            cs.sendMessage(ChatColor.GOLD+"[Locato] " + ChatColor.RESET + "Failed to delete place: \"" + place + "\" from database record. Check server console for more info.");
        }finally {
            try {
                assert pss != null;
                pss.close();
                con.close();
            } catch (SQLException throwables) {
                if(zerrium.Locato.debug) throwables.printStackTrace();
            }
        }
    }
}
