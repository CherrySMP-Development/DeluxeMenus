package com.extendedclip.deluxemenus.nbt;

import com.extendedclip.deluxemenus.utils.VersionHelper;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Optional;
import java.util.function.Consumer;

public final class NbtProvider {

    private static boolean NBT_HOOKED;

    private static Method getStringMethod;
    private static Method setStringMethod;
    private static Method setBooleanMethod;
    private static Method setByteMethod;
    private static Method setShortMethod;
    private static Method setIntMethod;
    private static Method removeTagMethod;
    private static Method hasTagMethod;
    private static Method getTagMethod;
    private static Method setTagMethod;
    private static Method containsMethod;
    private static Method asNMSCopyMethod;
    private static Method asBukkitCopyMethod;
    private static Method customDataUpdateMethod;
    private static Method customDataCopyTagMethod;
    private static Method dataComponentGetMethod;

    private static Constructor<?> nbtCompoundConstructor;
    private static boolean usesDataComponents;
    private static Object customDataComponentType;

    static {
        try {
            final Class<?> itemStackClass = VersionHelper.getNMSClass("world.item", "ItemStack");
            final Class<?> inventoryClass = VersionHelper.getCraftClass("inventory.CraftItemStack");
            final Class<?> compoundClass = getCompoundClass();

            nbtCompoundConstructor = compoundClass.getDeclaredConstructor();

            asNMSCopyMethod = inventoryClass.getMethod("asNMSCopy", ItemStack.class);
            asBukkitCopyMethod = inventoryClass.getMethod("asBukkitCopy", itemStackClass);

            if (VersionHelper.HAS_DATA_COMPONENTS) {
                setupDataComponentNbt(itemStackClass, compoundClass);
            } else {
                setupLegacyNbt(itemStackClass, compoundClass);
            }

            NBT_HOOKED = true;
        } catch (NoSuchMethodException | ClassNotFoundException e) {
            NBT_HOOKED = false;
        } catch (NoSuchFieldException | IllegalAccessException e) {
            NBT_HOOKED = false;
        }
    }

    public static boolean isAvailable() {
        return NBT_HOOKED;
    }

    /**
     * Sets an NBT tag to the an {@link ItemStack}.
     *
     * @param itemStack The current {@link ItemStack} to be set.
     * @param key       The NBT key to use.
     * @param value     The tag value to set.
     * @return An {@link ItemStack} that has NBT set.
     */
    public static ItemStack setString(final ItemStack itemStack, final String key, final String value) {
        if (itemStack == null) return null;
        if (itemStack.getType() == Material.AIR) return itemStack;

        if (usesDataComponents) {
            return updateCustomData(itemStack, compound -> setString(compound, key, value));
        }

        Object nmsItemStack = asNMSCopy(itemStack);
        Object itemCompound = hasTag(nmsItemStack) ? getTag(nmsItemStack) : newNBTTagCompound();

        setString(itemCompound, key, value);
        setTag(nmsItemStack, itemCompound);

        return asBukkitCopy(nmsItemStack);
    }

    /**
     * Sets a boolean to the {@link ItemStack}.
     * Mainly used for setting an item to be unbreakable on older versions.
     *
     * @param itemStack The {@link ItemStack} to set the boolean to.
     * @param key       The key to use.
     * @param value     The boolean value.
     * @return An {@link ItemStack} with a boolean value set.
     */
    public static ItemStack setBoolean(final ItemStack itemStack, final String key, final boolean value) {
        if (itemStack == null) return null;
        if (itemStack.getType() == Material.AIR) return itemStack;

        if (usesDataComponents) {
            return updateCustomData(itemStack, compound -> setBoolean(compound, key, value));
        }

        Object nmsItemStack = asNMSCopy(itemStack);
        Object itemCompound = hasTag(nmsItemStack) ? getTag(nmsItemStack) : newNBTTagCompound();

        setBoolean(itemCompound, key, value);
        setTag(nmsItemStack, itemCompound);

        return asBukkitCopy(nmsItemStack);
    }

