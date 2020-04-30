package com.evermc.evershop.util;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.lang.reflect.Type;
import java.net.URL;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.evermc.evershop.EverShop;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import org.bukkit.Color;
import org.bukkit.FireworkEffect;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BookMeta;
import org.bukkit.inventory.meta.Damageable;
import org.bukkit.inventory.meta.EnchantmentStorageMeta;
import org.bukkit.inventory.meta.FireworkEffectMeta;
import org.bukkit.inventory.meta.FireworkMeta;
import org.bukkit.inventory.meta.PotionMeta;
import org.bukkit.inventory.meta.Repairable;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.potion.PotionData;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionType;

import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.chat.TranslatableComponent;
import net.md_5.bungee.api.chat.HoverEvent.Action;

import static com.evermc.evershop.util.LogUtil.severe;
import static com.evermc.evershop.util.LogUtil.info;
import static com.evermc.evershop.util.LogUtil.warn;

public class TranslationUtil {
    
    private static HashMap<String, HashMap<String, String>> tr_dicts = null;
    private static Set<String> item_dicts = null;
    private static String default_translation = null;

    public static void init(EverShop plugin){
        default_translation = plugin.getConfig().getString("evershop.default_translation");
        if (default_translation == null) {
            severe("TranslationLogic: No default translation set. Please check your configuration.");
            plugin.getServer().getPluginManager().disablePlugin(plugin);
            return;
        }
        if (!loadMsgsLang(plugin) || !loadItemLang(plugin)) {
            plugin.getServer().getPluginManager().disablePlugin(plugin);
            return;
        }
    }

    public static void reload(EverShop plugin){
        String default_translation_bak = default_translation;

        default_translation = plugin.getConfig().getString("evershop.default_translation");
        if (default_translation == null) {
            severe("TranslationLogic: No default translation set. Please check your configuration.");
            severe("TranslationLogic: Plugin reload failed.");
            default_translation = default_translation_bak;
            return;
        }
        if (!loadMsgsLang(plugin)) {
            severe("TranslationLogic: Plugin reload failed.");
            default_translation = default_translation_bak;
            return;
        }
    }

    public static void send(BaseComponent msg, CommandSender sender){
        BaseComponent tc = new TextComponent("");
        tc.addExtra(title());
        tc.addExtra(msg);
        sender.spigot().sendMessage(tc);
    }

    public static BaseComponent title(){
        TextComponent tc = new TextComponent("[EverShop] ");
        tc.setColor(ChatColor.DARK_AQUA);
        return tc;
    }

    public static void send(String msg, CommandSender sender){
        send(tr(msg, sender), sender);
    }

    public static void send(String msg, CommandSender sender, Object...obj){
        send(tr(msg, sender, obj), sender);
    }

    enum ColorTr{
        black       (0x1E1B1B),
        red         (0xB3312C),
        green       (0x3B511A),
        purple      (0x7B2FBE),
        cyan        (0x287697),
        light_gray  (0xABABAB),
        gray        (0x434343),
        pink        (0xD88198),
        lime        (0x41CD34),
        yellow      (0xDECF2A),
        light_blue  (0x6689D3),
        magenta     (0xC354CD),
        orange      (0xEB8844),
        brown       (0x51301A),
        blue        (0x253192),
        white       (0xF0F0F0);
        private int value;
        ColorTr(int value){
            this.value = value;
        }
        public static BaseComponent tr(Color c){
            int ic = c.asRGB();
            for (ColorTr t:ColorTr.values()){
                if (ic == t.value){
                    return new TranslatableComponent("item.minecraft.firework_star." + t.name());
                }
            }
            return new TextComponent(c.toString());
        }
    }
    
