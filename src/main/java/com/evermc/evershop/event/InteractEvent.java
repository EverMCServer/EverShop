package com.evermc.evershop.event;

import java.util.logging.Level;

import com.evermc.evershop.EverShop;
import com.evermc.evershop.logic.DataLogic;
import com.evermc.evershop.logic.PlayerLogic;
import com.evermc.evershop.logic.ShopLogic;
import com.evermc.evershop.util.LogUtil;

import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.block.Container;
import org.bukkit.block.Sign;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.Event.Result;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.inventory.DoubleChestInventory;

import static com.evermc.evershop.util.TranslationUtil.send;

public class InteractEvent implements Listener{
    
    private EverShop plugin;

    public InteractEvent(EverShop plugin){
        this.plugin = plugin;
    }
    
    @EventHandler (priority = EventPriority.NORMAL)
    public void on(PlayerInteractEvent event){
        if (event.useInteractedBlock() != Result.ALLOW){
            // replacement of event.isCancelled()
            return;
        }
        if (event.getAction() != Action.LEFT_CLICK_BLOCK && event.getAction() != Action.RIGHT_CLICK_BLOCK){
            return;
        }
        if (event.getMaterial() == ShopLogic.getDestroyMaterial()){
            return;
        }
        if (event.getPlayer().isSneaking()){
            return;
        }
        Block clicked = event.getClickedBlock();
        if (clicked == null){
            return;
        }
        if (clicked.getState() instanceof Sign){
            Sign sign = (Sign) clicked.getState();
            if (sign.getLine(0).length() > 0 && (int)sign.getLine(0).charAt(0) == 167){
                ShopLogic.accessShop(event.getPlayer(), clicked.getLocation(), event.getAction());
                // if clicked on formatted signs, no need to register, so return
                return;
            }
        }
        if (event.getMaterial() == ShopLogic.getLinkMaterial()){
            boolean ret = ShopLogic.registerBlock(event.getPlayer(), clicked, event.getAction());
            event.setCancelled(ret);
        } 
    }

    @EventHandler (priority = EventPriority.NORMAL)
    public void on(BlockBreakEvent event){
        
        if (event.isCancelled()){
            return;
        }
        Block b = event.getBlock();
        BlockState bs = b.getState();
        if ((bs instanceof Sign || ShopLogic.isLinkableBlock(b.getType())) && event.getPlayer().getInventory().getItemInMainHand().getType() == ShopLogic.getLinkMaterial()){
            // player is creating shop, cancel
            event.setCancelled(true);
            return;
        }
        if (bs instanceof Sign && ((Sign)event.getBlock().getState()).getLine(0).length() > 0 && (int)((Sign)event.getBlock().getState()).getLine(0).charAt(0) == 167){
            // try break an active shop, check it
            event.setCancelled(true);
            if (event.getPlayer().getGameMode() == GameMode.SURVIVAL || event.getPlayer().getInventory().getItemInMainHand().getType() == ShopLogic.getDestroyMaterial())
                ShopLogic.tryBreakShop(event.getBlock().getLocation(), event.getPlayer());
            return;
        }
        Location[] signs = ShopLogic.getAttachedSign(b);
        Location[] lblocks = ShopLogic.getAttachedBlock(b);
        if (signs == null && lblocks == null && !ShopLogic.isLinkableBlock(b.getType())){
            // no sign attached, and not a linkable block, break directly
            return;
        }
        // has sign attached or is a linkable block, check it
        event.setCancelled(true);
        ShopLogic.tryBreakBlock(event.getBlock().getLocation(), event.getPlayer(), signs, lblocks);
    }

    @EventHandler (priority = EventPriority.NORMAL)
    public void on(BlockPlaceEvent event){
        if (event.isCancelled()){
            return;
        }
        // if placed sign, delete record in this location in db
        if (event.getBlockPlaced().getState() instanceof Sign){
            DataLogic.removeShop(event.getBlockPlaced().getLocation());
        }
        if (event.getBlockPlaced().getType() == Material.CHEST || event.getBlockPlaced().getType() == Material.TRAPPED_CHEST){
            final Location loc = event.getBlockPlaced().getLocation();
            final Player p = event.getPlayer();
            Bukkit.getScheduler().runTaskLater(plugin, () -> {
                BlockState bs = loc.getBlock().getState();
                if (bs != null && bs instanceof Container && ((Container)bs).getInventory() instanceof DoubleChestInventory){
                    DoubleChestInventory dci = (DoubleChestInventory)((Container)bs).getInventory();
                    final Location right = dci.getRightSide().getLocation();
                    if (!right.equals(loc)){
                        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
                            int count = DataLogic.getBlockLinkedCount(right);
                            if (count > 0){
                                Bukkit.getScheduler().runTask(plugin, () -> {
                                    send("You cant place this", p);
                                    if (loc.getBlock().getState() == null || !(loc.getBlock().getState() instanceof Container)){
                                        LogUtil.log(Level.SEVERE, "DoubleChest detect: " + loc + " should be a chest, but actually " + 
                                            loc.getBlock().getType() + ", database lag? Player name=" + p.getName() + ", uuid=" + p.getUniqueId());
                                    } else {
                                        loc.getBlock().breakNaturally();
                                    }
                                });
                            }
                        });
                    }
                }
            }, 1);
        }
    }

    @EventHandler (priority = EventPriority.NORMAL)
    public void on(PlayerJoinEvent event){
        // Saved player info will be cached at server start.
        // This is used to update playerinfo or add new player info.
        PlayerLogic.updatePlayerInfo(event.getPlayer());
    }
}