package com.evermc.evershop.util;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

import com.evermc.evershop.logic.DataLogic;
import static com.evermc.evershop.util.LogUtil.severe;

import org.bukkit.Location;

/**
 * The same to the ConfigurationSerializable version of Location
 * This is created to avoid using Bukkit API in async operations.
 * 
 * Just used to save chest/redstone places -> use int, no yaw and pitch
 */
public class SerializableLocation implements Serializable{

    static final long serialVersionUID = 1L;
    public int x;
    public int y;
    public int z;
    public int world;
    public SerializableLocation(Location loc){
        this.x = loc.getBlockX();
        this.y = loc.getBlockY();
        this.z = loc.getBlockZ();
        this.world = DataLogic.getWorldId(loc.getWorld());
    }

    public Map<String, Object> serialize() {
        Map<String, Object> data = new HashMap<String, Object>();
        data.put("world", this.world);
        data.put("x", this.x);
        data.put("y", this.y);
        data.put("z", this.z);
        return data;
    }

    public Location toLocation(){
        return new Location(DataLogic.getWorld(this.world), this.x, this.y, this.z);
    }

    public static Location toLocation(int world, int x, int y, int z){
        return new Location(DataLogic.getWorld(world), x, y, z);
    }

    public String toString(){
        return "SeriLoc{world=" + this.world + ", x=" + this.x + ", y=" + this.y + ", z=" + this.z;
    }

    
    public static byte[] serialize(Collection<SerializableLocation> targetOut, Collection<SerializableLocation> targetIn){
        SerializableLocation[][] result = new SerializableLocation[2][];
        int top = 0;
        result[0] = new SerializableLocation[targetOut.size()];
        for (SerializableLocation loc: targetOut){
            result[0][top++] = loc;
        }
        top = 0;
        result[1] = new SerializableLocation[targetIn.size()];
        for (SerializableLocation loc: targetIn){
            result[1][top++] = loc;
        }
        try{
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            ObjectOutputStream outputStream = new ObjectOutputStream(out);
            outputStream.writeObject(result);
            byte [] bytes = out.toByteArray();
            outputStream.close();
            return bytes;
        } catch (Exception e) {
            e.printStackTrace();
            severe("SeriLoc: Failed to serialize.");
            return null ;
        }
    }

    @SuppressWarnings("unchecked")
    public static HashSet<SerializableLocation>[] deserialize(byte[] data){
        HashSet<?>[] result = new HashSet<?>[2];
        HashSet<SerializableLocation> targetOut = new HashSet<SerializableLocation>();
        HashSet<SerializableLocation> targetIn = new HashSet<SerializableLocation>();
        try{
            ObjectInputStream in  = new ObjectInputStream(new ByteArrayInputStream(data));
            Object obj = in.readObject();
            if (!(obj instanceof SerializableLocation[][])){
                throw new Exception("Not a SeriLoc array");
            }
            SerializableLocation[] targetOutArray = ((SerializableLocation[][])obj)[0];
            SerializableLocation[] targetInArray = ((SerializableLocation[][])obj)[1];
            for (SerializableLocation target: targetOutArray){
                targetOut.add(target);
            }
            for (SerializableLocation target: targetInArray){
                targetIn.add(target);
            }
            result[0] = targetOut;
            result[1] = targetIn;
            return (HashSet<SerializableLocation>[])result;
        } catch (Exception e) {
            e.printStackTrace();
            severe("SeriLoc: Failed to deserialize.");
            return null ;
        }
    }

}