    enum PotionDuration {
        UNCRAFTABLE     (0,0,0),
        WATER           (0,0,0),
        MUNDANE         (0,0,0),
        THICK           (0,0,0),
        AWKWARD         (0,0,0),
        NIGHT_VISION    (3600,9600,0),
        INVISIBILITY    (3600,9600,0),
        JUMP            (3600,9600,1800),
        FIRE_RESISTANCE (3600,9600,0),
        SPEED           (3600,9600,1800),
        SLOWNESS        (1800,4800,400),
        WATER_BREATHING (3600,9600,0),
        INSTANT_HEAL    (0,0,0),
        INSTANT_DAMAGE  (0,0,0),
        POISON          (900,1800,432),
        REGEN           (900,1800,432),
        STRENGTH        (3600,9600,1800),
        WEAKNESS        (1800,4800,0),
        LUCK            (6000,0,0),
        TURTLE_MASTER   (400,800,400),
        SLOW_FALLING    (1800,4800,0)
        ;
        private int[] dur = new int[3];
        PotionDuration(int d1, int d2, int d3){
            dur[0] = d1;
            dur[1] = d2;
            dur[2] = d3;
        }
        public static int[] get(PotionType pt){
            return PotionDuration.valueOf(pt.name()).dur;
        }
    }

    enum PotionTypeEnum{
        AWKWARD("awkward"),
        FIRE_RESISTANCE("fire_resistance"),
        INSTANT_DAMAGE("harming"),
        INSTANT_HEAL("healing"),
        STRENGTH("strength"),
        INVISIBILITY("invisibility"),
        JUMP("leaping"),
        LUCK("luck"),
        MUNDANE("mundane"),
        NIGHT_VISION("night_vision"),
        POISON("poison"),
        REGEN("regeneration"),
        SLOWNESS("slowness"),
        SLOW_FALLING("slow_falling"),
        SPEED("swiftness"),
        THICK("thick"),
        TURTLE_MASTER("turtle_master"),
        UNCRAFTABLE("empty"),
        WATER("water"),
        WATER_BREATHING("water_breathing"),
        WEAKNESS("weakness");
    
        private String hash;
        PotionTypeEnum(String hash){
            this.hash = hash;
        }
        public static String getHash(PotionType pt){
            return PotionTypeEnum.valueOf(pt.name()).hash;
        }
    }

    public static String getLocale(Player p, boolean isMsg){
        String lang = p.getLocale();
        if (isMsg){
            return tr_dicts.containsKey(lang)?lang:default_translation;
        } else {
            return lang;
        }
    }

    public static BaseComponent tr(String str, Player p, Object...args){
        return tr(str, getLocale(p, true), args);
    }

    public static BaseComponent tr(String str){
        return tr(str, "en_us");
    }

    public static BaseComponent tr(String str, CommandSender p, Object...args){
        if (p instanceof Player){
            return tr(str, (Player)p, args);
        } else {
            return tr(str, "en_us", args);
        }
    }

    public static BaseComponent tr(String str, String lang, Object...args){
        String s;
        s = tr_dicts.get(lang).get(str);
        if (s != null)
            return new TranslatableComponent(s, args);
        else 
            return new TranslatableComponent(str, args);
    }
    
