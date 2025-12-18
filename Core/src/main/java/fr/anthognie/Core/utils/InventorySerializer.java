package fr.anthognie.Core.utils;

import org.bukkit.inventory.ItemStack;
import org.bukkit.util.io.BukkitObjectInputStream;
import org.bukkit.util.io.BukkitObjectOutputStream;
import org.yaml.snakeyaml.external.biz.base64Coder.Base64Coder;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

public class InventorySerializer {

    /**
     * Convertit un tableau d'items en une chaîne de texte Base64.
     */
    public static String itemStackArrayToBase64(ItemStack[] items) {
        try {
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            BukkitObjectOutputStream dataOutput = new BukkitObjectOutputStream(outputStream);

            dataOutput.writeInt(items.length); // Sauvegarde le nombre d'items

            for (ItemStack item : items) {
                dataOutput.writeObject(item); // Sauvegarde chaque item
            }

            dataOutput.close();
            return Base64Coder.encodeLines(outputStream.toByteArray());
        } catch (Exception e) {
            throw new IllegalStateException("Impossible de sauvegarder les items.", e);
        }
    }

    /**
     * Reconvertit une chaîne de texte Base64 en un tableau d'items.
     */
    public static ItemStack[] itemStackArrayFromBase64(String data) throws IOException {
        try {
            ByteArrayInputStream inputStream = new ByteArrayInputStream(Base64Coder.decodeLines(data));
            BukkitObjectInputStream dataInput = new BukkitObjectInputStream(inputStream);

            ItemStack[] items = new ItemStack[dataInput.readInt()]; // Lit le nombre d'items

            for (int i = 0; i < items.length; i++) {
                items[i] = (ItemStack) dataInput.readObject(); // Lit chaque item
            }

            dataInput.close();
            return items;
        } catch (ClassNotFoundException e) {
            throw new IOException("Impossible de lire les items.", e);
        }
    }
}