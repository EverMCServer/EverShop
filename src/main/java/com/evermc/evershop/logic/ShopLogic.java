package com.evermc.evershop.logic;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.evermc.evershop.EverShop;
import com.evermc.evershop.structure.PlayerInfo;
import com.evermc.evershop.structure.ShopInfo;
import com.evermc.evershop.structure.TransactionInfo;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.BlockState;
import org.bukkit.block.Container;
import org.bukkit.block.Sign;
import org.bukkit.entity.Player;
import org.bukkit.event.block.Action;
import org.bukkit.inventory.DoubleChestInventory;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import static com.evermc.evershop.handler.TranslationHandler.tr;

public class ShopLogic {

    private static EverShop plugin;
    
    private static Material linkMaterial;
    private static Material destroyMaterial;

    static{
        linkMaterial = null;
        destroyMaterial = null;
        plugin = null;
    }

    private static Set<Location> pendingRemoveBlocks = new HashSet<Location>();

    static final List<Material> linkable_container = Arrays.asList(
        Material.CHEST,
        Material.TRAPPED_CHEST,
        Material.BARREL,
        Material.HOPPER
    );

    static final List<Material> linkable_redstone = Arrays.asList(
        Material.LEVER,
        Material.ACACIA_BUTTON,
        Material.BIRCH_BUTTON,
        Material.DARK_OAK_BUTTON,
        Material.JUNGLE_BUTTON,
        Material.OAK_BUTTON,
        Material.SPRUCE_BUTTON,
        Material.STONE_BUTTON
    );
    
    public static void init(EverShop _plugin){
        plugin = _plugin;
        linkMaterial = Material.matchMaterial(plugin.getConfig().getString("evershop.linkMaterial"));
        destroyMaterial = Material.matchMaterial(plugin.getConfig().getString("evershop.destroyMaterial"));
    }

    public static Material getLinkMaterial(){
        return linkMaterial;
    }

    public static Material getDestroyMaterial(){
        return destroyMaterial;
    }

    public static boolean isLinkableBlock(Material m){
        return linkable_container.contains(m) || linkable_redstone.contains(m);
    }

    public static void accessShop(final Player p, final Location loc, final Action action){
        Bukkit.getScheduler().runTaskAsynchronously(plugin, ()->{
            ShopInfo si = DataLogic.getShopInfo(loc);
            if (si == null) {
                Bukkit.getScheduler().runTask(plugin, () -> {
                    if (loc.getBlock().getState() instanceof Sign){
                        Sign sign = (Sign)loc.getBlock().getState();
                        sign.setLine(0, ChatColor.stripColor(sign.getLine(0)));
                        sign.update();
                    }
                });
                return;
            }
            if (action == Action.LEFT_CLICK_BLOCK){
                // TODO - check perm
                String[] t = itemToString(si, p);
                final String str = tr("This shop %1$s %2$s for %3$s!", p, TransactionLogic.getName(si.action_id), t[0], t[1]);
                Bukkit.getScheduler().runTask(plugin, () -> {
                    TransactionInfo ti = new TransactionInfo(si, p);
                    si.setSignState(ti.shopHasItems());
                    p.sendMessage(str);
                });
            } else {
                // TODO - check perm
                Bukkit.getScheduler().runTask(plugin, () -> {
                    TransactionLogic.doTransaction(si, p);
                });
            }
        });
    }