    public static BaseComponent tr(ItemStack is){
        
        TextComponent message = new TextComponent();

        message.setColor(ChatColor.YELLOW);
        message.setText(is.getAmount() + " ");
        message.addExtra(_tr(is));
        
        if (is.hasItemMeta() && is.getItemMeta().hasDisplayName()){
            TextComponent itemName = new TextComponent("\"" + is.getItemMeta().getDisplayName() + "\"");
            itemName.setItalic(true);
            itemName.setColor(ChatColor.AQUA);
            message.addExtra(itemName);
        }
        
        if (is.hasItemMeta() && is.getItemMeta().isUnbreakable()){
            TextComponent unbreakable = new TextComponent("[");
            unbreakable.setItalic(false);
            unbreakable.setColor(ChatColor.GREEN);
            unbreakable.addExtra(new TranslatableComponent("item.unbreakable"));
            unbreakable.addExtra("]");
            message.addExtra(unbreakable);
        }

        if (is.hasItemMeta() && is.getItemMeta() instanceof Damageable && ((Damageable)is.getItemMeta()).hasDamage()){
            int maxdur = is.getType().getMaxDurability();
            int curdur = maxdur - ((Damageable)is.getItemMeta()).getDamage();
            TextComponent damage = new TextComponent("[");
            damage.setItalic(false);
            damage.setColor(ChatColor.GREEN);
            damage.addExtra(new TranslatableComponent("item.durability", curdur, maxdur));
            damage.addExtra("]");
            message.addExtra(damage);
        }

        if (is.getEnchantments().size() > 0){
            if (is.hasItemMeta() && is.getItemMeta() instanceof Repairable){
                message.addExtra(_tr((Repairable)is.getItemMeta()));
            }
            message.addExtra(_tr(is.getEnchantments()));
        }

        if (is.hasItemMeta() && is.getItemMeta() instanceof EnchantmentStorageMeta) {
            if (is.hasItemMeta() && is.getItemMeta() instanceof Repairable) {
                message.addExtra(_tr((Repairable)is.getItemMeta()));
            }
            if (((EnchantmentStorageMeta)is.getItemMeta()).hasStoredEnchants()) {
                message.addExtra(_tr(((EnchantmentStorageMeta)is.getItemMeta()).getStoredEnchants()));
            }
        }

        if (is.getType().isRecord()) {
            TextComponent recordName = new TextComponent("\"" + is.getType().toString().substring(11) + "\"");
            recordName.setItalic(true);
            recordName.setColor(ChatColor.AQUA);
            message.addExtra(recordName);
        }

        if (is.hasItemMeta() && is.getItemMeta() instanceof BookMeta){
            BookMeta bm = (BookMeta)is.getItemMeta();
            if (bm.getTitle() != null) {
                TextComponent recordName = new TextComponent("\"" + bm.getTitle() + "\"");
                recordName.setItalic(true);
                recordName.setColor(ChatColor.AQUA);
                message.addExtra(recordName);
            }
            if (bm.getAuthor() != null) {
                message.addExtra(" by " + bm.getAuthor());
            }
            message.setHoverEvent(new HoverEvent(Action.SHOW_TEXT, new ComponentBuilder(bm.getPage(1)).create()));
        }

        if (is.hasItemMeta() && is.getItemMeta() instanceof FireworkMeta){
            FireworkMeta fwm = (FireworkMeta) is.getItemMeta();
            TextComponent firework = new TextComponent("(");
            firework.setColor(ChatColor.AQUA);
            firework.addExtra(new TranslatableComponent("item.minecraft.firework_rocket.flight"));
            firework.addExtra("" + fwm.getPower());
            if(fwm.hasEffects()) {
                firework.addExtra(", ");
                for(FireworkEffect effect : fwm.getEffects()) {
                    firework.addExtra(_tr(effect));
                }
            }
            firework.addExtra(")");
            message.addExtra(firework);
        }

        if (is.hasItemMeta() && is.getItemMeta() instanceof FireworkEffectMeta){
            FireworkEffectMeta fwm = (FireworkEffectMeta) is.getItemMeta();
            if(fwm.getEffect() != null) {
                TextComponent firework = new TextComponent("(");
                firework.setColor(ChatColor.AQUA);
                firework.addExtra(_tr(fwm.getEffect()));
                firework.addExtra(")");
                message.addExtra(firework);
            }
        }
        
        if (is.hasItemMeta() && is.getItemMeta() instanceof PotionMeta){
            PotionData pd = ((PotionMeta)is.getItemMeta()).getBasePotionData();
            int dur;
            if(pd.isUpgraded()){
                message.addExtra("II ");
                dur = PotionDuration.get(pd.getType())[2];
            }
            else if(pd.isExtended()){
                dur = PotionDuration.get(pd.getType())[1];
            }else{
                dur = PotionDuration.get(pd.getType())[0];
            }
            if (is.getType() == Material.LINGERING_POTION){
                dur /= 4;
            }
            if (is.getType() == Material.TIPPED_ARROW){
                dur /= 8;
            }
            if (dur > 0) {
                message.addExtra(String.format("(%02d:%02d)", dur/1200, dur/20%60));
            }
            TextComponent extra = new TextComponent();
            for(PotionEffect pe : ((PotionMeta)is.getItemMeta()).getCustomEffects()){
                extra.addExtra(" (");
                extra.addExtra(new TranslatableComponent("effect.minecraft." + pe.getType().getName().toLowerCase()));
                extra.addExtra(" " + binaryToRoman(pe.getAmplifier() + 1) + 
                                String.format(" %02d:%02d", pe.getDuration()/1200, pe.getDuration()/20%60) + ")");
            }
            message.addExtra(extra);
        }

        if (!is.hasItemMeta() || !(is.getItemMeta() instanceof BookMeta)){
            String nbt = NBTUtil.toNBTString(is);
            message.setHoverEvent(new HoverEvent(Action.SHOW_ITEM, new ComponentBuilder(nbt).create()));
        }
        return message;
    }