    /**
     * Gets the NBT tag based on a given key.
     *
     * @param itemStack The {@link ItemStack} to get from.
     * @param key       The key to look for.
     * @return The tag that was stored in the {@link ItemStack}.
     */
    public static String getString(final ItemStack itemStack, final String key) {
        if (itemStack == null) return null;
        if (itemStack.getType() == Material.AIR) return null;

        if (usesDataComponents) {
            final Object itemCompound = getCustomDataTag(itemStack);
            if (itemCompound == null) return null;
            return getString(itemCompound, key);
        }

        Object nmsItemStack = asNMSCopy(itemStack);
        Object itemCompound = hasTag(nmsItemStack) ? getTag(nmsItemStack) : newNBTTagCompound();

        return getString(itemCompound, key);
    }

    public static ItemStack setByte(final ItemStack itemStack, final String key, final byte value) {
        if (itemStack == null) return null;
        if (itemStack.getType() == Material.AIR) return null;

        if (usesDataComponents) {
            return updateCustomData(itemStack, compound -> setByte(compound, key, value));
        }

        Object nmsItemStack = asNMSCopy(itemStack);
        Object itemCompound = hasTag(nmsItemStack) ? getTag(nmsItemStack) : newNBTTagCompound();

        setByte(itemCompound, key, value);
        setTag(nmsItemStack, itemCompound);

        return asBukkitCopy(nmsItemStack);
    }

    public static ItemStack setShort(final ItemStack itemStack, final String key, final short value) {
        if (itemStack == null) return null;
        if (itemStack.getType() == Material.AIR) return null;

        if (usesDataComponents) {
            return updateCustomData(itemStack, compound -> setShort(compound, key, value));
        }

        Object nmsItemStack = asNMSCopy(itemStack);
        Object itemCompound = hasTag(nmsItemStack) ? getTag(nmsItemStack) : newNBTTagCompound();

        setShort(itemCompound, key, value);
        setTag(nmsItemStack, itemCompound);

        return asBukkitCopy(nmsItemStack);
    }

    public static ItemStack setInt(final ItemStack itemStack, final String key, final int value) {
        if (itemStack == null) return null;
        if (itemStack.getType() == Material.AIR) return null;

        if (usesDataComponents) {
            return updateCustomData(itemStack, compound -> setInt(compound, key, value));
        }

        Object nmsItemStack = asNMSCopy(itemStack);
        Object itemCompound = hasTag(nmsItemStack) ? getTag(nmsItemStack) : newNBTTagCompound();

        setInt(itemCompound, key, value);
        setTag(nmsItemStack, itemCompound);

        return asBukkitCopy(nmsItemStack);
    }

    public static boolean hasKey(final ItemStack itemStack, final String key) {
        if (itemStack == null) return false;

        if (usesDataComponents) {
            final Object itemCompound = getCustomDataTag(itemStack);
            if (itemCompound == null) return false;
            try {
                return (boolean) containsMethod.invoke(itemCompound, key);
            } catch (IllegalAccessException | InvocationTargetException e) {
                return false;
            }
        }

        final Object nmsItemStack = asNMSCopy(itemStack);
        final Object itemCompound = hasTag(nmsItemStack) ? getTag(nmsItemStack) : newNBTTagCompound();
        try {
            return (boolean) containsMethod.invoke(itemCompound, key);
        } catch (IllegalAccessException | InvocationTargetException e) {
            return false;
        }
    }

    public static ItemStack removeKey(final ItemStack itemStack, final String key) {
        if (itemStack == null) return null;
        if (itemStack.getType() == Material.AIR) return null;

        if (usesDataComponents) {
            return updateCustomData(itemStack, compound -> removeTag(compound, key));
        }

        Object nmsItemStack = asNMSCopy(itemStack);
        if (!hasTag(nmsItemStack)) return itemStack;
        Object itemCompound = hasTag(nmsItemStack) ? getTag(nmsItemStack) : newNBTTagCompound();

        removeTag(itemCompound, key);
        setTag(nmsItemStack, itemCompound);

        return asBukkitCopy(nmsItemStack);
    }

