package com.evermc.evershop.event;

import java.util.logging.Level;

import com.evermc.evershop.EverShop;
import com.evermc.evershop.util.LogUtil;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.block.Block;
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
    private Material linkMaterial;
    private Material destroyMaterial;

    public InteractEvent(EverShop plugin){
        this.plugin = plugin;
        linkMaterial = Material.matchMaterial(plugin.getConfig().getString("evershop.linkMaterial"));
        destroyMaterial = Material.matchMaterial(plugin.getConfig().getString("evershop.destroyMaterial"));
    }
    
    @EventHandler (priority = EventPriority.NORMAL)
    public void on(PlayerInteractEvent event){
        if (event.useInteractedBlock() != Result.DENY){
            return;
        }
        if (event.getAction() != Action.LEFT_CLICK_BLOCK && event.getAction() != Action.RIGHT_CLICK_BLOCK){
            return;
        }
         
        int player_id = plugin.getPlayerLogic().getPlayer(event.getPlayer());
        int world_id = plugin.getDataLogic().getWorldId(event.getPlayer().getWorld());
        //getclickedblockinfo
        if (event.getMaterial() == linkMaterial){
            // if event.getaction right && not advanced return
            // if clickedblock is activated shop return
            Block clicked = event.getClickedBlock();
            if (clicked == null){
                return;
            }
        }
    }

    @EventHandler (priority = EventPriority.NORMAL)
    public void on(BlockBreakEvent event){
        System.out.println("break@");
        // delete record in db
    }

    @EventHandler (priority = EventPriority.NORMAL)
    public void on(BlockPlaceEvent event){
        System.out.println("place@");
        // if have record, delete record in db
    }

    @EventHandler (priority = EventPriority.NORMAL)
    public void on(PlayerJoinEvent event){
        Bukkit.getScheduler().runTaskAsynchronously(plugin, ()->{
            plugin.getPlayerLogic().getPlayer(event.getPlayer());
        });
    }
}