package net.dirtcraft.mods.dirt_essentials.mixins;

import net.minecraft.Util;
import net.minecraft.network.chat.ChatType;
import net.minecraft.network.chat.Component;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.UUID;

@Mixin(net.minecraft.server.players.PlayerList.class)
public class MixinJoinLeaveMessages {

	@Inject(at = @At("HEAD"), method = "broadcastMessage(Lnet/minecraft/network/chat/Component;Lnet/minecraft/network/chat/ChatType;Ljava/util/UUID;)V", cancellable = true)
	public void broadcastMessage(Component pMessage, ChatType pChatType, UUID pSenderUuid, CallbackInfo ci) {
		if (pChatType == ChatType.SYSTEM && pSenderUuid.equals(Util.NIL_UUID) && pMessage.getString().contains("joined the game"))
			ci.cancel();

		if (pChatType == ChatType.SYSTEM && pSenderUuid.equals(Util.NIL_UUID) && pMessage.getString().contains("left the game"))
			ci.cancel();
	}
}