    /**
     * Mimics the itemCompound#setString method.
     *
     * @param itemCompound The ItemCompound.
     * @param key          The key to add.
     * @param value        The value to add.
     */
    private static void setString(final Object itemCompound, final String key, final String value) {
        try {
            setStringMethod.invoke(itemCompound, key, value);
        } catch (IllegalAccessException | InvocationTargetException ignored) {
        }
    }

    private static void setBoolean(final Object itemCompound, final String key, final boolean value) {
        try {
            setBooleanMethod.invoke(itemCompound, key, value);
        } catch (IllegalAccessException | InvocationTargetException ignored) {
        }
    }

    private static void setByte(final Object itemCompound, final String key, final byte value) {
        try {
            setByteMethod.invoke(itemCompound, key, value);
        } catch (IllegalAccessException | InvocationTargetException ignored) {
        }
    }

    private static void setShort(final Object itemCompound, final String key, final short value) {
        try {
            setShortMethod.invoke(itemCompound, key, value);
        } catch (IllegalAccessException | InvocationTargetException ignored) {
        }
    }

    private static void setInt(final Object itemCompound, final String key, final int value) {
        try {
            setIntMethod.invoke(itemCompound, key, value);
        } catch (IllegalAccessException | InvocationTargetException ignored) {
        }
    }

    /**
     * Mimics the itemCompound#getString method.
     *
     * @param itemCompound The ItemCompound.
     * @param key          The key to get from.
     * @return A string with the value from the key.
     */
    private static String getString(final Object itemCompound, final String key) {
        try {
            final Object value = getStringMethod.invoke(itemCompound, key);
            if (value instanceof Optional) {
                return ((Optional<?>) value).map(Object::toString).orElse(null);
            }
            return (String) value;
        } catch (IllegalAccessException | InvocationTargetException e) {
            return null;
        }
    }

    /**
     * Mimics the nmsItemStack#hasTag method.
     *
     * @param nmsItemStack the NMS ItemStack to check from.
     * @return True or false depending if it has tag or not.
     */
    private static boolean hasTag(final Object nmsItemStack) {
        try {
            return (boolean) hasTagMethod.invoke(nmsItemStack);
        } catch (IllegalAccessException | InvocationTargetException e) {
            return false;
        }
    }

    /**
     * Mimics the nmsItemStack#getTag method.
     *
     * @param nmsItemStack The NMS ItemStack to get from.
     * @return The tag compound.
     */
    public static Object getTag(final Object nmsItemStack) {
        try {
            return getTagMethod.invoke(nmsItemStack);
        } catch (IllegalAccessException | InvocationTargetException e) {
            return null;
        }
    }

    /**
     * Mimics the nmsItemStack#setTag method.
     *
     * @param nmsItemStack the NMS ItemStack to set the tag to.
     * @param itemCompound The item compound to set.
     */
    private static void setTag(final Object nmsItemStack, final Object itemCompound) {
        try {
            setTagMethod.invoke(nmsItemStack, itemCompound);
        } catch (IllegalAccessException | InvocationTargetException ignored) {
        }
    }

    /**
     * Mimics the nmsItemStack#removeTag method.
     *
     * @param nmsItemStack the NMS ItemStack to remove the tag from.
     * @param itemCompound The item compound to remove.
     */
    private static void removeTag(final Object nmsItemStack, final Object itemCompound) {
        try {
            removeTagMethod.invoke(nmsItemStack, itemCompound);
        } catch (IllegalAccessException | InvocationTargetException ignored) {
        }
    }

    /**
     * Mimics the new NBTTagCompound instantiation.
     *
     * @return The new NBTTagCompound.
     */
    private static Object newNBTTagCompound() {
        try {
            return nbtCompoundConstructor.newInstance();
        } catch (IllegalAccessException | InstantiationException | InvocationTargetException e) {
            return null;
        }
    }

    /**
     * Mimics the CraftItemStack#asNMSCopy method.
     *
     * @param itemStack The ItemStack to make NMS copy.
     * @return An NMS copy of the ItemStack.
     */
    public static Object asNMSCopy(final ItemStack itemStack) {
        try {
            return asNMSCopyMethod.invoke(null, itemStack);
        } catch (IllegalAccessException | InvocationTargetException e) {
            return null;
        }
    }

