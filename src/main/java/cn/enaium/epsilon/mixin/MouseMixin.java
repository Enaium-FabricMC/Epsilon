package cn.enaium.epsilon.mixin;

import cn.enaium.epsilon.client.events.MouseScrollEvent;
import net.minecraft.client.Mouse;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Project: Epsilon
 * -----------------------------------------------------------
 * Copyright © 2020 | Enaium | All rights reserved.
 */
@Mixin(Mouse.class)
class MouseMixin {
    @Inject(at = @At("HEAD"), method = "onMouseScroll")
    private void onKey(long windowHandle, double up, double down, CallbackInfo callbackInfo) {
        new MouseScrollEvent(windowHandle, up, down).call();
    }
}