package dev.julian.ezshow.platform.forge112.command;

import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.Style;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.util.text.event.HoverEvent;
import net.minecraft.util.text.translation.I18n;
import net.minecraftforge.common.util.Constants;

/** Creates an item chat component while keeping ordinary names client-localizable. */
final class ItemTextComponentFactory {
    private ItemTextComponentFactory() {
    }

    static ITextComponent create(ItemStack stack) {
        ITextComponent component = new TextComponentString("[")
            .appendSibling(createVisibleName(stack))
            .appendText("]");

        NBTTagCompound serializedStack = stack.writeToNBT(new NBTTagCompound());
        component.getStyle()
            .setHoverEvent(new HoverEvent(
                HoverEvent.Action.SHOW_ITEM,
                new TextComponentString(serializedStack.toString())
            ))
            .setColor(stack.getItem().getForgeRarity(stack).getColor());
        return component;
    }

    static ITextComponent createVisibleName(ItemStack stack) {
        if (stack.hasDisplayName()) {
            return new TextComponentString(stack.getDisplayName())
                .setStyle(new Style().setItalic(Boolean.TRUE));
        }

        NBTTagCompound display = stack.getSubCompound("display");
        if (display != null && display.hasKey("LocName", Constants.NBT.TAG_STRING)) {
            return new TextComponentTranslation(display.getString("LocName"));
        }

        String translationKey = stack.getItem().getUnlocalizedName(stack) + ".name";
        String defaultDisplayKey = stack.getItem().getUnlocalizedNameInefficiently(stack) + ".name";
        String defaultDisplayName = I18n.translateToLocal(defaultDisplayKey).trim();
        String actualDisplayName = stack.getDisplayName();
        if (!actualDisplayName.equals(defaultDisplayName)) {
            return new TextComponentString(actualDisplayName);
        }

        return new TextComponentTranslation(translationKey);
    }
}
