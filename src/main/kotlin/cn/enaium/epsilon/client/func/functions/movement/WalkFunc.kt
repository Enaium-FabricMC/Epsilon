package cn.enaium.epsilon.client.func.functions.movement

import cn.enaium.cf4m.annotation.Event
import cn.enaium.cf4m.annotation.module.Disable
import cn.enaium.cf4m.annotation.module.Module
import cn.enaium.cf4m.module.Category
import cn.enaium.epsilon.client.MC
import cn.enaium.epsilon.client.events.MotioningEvent

/**
 * Project: Epsilon
 * License: GPL-3.0
 * -----------------------------------------------------------
 * Copyright © 2020-2021 | Enaium | All rights reserved.
 */
@Module("AutoWalk", category = Category.MOVEMENT)
class WalkFunc {

    @Event
    fun on(motioningEvent: MotioningEvent) {
        MC.options.keyForward.isPressed = true
    }

    @Disable
    fun disable() {
        MC.options.keyForward.isPressed = false
    }

}