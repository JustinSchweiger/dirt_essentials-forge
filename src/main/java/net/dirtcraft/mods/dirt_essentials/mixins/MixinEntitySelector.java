package net.dirtcraft.mods.dirt_essentials.mixins;

import net.dirtcraft.mods.dirt_essentials.permissions.EssentialsPermissions;
import net.dirtcraft.mods.dirt_essentials.permissions.PermissionHandler;
import net.minecraft.commands.CommandSourceStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(net.minecraft.commands.arguments.selector.EntitySelector.class)
public class MixinEntitySelector {
	@Inject(at = @At("HEAD"), method = "checkPermissions(Lnet/minecraft/commands/CommandSourceStack;)V", cancellable = true)
	private void checkPermissions(CommandSourceStack pSource, CallbackInfo ci) {
		if (PermissionHandler.hasPermission(pSource, EssentialsPermissions.USE_SELECTOR)) {
			ci.cancel();
		}
	}
}