    public static BaseComponent _tr(Map<Enchantment, Integer> enchant){
        TextComponent result = new TextComponent("(");
        result.setColor(ChatColor.LIGHT_PURPLE);
        Iterator<Enchantment> it = enchant.keySet().iterator();
        while(it.hasNext()) {
            Enchantment e = it.next();
            result.addExtra(new TranslatableComponent("enchantment.minecraft." + e.getKey().getKey()));
            result.addExtra(" " + binaryToRoman(enchant.get(e)));
            if (it.hasNext()){
                result.addExtra(", ");
            }
        }
        result.addExtra(")");
        return result;
    }

    public static String tr(Location loc){
        return "(" + loc.getBlockX() + "," + loc.getBlockY() + "," + loc.getBlockZ() + ")";
    }

    public static String binaryToRoman(int binary) {
        final String[] RCODE = {"X", "IX", "V", "IV", "I"};
        final int[]    BVAL  = {10,   9,   5,   4,    1};
        if (binary <= 0) {
            return "";
        }
        if (binary >= 11) {
            return Integer.toString(binary);
        }
        String roman = "";
        for (int i = 0; i < RCODE.length; i++) {
            while (binary >= BVAL[i]) {
                binary -= BVAL[i];
                roman  += RCODE[i];
            }
        }
        return roman;
    }

    public static BaseComponent _tr(Repairable rp){
        TextComponent result = new TextComponent("[");
        if (rp.getRepairCost() > 34) {
            result.setColor(ChatColor.DARK_RED);
        } else {
            result.setColor(ChatColor.GREEN);
        }
        if (rp.getRepairCost() > 38) {
            result.addExtra(new TranslatableComponent("container.repair.cost"));
            result.addExtra(new TranslatableComponent("container.repair.expensive"));
        } else {
            result.addExtra(new TranslatableComponent("container.repair.cost", rp.getRepairCost()));
        }
        result.addExtra("]");
        return result;
    }

    private static BaseComponent _tr(FireworkEffect effect){
        TextComponent result = new TextComponent();
        result.addExtra(_tr(effect.getType()));
        result.addExtra(", ");

        if (effect.getColors().size() > 0){
            for (Color c:effect.getColors()){
                result.addExtra(ColorTr.tr(c));
                result.addExtra(", ");
            }
        }
        if (effect.getFadeColors().size() > 0){
            result.addExtra(new TranslatableComponent("item.minecraft.firework_star.fade_to"));
            result.addExtra(" ");
            for (Color c:effect.getFadeColors()){
                result.addExtra(ColorTr.tr(c));
                result.addExtra(", ");
            }
        }
        if (effect.hasFlicker()){
            result.addExtra(new TranslatableComponent("item.minecraft.firework_star.flicker"));
            result.addExtra(", ");
        }
        if (effect.hasTrail()){
            result.addExtra(new TranslatableComponent("item.minecraft.firework_star.trail"));
            result.addExtra(", ");
        }
        List<BaseComponent> extras = result.getExtra();
        extras.remove(extras.size() - 1);
        result.setExtra(extras);
        return result;
    }

    private static BaseComponent _tr(FireworkEffect.Type type) {
        switch(type){
            case STAR: return new TranslatableComponent("item.minecraft.firework_star.shape.star");
            case BALL: return new TranslatableComponent("item.minecraft.firework_star.shape.small_ball");
            case BALL_LARGE: return new TranslatableComponent("item.minecraft.firework_star.shape.large_ball");
            case BURST: return new TranslatableComponent("item.minecraft.firework_star.shape.burst");
            case CREEPER: return new TranslatableComponent("item.minecraft.firework_star.shape.creeper");
            default: return new TranslatableComponent("item.minecraft.firework_star.shape");
        }
    }

