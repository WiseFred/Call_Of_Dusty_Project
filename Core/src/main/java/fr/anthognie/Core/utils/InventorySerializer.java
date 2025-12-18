package fr.anthognie.Core.utils;

import org.bukkit.Bukkit;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.io.BukkitObjectInputStream;
import org.bukkit.util.io.BukkitObjectOutputStream;
import org.yaml.snakeyaml.external.biz.base64Coder.Base64Coder;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.lang.reflect.Method;

public class InventorySerializer {

    // ... (Garde les méthodes itemStackArrayToBase64 et itemStackArrayFromBase64 d'avant ici) ...
    // Ajoute ces deux méthodes en dessous :

    // --- GESTION ITEM UNIQUE (POUR LA DB) ---

    public static String singleItemToBase64(ItemStack item) {
        try {
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            BukkitObjectOutputStream dataOutput = new BukkitObjectOutputStream(outputStream);

            // On utilise la même logique NBT que pour les tableaux
            if (item != null && item.getType() != org.bukkit.Material.AIR) {
                dataOutput.writeObject(saveNBT(item));
            } else {
                dataOutput.writeObject(null);
            }

            dataOutput.close();
            return Base64Coder.encodeLines(outputStream.toByteArray());
        } catch (Exception e) {
            throw new IllegalStateException("Erreur sauvegarde item unique.", e);
        }
    }

    public static ItemStack singleItemFromBase64(String data) {
        try {
            ByteArrayInputStream inputStream = new ByteArrayInputStream(Base64Coder.decodeLines(data));
            BukkitObjectInputStream dataInput = new BukkitObjectInputStream(inputStream);

            Object readObject = dataInput.readObject();
            dataInput.close();

            if (readObject != null) {
                return restoreNBT(readObject);
            }
            return null;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    // ... (Garde les méthodes privées saveNBT, restoreNBT, getNMSClass, getOBCClass d'avant) ...
    // (Assure-toi de copier les méthodes privées que je t'ai donné dans le message précédent)

    // RAPPEL DES MÉTHODES PRIVÉES DU MESSAGE PRÉCÉDENT (A INCLURE ABSOLUMENT)
    private static Object saveNBT(ItemStack item) {
        try {
            Class<?> craftItemStackClass = getOBCClass("inventory.CraftItemStack");
            Method asNMSCopy = craftItemStackClass.getMethod("asNMSCopy", ItemStack.class);
            Object nmsItem = asNMSCopy.invoke(null, item);
            Class<?> nmsItemClass = getNMSClass("world.item.ItemStack");
            Class<?> nbtTagCompoundClass = getNMSClass("nbt.NBTTagCompound");
            Object nbtTag = nbtTagCompoundClass.newInstance();
            Method saveMethod = nmsItemClass.getMethod("b", nbtTagCompoundClass);
            saveMethod.invoke(nmsItem, nbtTag);
            return nbtTag.toString();
        } catch (Exception e) { return item; }
    }

    private static ItemStack restoreNBT(Object serialized) {
        if (serialized instanceof ItemStack) return (ItemStack) serialized;
        if (serialized instanceof String) {
            try {
                String nbtString = (String) serialized;
                Class<?> tagParserClass = getNMSClass("nbt.MojangsonParser");
                Method parseMethod = tagParserClass.getMethod("a", String.class);
                Object nbtTag = parseMethod.invoke(null, nbtString);
                Class<?> nmsItemClass = getNMSClass("world.item.ItemStack");
                Method createMethod = nmsItemClass.getMethod("a", getNMSClass("nbt.NBTTagCompound"));
                Object nmsItem = createMethod.invoke(null, nbtTag);
                Class<?> craftItemStackClass = getOBCClass("inventory.CraftItemStack");
                Method asBukkitCopy = craftItemStackClass.getMethod("asBukkitCopy", nmsItemClass);
                return (ItemStack) asBukkitCopy.invoke(null, nmsItem);
            } catch (Exception e) { e.printStackTrace(); }
        }
        return null;
    }

    private static Class<?> getNMSClass(String name) throws ClassNotFoundException {
        return Class.forName("net.minecraft." + name);
    }
    private static Class<?> getOBCClass(String name) throws ClassNotFoundException {
        String version = Bukkit.getServer().getClass().getPackage().getName().split("\\.")[3];
        return Class.forName("org.bukkit.craftbukkit." + version + "." + name);
    }
}