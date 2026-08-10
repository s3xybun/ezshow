package dev.julian.ezshow.platform.forge112.command;

import dev.julian.ezshow.core.cooldown.CooldownGate;
import dev.julian.ezshow.platform.forge112.config.EzShowConfig;
import dev.julian.ezshow.platform.forge112.permission.EzShowPermissions;
import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.command.WrongUsageException;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentString;

import java.util.UUID;
import java.util.concurrent.TimeUnit;

public final class ShowCommand extends CommandBase {
    private final CooldownGate<UUID> cooldowns;

    public ShowCommand(CooldownGate<UUID> cooldowns) {
        this.cooldowns = cooldowns;
    }

    @Override
    public String getName() {
        return "show";
    }

    @Override
    public String getUsage(ICommandSender sender) {
        return "/show";
    }

    @Override
    public int getRequiredPermissionLevel() {
        return 0;
    }

    @Override
    public boolean checkPermission(MinecraftServer server, ICommandSender sender) {
        return sender instanceof EntityPlayerMP && EzShowPermissions.canShow((EntityPlayerMP) sender);
    }

    @Override
    public void execute(MinecraftServer server, ICommandSender sender, String[] arguments) throws CommandException {
        if (arguments.length != 0) {
            throw new WrongUsageException(getUsage(sender));
        }

        EntityPlayerMP player = getCommandSenderAsPlayer(sender);
        ItemStack stack = selectHeldItem(player.getHeldItemMainhand(), player.getHeldItemOffhand());
        if (stack.isEmpty()) {
            throw new WrongUsageException(getUsage(sender));
        }

        if (!EzShowPermissions.bypassesCooldown(player)) {
            long duration = TimeUnit.SECONDS.toNanos(EzShowConfig.cooldownSeconds);
            boolean acquired = cooldowns.tryAcquire(
                player.getUniqueID(),
                System.nanoTime(),
                duration
            );
            if (!acquired) {
                throw new WrongUsageException(getUsage(sender));
            }
        }

        server.getPlayerList().sendMessage(createShareMessage(player, stack.copy()), false);
    }

    static ItemStack selectHeldItem(ItemStack mainHand, ItemStack offHand) {
        return mainHand.isEmpty() ? offHand : mainHand;
    }

    private ITextComponent createShareMessage(EntityPlayerMP player, ItemStack stack) {
        return new TextComponentString("")
            .appendSibling(player.getDisplayName())
            .appendText(": ")
            .appendSibling(ItemTextComponentFactory.create(stack));
    }
}