    private static BaseComponent _tr(ItemStack is){
        if (is.hasItemMeta() && is.getItemMeta() instanceof SkullMeta){
            if (((SkullMeta)is.getItemMeta()).hasOwner()) {
                return new TranslatableComponent("block.minecraft.player_head.named", ((SkullMeta)is.getItemMeta()).getOwningPlayer().getName());
            }
        }
        if (is.hasItemMeta() && is.getItemMeta() instanceof PotionMeta){
            PotionType pt = ((PotionMeta)is.getItemMeta()).getBasePotionData().getType();
            return new TranslatableComponent("item.minecraft." + is.getType().name().toLowerCase() + ".effect." + PotionTypeEnum.getHash(pt));
        }
        String name = is.getType().name().toLowerCase();
        if (name.endsWith("_wall_banner")){
            name.replace("_wall_banner", "_banner");
        }
        if (item_dicts.contains("item.minecraft." + name)){
            return new TranslatableComponent("item.minecraft." + name);
        }
        if (item_dicts.contains("block.minecraft." + name)){
            return new TranslatableComponent("block.minecraft." + name);
        }
        warn("TranslationUtil: Unsupported material: " + is.getType());
        return new TextComponent(name);
    }

    public static BaseComponent tr(Material t){
        String name = t.name().toLowerCase();
        if (name.endsWith("_wall_banner")){
            name.replace("_wall_banner", "_banner");
        }
        if (item_dicts.contains("item.minecraft." + name)){
            return new TranslatableComponent("item.minecraft." + name);
        }
        if (item_dicts.contains("block.minecraft." + name)){
            return new TranslatableComponent("block.minecraft." + name);
        }
        warn("TranslationUtil: Unsupported material: " + t);
        return new TextComponent(name);
    }
    private static boolean loadItemLang(EverShop plugin){
        BufferedReader reader = null;
        URL json = null;
        try{
            Enumeration<URL> e = ClassLoader.getSystemResources("assets/minecraft/lang/en_us.json");
            if (e.hasMoreElements()) json = e.nextElement();
            else throw new Exception();
            reader = new BufferedReader(new InputStreamReader(json.openStream()));
        } catch (Exception e){
            severe("Cannot load en_us.json from server jar, unsupported server?");
            return false;
        }

        Type type = new TypeToken<Map<String, String>>(){}.getType();
        Gson gson = new Gson();
        Map<String, String> map = gson.fromJson(reader, type);
        try{
            reader.close();
        } catch (Exception e){
            e.printStackTrace();
            warn("TranslationLogic: failed to close item list file");
        }
        item_dicts = map.keySet();
        info("TranslationLogic: Item list loaded");
        
        if (item_dicts == null || item_dicts.size() == 0) {
            severe("TranslationLogic: unable to load item list, disable the plugin.");
            return false;
        }
        return true;
    }
    
    private static boolean loadMsgsLang(EverShop plugin){
        HashMap<String, HashMap<String, String>> tr_dicts_bak = tr_dicts;
        tr_dicts = new HashMap<String, HashMap<String, String>>();
        File folder = new File(plugin.getDataFolder(), "i18n");
        if (!folder.exists()){
            folder.mkdirs();
        }
        if (!folder.exists()){
            severe("TranslationLogic: failed to create message translation folder.");
            tr_dicts = tr_dicts_bak;
            return false;
        }
        String[] i18n_files = {"zh_cn.yml", "en_us.yml"};
        for (String n:i18n_files){
            if (!new File(plugin.getDataFolder(), "i18n/" + n).exists())
                plugin.saveResource("i18n/" + n, false);
        }
        for (File f : folder.listFiles()){
            HashMap<String, String> dict = new HashMap<String, String>();
            FileConfiguration conf = YamlConfiguration.loadConfiguration(f);
            for (String key: conf.getKeys(false)){
                dict.put(key, conf.getString(key));
            }
            tr_dicts.put(f.getName().split("\\.")[0] , dict);
        }
        info("TranslationLogic: loaded " + tr_dicts.size() + " message translations.");
        if (tr_dicts.size() == 0)  {
            tr_dicts = tr_dicts_bak;
            return false;
        }
        if (!tr_dicts.containsKey(default_translation)) {
            severe("TranslationLogic: Messages of default language " + default_translation + " is not loaded.");
            tr_dicts = tr_dicts_bak;
            return false;
        }
        return true;
    }
}  
