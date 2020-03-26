package com.evermc.evershop.event;

import com.evermc.evershop.EverShop;
import com.evermc.evershop.logic.PlayerLogic;
import com.evermc.evershop.logic.ShopLogic;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.Sign;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.Event.Result;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;

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
        Block clicked = event.getClickedBlock();
        if (clicked == null){
            return;
        }
        if (clicked.getState() instanceof Sign){
            Sign sign = (Sign) clicked.getState();
            if ((int)sign.getLine(0).charAt(0) == 167){
                plugin.getShopLogic().accessShop(event.getPlayer(), clicked.getLocation(), event.getAction());
                // if clicked on formatted signs, no need to register, so return
                return;
            }
        }
        if (event.getMaterial() == ShopLogic.getLinkMaterial()){
            plugin.getShopLogic().registerBlock(event.getPlayer(), clicked, event.getAction());
        } 
    }

    @EventHandler (priority = EventPriority.NORMAL)
    public void on(BlockBreakEvent event){
        
        if (event.isCancelled()){
            return;
        }
        // TODO - op break
        if (event.getBlock().getState() instanceof Sign && (int)((Sign)event.getBlock().getState()).getLine(0).charAt(0) == 167){
            event.setCancelled(true);
            plugin.getShopLogic().tryBreakShop(event.getBlock().getLocation(), event.getPlayer());
        }
        if (!plugin.getShopLogic().isLinkableBlock(event.getBlock().getType())){
            return;
        }
        event.setCancelled(true);
        plugin.getShopLogic().tryBreakBlock(event.getBlock().getLocation(), event.getPlayer());
    }

    @EventHandler (priority = EventPriority.NORMAL)
    public void on(BlockPlaceEvent event){
        if (event.isCancelled()){
            return;
        }
        // if placed sign, delete record in this location in db
        if (event.getBlockPlaced().getState() instanceof Sign){
            plugin.getDataLogic().removeShop(event.getBlockPlaced().getLocation());
        }
        if (event.getBlockPlaced().getType() == Material.CHEST || event.getBlockPlaced().getType() == Material.TRAPPED_CHEST){
            // TODO - check if generates a double chest. -> cancel the event
        }
    }

    @EventHandler (priority = EventPriority.NORMAL)
    public void on(PlayerJoinEvent event){
        // Saved player info will be cached at server start.
        // This is used to update playerinfo or add new player info.
        Bukkit.getScheduler().runTaskAsynchronously(plugin, ()->{
            PlayerLogic.getPlayer(event.getPlayer());
        });
    }
}