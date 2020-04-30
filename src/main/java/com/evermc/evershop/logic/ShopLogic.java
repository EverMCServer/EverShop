package com.evermc.evershop.logic;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;

import com.evermc.evershop.EverShop;
import com.evermc.evershop.handler.WorldGuardHandler;
import com.evermc.evershop.structure.PlayerInfo;
import com.evermc.evershop.structure.ShopInfo;
import com.evermc.evershop.structure.TransactionInfo;
import com.evermc.evershop.util.RedstoneUtil;
import com.evermc.evershop.util.SerializableLocation;
import com.evermc.evershop.util.TranslationUtil;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.Container;
import org.bukkit.block.Sign;
import org.bukkit.block.data.type.Switch;
import org.bukkit.entity.Player;
import org.bukkit.event.block.Action;
import org.bukkit.inventory.DoubleChestInventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.metadata.MetadataValue;

import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.TextComponent;

import static com.evermc.evershop.util.TranslationUtil.send;
import static com.evermc.evershop.util.TranslationUtil.tr;
import static com.evermc.evershop.util.LogUtil.severe;

public class ShopLogic {

    private static EverShop plugin = null;
    
    private static Material linkMaterial = null;
    private static Material destroyMaterial = null;
    private static int maxLinkBlocks = 0;

