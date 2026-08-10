package dev.julian.ezshow.platform.forge112.command;

import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import org.junit.Test;

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
}
