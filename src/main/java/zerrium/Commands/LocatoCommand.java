package zerrium.Commands;

import org.bukkit.*;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import zerrium.LocatoSqlCon;
import zerrium.LocatoZChunk;
import zerrium.LocatoZLocation;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Objects;

public class LocatoCommand implements CommandExecutor {
    private CommandSender cs;
    private final HashMap<String, Location> hm = new HashMap<>();
    private static final String no_perm = "[Locato] " + ChatColor.RESET + "Sorry you do not have permission to perform this command. Ask your admin for more info.";

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        cs = sender;
        final String msg = ChatColor.GOLD+"[Locato] " + ChatColor.RESET + "usage:\n" +
                "/locato <remove/status> <place_name>\n" +
                "/locato <add/edit> <place_name> (optional for add and player: <chunk1 pos x> <chunk1 pos y> <chunk1 pos z> <chunk2 pos x> <chunk2 pos y> <chunk2 pos z> <dimension: world or world_nether or world_the_end>)\n" +
                "/locato <search> <keyword>\n";
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
                        doAddEdit("add", args);
                        return true;
                    case "edit":
                        doAddEdit("edit", args);
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
                if(!cs.hasPermission("locato.search")){
                    cs.sendMessage(ChatColor.GOLD + no_perm);
                    return;
                }
                ArrayList<String> result = new ArrayList<>();
                for(LocatoZLocation zl :zerrium.Locato.zLocations){
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
                if(!cs.hasPermission("locato.status")){
                    cs.sendMessage(ChatColor.GOLD+"[Locato] " + no_perm);
                    return;
                }
                int index = zerrium.Locato.zLocations.indexOf(new LocatoZLocation(place));
                if(index == -1){
                    cs.sendMessage(ChatColor.GOLD+"[Locato] " + ChatColor.RESET + "No registered places found with \"" + place + "\" name.");
                }else{
                    LocatoZLocation zl = zerrium.Locato.zLocations.get(index);
                    int[] chunk1 = zl.getChunk1().getCoord();
                    int[] chunk2 = zl.getChunk2().getCoord();
                    World w = Bukkit.getWorld(zl.getDimension());
                    ArrayList<Player> p = new ArrayList<>();

                    for(int i=Math.min(chunk1[0], chunk2[0]); i<=Math.max(chunk1[0], chunk2[0]); i++){
                        for(int j=Math.min(chunk1[1], chunk2[1]); j<=Math.max(chunk1[1], chunk2[1]); j++){
                            assert w != null;
                            Chunk c = w.getChunkAt(i, j);
                            for(Entity e:c.getEntities()){
                                if(e.getType() == EntityType.PLAYER){
                                    Player temp = Bukkit.getPlayer(e.getUniqueId());
                                    assert temp != null;
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
                if(!cs.hasPermission("locato.delete")){
                    cs.sendMessage(ChatColor.GOLD+"[Locato] " + ChatColor.RESET + no_perm);
                    return;
                }
                int index = zerrium.Locato.zLocations.indexOf(new LocatoZLocation(place));
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
                if(!cs.hasPermission("locato.add")){
                    cs.sendMessage(ChatColor.GOLD+"[Locato] " + ChatColor.RESET + no_perm);
                    return;
                }
                if(hm.get(place) == null){
                    if(zerrium.Locato.zLocations.contains(new LocatoZLocation(place))){
                        cs.sendMessage(ChatColor.GOLD+"[Locato] " + ChatColor.RESET + "Place of \"" + place + "\" is already recorded on database. Try again using another name!");
                    }else{
                        hm.put(place, ((Player) cs).getLocation());
                        cs.sendMessage(ChatColor.GOLD+"[Locato] " + ChatColor.RESET + "First position recorded. Go to second position and perform \"/locato add " + place + "\" again to save this place.");
                    }
                }else{
                    Location lo1 = hm.get(place);
                    Location lo2 = ((Player) cs).getLocation();
                    if(!Objects.requireNonNull(lo1.getWorld()).getName().equals(Objects.requireNonNull(lo2.getWorld()).getName())){
                        cs.sendMessage(ChatColor.GOLD+"[Locato] " + ChatColor.RESET + "The location must be on the same world!");
                        return;
                    }
                    SQL_add_edit("add", place, Objects.requireNonNull(lo1.getWorld()).getName(), lo1.getChunk().getX(), lo1.getChunk().getZ(), (int) lo1.getY(), lo2.getChunk().getX(), lo2.getChunk().getZ(), (int) lo2.getY());
                }
            }
        };
        r.runTaskAsynchronously(zerrium.Locato.getPlugin(zerrium.Locato.class));
    }

    private void doAddEdit(String add_edit, String[] args){
        BukkitRunnable r = new BukkitRunnable() {
            @Override
            public void run() {
                int chunk1_x, chunk1_z, elevation1, chunk2_x, chunk2_z, elevation2;
                try{
                    chunk1_x = Integer.parseInt(args[2]);
                    elevation1 = Integer.parseInt(args[3]);
                    chunk1_z = Integer.parseInt(args[4]);
                    chunk2_x = Integer.parseInt(args[5]);
                    elevation2 = Integer.parseInt(args[6]);
                    chunk2_z = Integer.parseInt(args[7]);
                } catch (NumberFormatException e) {
                    cs.sendMessage(ChatColor.GOLD+"[Locato] " + ChatColor.RESET + "Arguments for <chunk1 pos x> <chunk1 pos y> <chunk1 pos z> <chunk2 pos x> <chunk2 pos y> <chunk2 pos z> must be numbers!");
                    return;
                } catch (ArrayIndexOutOfBoundsException e){
                    if(zerrium.Locato.debug) e.printStackTrace();
                    return;
                }
                String place = args[1];
                String dimension = args[8];
                if(add_edit.equals("add") && !cs.hasPermission("locato.add")){
                    cs.sendMessage(ChatColor.GOLD+"[Locato] " + ChatColor.RESET + no_perm);
                    return;
                }else if(add_edit.equals("edit") && !cs.hasPermission("locato.edit")){
                    cs.sendMessage(ChatColor.GOLD+"[Locato] " + ChatColor.RESET + no_perm);
                    return;
                }
                SQL_add_edit(add_edit, place, dimension, chunk1_x, chunk1_z, elevation1, chunk2_x, chunk2_z, elevation2);
            }
        };
        r.runTaskAsynchronously(zerrium.Locato.getPlugin(zerrium.Locato.class));
    }

    private void SQL_add_edit(String add_edit, String place, String dimension, int chunk1_x, int chunk1_z, int elevation1, int chunk2_x, int chunk2_z, int elevation2){
        System.out.println(ChatColor.YELLOW + "[Locato]" + ChatColor.RESET + (add_edit.equals("add") ? " Adding" : " Editing") +" place: " + place + " to database...");
        PreparedStatement pss = null;
        Connection con = null;
        try {
            con = LocatoSqlCon.openConnection();
            pss = con.prepareStatement((add_edit.equals("add") ?
                    "insert into locato(dimension, chunk1_x, chunk1_z, elevation1, chunk2_x, chunk2_z, elevation2, place_id) values (?, ?, ?, ?, ?, ?, ?, ?)" :
                    "update locato set dimension=?, chunk1_x=?, chunk1_z=?, elevation1=?, chunk2_x=?, chunk2_z=?, elevation2=? where place_id=?"));
            pss.setString(1, dimension);
            pss.setInt(2, chunk1_x);
            pss.setInt(3, chunk1_z);
            pss.setInt(4, elevation1);
            pss.setInt(5, chunk2_x);
            pss.setInt(6, chunk2_z);
            pss.setInt(7, elevation2);
            pss.setString(8, place);
            pss.executeUpdate();
            if(add_edit.equals("add")){
                hm.remove(place);
            }else{
                zerrium.Locato.zLocations.remove(new LocatoZLocation(place));
            }
            zerrium.Locato.zLocations.add(new LocatoZLocation(place, dimension, new LocatoZChunk(chunk1_x, chunk1_z, elevation1), new LocatoZChunk(chunk2_x, chunk2_z, elevation2)));
            final String m = "[Locato] " + ChatColor.RESET + (add_edit.equals("add") ? "Added" : "Edited") + " place: \"" + place + "\" to database record.";
            System.out.println(ChatColor.YELLOW + m);
            cs.sendMessage(ChatColor.GOLD + m);
        } catch (SQLException throwables) {
            throwables.printStackTrace();
            cs.sendMessage(ChatColor.GOLD+"[Locato] " + ChatColor.RESET + "Failed to" + (add_edit.equals("add") ? " add " : " edit ") + "place: \"" + place + "\" from database record. Check server console for more info.");
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
            con = LocatoSqlCon.openConnection();
            pss = con.prepareStatement("delete from locato where place_id=?");
            pss.setString(1, place);
            int row = pss.executeUpdate();
            System.out.println(ChatColor.YELLOW + "[Locato]" + ChatColor.RESET +
                    (row > 0 ? " Deleted place: " + place + " from database." : " No place of " + place + " found on the database. No rows affected."));
            zerrium.Locato.zLocations.remove(index);
            final String m = "[Locato] " + ChatColor.RESET + "Deleted place: \"" + place + "\" from database record.";
            cs.sendMessage(ChatColor.GOLD + m);
            System.out.println(ChatColor.YELLOW + m);
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
