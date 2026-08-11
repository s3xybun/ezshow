package dev.julian.ezshow.platform.forge112.command;

import net.minecraft.init.Bootstrap;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextComponentTranslation;
import org.junit.BeforeClass;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class ItemTextComponentFactoryTest {
    @BeforeClass
    public static void initializeMinecraftRegistries() {
        Bootstrap.register();
    }

    @Test
    public void defaultNameRemainsAClientTranslationKey() {
        Item item = new Item().setUnlocalizedName("ezshow.test_item");
        ItemStack stack = new ItemStack(item);

        ITextComponent name = ItemTextComponentFactory.createVisibleName(stack);

        assertTrue(name instanceof TextComponentTranslation);
        assertEquals("item.ezshow.test_item.name", ((TextComponentTranslation) name).getKey());
    }

    @Test
    public void serverLocalizationCannotChangeTheClientTranslationKey() {
        Item item = new Item() {
            @Override
            public String getUnlocalizedName(ItemStack stack) {
                return "item.ezshow.raw_name";
            }

            @Override
            public String getUnlocalizedNameInefficiently(ItemStack stack) {
                return "server-localized-name";
            }
        };
        ItemStack stack = new ItemStack(item);

        ITextComponent name = ItemTextComponentFactory.createVisibleName(stack);

        assertTrue(name instanceof TextComponentTranslation);
        assertEquals("item.ezshow.raw_name.name", ((TextComponentTranslation) name).getKey());
    }

    @Test
    public void explicitStackNameRemainsLiteralAndItalic() {
        ItemStack stack = new ItemStack(new Item());
        stack.setStackDisplayName("宝剑");

        ITextComponent name = ItemTextComponentFactory.createVisibleName(stack);

        assertTrue(name instanceof TextComponentString);
        assertEquals("宝剑", ((TextComponentString) name).getText());
        assertTrue(name.getStyle().getItalic());
    }

    @Test
    public void translatableStackNameRemainsAClientTranslationKey() {
        ItemStack stack = new ItemStack(new Item());
        stack.setTranslatableName("item.ezshow.localized_name");

        ITextComponent name = ItemTextComponentFactory.createVisibleName(stack);

        assertTrue(name instanceof TextComponentTranslation);
        assertEquals("item.ezshow.localized_name", ((TextComponentTranslation) name).getKey());
    }

    @Test
    public void dynamicallyComposedItemNameUsesTheItemsDisplayName() {
        Item item = new Item() {
            @Override
            public String getItemStackDisplayName(ItemStack stack) {
                return "Golden Spear";
            }
        }.setUnlocalizedName("ezshow.dynamic_item");
        ItemStack stack = new ItemStack(item);

        ITextComponent name = ItemTextComponentFactory.createVisibleName(stack);

        assertTrue(name instanceof TextComponentString);
        assertEquals("Golden Spear", ((TextComponentString) name).getText());
    }
}
