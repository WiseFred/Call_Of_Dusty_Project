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

    // --- ALIAS DE COMPATIBILITÉ (Ceux qui manquaient pour EconomyManager) ---
    public static String toBase64(ItemStack[] items) {
        return itemStackArrayToBase64(items);
    }

    public static ItemStack[] fromBase64(String data) throws IOException {
        return itemStackArrayFromBase64(data);
    }
    // ------------------------------------------------------------------------

    // --- GESTION TABLEAUX (POUR INVENTAIRES COMPLETS) ---

    public static String itemStackArrayToBase64(ItemStack[] items) {
        try {
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            BukkitObjectOutputStream dataOutput = new BukkitObjectOutputStream(outputStream);

            dataOutput.writeInt(items.length);

            for (ItemStack item : items) {
                if (item != null && item.getType() != org.bukkit.Material.AIR) {
                    dataOutput.writeObject(saveNBT(item)); // Sauvegarde NBT Moddé
                } else {
                    dataOutput.writeObject(null);
                }
            }

            dataOutput.close();
            return Base64Coder.encodeLines(outputStream.toByteArray());
        } catch (Exception e) {
            throw new IllegalStateException("Erreur lors de la sauvegarde des items (Array).", e);
        }
    }

    public static ItemStack[] itemStackArrayFromBase64(String data) throws IOException {
        try {
            ByteArrayInputStream inputStream = new ByteArrayInputStream(Base64Coder.decodeLines(data));
            BukkitObjectInputStream dataInput = new BukkitObjectInputStream(inputStream);

            ItemStack[] items = new ItemStack[dataInput.readInt()];

            for (int i = 0; i < items.length; i++) {
                Object readObject = dataInput.readObject();
                if (readObject != null) {
                    items[i] = restoreNBT(readObject); // Restaure NBT Moddé
                } else {
                    items[i] = null;
                }
            }

            dataInput.close();
            return items;
        } catch (ClassNotFoundException e) {
            throw new IOException("Erreur lors du chargement des items (Array).", e);
        }
    }

    // --- GESTION ITEM UNIQUE (POUR LA CONFIG / ITEMDB) ---

    public static String singleItemToBase64(ItemStack item) {
        try {
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            BukkitObjectOutputStream dataOutput = new BukkitObjectOutputStream(outputStream);

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

    // --- MÉTHODES PRIVÉES DE RÉFLEXION (NMS / ARCLIGHT) ---

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
        } catch (Exception e) {
            return item; // Fallback
        }
    }

    private static ItemStack restoreNBT(Object serialized) {
        if (serialized instanceof ItemStack) {
            return (ItemStack) serialized;
        }
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

            } catch (Exception e) {
                e.printStackTrace();
            }
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