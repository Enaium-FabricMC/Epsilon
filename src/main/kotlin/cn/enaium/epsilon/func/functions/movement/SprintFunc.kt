package cn.enaium.epsilon.func.functions.movement

import cn.enaium.epsilon.event.events.UpdateEvent
import cn.enaium.epsilon.func.Category
import cn.enaium.epsilon.func.Func
import net.minecraft.client.MinecraftClient
import org.lwjgl.glfw.GLFW

/**
 * Project: Epsilon
 * -----------------------------------------------------------
 * Copyright © 2020 | Enaium | All rights reserved.
 */
class SprintFunc : Func("Sprint", GLFW.GLFW_KEY_V, Category.MOVEMENT) {

    fun onUpdate(updateEvent: UpdateEvent) {
        MinecraftClient.getInstance().player!!.isSprinting = true
    }

}