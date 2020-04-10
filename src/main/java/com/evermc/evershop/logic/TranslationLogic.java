package com.evermc.evershop.logic;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.lang.reflect.Type;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.evermc.evershop.EverShop;
import com.google.gson.Gson;
import com.google.gson.JsonParser;
import com.google.gson.JsonObject;
import com.google.gson.reflect.TypeToken;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Color;
import org.bukkit.FireworkEffect;
import org.bukkit.Location;
import org.bukkit.Material;
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

import static com.evermc.evershop.util.LogUtil.severe;
import static com.evermc.evershop.util.LogUtil.info;
import static com.evermc.evershop.util.LogUtil.warn;

public class TranslationLogic {

    private static HashMap<String, HashMap<String, String>> tr_dicts = new HashMap<String, HashMap<String, String>>();
    private static HashMap<String, Map<String, String>> item_dicts = new HashMap<String, Map<String, String>>();
    private static String default_translation = null;

    public static void init(EverShop plugin){

        List<String> item_translation = plugin.getConfig().getStringList("evershop.item_translation");
        if (item_translation.size() == 0) {
            severe("TranslationLogic: No item translation set. Please check your configuration.");
            plugin.getServer().getPluginManager().disablePlugin(plugin);
            return;
        }
        default_translation = plugin.getConfig().getString("evershop.default_translation");
        if (default_translation == null) {
            severe("TranslationLogic: No default translation set. Please check your configuration.");
            plugin.getServer().getPluginManager().disablePlugin(plugin);
            return;
        }
        checkMsgsLang(plugin);
        checkItemLang(plugin, item_translation);
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
        public static String tr(Color c, String lang){
            int ic = c.asRGB();
            for (ColorTr t:ColorTr.values()){
                if (ic == t.value){
                    return _tr("item.minecraft.firework_star." + t.name(), lang);
                }
            }
            return c.toString();
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
            return item_dicts.containsKey(lang)?lang:default_translation;
        }
    }

    public static String tr(String str, Player p, Object...args){
        return tr(str, getLocale(p, true), args);
    }

    public static String tr(String str, String lang, Object...args){
        String s;
        s = tr_dicts.get(lang).get(str);
        if (s != null)
            return String.format(s, args);
        else 
            return String.format(str, args);
    }

    public static String tr(ItemStack is, Player p){
        return tr(is, getLocale(p, false));
    }

    public static String tr(Material is, Player p){
        return tr(is, getLocale(p, false));
    }

    public static String tr(Location loc, Player p){
        return tr(loc, getLocale(p, true));
    }

