package cn.enaium.epsilon.client.ui

import cn.enaium.cf4m.CF4M
import cn.enaium.epsilon.client.IMC
import cn.enaium.epsilon.client.MC
import cn.enaium.epsilon.client.setting.EnableSetting
import cn.enaium.epsilon.client.ui.elements.Element
import cn.enaium.epsilon.client.ui.elements.TextField
import net.minecraft.client.gui.screen.Screen
import net.minecraft.client.render.Tessellator
import net.minecraft.client.render.VertexFormat
import net.minecraft.client.render.VertexFormats
import net.minecraft.client.util.math.MatrixStack
import net.minecraft.text.LiteralText
import net.minecraft.util.Identifier
import org.lwjgl.opengl.GL11


/**
 * Project: Epsilon
 * License: GPL-3.0
 * -----------------------------------------------------------
 * Copyright © 2020-2021 | Enaium | All rights reserved.
 */
open class UI : Screen(LiteralText("")) {
    val elements: ArrayList<Element> = ArrayList()

    val BACKGROUND_STONE = Identifier("textures/gui/advancements/backgrounds/stone.png")
    val BACKGROUND_NETHER = Identifier("textures/gui/advancements/backgrounds/nether.png")
    val BACKGROUND_HUSBANDRY = Identifier("textures/gui/advancements/backgrounds/husbandry.png")
    val BACKGROUND_END = Identifier("textures/gui/advancements/backgrounds/end.png")
    val BACKGROUND_ADVENTURE = Identifier("textures/gui/advancements/backgrounds/adventure.png")


    override fun init() {
        initUI()
        super.init()
    }

    open fun initUI() {
        elements.clear()
    }

    override fun tick() {
        for (element in elements) {
            element.tick()
        }
        super.tick()
    }

    protected fun addElement(element: Element) {
        elements.add(element)
    }

    protected fun addElementAll(vararg element: Element) {
        elements.addAll(element)
    }

    private val start = System.currentTimeMillis()

    override fun render(matrices: MatrixStack, mouseX: Int, mouseY: Int, delta: Float) {

        for (element in elements) {
            if (element.visible)
                element.render(matrices, mouseX, mouseY, delta)
        }

        if (CF4M.module.getByName("ClickGUI").setting.getByName("blur")
                .getSetting<EnableSetting>().enable && IMC.gameRenderer.isShadersEnabled
        ) {

//            IMC.gameRenderer.invokeLoadShader(Identifier("shaders/post/blur.json"))


        }
        super.render(matrices, mouseX, mouseY, delta)
    }

    override fun mouseClicked(mouseX: Double, mouseY: Double, button: Int): Boolean {
        for (element in elements) {
            if (element.visible && element.enabled && element.hovered || element is TextField)
                element.mouseClicked(mouseX, mouseY, button)
        }
        return super.mouseClicked(mouseX, mouseY, button)
    }

    override fun mouseReleased(mouseX: Double, mouseY: Double, button: Int): Boolean {
        for (element in elements) {
            if (element.visible && element.enabled && element.hovered)
                element.mouseReleased(mouseX, mouseY, button)
        }
        return super.mouseReleased(mouseX, mouseY, button)
    }

    override fun mouseScrolled(mouseX: Double, mouseY: Double, amount: Double): Boolean {
        for (element in elements) {
            if (element.visible && element.enabled && element.hovered)
                element.mouseScrolled(mouseX, mouseY, amount)
        }
        return super.mouseScrolled(mouseX, mouseY, amount)
    }

    override fun mouseDragged(mouseX: Double, mouseY: Double, button: Int, deltaX: Double, deltaY: Double): Boolean {
        for (element in elements) {
            if (element.visible && element.enabled)
                element.mouseDragged(mouseX, mouseY, button, deltaX, deltaY)
        }
        return super.mouseDragged(mouseX, mouseY, button, deltaX, deltaY)
    }

    override fun keyPressed(keyCode: Int, scanCode: Int, modifiers: Int): Boolean {
        for (element in elements) {
            element.keyPressed(keyCode, scanCode, modifiers)
        }
        return super.keyPressed(keyCode, scanCode, modifiers)
    }

    override fun keyReleased(keyCode: Int, scanCode: Int, modifiers: Int): Boolean {
        for (element in elements) {
            element.keyReleased(keyCode, scanCode, modifiers)
        }
        return super.keyReleased(keyCode, scanCode, modifiers)
    }

    override fun charTyped(char: Char, keyCode: Int): Boolean {
        for (element in elements) {
            element.charTyped(char, keyCode)
        }
        return super.charTyped(char, keyCode)
    }

    fun renderBackground(background: Identifier) {
        val tessellator = Tessellator.getInstance()
        val bufferBuilder = tessellator.buffer
        client!!.textureManager.bindTexture(background)
        GL11.glColor4f(1.0f, 1.0f, 1.0f, 1.0f)
        val f = 32.0f
        bufferBuilder.begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_TEXTURE_COLOR)
        bufferBuilder.vertex(0.0, height.toDouble(), 0.0)
            .texture(0.0f, height.toFloat() / 32.0f + 0.toFloat()).color(64, 64, 64, 255).next()
        bufferBuilder.vertex(width.toDouble(), height.toDouble(), 0.0)
            .texture(width.toFloat() / 32.0f, height.toFloat() / 32.0f + 0.toFloat())
            .color(64, 64, 64, 255).next()
        bufferBuilder.vertex(width.toDouble(), 0.0, 0.0)
            .texture(width.toFloat() / 32.0f, 0.toFloat()).color(64, 64, 64, 255).next()
        bufferBuilder.vertex(0.0, 0.0, 0.0).texture(0.0f, 0.toFloat()).color(64, 64, 64, 255).next()
        tessellator.draw()
    }

    override fun onClose() {
        if (IMC.gameRenderer.isShadersEnabled) {
            MC.gameRenderer.toggleShadersEnabled()
        }
        super.onClose()
    }
}