    public static String[] itemToString(ShopInfo si, Player p){
        String[] ret = new String[2];
        if (TransactionLogic.itemsetCount(si.action_id) == 2){
            // trade shop
            ArrayList<HashSet<ItemStack>> items = si.getDoubleItems();
            ret[0] = "";
            for (ItemStack is : items.get(0)){
                ret[0] += tr(is,p) + ", ";
            }
            ret[0] = ret[0].substring(0, ret[0].length() - 2);
            ret[1] = "";
            for (ItemStack is : items.get(1)){
                ret[1] += tr(is,p) + ", ";
            }
            ret[1] = ret[1].substring(0, ret[1].length() - 2);
            if (si.price > 0){
                ret[1] += " + $" +si.price;
            } else if (si.price < 0){
                ret[0] += " + $" + (-si.price);
            }
        } else if (TransactionLogic.itemsetCount(si.action_id) == 1){
            // buy
            HashSet<ItemStack> items = si.getAllItems();
            ret[0] = "";
            for (ItemStack is : items){
                ret[0] += tr(is,p) + ", ";
            }
            ret[0] = ret[0].substring(0, ret[0].length() - 2);
            ret[1] = "$" + si.price;
        } else {
            ret[1] = "$" + si.price;
        }
        return ret;
    }

    public static boolean registerBlock(final Player p, Block block, Action action){

        PlayerInfo player = PlayerLogic.getPlayerInfo(p);
        if (action == Action.RIGHT_CLICK_BLOCK && !player.advanced){
            return false;
        }
        if (linkable_container.contains(block.getType()) || linkable_redstone.contains(block.getType()) || block.getState() instanceof Sign){

            // TODO: WorldGuard Check if block is not Sign

            if (linkable_container.contains(block.getType())){
                if (!player.reg_is_container && (player.reg1.size()!=0 || player.reg2.size()!=0)){
                    p.sendMessage(tr("You cant link redstone components and inventory at the same time", p));
                    return true;
                }
                player.reg_is_container = true;
                Container container = (Container) block.getState();
                Location loc;
                if (container.getInventory().getSize() == 54){
                    loc = ((DoubleChestInventory)container.getInventory()).getLeftSide().getLocation();
                    Location loc2 = ((DoubleChestInventory)container.getInventory()).getRightSide().getLocation();
                    if (player.reg1.contains(loc2)){
                        player.reg1.remove(loc2);
                        p.sendMessage(tr("unlinked this",p));
                        p.sendMessage(getRegisteredContents(p));
                        return true;
                    }
                    if (player.reg2.contains(loc2)){
                        player.reg2.remove(loc2);
                        p.sendMessage(tr("unlinked this",p));
                        p.sendMessage(getRegisteredContents(p));
                        return true;
                    }
                }else{
                    loc = block.getLocation();
                }
                if (player.reg1.contains(loc)){
                    player.reg1.remove(loc);
                    p.sendMessage(tr("unlinked this",p));
                    p.sendMessage(getRegisteredContents(p));
                    return true;
                }
                if (player.reg2.contains(loc)){
                    player.reg2.remove(loc);
                    p.sendMessage(tr("unlinked this",p));
                    p.sendMessage(getRegisteredContents(p));
                    return true;
                }
                if (action == Action.RIGHT_CLICK_BLOCK)
                    player.reg2.add(loc);
                else 
                    player.reg1.add(loc);
                    p.sendMessage(tr("linked %1$s", p, tr(block.getType(), p)));
                    p.sendMessage(getRegisteredContents(p));
            }

            else if (linkable_redstone.contains(block.getType())){
                if (player.reg_is_container && (player.reg1.size()!=0 || player.reg2.size()!=0)){
                    p.sendMessage(tr("You cant link redstone components and inventory at the same time", p));
                    return true;
                }
                player.reg_is_container = false;
                Location loc = block.getLocation();
                if (player.reg1.contains(loc)){
                    player.reg1.remove(loc);
                    p.sendMessage(tr("unlinked this",p));
                    p.sendMessage(getRegisteredContents(p));
                }else{
                    player.reg1.add(block.getLocation());
                    p.sendMessage(tr("linked %1$s", p, tr(block.getType(), p)));
                    p.sendMessage(getRegisteredContents(p));
                }
            }

            else if (block.getState() instanceof Sign){
                String line = ((Sign)block.getState()).getLine(0);
                int a = TransactionLogic.getId(line);
                if (a == 0) {
                    p.sendMessage(tr("The sign does not contain an available action!", p));
                    return true;
                }
                if (player.reg1.size() == 0){
                    p.sendMessage(tr("You should register items first!", p));
                    return true;
                }
                if (player.reg_is_container != TransactionLogic.isContainerShop(a)){
                    p.sendMessage(tr("Shop type and your selection is not match!", p));
                    return true;
                }
                final ShopInfo newshop = new ShopInfo(a, player, block.getLocation(), TransactionLogic.getPrice(line));
                if (TransactionLogic.isContainerShop(a) && newshop.getAllItems().size() == 0){
                    p.sendMessage(tr("You should put some items in the chest first!", p));
                    return true;
                }
                final Sign sign = (Sign)block.getState();
                DataLogic.saveShop(newshop, () -> {
                    String lin = sign.getLine(0);
                    lin = ChatColor.BLUE.toString() + ChatColor.BOLD.toString() + lin;
                    sign.setLine(0, lin);
                    sign.update();
                    PlayerLogic.getPlayerInfo(p).removeRegs();
                    String[] t = itemToString(newshop, p);
                    p.sendMessage(tr("You have created a shop %1$s %2$s for %3$s!", p, TransactionLogic.getName(newshop.action_id), t[0], t[1]));
                }, () -> {
                    p.sendMessage(tr("Failed to create shop, maybe you put too many items in the shop", p));
                });
            }
            return true;
        }
        return false;
    }