    public static String tr(ItemStack is, String lang){
        String txtcolor = ChatColor.YELLOW.toString();
        String enccolor = ChatColor.LIGHT_PURPLE.toString();
        String durcolor = ChatColor.GREEN.toString();
        String attrcolor = ChatColor.AQUA.toString();
        String namecolor = attrcolor + ChatColor.ITALIC.toString();

        String ret = txtcolor + is.getAmount() + " " + _tr(is, lang);

        if (is.hasItemMeta() && is.getItemMeta().hasDisplayName()){
            ret += namecolor + "\"" + is.getItemMeta().getDisplayName() + "\"";
        }
        
        if (is.hasItemMeta() && is.getItemMeta().isUnbreakable()){
            ret += durcolor + "[" + _tr("item.unbreakable", lang) + "]";
        }

        if (is.hasItemMeta() && is.getItemMeta() instanceof Damageable && ((Damageable)is.getItemMeta()).hasDamage()){
            int maxdur = is.getType().getMaxDurability();
            int curdur = maxdur - ((Damageable)is.getItemMeta()).getDamage();
            ret += durcolor + "[" + _tr("item.durability", lang, curdur, maxdur) + "]";
        }

        if (is.getEnchantments().size() > 0){
            if (is.hasItemMeta() && is.getItemMeta() instanceof Repairable){
                ret += durcolor + "[" + _tr("container.repair.cost", lang, ((Repairable)is.getItemMeta()).getRepairCost()) + "]";
            }
            ret += enccolor + tr(is.getEnchantments(), lang);
        }

        if (is.hasItemMeta() && is.getItemMeta() instanceof EnchantmentStorageMeta){
            if (is.hasItemMeta() && is.getItemMeta() instanceof Repairable){
                ret += durcolor + "[" + _tr("container.repair.cost", lang, ((Repairable)is.getItemMeta()).getRepairCost()) + "]";
            }
            if (((EnchantmentStorageMeta)is.getItemMeta()).hasStoredEnchants()){
                ret += enccolor + tr(((EnchantmentStorageMeta)is.getItemMeta()).getStoredEnchants(), lang);
            }
        }

        if (is.getType().isRecord()){
            ret += namecolor + "\"" + is.getType().toString().substring(11) + "\"";
        }

        if (is.hasItemMeta() && is.getItemMeta() instanceof BookMeta){
            BookMeta bm = (BookMeta)is.getItemMeta();
            if (bm.getTitle() != null) ret += namecolor + "\"" + bm.getTitle() + "\"";
            if (bm.getAuthor() != null) ret += durcolor + " by " + bm.getAuthor();
            if (bm.getPage(1).length() >= 10)
            ret += enccolor + "[" + bm.getPage(1).substring(0, 10) + "...]";
            else if (bm.getPage(1).length() > 0)
            ret += enccolor + "[" + bm.getPage(1) + "]";
            else
            ret += enccolor + "[empty]";
        }

        if (is.hasItemMeta() && is.getItemMeta() instanceof FireworkMeta){
            FireworkMeta fwm = (FireworkMeta) is.getItemMeta();
            ret += attrcolor + " (" + _tr("item.minecraft.firework_rocket.flight", lang) + fwm.getPower();
            if(fwm.hasEffects()) {
                ret += ", ";
                for(FireworkEffect effect : fwm.getEffects()) {
                    ret += _tr(effect, lang);
                }
            }
            ret += ")";
        }

        if (is.hasItemMeta() && is.getItemMeta() instanceof FireworkEffectMeta){
            FireworkEffectMeta fwm = (FireworkEffectMeta) is.getItemMeta();
            if(fwm.getEffect() != null) {
                ret += attrcolor + " (" + _tr(fwm.getEffect(), lang) + ")";
            }
        }
        
        if (is.hasItemMeta() && is.getItemMeta() instanceof PotionMeta){
            PotionData pd = ((PotionMeta)is.getItemMeta()).getBasePotionData();
            int dur;
            ret += attrcolor;
            if(pd.isUpgraded()){
                ret += "II ";
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
                ret += String.format("(%02d:%02d)", dur/1200, dur/20%60);
            }

            for(PotionEffect pe : ((PotionMeta)is.getItemMeta()).getCustomEffects()){
                ret += attrcolor +" (";
                ret += _tr("effect.minecraft." + pe.getType().getName().toLowerCase(), lang);
                ret += " " + binaryToRoman(pe.getAmplifier() + 1);
                ret += String.format(" %02d:%02d", pe.getDuration()/1200, pe.getDuration()/20%60);
                ret += ")";
            }
        }

        return ret + ChatColor.RESET.toString();
    }

    public static String tr(Map<Enchantment, Integer> enchant, String lang){

        String ret = "(";
        for(Enchantment e : enchant.keySet()) {
            ret += _tr("enchantment.minecraft." + e.getKey().getKey(), lang) + " " + binaryToRoman(enchant.get(e)) + ", ";
        }
        return ret.substring(0, ret.length()-2) + ")";
    }

    public static String tr(Material item, String lang){
        String name = item.name().toLowerCase();
        if (name.endsWith("_wall_banner")){
            name.replace("_wall_banner", "_banner");
        }
        if (item_dicts.get(lang).containsKey("item.minecraft." + name)){
            return item_dicts.get(lang).get("item.minecraft." + name);
        }
        if (item_dicts.get(lang).containsKey("block.minecraft." + name)){
            return item_dicts.get(lang).get("block.minecraft." + name);
        }
        warn("TranslationLogic: Unsupported material: " + item);
        return name;
    }

