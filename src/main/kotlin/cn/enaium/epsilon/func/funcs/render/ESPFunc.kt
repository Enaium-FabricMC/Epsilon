package cn.enaium.epsilon.func.funcs.render

import cn.enaium.epsilon.Epsilon.MC
import cn.enaium.epsilon.event.EventAT
import cn.enaium.epsilon.event.events.Render3DEvent
import cn.enaium.epsilon.func.Category
import cn.enaium.epsilon.func.Func
import cn.enaium.epsilon.func.FuncAT
import cn.enaium.epsilon.utils.Render3DUtils
import net.minecraft.util.math.Box
import org.lwjgl.opengl.GL11
import java.awt.Color

/**
 * Project: Epsilon
 * -----------------------------------------------------------
 * Copyright © 2020 | Enaium | All rights reserved.
 */
@FuncAT
class ESPFunc : Func("ESP", 0, Category.RENDER) {
    override fun onEnable() {
        super.onEnable()
        GL11.glNewList(1, GL11.GL_COMPILE)
        val bb = Box(-0.5, 0.0, -0.5, 0.5, 1.0, 0.5)
        Render3DUtils.drawOutlined(bb)
        GL11.glEndList()
    }

    override fun onDisable() {
        super.onDisable()
        GL11.glDeleteLists(1, 1)
    }

    @EventAT
    fun on(render3DEvent: Render3DEvent) {
        for (e in MC.world!!.entities) {
            Render3DUtils.drawOutlinedBox(e, 0.5, render3DEvent.tickDelta, Color.CYAN)
        }
    }
}