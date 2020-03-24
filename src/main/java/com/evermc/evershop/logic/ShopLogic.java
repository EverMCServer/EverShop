package com.evermc.evershop.logic;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import com.evermc.evershop.EverShop;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
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

public class ShopLogic {

    private EverShop plugin;

    private Set<Location> pendingRemoveBlocks = new HashSet<Location>();

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
    }

    public boolean isLinkableBlock(Material m){
        return linkable_container.contains(m) || linkable_redstone.contains(m);
    }

    public void accessShop(Player p, Location loc, Action action){

    }
    public void registerBlock(final Player p, Block block, Action action){

        PlayerInfo player = plugin.getPlayerLogic().getPlayerInfo(p);
        if (action == Action.RIGHT_CLICK_BLOCK && !player.advanced){
            return;
        }
        if (linkable_container.contains(block.getType()) || linkable_redstone.contains(block.getType()) || block.getState() instanceof Sign){
                
            // TODO: WorldGuard Check if block is not Sign

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
                    Location loc2 = ((DoubleChestInventory)container.getInventory()).getRightSide().getLocation();
                    if (player.reg1.contains(loc2)){
                        player.reg1.remove(loc2);
                        p.sendMessage("unlinked, cur:" + getRegisteredContents(player));
                        return;
                    }
                    if (player.reg2.contains(loc2)){
                        player.reg2.remove(loc2);
                        p.sendMessage("unlinked, cur:" + getRegisteredContents(player));
                        return;
                    }
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

            else if (linkable_redstone.contains(block.getType())){
                if (player.reg_is_container && (player.reg1.size()!=0 || player.reg2.size()!=0)){
                    p.sendMessage("You can't link redstone components and inventory at the same time");
                    return;
                }
                player.reg_is_container = false;
                Location loc = block.getLocation();
                if (player.reg1.contains(loc)){
                    player.reg1.remove(loc);
                    p.sendMessage("unlinked " + block.getType());
                }else{
                    player.reg1.add(block.getLocation());
                    p.sendMessage("linked " + block.getType());
                }
            }

            else if (block.getState() instanceof Sign){
                String line = ((Sign)block.getState()).getLine(0);
                int a = plugin.getTransactionLogic().getActionType(line);
                if (a == 0) {
                    p.sendMessage("The sign does not contain an available action!");
                    return;
                }
                if (player.reg1.size() == 0){
                    p.sendMessage("You should register items first!");
                    return;
                }
                if (player.reg_is_container != plugin.getTransactionLogic().isContainerShop(a)){
                    p.sendMessage("Shop type and your selection is not match!");
                    return;
                }
                int n = getPrice(line);
                // TODO - init perm when creating shop
                final ShopInfo newshop = new ShopInfo(this.plugin, a, player.id, block.getLocation(), n, player.reg1, getRegisteredItemStacks(player), "");
                final Sign sign = (Sign)block.getState();
                plugin.getDataLogic().saveShop(newshop, () -> {
                    String lin = sign.getLine(0);
                    lin = ChatColor.BLUE.toString() + lin;
                    sign.setLine(0, lin);
                    sign.update();
                    plugin.getPlayerLogic().getPlayerInfo(p).removeRegs();
                    p.sendMessage("You have created a " + plugin.getTransactionLogic().getShopType(newshop.action_id) + " shop, price is " + newshop.price);
                }, () -> {
                    p.sendMessage("Failed to create shop, maybe you put too many items in the shop.");
                });
            }
        }
    }

    private int getPrice(String line){
        String ret = "";
        int i = line.length() - 1;
        while (i >= 0 && !Character.isDigit(line.charAt(i))) i--;
        for (;i >= 0 && Character.isDigit(line.charAt(i)); i--){
            ret = line.charAt(i) + ret;
        }
        if ("".equals(ret)) return 0;
        return Integer.parseInt(ret);
    }

    private HashSet<ItemStack> getRegisteredItemStacks(PlayerInfo player){

        HashSet<ItemStack> items = new HashSet<ItemStack>();
        if (player.reg_is_container){
            for (Location loc : player.reg1){
                if (!(loc.getBlock().getState() instanceof Container)){
                    player.reg1.remove(loc);
                    continue;
                }
                Inventory inv = ((Container)loc.getBlock().getState()).getInventory();
                if (inv.getSize() == 54 && ((DoubleChestInventory)inv).getRightSide().getLocation().equals(loc)){
                    player.reg1.remove(loc);
                    player.reg2.remove(loc);
                    player.reg1.add(((DoubleChestInventory)inv).getLeftSide().getLocation());
                    continue;
                }
                for (ItemStack is : inv.getContents()){
                    if (is == null) continue;
                    boolean duplicate = false;
                    for (ItemStack isc : items){
                        if (isc.isSimilar(is)){
                            duplicate = true;
                            isc.setAmount(isc.getAmount() + is.getAmount());
                            break;
                        }
                    }
                    if (!duplicate){
                        items.add(is.clone());
                    }
                }
            }
            for (Location loc : player.reg2){
                if (!(loc.getBlock().getState() instanceof Container)){
                    player.reg2.remove(loc);
                    continue;
                }
                Inventory inv = ((Container)loc.getBlock().getState()).getInventory();
                if (inv.getSize() == 54 && ((DoubleChestInventory)inv).getRightSide().getLocation().equals(loc)){
                    player.reg1.remove(loc);
                    player.reg2.remove(loc);
                    player.reg2.add(((DoubleChestInventory)inv).getLeftSide().getLocation());
                    continue;
                }
            }
        }
        return items;
    }
    private String getRegisteredContents(PlayerInfo player){
        String result = "";
        HashSet<ItemStack> items = getRegisteredItemStacks(player);
        for (ItemStack isc : items){
            result += "" + isc.getType() + "x" + isc.getAmount() + ";";
        }
        return result;
    }

    public void tryBreakBlock(final Location loc, final Player p){
        if (pendingRemoveBlocks.contains(loc)){
            return;
        }
        pendingRemoveBlocks.add(loc);
        Bukkit.getScheduler().runTaskAsynchronously(plugin, ()->{
            ShopInfo[] si = plugin.getDataLogic().getBlockInfo(loc);
            if (si == null){
                Bukkit.getScheduler().runTask(plugin, ()->{
                    pendingRemoveBlocks.remove(loc);
                    loc.getBlock().breakNaturally();
                });
            } else {
                Bukkit.getScheduler().runTask(plugin, ()->{
                    pendingRemoveBlocks.remove(loc);
                    p.sendMessage("You cannot break this");
                    // TODO - check perm and show info about shop
                });
            }
        });
    }
}