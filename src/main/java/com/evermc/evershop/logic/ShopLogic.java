package com.evermc.evershop.logic;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.evermc.evershop.EverShop;
import com.evermc.evershop.action.ActionType;
import com.evermc.evershop.database.SQLDataSource;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.Container;
import org.bukkit.block.Sign;
import org.bukkit.entity.Player;
import org.bukkit.event.block.Action;
import org.bukkit.inventory.DoubleChestInventory;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;


class ShopInfo{
    int id;
    int epoch;
    ActionType action_id;
    int player_id;
    int world_id;
    int x;
    int y;
    int z;
}


public class ShopLogic {

    private EverShop plugin;
    private SQLDataSource SQL;

    List<Material> linkable_container = Arrays.asList(
        Material.CHEST,
        Material.TRAPPED_CHEST,
        Material.BARREL,
        Material.HOPPER
    );

    List<Material> linkable_redstone = Arrays.asList(
        Material.LEVER,
        Material.ACACIA_BUTTON,
        Material.BIRCH_BUTTON,
        Material.DARK_OAK_BUTTON,
        Material.JUNGLE_BUTTON,
        Material.OAK_BUTTON,
        Material.SPRUCE_BUTTON
    );
    
    public ShopLogic(EverShop plugin){
        this.plugin = plugin;
        this.SQL = plugin.getDataLogic().getSQL();
    }

    public void accessShop(Player p, Location loc, Action action){

    }
    public void registerBlock(Player p, Block block, Action action){

        PlayerInfo player = plugin.getPlayerLogic().getPlayerInfo(p);
        if (action == Action.RIGHT_CLICK_BLOCK && !player.advanced){
            return;
        }
        if (linkable_container.contains(block.getType()) || linkable_redstone.contains(block.getType()) || block.getState() instanceof Sign){
                
            // TODO: WorldGuard Check

            if (linkable_container.contains(block.getType())){
                if (!player.reg_is_container && (player.reg1.size()!=0 || player.reg2.size()!=0)){
                    p.sendMessage("You can't link redstone components and inventory at the same time");
                    return;
                }
                player.reg_is_container = true;
                Container container = (Container) block.getState();
                Location loc;
                if (container.getInventory().getSize() == 54){
                    loc = ((DoubleChestInventory)container.getInventory()).getLeftSide().getLocation();
                }else{
                    loc = block.getLocation();
                }
                if (player.reg1.contains(loc)){
                    player.reg1.remove(loc);
                    p.sendMessage("unlinked, cur:" + getRegisteredContents(player));
                    return;
                }
                if (player.reg2.contains(loc)){
                    player.reg2.remove(loc);
                    p.sendMessage("unlinked, cur:" + getRegisteredContents(player));
                    return;
                }
                if (action == Action.RIGHT_CLICK_BLOCK)
                    player.reg2.add(loc);
                else 
                    player.reg1.add(loc);
                p.sendMessage("linked " + block.getType() + ", cur:" + getRegisteredContents(player));
            }

        }
    }
    public String getRegisteredContents(PlayerInfo player){
        String result = "";
        Set<ItemStack> items = new HashSet<ItemStack>();
        if (player.reg_is_container){
            for (Location loc : player.reg1){
                Inventory inv = ((Container)loc.getBlock().getState()).getInventory();
                for (ItemStack is : inv.getContents()){
                    if (is == null) continue;
                    boolean duplicate = false;
                    for (ItemStack isc : items){
                        if (isc.isSimilar(is)){
                            duplicate = true;
                            items.remove(isc);
                            isc.setAmount(isc.getAmount() + is.getAmount());
                            items.add(isc);
                            break;
                        }
                    }
                    if (!duplicate){
                        items.add(is);
                    }
                }
            }
            for (ItemStack isc : items){
                result += "" + isc.getType() + "x" + isc.getAmount() + ";";
            }
        }
        return result;
    }
}