    private static Set<Location> pendingRemoveBlocks = new CopyOnWriteArraySet<Location>();

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
        if (linkMaterial == null) {
            severe("link material: " + plugin.getConfig().getString("evershop.linkMaterial") + " does not exist. use default.");
            linkMaterial = Material.REDSTONE;
        }
        destroyMaterial = Material.matchMaterial(plugin.getConfig().getString("evershop.destroyMaterial"));
        if (destroyMaterial == null) {
            severe("destroy material: " + plugin.getConfig().getString("evershop.destroyMaterial") + " does not exist. use default.");
            destroyMaterial = Material.GOLDEN_AXE;
        }
        maxLinkBlocks = plugin.getConfig().getInt("evershop.maxLinkBlocks");
    }

    public static void reload(EverShop _plugin) {
        init(_plugin);
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
            PlayerInfo pi = PlayerLogic.getPlayerInfo(p);
            if (si == null) {
                severe("accessShop error: no shop data at " + loc);
                return;
            }
            if (action == Action.LEFT_CLICK_BLOCK){
                if (pi.getId() != si.getOwnerId() && !si.getExtraInfo().checkPermission(p)) {
                    p.sendMessage("no permission.");
                    return;
                }
                Bukkit.getScheduler().runTask(plugin, () -> {
                    TransactionInfo ti = new TransactionInfo(si, p);
                    si.setSignState(ti.shopHasItems());
                    BaseComponent[] t = itemToString(si);
                    send("%1$s " + TransactionLogic.getEnum(si.getAction()).name() + " shop %2$s for %3$s!", p, 
                    PlayerLogic.getPlayerName(si.getOwnerId()), t[0] , t[1]);
                });
            } else {
                if (pi.getId() != si.getOwnerId() && !si.getExtraInfo().checkPermission(p)) {
                    p.sendMessage("no permission.");
                    return;
                }
                Bukkit.getScheduler().runTask(plugin, () -> {
                    TransactionLogic.doTransaction(si, p);
                });
            }
        });
    }

    public static BaseComponent[] itemToString(ShopInfo si){
        BaseComponent[] ret = new BaseComponent[2];
        if (TransactionLogic.itemsetCount(si.getAction()) == 2){
            // trade shop
            ret[0] = new TextComponent();
            Iterator<ItemStack> it = si.getItemOut().iterator();
            while(it.hasNext()){
                ItemStack is = it.next();
                ret[0].addExtra(TranslationUtil.tr(is));
                if (it.hasNext()){
                    ret[0].addExtra(", ");
                }
            }
            ret[1] = new TextComponent();
            it = si.getItemIn().iterator();
            while(it.hasNext()){
                ItemStack is = it.next();
                ret[1].addExtra(TranslationUtil.tr(is));
                if (it.hasNext()){
                    ret[1].addExtra(", ");
                }
            }
            if (si.getPrice()> 0){
                TextComponent tc = new TextComponent("$" +si.getPrice());
                tc.setColor(net.md_5.bungee.api.ChatColor.YELLOW);
                ret[1].addExtra(" + ");
                ret[1].addExtra(tc);
            } else if (si.getPrice() < 0){
                TextComponent tc = new TextComponent("$" + (-si.getPrice()));
                tc.setColor(net.md_5.bungee.api.ChatColor.YELLOW);
                ret[0].addExtra(" + ");
                ret[0].addExtra(tc);
            }
        } else if (TransactionLogic.itemsetCount(si.getAction()) == 1){
            // buy/sell
            HashSet<ItemStack> items = si.getItemAll();
            ret[0] = new TextComponent();
            Iterator<ItemStack> it = items.iterator();
            while(it.hasNext()){
                ItemStack is = it.next();
                ret[0].addExtra(TranslationUtil.tr(is));
                if (it.hasNext()){
                    ret[0].addExtra(", ");
                }
            }
            TextComponent tc = new TextComponent("$" + si.getPrice());
            tc.setColor(net.md_5.bungee.api.ChatColor.YELLOW);
            ret[1] = tc;
        } else {
            TextComponent tc = new TextComponent("$" + si.getPrice());
            tc.setColor(net.md_5.bungee.api.ChatColor.YELLOW);
            ret[1] = tc;
        }
        return ret;
    }

    public static boolean registerBlock(final Player p, Block block, Action action){

        PlayerInfo player = PlayerLogic.getPlayerInfo(p);
        if (action == Action.RIGHT_CLICK_BLOCK && !player.isAdvanced()){
            return false;
        }
        if (linkable_container.contains(block.getType()) || linkable_redstone.contains(block.getType()) || block.getState() instanceof Sign){

            if (!(block.getState() instanceof Sign) && !WorldGuardHandler.canAccessChest(p, block.getLocation())) {
                send("You cant link this", p);
                return true;
            }

            if (linkable_container.contains(block.getType())){
                if (player.getReg1().size()!=0 || player.getReg2().size()!=0) {
                    if (!player.isContainer()){
                        send("You cant link redstone components and inventory at the same time", p);
                        return true;
                    }
                    Location selected_loc;
                    if (player.getReg1().iterator().hasNext()){
                        selected_loc = player.getReg1().iterator().next();
                    } else {
                        selected_loc = player.getReg2().iterator().next();
                    }
                    if (!p.hasPermission("evershop.multiworld") && !block.getLocation().getWorld().equals(selected_loc.getWorld())){
                        send("You cant make multi-world shops", p);
                        return true;
                    }
                    if (player.getReg1().size() + player.getReg2().size() > maxLinkBlocks){
                        send("You have linked too many blocks", p);
                        return true;
                    }
                }
                
                player.setContainer(true);
                Container container = (Container) block.getState();
                Location loc;
                if (container.getInventory().getSize() == 54){
                    loc = ((DoubleChestInventory)container.getInventory()).getLeftSide().getLocation();
                    Location loc2 = ((DoubleChestInventory)container.getInventory()).getRightSide().getLocation();
                    if (player.getReg1().contains(loc2) || player.getReg2().contains(loc2)){
                        player.getReg1().remove(loc2);
                        player.getReg2().remove(loc2);
                        send("unlinked this",p);
                        BaseComponent content = getRegisteredContents(p);
                        if (content != null) send(content, p);
                        return true;
                    }
                }else{
                    loc = block.getLocation();
                }
                if (player.getReg1().contains(loc) || player.getReg2().contains(loc)){
                    player.getReg1().remove(loc);
                    player.getReg2().remove(loc);
                    send("unlinked this",p);
                    BaseComponent content = getRegisteredContents(p);
                    if (content != null) send(content, p);
                    return true;
                }
                if (action == Action.RIGHT_CLICK_BLOCK){
                    player.getReg2().add(loc);
                } else { 
                    player.getReg1().add(loc);
                }
                send("linked %1$s", p, tr(block.getType()));
                BaseComponent content = getRegisteredContents(p);
                if (content != null) send(content, p);
            }

            else if (linkable_redstone.contains(block.getType())){
                if (player.isContainer() && (player.getReg1().size()!=0 || player.getReg2().size()!=0)){
                    send("You cant link redstone components and inventory at the same time", p);
                    return true;
                }
                player.setContainer(false);
                Location loc = block.getLocation();
                if (player.getReg1().contains(loc)){
                    player.getReg1().remove(loc);
                    send("unlinked this",p);
                    BaseComponent content = getRegisteredContents(p);
                    if (content != null) send(content, p);
                }else{
                    player.getReg1().add(block.getLocation());
                    send("linked %1$s", p, tr(block.getType()));
                    BaseComponent content = getRegisteredContents(p);
                    if (content != null) send(content, p);
                }
            }

            else if (block.getState() instanceof Sign){
                String line = ((Sign)block.getState()).getLine(0);
                int actionid = TransactionLogic.getId(line);
                if (actionid == 0) {
                    send("The sign does not contain an available action!", p);
                    return true;
                }
                if (!line.equals(ChatColor.stripColor(line))) {
                    send("You cant create shops on formatted signs!", p);
                    return true;
                }
                if (player.getReg1().size() == 0 && actionid != TransactionLogic.DISPOSE.id()){
                    send("You should register items first!", p);
                    return true;
                }
                if (actionid == TransactionLogic.DISPOSE.id() && player.getReg1().size() > 1) {
                    send("You can link at most one chest to a dispose shop", p);
                    return true;
                }
                if (player.isContainer() != TransactionLogic.isContainerShop(actionid)){
                    send("Shop type and your selection is not match!", p);
                    return true;
                }
                if (!p.hasPermission("evershop.create." + TransactionLogic.getEnum(actionid).name().toLowerCase())){
                    send("You do not have permission to create a %1$s shop", p, TransactionLogic.getName(actionid));
                    return true;
                }
                if (!p.hasPermission("evershop.multiworld")){
                    World w = null;
                    for (Location l : player.getReg1()){
                        if (w == null) w = l.getWorld();
                        else if (!w.equals(l.getWorld())){
                            send("You cant make multi-world shops", p);
                            return true;
                        }
                    }
                    for (Location l : player.getReg2()){
                        if (w == null) w = l.getWorld();
                        else if (!w.equals(l.getWorld())){
                            send("You cant make multi-world shops", p);
                            return true;
                        }
                    }
                }
                if (TransactionLogic.needItemSet(actionid) && player.getReg1Items().size() == 0){
                    send("You should put some items in the chest first!", p);
                    return true;
                }
                final ShopInfo newshop = new ShopInfo(actionid, player, block.getLocation(), TransactionLogic.getPrice(line));
                final Sign sign = (Sign)block.getState();
                DataLogic.saveShop(newshop, (shopid) -> {
                    String lin = sign.getLine(0);
                    lin = ChatColor.DARK_BLUE.toString() + ChatColor.BOLD.toString() + lin;
                    sign.setLine(0, lin);
                    sign.update();
                    PlayerLogic.getPlayerInfo(p).removeRegs();
                    sign.setMetadata("shopid", new FixedMetadataValue(EverShop.getInstance(), shopid));
                    BaseComponent[] t = itemToString(newshop);
                    send("setup " + TransactionLogic.getEnum(newshop.getAction()).name() + " shop %1$s for %2$s!", p, 
                         t[0] , t[1]);
                }, () -> {
                    send("Failed to create shop, maybe you put too many items in the shop", p);
                });
            }
            return true;
        }
        return false;
    }

    private static BaseComponent getRegisteredContents(Player p){
        PlayerInfo pi = PlayerLogic.getPlayerInfo(p);
        if (pi.isContainer()){
            if (pi.getReg1Items().size() != 0 || (pi.isAdvanced() && pi.getReg2Items().size() != 0)){
                return getRegisteredInventoryContents(p);
            }
        } else {
            if (pi.getReg1().size() != 0){
                return getRegisteredRedstoneTargets(p);
            }
        }
        return null;
    }

    private static BaseComponent getRegisteredRedstoneTargets(Player p){
        BaseComponent result = tr("Current selection:", p);
        PlayerInfo pi = PlayerLogic.getPlayerInfo(p);
        for (Location l : pi.getReg1()){
            Material m = l.getBlock().getType();
            if (linkable_redstone.contains(m)){
                result.addExtra(tr(m));
                result.addExtra("@" + tr(l) + ", ");
            } 
        }
        return result;
    }

    private static BaseComponent getRegisteredInventoryContents(Player p){
        BaseComponent result;
        PlayerInfo player = PlayerLogic.getPlayerInfo(p);
        if (!player.isAdvanced()){
            result = tr("Current selection:", p);
            Iterator<ItemStack> it = player.getReg1Items().iterator();
            while (it.hasNext()) {
                ItemStack isc = it.next();
                result.addExtra(tr(isc));
                if (it.hasNext()){
                    result.addExtra(", ");
                }
            }
        } else {
            result = tr("Main selection:", p);
            Iterator<ItemStack> it = player.getReg1Items().iterator();
            while (it.hasNext()) {
                ItemStack isc = it.next();
                result.addExtra(tr(isc));
                if (it.hasNext()){
                    result.addExtra(", ");
                }
            }
            result.addExtra("\n");
            result.addExtra(TranslationUtil.title());
            result.addExtra(tr("Sub selection:", p));
            it = player.getReg2Items().iterator();
            while (it.hasNext()) {
                ItemStack isc = it.next();
                result.addExtra(tr(isc));
                if (it.hasNext()){
                    result.addExtra(", ");
                }
            }
        }
        return result;
    }

    public static void tryBreakShop(final Location loc, final Player p){
        if (pendingRemoveBlocks.contains(loc)){
            return;
        }
        pendingRemoveBlocks.add(loc);
        
        Location[] signs = ShopLogic.getAttachedSign(loc.getBlock());
        for (Location loca : signs) {
            if (ShopLogic.isShopSign(loca.getBlock())) {
                send("You cannot break this block because there are shops attached to it", p);
                pendingRemoveBlocks.remove(loca);
                return;
            }
        }
        if (!ShopLogic.isShopSign(loc.getBlock())){
            pendingRemoveBlocks.remove(loc);
            loc.getBlock().breakNaturally();
            return;
        }
        Bukkit.getScheduler().runTaskAsynchronously(plugin, ()->{
            int pl = DataLogic.getShopOwner(loc);
            if (pl == PlayerLogic.getPlayerId(p)
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
                    send("You cannot remove others shop!", p);
                });
            }
        });
    }
    public static void tryBreakBlock(final Location lo, final Player p, final Location[] locs, final Location[] blocs){
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

        // first, check attached blocks
        for (Location loca : locs) {
            if (ShopLogic.isShopSign(loca.getBlock())) {
                send("You cannot break this block because there are shops attached on it", p);
                pendingRemoveBlocks.remove(loc);
                return;
            }
        }

        Bukkit.getScheduler().runTaskAsynchronously(plugin, ()->{
            if (blocs != null){
                for (Location loca : blocs){
                    int count = DataLogic.getBlockLinkedCount(loca);
                    if (count > 0){
                        Bukkit.getScheduler().runTask(plugin, ()->{
                            send("You cannot break this block because there are shops linked to it", p);
                            pendingRemoveBlocks.remove(loc);
                        });
                        return;
                    }
                }
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
                        Block b = DataLogic.getWorld(sii.getWorldID()).getBlockAt(sii.getX(), sii.getY(), sii.getZ());
                        if (ShopLogic.isShopSign(b)){
                            if (sii.getOwnerId() == PlayerLogic.getPlayerId(p)){
                                loc_str += SerializableLocation.toString(b.getLocation()) + ", ";
                            }else{
                                count++;
                            }
                            tcount ++;
                        } else {
                            // detect unavailable shops (have shop info but no signs)
                            DataLogic.removeShop(sii.getId());
                        }
                    }
                    BaseComponent other_str;
                    if (count > 0){
                        other_str = tr("%1$s shops of other player", p, count);
                    } else {
                        other_str = new TextComponent();
                    }
                    if (loc_str.length() > 2) loc_str = loc_str.substring(0, loc_str.length() - 2);
                    if (tcount == 0){
                        lo.getBlock().breakNaturally();
                    }else{
                        send("You cannot break this! Its locked by %1$s %2$s", p, loc_str, other_str);
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
        return ret.toArray(new Location[ret.size()]);
    }

    public static Location[] getAttachedBlock(Block b){
        BlockFace[] iter = {BlockFace.SOUTH, BlockFace.NORTH, BlockFace.EAST, BlockFace.WEST};
        HashSet<Location> ret = new HashSet<Location>();
        for (BlockFace i : iter){
            if ((b.getRelative(i).getBlockData() instanceof Switch) && RedstoneUtil.getAttachedFace(b.getRelative(i)) == i.getOppositeFace()){
                ret.add(b.getRelative(i).getLocation());
                continue;
            }
        }
        if (b.getRelative(BlockFace.UP).getBlockData() instanceof Switch && RedstoneUtil.getAttachedFace(b.getRelative(BlockFace.UP)) == BlockFace.DOWN){
            ret.add(b.getRelative(BlockFace.UP).getLocation());
        }
        if (b.getRelative(BlockFace.DOWN).getBlockData() instanceof Switch && RedstoneUtil.getAttachedFace(b.getRelative(BlockFace.DOWN)) == BlockFace.UP){
            ret.add(b.getRelative(BlockFace.DOWN).getLocation());
        }
        return ret.toArray(new Location[ret.size()]);
    }

    public static boolean isShopSign(Block b) {
        return isShopSign(b, true);
    }

    public static boolean isShopSign(Block b, boolean checkmetadata) {
        if (b != null && b.getState() instanceof Sign) {
            Sign sign = (Sign)b.getState();
            if (checkmetadata && !sign.hasMetadata("shopid")) return false;
            return sign.getLine(0).length() > 0 && (int)sign.getLine(0).charAt(0) == 167 && (int)sign.getLine(0).charAt(2) == 167;
        }
        return false;
    }

    public static int getShopId(Sign s) {
        for (MetadataValue value : s.getMetadata("shopid")) {
            if (value.getOwningPlugin().getName().equals("EverShop")) {
                return value.asInt();
            }
        }
        return 0;
    }
}