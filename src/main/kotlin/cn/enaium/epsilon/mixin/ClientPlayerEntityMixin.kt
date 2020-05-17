package cn.enaium.epsilon.mixin

import cn.enaium.epsilon.Epsilon
import cn.enaium.epsilon.event.Event
import cn.enaium.epsilon.event.events.MotionEvent
import cn.enaium.epsilon.event.events.UpdateEvent
import net.minecraft.client.network.ClientPlayerEntity
import org.spongepowered.asm.mixin.Mixin
import org.spongepowered.asm.mixin.injection.At
import org.spongepowered.asm.mixin.injection.Inject
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo

/**
 * Project: Epsilon
 * -----------------------------------------------------------
 * Copyright © 2020 | Enaium | All rights reserved.
 */
@Mixin(ClientPlayerEntity::class)
class ClientPlayerEntityMixin {
    @Inject(at = [At("HEAD")], method = ["sendChatMessage"], cancellable = true)
    private fun onSendChatMessage(message: String, callbackInfo: CallbackInfo) {
        if (Epsilon.commandManager.processCommand(message)) {
            callbackInfo.cancel()
        }
    }

    @Inject(at = [At("HEAD")], method = ["tick()V"])
    private fun preTick(callbackInfo: CallbackInfo) {
        UpdateEvent().call()
    }

    @Inject(at = [At("HEAD")], method = ["sendMovementPackets()V"])
    private fun onSendMovementPacketsHEAD(callbackInfo: CallbackInfo) {
        MotionEvent(Event.Type.PRE).call()
    }

    @Inject(at = [At("TAIL")], method = ["sendMovementPackets()V"])
    private fun onSendMovementPacketsTAIL(callbackInfo: CallbackInfo) {
        MotionEvent(Event.Type.POST).call()
    }
}