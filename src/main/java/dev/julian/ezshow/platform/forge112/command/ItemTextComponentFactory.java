package dev.julian.ezshow.platform.forge112.command;

import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.Style;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.util.text.event.HoverEvent;
import net.minecraftforge.common.util.Constants;

/** Creates an item chat component without localizing its default name on the server. */
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
        return new TextComponentTranslation(translationKey);
    }
}
