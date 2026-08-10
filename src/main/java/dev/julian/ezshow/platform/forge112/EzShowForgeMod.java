package dev.julian.ezshow.platform.forge112;

import dev.julian.ezshow.core.cooldown.CooldownGate;
import dev.julian.ezshow.platform.forge112.command.ShowCommand;
import dev.julian.ezshow.platform.forge112.permission.EzShowPermissions;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventHandler;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLServerStartingEvent;

import java.util.UUID;

@Mod(
    modid = EzShowForgeMod.MOD_ID,
    name = EzShowForgeMod.NAME,
    version = EzShowForgeMod.VERSION,
    dependencies = "required-after:forge@[14.23.5.2859,)",
    acceptedMinecraftVersions = "[1.12.2]",
    acceptableRemoteVersions = "*"
)
public final class EzShowForgeMod {
    public static final String MOD_ID = "ezshow";
    public static final String NAME = "ezshow";
    public static final String VERSION = "1.1.1";

    @EventHandler
    public void init(FMLInitializationEvent event) {
        EzShowPermissions.register();
    }

    @EventHandler
    public void serverStarting(FMLServerStartingEvent event) {
        event.registerServerCommand(new ShowCommand(new CooldownGate<UUID>()));
    }
}
