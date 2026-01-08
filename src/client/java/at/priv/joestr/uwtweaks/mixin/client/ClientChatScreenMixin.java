package at.priv.joestr.uwtweaks.mixin.client;

import at.priv.joestr.uwtweaks.miscellaneous.ReplyBackState;
import net.minecraft.client.gui.screens.ChatScreen;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ChatScreen.class)
public class ClientChatScreenMixin {
    private static final Logger log = LoggerFactory.getLogger("uwtweaks");
    private ReplyBackState state = ReplyBackState.getInstance();

    @Inject(at = @At("HEAD"), method = "onClose")
    private void onChatBarClosed(CallbackInfo info) {
        if (!state.isEnabled()) {
            return;
        }

        state.setChatOpened(false);
        state.setReceivedAnotherWhisperWhileDraftingResponse(false);
    }
}