    public static String tr(Location loc, String lang){
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

    private static String _tr(FireworkEffect effect, String lang){
        String ret = "";
        ret += _tr(effect.getType(), lang) + ", ";

        if (effect.getColors().size() > 0){
            for (Color c:effect.getColors()){
                ret += ColorTr.tr(c, lang) + ", ";
            }
        }
        if (effect.getFadeColors().size() > 0){
            ret += _tr("item.minecraft.firework_star.fade_to", lang) + " ";
            for (Color c:effect.getFadeColors()){
                ret += ColorTr.tr(c, lang) + ", ";
            }
        }
        if (effect.hasFlicker()){
            ret += _tr("item.minecraft.firework_star.flicker", lang) + ", ";
        }
        if (effect.hasTrail()){
            ret += _tr("item.minecraft.firework_star.trail", lang) + ", ";
        }
        return ret.substring(0, ret.length() - 2);
    }

    private static String _tr(FireworkEffect.Type type, String lang) {
        switch(type){
            case STAR: return _tr("item.minecraft.firework_star.shape.star", lang);
            case BALL: return _tr("item.minecraft.firework_star.shape.small_ball", lang);
            case BALL_LARGE: return _tr("item.minecraft.firework_star.shape.large_ball", lang);
            case BURST: return _tr("item.minecraft.firework_star.shape.burst", lang);
            case CREEPER: return _tr("item.minecraft.firework_star.shape.creeper", lang);
            default: return "";
        }
    }

    private static String _tr(ItemStack is, String lang){
        if (is.hasItemMeta() && is.getItemMeta() instanceof SkullMeta){
            if (((SkullMeta)is.getItemMeta()).hasOwner()) {
                return String.format(_tr("block.minecraft.player_head.named", lang, ((SkullMeta)is.getItemMeta()).getOwningPlayer().getName()));
            }
        }
        if (is.hasItemMeta() && is.getItemMeta() instanceof PotionMeta){
            PotionType pt = ((PotionMeta)is.getItemMeta()).getBasePotionData().getType();
            return _tr("item.minecraft." + is.getType().name().toLowerCase() + ".effect." + PotionTypeEnum.getHash(pt), lang);
        }
        return tr(is.getType(), lang);
    }

    private static String _tr(String node, String lang, Object... format){
        if (item_dicts.get(lang).containsKey(node))
            return String.format(item_dicts.get(lang).get(node), format);
        else{
            warn("TranslationLogic: Unknown node: " + node);
            return "";
        }
    }

    class NMS_Manifest_Root{
        Object latest;
        List<NMS_Manifest_Version> versions;
    }
    class NMS_Manifest_Version{
        String id;
        String type;
        String url;
        String time;
        String releaseTime;
    }

    private static void checkItemLang(EverShop plugin, final List<String> langs){
        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            File folder =  new File(plugin.getDataFolder(), "i18n" + File.separator + "items");
            List<String> fetchList = new ArrayList<String>();
            if (!folder.exists()){
                folder.mkdirs();
            }
            if (!folder.exists()){
                severe("TranslationLogic: failed to create item translation folder.");
                plugin.getServer().getPluginManager().disablePlugin(plugin);
            }
            for (String lang : langs){
                if (!"en_us".equals(lang)){
                    File file = new File(folder, lang + ".json");
                    if (!file.exists()){
                        fetchList.add(lang);
                    }
                }
            }
            if (fetchList.size() > 0){
                info("TranslationLogic: need to fetch [" + String.join(",", fetchList) + "]");
                fetchItemLang(plugin, fetchList);
            }
            loadItemLang(plugin, langs);
        });
    }
    private static void fetchItemLang(EverShop plugin, final List<String> langs){
        try{
            Gson gson = new Gson();
            JsonParser parser = new JsonParser();
            BufferedReader reader = null;
            String version = Bukkit.getBukkitVersion().split("-")[0];
            info("TranslationLogic: Fetching version_manifest...");
            URL url_1 = new URL("https://launchermeta.mojang.com/mc/game/version_manifest.json");
            reader = new BufferedReader(new InputStreamReader(url_1.openStream()));
            NMS_Manifest_Root root = gson.fromJson(reader, NMS_Manifest_Root.class);
            reader.close();
            URL url_2 = null;
            for (NMS_Manifest_Version v : root.versions){
                if (version.equals(v.id)){
                    url_2 = new URL(v.url);
                    break;
                }
            }
            if (url_2 == null) {
                severe("TranslationLogic: Unable to find version information: " + version);
                throw new Exception();
            }
            info("TranslationLogic: Fetching metadata of " + version + "...");
            reader = new BufferedReader(new InputStreamReader(url_2.openStream()));
            JsonObject jobj = parser.parse(reader).getAsJsonObject();
            reader.close();
            String assetsURL = jobj.getAsJsonObject("assetIndex").get("url").getAsString();
            if (assetsURL == null){
                severe("TranslationLogic: Unable to get assets information.");
                throw new Exception();
            }
            URL url_3 = new URL(assetsURL);
            info("TranslationLogic: Fetching assets information...");
            reader = new BufferedReader(new InputStreamReader(url_3.openStream()));
            jobj = parser.parse(reader).getAsJsonObject().getAsJsonObject("objects");
            reader.close();
            
            File folder =  new File(plugin.getDataFolder(), "i18n" + File.separator + "items");
            for (String lang : langs){
                JsonObject tObj = jobj.getAsJsonObject("minecraft/lang/" + lang + ".json");
                if (tObj == null){
                    severe("TranslationLogic: Not a language: " + lang + ", check your configuration.");
                    continue;
                }
                info("TranslationLogic: Fetching " + lang + "...");
                String url_4 = null;
                try{
                    String hash = tObj.get("hash").getAsString();
                    url_4 = "http://resources.download.minecraft.net/" + hash.substring(0, 2) + "/" + hash;
                    URL json = new URL(url_4);
                    File file = new File(folder, lang + ".json");
                    Files.copy(json.openStream(), file.toPath(), StandardCopyOption.REPLACE_EXISTING);
                } catch(Exception e){
                    severe("Cannot load " + lang + " from mojang, url="+url_4);
                    e.printStackTrace();
                    return;
                }
            }
        } catch (Exception e){
            warn("TranslationLogic: Error when get item translation files from mojang. Please check your config or add these files manually.");
            plugin.getServer().getPluginManager().disablePlugin(plugin);
        }
    }
    private static void loadItemLang(EverShop plugin, List<String> langs){
        BufferedReader reader = null;
        File folder =  new File(plugin.getDataFolder(), "i18n" + File.separator + "items");
        for (String lang : langs){
            if ("en_us".equals(lang)){
                URL json = null;
                try{
                    Enumeration<URL> e = ClassLoader.getSystemResources("assets/minecraft/lang/en_us.json");
                    if (e.hasMoreElements()) json = e.nextElement();
                    reader = new BufferedReader(new InputStreamReader(json.openStream()));
                } catch (Exception e){
                    severe("Cannot load en_us.json from server jar, unsupported server?");
                    continue;
                }
            } else {
                try{
                    File file = new File(folder, lang + ".json");
                    reader = new BufferedReader(new FileReader(file));
                } catch(Exception e){
                    e.printStackTrace();
                    severe("Cannot load " + lang);
                    continue;
                }
            }
            Type type = new TypeToken<Map<String, String>>() {}.getType();
            Gson gson = new Gson();
            Map<String, String> map = gson.fromJson(reader, type);
            try{
                reader.close();
            } catch (Exception e){
                e.printStackTrace();
                warn("TranslationLogic: failed to close " + lang);
            }
            item_dicts.put(lang, map);
            info("TranslationLogic: " + map.get("language.name") + "/" + lang + " loaded");
        }
        info("TranslationLogic: loaded " + item_dicts.size() + " item translations.");
        if (item_dicts.size() == 0)  {
            plugin.getServer().getPluginManager().disablePlugin(plugin);
        }
        if (!item_dicts.containsKey(default_translation)) {
            severe("TranslationLogic: Items of default language " + default_translation + " is not loaded.");
            plugin.getServer().getPluginManager().disablePlugin(plugin);
        }
    }
    
    private static void checkMsgsLang(EverShop plugin){
        File folder = new File(plugin.getDataFolder(), "i18n" + File.separator + "msgs");
        if (!folder.exists()){
            folder.mkdirs();
        }
        if (!folder.exists()){
            severe("TranslationLogic: failed to create message translation folder.");
            plugin.getServer().getPluginManager().disablePlugin(plugin);
        }
        String[] i18n_files = {"zh_cn.yml", "en_us.yml"};
        for (String n:i18n_files){
            File outFile = new File(folder, n);
            if (!outFile.exists()){
                try{
                    InputStream in = plugin.getResource("i18n/" + n);
                    OutputStream out = new FileOutputStream(outFile);
                    byte[] buf = new byte[1024];
                    int len;
                    while ((len = in.read(buf)) > 0) {
                        out.write(buf, 0, len);
                    }
                    out.close();
                    in.close();
                } catch (Exception e){
                    e.printStackTrace();
                    severe("TranslationLogic: failed to save message translation " + n);
                }
            }
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
            plugin.getServer().getPluginManager().disablePlugin(plugin);
        }
        if (!tr_dicts.containsKey(default_translation)) {
            severe("TranslationLogic: Messages of default language " + default_translation + " is not loaded.");
            plugin.getServer().getPluginManager().disablePlugin(plugin);
        }
    }
}  