    /**
     * Mimics the CraftItemStack#asBukkitCopy method.
     *
     * @param nmsItemStack The NMS ItemStack to turn into {@link ItemStack}.
     * @return The new {@link ItemStack}.
     */
    public static ItemStack asBukkitCopy(final Object nmsItemStack) {
        try {
            return (ItemStack) asBukkitCopyMethod.invoke(null, nmsItemStack);
        } catch (IllegalAccessException | InvocationTargetException e) {
            return null;
        }
    }

    private static Class<?> getCompoundClass() throws ClassNotFoundException {
        try {
            return Class.forName("net.minecraft.nbt.CompoundTag");
        } catch (ClassNotFoundException ignored) {
            return VersionHelper.getNMSClass("nbt", "NBTTagCompound");
        }
    }

    private static void setupDataComponentNbt(final Class<?> itemStackClass, final Class<?> compoundClass)
            throws ClassNotFoundException, NoSuchMethodException, NoSuchFieldException, IllegalAccessException {
        usesDataComponents = true;

        containsMethod = compoundClass.getMethod("contains", String.class);
        getStringMethod = compoundClass.getMethod("getString", String.class);
        setStringMethod = compoundClass.getMethod("putString", String.class, String.class);
        setBooleanMethod = compoundClass.getMethod("putBoolean", String.class, boolean.class);
        setByteMethod = compoundClass.getMethod("putByte", String.class, byte.class);
        setShortMethod = compoundClass.getMethod("putShort", String.class, short.class);
        setIntMethod = compoundClass.getMethod("putInt", String.class, int.class);
        removeTagMethod = compoundClass.getMethod("remove", String.class);

        final Class<?> dataComponentsClass = Class.forName("net.minecraft.core.component.DataComponents");
        final Class<?> dataComponentTypeClass = Class.forName("net.minecraft.core.component.DataComponentType");
        final Class<?> dataComponentGetterClass = Class.forName("net.minecraft.core.component.DataComponentGetter");
        final Class<?> customDataClass = Class.forName("net.minecraft.world.item.component.CustomData");

        final Field customDataField = dataComponentsClass.getField("CUSTOM_DATA");
        customDataComponentType = customDataField.get(null);
        customDataUpdateMethod = customDataClass.getMethod("update", dataComponentTypeClass, itemStackClass, Consumer.class);
        customDataCopyTagMethod = customDataClass.getMethod("copyTag");
        dataComponentGetMethod = dataComponentGetterClass.getMethod("get", dataComponentTypeClass);
    }

    private static void setupLegacyNbt(final Class<?> itemStackClass, final Class<?> compoundClass) throws NoSuchMethodException {
        usesDataComponents = false;

        containsMethod = compoundClass.getMethod(VersionConstants.CONTAINS_METHOD_NAME, String.class);
        getStringMethod = compoundClass.getMethod(VersionConstants.GET_STRING_METHOD_NAME, String.class);
        setStringMethod = compoundClass.getMethod(VersionConstants.SET_STRING_METHOD_NAME, String.class, String.class);
        setBooleanMethod = compoundClass.getMethod(VersionConstants.SET_BOOLEAN_METHOD_NAME, String.class, boolean.class);
        setByteMethod = compoundClass.getMethod(VersionConstants.SET_BYTE_METHOD_NAME, String.class, byte.class);
        setShortMethod = compoundClass.getMethod(VersionConstants.SET_SHORT_METHOD_NAME, String.class, short.class);
        setIntMethod = compoundClass.getMethod(VersionConstants.SET_INTEGER_METHOD_NAME, String.class, int.class);
        removeTagMethod = compoundClass.getMethod(VersionConstants.REMOVE_TAG_METHOD_NAME, String.class);
        hasTagMethod = itemStackClass.getMethod(VersionConstants.HAS_TAG_METHOD_NAME);
        getTagMethod = itemStackClass.getMethod(VersionConstants.GET_TAG_METHOD_NAME);
        setTagMethod = itemStackClass.getMethod(VersionConstants.SET_TAG_METHOD_NAME, compoundClass);
    }