    public static HashSet<ItemStack> getReg1(PlayerInfo player){
        player.cleanupRegs();
        HashSet<ItemStack> items = new HashSet<ItemStack>();
        if (player.reg_is_container){
            for (Location loc : player.reg1){
                Inventory inv = ((Container)loc.getBlock().getState()).getInventory();
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
        }
        return items;
    }

    public static HashSet<ItemStack> getReg2(PlayerInfo player){
        player.cleanupRegs();
        HashSet<ItemStack> items = new HashSet<ItemStack>();
        if (player.reg_is_container){
            for (Location loc : player.reg2){
                Inventory inv = ((Container)loc.getBlock().getState()).getInventory();
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
        }
        return items;
    }

    private static String getRegisteredContents(Player p){
        String result;
        PlayerInfo player = PlayerLogic.getPlayerInfo(p);
        if (!player.advanced){
            result = tr("Current selection:", p);
            HashSet<ItemStack> items = getReg1(player);
            for (ItemStack isc : items){
                result += tr(isc, p) + ", ";
            }
            if (items.size() > 0)
                result = result.substring(0, result.length() - 2);
        } else {
            result = tr("Main selection:", p);
            HashSet<ItemStack> items = getReg1(player);
            for (ItemStack isc : items){
                result += tr(isc, p) + ", ";
            }
            if (items.size() > 0)
                result = result.substring(0, result.length() - 2);
            result += "\n";
            result += tr("Sub selection:", p);
            items = getReg2(player);
            for (ItemStack isc : items){
                result += tr(isc, p) + ", ";
            }
            if (items.size() > 0)
                result = result.substring(0, result.length() - 2);
        }
        return result;
    }

    public static void tryBreakShop(final Location loc, final Player p){
        if (pendingRemoveBlocks.contains(loc)){
            return;
        }
        pendingRemoveBlocks.add(loc);
        Bukkit.getScheduler().runTaskAsynchronously(plugin, ()->{
            int pl = DataLogic.getShopOwner(loc);
            if (pl == 0){
                Bukkit.getScheduler().runTask(plugin, ()->{
                    pendingRemoveBlocks.remove(loc);
                    loc.getBlock().breakNaturally();
                });
            } else {
                if (pl == PlayerLogic.getPlayer(p)
                     || ( p.hasPermission("evershop.admin.remove")
                         && p.getInventory().getItemInMainHand().getType() == ShopLogic.getDestroyMaterial())
                    ){
                    Bukkit.getScheduler().runTask(plugin, ()->{
                        pendingRemoveBlocks.remove(loc);
                        DataLogic.removeShop(loc);
                        loc.getBlock().breakNaturally();
                    });
                } else {
                    Bukkit.getScheduler().runTask(plugin, ()->{
                        pendingRemoveBlocks.remove(loc);
                        p.sendMessage(tr("You cannot break this!", p));
                    });
                }
            }
        });
    }
    public static void tryBreakBlock(final Location lo, final Player p, final Location[] locs){
        final Location loc;
        final Material blocktype = lo.getBlock().getType();
        if (lo.getBlock().getState() instanceof Container && ((Container) lo.getBlock().getState()).getInventory().getSize() == 54){
            loc = ((DoubleChestInventory)((Container) lo.getBlock().getState()).getInventory()).getLeftSide().getLocation();
        }else{
            loc = lo;
        }
        if (pendingRemoveBlocks.contains(loc)){
            return;
        }
        pendingRemoveBlocks.add(loc);

        Bukkit.getScheduler().runTaskAsynchronously(plugin, ()->{
            // first, check attached signs
            int[] sis = DataLogic.getShopOwner(locs);
            if (sis != null){
                Bukkit.getScheduler().runTask(plugin, ()->{
                    p.sendMessage(tr("you cannot break this block because there are shops attached on it", p));
                    pendingRemoveBlocks.remove(loc);
                });
                return;
            }
            // second, check if block has connected to shops
            if (!isLinkableBlock(blocktype)){
                // not a linkable block, break it
                Bukkit.getScheduler().runTask(plugin, ()->{
                    pendingRemoveBlocks.remove(loc);
                    lo.getBlock().breakNaturally();
                });
            }
            final ShopInfo[] si = DataLogic.getBlockInfo(loc);
            if (si == null){
                Bukkit.getScheduler().runTask(plugin, ()->{
                    pendingRemoveBlocks.remove(loc);
                    lo.getBlock().breakNaturally();
                });
            } else {
                Bukkit.getScheduler().runTask(plugin, ()->{
                    pendingRemoveBlocks.remove(loc);
                    String loc_str = "";
                    int count = 0;
                    int tcount = 0;
                    for (ShopInfo sii : si){
                        BlockState bs = DataLogic.getWorld(sii.world_id).getBlockAt(sii.x, sii.y, sii.z).getState();
                        if (bs instanceof Sign && ((Sign)bs).getLine(0).length() > 0 && (int)((Sign)bs).getLine(0).charAt(0) == 167){
                            if (sii.player_id == PlayerLogic.getPlayer(p)){
                                loc_str += "(" + sii.x +"," + sii.y + "," + sii.z + "), ";
                            }else{
                                count++;
                            }
                            tcount ++;
                        } else {
                            // detect unavailable shops (have shop info but no signs)
                            DataLogic.removeShop(sii.id);
                        }
                    }
                    String other_str = "";
                    if (count > 0){
                        other_str = tr("%1$s shops of other player", p, count);
                    }
                    if (loc_str.length() > 2) loc_str = loc_str.substring(0, loc_str.length() - 2);
                    if (tcount == 0){
                        lo.getBlock().breakNaturally();
                    }else{
                        p.sendMessage(tr("You cannot break this! Its locked by %1$s %2$s", p, loc_str, other_str));
                    }
                });
            }
        });
    }
    public static Location[] getAttachedSign(Block b){
        BlockFace[] iter = {BlockFace.SOUTH, BlockFace.NORTH, BlockFace.EAST, BlockFace.WEST};
        HashSet<Location> ret = new HashSet<Location>();
        for (BlockFace i : iter){
            if ((b.getRelative(i).getBlockData() instanceof org.bukkit.block.data.type.WallSign) &&
                ((org.bukkit.block.data.type.WallSign)b.getRelative(i).getBlockData()).getFacing() == i){
                    ret.add(b.getRelative(i).getLocation());
                }
        }
        if (b.getRelative(BlockFace.UP).getBlockData() instanceof org.bukkit.block.data.type.Sign){
            ret.add(b.getRelative(BlockFace.UP).getLocation());
        }
        return ret.size() == 0? null:ret.toArray(new Location[ret.size()]);
    }

}