package com.evermc.evershop.handler;

import java.util.Map;
import java.util.logging.Level;

import com.evermc.evershop.EverShop;
import com.meowj.langutils.lang.LanguageHelper;

import org.bukkit.ChatColor;
import org.bukkit.Color;
import org.bukkit.FireworkEffect;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BookMeta;
import org.bukkit.inventory.meta.Damageable;
import org.bukkit.inventory.meta.EnchantmentStorageMeta;
import org.bukkit.inventory.meta.FireworkEffectMeta;
import org.bukkit.inventory.meta.FireworkMeta;
import org.bukkit.inventory.meta.PotionMeta;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginDescriptionFile;
import org.bukkit.potion.PotionData;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionType;

import static com.evermc.evershop.util.LogUtil.log;

public class TranslationHandler {

    private static String lang;
    private static boolean force_tr;
    private static boolean enabled;

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
    public static void init(EverShop plugin){

        lang = plugin.getConfig().getString("evershop.language");
        if (lang == null) force_tr = false;
        else force_tr = true;

        Plugin tr = plugin.getServer().getPluginManager().getPlugin("LangUtils");
        if (tr == null){
            log(Level.WARNING, "LangUtils plugin not found, disable item translation.");
            enabled = false;
        } else {
            PluginDescriptionFile desc = tr.getDescription();
            log(Level.INFO, "Hooked LangUtils-" + desc.getVersion());
            enabled = true;
        }
    }

    public static String tr(ItemStack is, Player p){
        return tr(is, force_tr?lang:p.getLocale());
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
            ret += enccolor + tr(is.getEnchantments(), lang);
        }

        if (is.hasItemMeta() && is.getItemMeta() instanceof EnchantmentStorageMeta){
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
            ret += String.format("(%02d:%02d)", dur/1200, dur/20%60);

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
        if (enabled) return LanguageHelper.getItemName(is,lang);
        else return is.getType().toString();
    }

    private static String _tr(String node, String lang, Object... format){
        if (enabled) 
            return String.format(LanguageHelper.translateToLocal(node,lang),format);
        else{
            String[] t = node.split("\\.");
            String ret = t[t.length-1];
            for (Object o:format){
                ret += "," + o.toString();
            }
            return ret;
        }
    }

}   