    private static ItemStack updateCustomData(final ItemStack itemStack, final Consumer<Object> mutator) {
        final Object nmsItemStack = asNMSCopy(itemStack);
        if (nmsItemStack == null) return itemStack;

        try {
            customDataUpdateMethod.invoke(null, customDataComponentType, nmsItemStack, mutator);
            final ItemStack bukkitCopy = asBukkitCopy(nmsItemStack);
            return bukkitCopy == null ? itemStack : bukkitCopy;
        } catch (IllegalAccessException | InvocationTargetException e) {
            return itemStack;
        }
    }

    private static Object getCustomDataTag(final ItemStack itemStack) {
        final Object nmsItemStack = asNMSCopy(itemStack);
        if (nmsItemStack == null) return null;

        try {
            final Object customData = dataComponentGetMethod.invoke(nmsItemStack, customDataComponentType);
            if (customData == null) return null;
            return customDataCopyTagMethod.invoke(customData);
        } catch (IllegalAccessException | InvocationTargetException e) {
            return null;
        }
    }

    private static class VersionConstants {

        private final static String CONTAINS_METHOD_NAME = containsMethodName();
        private final static String GET_STRING_METHOD_NAME = getStringMethodName();
        private final static String SET_STRING_METHOD_NAME = setStringMethodName();
        private final static String SET_BOOLEAN_METHOD_NAME = setBooleanMethodName();
        private final static String SET_BYTE_METHOD_NAME = setByteMethodName();
        private final static String SET_SHORT_METHOD_NAME = setShortMethodName();
        private final static String SET_INTEGER_METHOD_NAME = setIntegerMethodName();
        private final static String REMOVE_TAG_METHOD_NAME = removeTagMethodName();
        private final static String HAS_TAG_METHOD_NAME = hasTagMethodName();
        private final static String GET_TAG_METHOD_NAME = getTagMethodName();
        private final static String SET_TAG_METHOD_NAME = setTagMethodName();

        private static String getStringMethodName() {
            if (VersionHelper.HAS_OBFUSCATED_NAMES) return "l";
            return "getString";
        }

        private static String setStringMethodName() {
            if (VersionHelper.HAS_OBFUSCATED_NAMES) return "a";
            return "setString";
        }

        private static String setBooleanMethodName() {
            if (VersionHelper.HAS_OBFUSCATED_NAMES) return "a";
            return "setBoolean";
        }

        private static String setByteMethodName() {
            if (VersionHelper.HAS_OBFUSCATED_NAMES) return "a";
            return "setByte";
        }

        private static String setShortMethodName() {
            if (VersionHelper.HAS_OBFUSCATED_NAMES) return "a";
            return "setShort";
        }

        private static String setIntegerMethodName() {
            if (VersionHelper.HAS_OBFUSCATED_NAMES) return "a";
            return "setInt";
        }

        private static String hasTagMethodName() {
            if (VersionHelper.CURRENT_VERSION >= 1200) return "u"; // 1.20 variable change
            if (VersionHelper.CURRENT_VERSION >= 1190) return "t"; // 1.19 variable change
            if (VersionHelper.CURRENT_VERSION == 1182) return "s"; // 1.18.2 variable change
            if (VersionHelper.HAS_OBFUSCATED_NAMES) return "r"; // 1.18-1.18.1
            return "hasTag";
        }

        private static String getTagMethodName() {
            if (VersionHelper.CURRENT_VERSION >= 1200) return "v"; // 1.20 variable change
            if (VersionHelper.CURRENT_VERSION >= 1190) return "u"; // 1.19 variable change
            if (VersionHelper.CURRENT_VERSION == 1182) return "t"; // 1.18.2 variable change
            if (VersionHelper.HAS_OBFUSCATED_NAMES) return "s"; // 1.18-1.18.1
            return "getTag";
        }

        private static String containsMethodName() {
            if (VersionHelper.HAS_OBFUSCATED_NAMES) return "e";
            return "hasKey";
        }

        private static String setTagMethodName() {
            if (VersionHelper.HAS_OBFUSCATED_NAMES) return "c";
            return "setTag";
        }

        private static String removeTagMethodName() {
            if (VersionHelper.HAS_OBFUSCATED_NAMES) return "r";
            return "remove";
        }

    }
}
