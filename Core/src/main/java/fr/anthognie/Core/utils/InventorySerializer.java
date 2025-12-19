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

    // --- GESTION TABLEAUX (POUR INVENTAIRES COMPLETS) ---

    public static String itemStackArrayToBase64(ItemStack[] items) {
        try {
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            BukkitObjectOutputStream dataOutput = new BukkitObjectOutputStream(outputStream);

            dataOutput.writeInt(items.length);

            for (ItemStack item : items) {
                // On utilise une méthode spéciale pour sauvegarder le NBT complet
                if (item != null && item.getType() != org.bukkit.Material.AIR) {
                    dataOutput.writeObject(saveNBT(item)); // Sauvegarde NBT
                } else {
                    dataOutput.writeObject(null);
                }
            }

            dataOutput.close();
            return Base64Coder.encodeLines(outputStream.toByteArray());
        } catch (Exception e) {
            throw new IllegalStateException("Erreur lors de la sauvegarde des items (NBT Array).", e);
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
                    items[i] = restoreNBT(readObject); // Restaure NBT
                } else {
                    items[i] = null;
                }
            }

            dataInput.close();
            return items;
        } catch (ClassNotFoundException e) {
            throw new IOException("Erreur lors du chargement des items (NBT Array).", e);
        }
    }

    // --- GESTION ITEM UNIQUE (POUR LA DB / CONFIG) ---

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
    // Ces méthodes assurent que les données du mod (NBT) ne sont pas perdues par Bukkit.

    private static Object saveNBT(ItemStack item) {
        try {
            // Tentative de conversion en NMS ItemStack
            Class<?> craftItemStackClass = getOBCClass("inventory.CraftItemStack");
            Method asNMSCopy = craftItemStackClass.getMethod("asNMSCopy", ItemStack.class);
            Object nmsItem = asNMSCopy.invoke(null, item);

            // Sauvegarde dans un NBTTagCompound
            Class<?> nmsItemClass = getNMSClass("world.item.ItemStack");
            Class<?> nbtTagCompoundClass = getNMSClass("nbt.NBTTagCompound");
            Object nbtTag = nbtTagCompoundClass.newInstance();

            Method saveMethod = nmsItemClass.getMethod("b", nbtTagCompoundClass); // 'b' est save(CompoundTag)
            saveMethod.invoke(nmsItem, nbtTag);

            return nbtTag.toString(); // Retourne le NBT en String
        } catch (Exception e) {
            // Fallback : Si la réflexion échoue, on sauvegarde l'item Bukkit classique
            return item;
        }
    }

    private static ItemStack restoreNBT(Object serialized) {
        if (serialized instanceof ItemStack) {
            return (ItemStack) serialized;
        }
        if (serialized instanceof String) {
            try {
                String nbtString = (String) serialized;

                // Parse le String en NBTTagCompound
                Class<?> tagParserClass = getNMSClass("nbt.MojangsonParser");
                Method parseMethod = tagParserClass.getMethod("a", String.class); // 'a' = parse
                Object nbtTag = parseMethod.invoke(null, nbtString);

                // Crée un ItemStack NMS depuis le tag
                Class<?> nmsItemClass = getNMSClass("world.item.ItemStack");
                Method createMethod = nmsItemClass.getMethod("a", getNMSClass("nbt.NBTTagCompound")); // 'a' = of
                Object nmsItem = createMethod.invoke(null, nbtTag);

                // Convertit en Bukkit ItemStack
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