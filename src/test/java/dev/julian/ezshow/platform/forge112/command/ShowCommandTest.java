package dev.julian.ezshow.platform.forge112.command;

import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextComponentTranslation;
import org.junit.Test;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

public class ShowCommandTest {
    @Test
    public void mainHandTakesPriority() {
        ItemStack mainHand = new ItemStack(new Item());
        ItemStack offHand = new ItemStack(new Item());

        assertSame(mainHand, ShowCommand.selectHeldItem(mainHand, offHand));
    }

    @Test
    public void offHandIsUsedWhenMainHandIsEmpty() {
        ItemStack offHand = new ItemStack(new Item());

        assertSame(offHand, ShowCommand.selectHeldItem(ItemStack.EMPTY, offHand));
    }

    @Test
    public void bothEmptyHandsRemainEmpty() {
        assertTrue(ShowCommand.selectHeldItem(ItemStack.EMPTY, ItemStack.EMPTY).isEmpty());
    }

    @Test
    public void shareMessageUsesTheVanillaPlayerChatFormat() {
        ITextComponent playerName = new TextComponentString("Steve");
        ITextComponent item = new TextComponentString("[Cobblestone]");

        ITextComponent message = ShowCommand.createShareMessage(playerName, item);

        assertTrue(message instanceof TextComponentTranslation);
        TextComponentTranslation translation = (TextComponentTranslation) message;
        assertEquals("chat.type.text", translation.getKey());
        assertArrayEquals(new Object[] {playerName, item}, translation.getFormatArgs());
    }
}
