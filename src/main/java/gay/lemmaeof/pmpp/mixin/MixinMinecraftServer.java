package gay.lemmaeof.pmpp.mixin;

import gay.lemmaeof.pmpp.impl.LevelInboxComponent;
import gay.lemmaeof.pmpp.init.PMPPComponents;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

import net.minecraft.server.MinecraftServer;
import net.minecraft.server.WorldGenerationProgressListener;
import net.minecraft.world.level.ServerWorldProperties;

@Mixin(MinecraftServer.class)
public class MixinMinecraftServer {
	@Inject(method = "createWorlds", at = @At("TAIL"), locals = LocalCapture.CAPTURE_FAILEXCEPTION)
	private void giveServer(WorldGenerationProgressListener listener, CallbackInfo info, ServerWorldProperties properties) {
		((LevelInboxComponent) PMPPComponents.INBOXES.get(properties)).setServer((MinecraftServer) (Object)this);
	}
}
