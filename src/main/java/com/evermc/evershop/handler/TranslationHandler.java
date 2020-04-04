package com.evermc.evershop.handler;

import java.util.Map;
import java.util.logging.Level;

import com.evermc.evershop.EverShop;
import com.meowj.langutils.lang.LanguageHelper;

import org.bukkit.ChatColor;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.Damageable;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginDescriptionFile;

import static com.evermc.evershop.util.LogUtil.log;

public class TranslationHandler {

    private static String lang;
    private static boolean force_tr;
    private static boolean enabled;

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
        String customcolor = ChatColor.AQUA.toString()+ChatColor.ITALIC.toString();

        String ret = txtcolor + is.getAmount() + " " + _tr(is, lang);

        if (is.hasItemMeta() && is.getItemMeta().hasDisplayName()){
            ret += customcolor + "\"" + is.getItemMeta().getDisplayName() + "\"";
        }
        
        if (is.hasItemMeta() && is.getItemMeta().isUnbreakable()){
            ret += durcolor + "[" + _tr("item.unbreakable", lang) + "]";
        }

        if(is.hasItemMeta() && is.getItemMeta() instanceof Damageable && ((Damageable)is.getItemMeta()).hasDamage()){
            int maxdur = is.getType().getMaxDurability();
            int curdur = maxdur - ((Damageable)is.getItemMeta()).getDamage();
            ret += durcolor + "[" + _tr("item.durability", lang, curdur, maxdur) + "]";
        }

        if(is.getEnchantments().size() > 0)
            ret += enccolor + tr(is.getEnchantments(), lang);

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