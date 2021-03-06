package cn.enaium.epsilon.client.ui.elements

import cn.enaium.epsilon.client.ui.Color
import cn.enaium.epsilon.client.utils.FontUtils
import cn.enaium.epsilon.client.utils.Render2DUtils
import com.mojang.blaze3d.platform.GlStateManager
import com.mojang.blaze3d.systems.RenderSystem
import net.minecraft.SharedConstants
import net.minecraft.client.MinecraftClient
import net.minecraft.client.font.TextRenderer
import net.minecraft.client.gui.DrawableHelper
import net.minecraft.client.gui.screen.Screen
import net.minecraft.client.render.Tessellator
import net.minecraft.client.render.VertexFormat
import net.minecraft.client.render.VertexFormats
import net.minecraft.client.util.math.MatrixStack
import net.minecraft.util.Util
import net.minecraft.util.math.MathHelper
import org.lwjgl.opengl.GL11
import java.util.*
import java.util.function.BiFunction
import java.util.function.Consumer
import java.util.function.Predicate

/**
 * Project: Epsilon
 * License: GPL-3.0
 * -----------------------------------------------------------
 * Copyright © 2020-2021 | Enaium | All rights reserved.
 */
class TextField : Element {

    private var textRenderer: TextRenderer? = null
    private var text: String = ""
    private var maxLength = 0
    private var focusedTicks = 0
    private var focus = false
    private var focusUnlocked = false
    private var editable = false
    private var selecting = false
    private var firstCharacterIndex = 0
    private var selectionStart = 0
    private var selectionEnd = 0
    private var editableColor = 0
    private var uneditableColor = 0
    private var suggestion: String? = null
    private var changedListener: Consumer<String>? = null
    private var textPredicate: Predicate<String>? = null
    private var renderTextProvider: BiFunction<String, Int, String>? = null

    constructor(x: Int, y: Int, width: Int, height: Int) : super(
        x,
        y,
        width,
        height
    ) {
        textRenderer = FontUtils.tr
        text = ""
        maxLength = 32
        focus = true
        focusUnlocked = true
        editable = true
        editableColor = 14737632
        uneditableColor = 7368816
        textPredicate =
            Predicate { obj: String? -> Objects.nonNull(obj) }
        renderTextProvider =
            BiFunction { string: String, _: Int -> string }
    }

    constructor(x: Int, y: Int, width: Int) : this(x, y, width, 20)

    fun setChangedListener(changedListener: Consumer<String>) {
        this.changedListener = changedListener
    }

    fun setRenderTextProvider(renderTextProvider: BiFunction<String, Int, String>) {
        this.renderTextProvider = renderTextProvider
    }

//    fun getNarrationMessage(): MutableText {
//        val text: Text = this.getMessage()
//        return TranslatableText("gui.narrate.editBox", *arrayOf(text, this.text))
//    }

    fun setText(text: String) {
        if (textPredicate!!.test(text)) {
            if (text.length > maxLength) {
                this.text = text.substring(0, maxLength)
            } else {
                this.text = text
            }
            this.setCursorToEnd()
            this.setSelectionEnd(selectionStart)
            this.onChanged(text)
        }
    }

    fun getText(): String {
        return text
    }

    fun getSelectedText(): String {
        val i = if (selectionStart < selectionEnd) selectionStart else selectionEnd
        val j = if (selectionStart < selectionEnd) selectionEnd else selectionStart
        return text.substring(i, j)
    }

    fun setTextPredicate(textPredicate: Predicate<String>) {
        this.textPredicate = textPredicate
    }

    fun write(string: String?) {
        val i = if (selectionStart < selectionEnd) selectionStart else selectionEnd
        val j = if (selectionStart < selectionEnd) selectionEnd else selectionStart
        val k = maxLength - text.length - (i - j)
        var string2 = SharedConstants.stripInvalidChars(string)
        var l = string2.length
        if (k < l) {
            string2 = string2.substring(0, k)
            l = k
        }
        val string3 = StringBuilder(text).replace(i, j, string2).toString()
        if (textPredicate!!.test(string3)) {
            text = string3
            this.setSelectionStart(i + l)
            this.setSelectionEnd(selectionStart)
            onChanged(text)
        }
    }

    private fun onChanged(newText: String) {
        if (changedListener != null) {
            changedListener!!.accept(newText)
        }
    }

    private fun erase(offset: Int) {
        if (Screen.hasControlDown()) {
            eraseWords(offset)
        } else {
            eraseCharacters(offset)
        }
    }

    fun eraseWords(wordOffset: Int) {
        if (!text.isEmpty()) {
            if (selectionEnd != selectionStart) {
                write("")
            } else {
                eraseCharacters(this.getWordSkipPosition(wordOffset) - selectionStart)
            }
        }
    }

    fun eraseCharacters(characterOffset: Int) {
        if (!text.isEmpty()) {
            if (selectionEnd != selectionStart) {
                write("")
            } else {
                val i: Int = this.method_27537(characterOffset)
                val j = Math.min(i, selectionStart)
                val k = Math.max(i, selectionStart)
                if (j != k) {
                    val string = StringBuilder(text).delete(j, k).toString()
                    if (textPredicate!!.test(string)) {
                        text = string
                        this.setCursor(j)
                    }
                }
            }
        }
    }

    fun getWordSkipPosition(wordOffset: Int): Int {
        return this.getWordSkipPosition(wordOffset, this.getCursor())
    }

    private fun getWordSkipPosition(wordOffset: Int, cursorPosition: Int): Int {
        return this.getWordSkipPosition(wordOffset, cursorPosition, true)
    }

    private fun getWordSkipPosition(wordOffset: Int, cursorPosition: Int, skipOverSpaces: Boolean): Int {
        var i = cursorPosition
        val bl = wordOffset < 0
        val j = Math.abs(wordOffset)
        for (k in 0 until j) {
            if (!bl) {
                val l = text.length
                i = text.indexOf(32.toChar(), i)
                if (i == -1) {
                    i = l
                } else {
                    while (skipOverSpaces && i < l && text[i] == ' ') {
                        ++i
                    }
                }
            } else {
                while (skipOverSpaces && i > 0 && text[i - 1] == ' ') {
                    --i
                }
                while (i > 0 && text[i - 1] != ' ') {
                    --i
                }
            }
        }
        return i
    }

    fun moveCursor(offset: Int) {
        setCursor(method_27537(offset))
    }

    private fun method_27537(i: Int): Int {
        return Util.moveCursor(text, selectionStart, i)
    }

    fun setCursor(cursor: Int) {
        setSelectionStart(cursor)
        if (!selecting) {
            this.setSelectionEnd(selectionStart)
        }
        onChanged(text)
    }

    fun setSelectionStart(cursor: Int) {
        selectionStart = MathHelper.clamp(cursor, 0, text.length)
    }

    fun setCursorToStart() {
        setCursor(0)
    }

    fun setCursorToEnd() {
        setCursor(text.length)
    }

    override fun keyPressed(keyCode: Int, scanCode: Int, modifiers: Int): Boolean {
        return if (!isActive()) {
            false
        } else {
            selecting = Screen.hasShiftDown()
            if (Screen.isSelectAll(keyCode)) {
                setCursorToEnd()
                this.setSelectionEnd(0)
                true
            } else if (Screen.isCopy(keyCode)) {
                MinecraftClient.getInstance().keyboard.clipboard = getSelectedText()
                true
            } else if (Screen.isPaste(keyCode)) {
                if (editable) {
                    write(MinecraftClient.getInstance().keyboard.clipboard)
                }
                true
            } else if (Screen.isCut(keyCode)) {
                MinecraftClient.getInstance().keyboard.clipboard = getSelectedText()
                if (editable) {
                    write("")
                }
                true
            } else {
                when (keyCode) {
                    259 -> {
                        if (editable) {
                            selecting = false
                            erase(-1)
                            selecting = Screen.hasShiftDown()
                        }
                        true
                    }
                    260, 264, 265, 266, 267 -> false
                    261 -> {
                        if (editable) {
                            selecting = false
                            erase(1)
                            selecting = Screen.hasShiftDown()
                        }
                        true
                    }
                    262 -> {
                        if (Screen.hasControlDown()) {
                            setCursor(this.getWordSkipPosition(1))
                        } else {
                            moveCursor(1)
                        }
                        true
                    }
                    263 -> {
                        if (Screen.hasControlDown()) {
                            setCursor(this.getWordSkipPosition(-1))
                        } else {
                            moveCursor(-1)
                        }
                        true
                    }
                    268 -> {
                        setCursorToStart()
                        true
                    }
                    269 -> {
                        setCursorToEnd()
                        true
                    }
                    else -> false
                }
            }
        }
    }

    fun isActive(): Boolean {
        return this.visible && this.isFocused() && this.isEditable()
    }

    override fun charTyped(chr: Char, keyCode: Int): Boolean {
        return if (!isActive()) {
            false
        } else if (SharedConstants.isValidChar(chr)) {
            if (editable) {
                write(chr.toString())
            }
            true
        } else {
            false
        }
    }

    override fun mouseClicked(mouseX: Double, mouseY: Double, button: Int): Boolean {
        return if (!this.visible) {
            false
        } else {
            val bl = hovered
            if (focusUnlocked) {
                setSelected(bl)
            }
            if (this.isFocused() && bl && button == 0) {
                var i = MathHelper.floor(mouseX) - x
                if (focus) {
                    i -= 4
                }
                val string =
                    textRenderer!!.trimToWidth(text.substring(firstCharacterIndex), getInnerWidth())
                setCursor(textRenderer!!.trimToWidth(string, i).length + firstCharacterIndex)
                true
            } else {
                false
            }
        }
    }

    fun setSelected(selected: Boolean) {
        this.setFocused(selected)
    }

    override fun render(matrices: MatrixStack, mouseX: Int, mouseY: Int, delta: Float) {
        ++focusedTicks
        this.hovered = Render2DUtils.isHovered(mouseX, mouseY, x, y, width, height)
        var j: Int
        if (hasBorder()) {
            j = if (this.isFocused()) Color.TextField.getFocus else Color.TextField.lostFocus
            DrawableHelper.fill(matrices, x, y, x + width, y + height, j)
            DrawableHelper.fill(matrices, x + 1, y + 1, x + width - 1, y + height - 1, Color.TextField.background)
        }
        j = if (editable) editableColor else uneditableColor
        val k = selectionStart - firstCharacterIndex
        var l = selectionEnd - firstCharacterIndex
        val string =
            textRenderer!!.trimToWidth(text.substring(firstCharacterIndex), getInnerWidth())
        val bl = k >= 0 && k <= string.length
        val bl2 = this.isFocused() && focusedTicks / 6 % 2 == 0 && bl
        val m = if (focus) x + 4 else x
        val n = if (focus) y + (height - 8) / 2 else y
        var o = m
        if (l > string.length) {
            l = string.length
        }
        if (string.isNotEmpty()) {
            val string2 = if (bl) string.substring(0, k) else string
            o = textRenderer!!.drawWithShadow(
                matrices,
                renderTextProvider!!.apply(string2, firstCharacterIndex),
                m.toFloat(),
                n.toFloat(),
                j
            )
        }
        val bl3 = selectionStart < text.length || text.length >= getMaxLength()
        var p = o
        if (!bl) {
            p = if (k > 0) m + width else m
        } else if (bl3) {
            p = o - 1
            --o
        }
        if (string.isNotEmpty() && bl && k < string.length) {
            textRenderer!!.drawWithShadow(
                matrices,
                renderTextProvider!!.apply(string.substring(k), selectionStart),
                o.toFloat(),
                n.toFloat(),
                j
            )
        }
        if (!bl3 && suggestion != null) {
            textRenderer!!.drawWithShadow(
                matrices,
                suggestion,
                (p - 1).toFloat(),
                n.toFloat(),
                -8355712
            )
        }
        var var10002: Int
        var var10003: Int
        var var10004: Int
        if (bl2) {
            if (bl3) {
                var10002 = n - 1
                var10003 = p - 1
                var10004 = n + 1
                DrawableHelper.fill(matrices, p, var10002, var10003, var10004 + 9, Color.TextField.cursor)
            } else {
                textRenderer!!.drawWithShadow(matrices, "_", p.toFloat(), n.toFloat(), j)
            }
        }
        if (l != k) {
            val q = m + textRenderer!!.getWidth(string.substring(0, l))
            var10002 = n - 1
            var10003 = q - 1
            var10004 = n + 1
            drawSelectionHighlight(p, var10002, var10003, var10004 + 9)
        }
    }

    private fun drawSelectionHighlight(x1: Int, y1: Int, x2: Int, y2: Int) {
        var x1 = x1
        var y1 = y1
        var x2 = x2
        var y2 = y2
        var j: Int
        if (x1 < x2) {
            j = x1
            x1 = x2
            x2 = j
        }
        if (y1 < y2) {
            j = y1
            y1 = y2
            y2 = j
        }
        if (x2 > x + width) {
            x2 = x + width
        }
        if (x1 > x + width) {
            x1 = x + width
        }
        val tessellator = Tessellator.getInstance()
        val bufferBuilder = tessellator.buffer
        GL11.glColor4f(0.0f, 0.0f, 255.0f, 255.0f)
        RenderSystem.disableTexture()
        RenderSystem.enableColorLogicOp()
        RenderSystem.logicOp(GlStateManager.LogicOp.OR_REVERSE)
        bufferBuilder.begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION)
        bufferBuilder.vertex(x1.toDouble(), y2.toDouble(), 0.0).next()
        bufferBuilder.vertex(x2.toDouble(), y2.toDouble(), 0.0).next()
        bufferBuilder.vertex(x2.toDouble(), y1.toDouble(), 0.0).next()
        bufferBuilder.vertex(x1.toDouble(), y1.toDouble(), 0.0).next()
        tessellator.draw()
        RenderSystem.disableColorLogicOp()
        RenderSystem.enableTexture()
    }

    fun setMaxLength(maximumLength: Int) {
        maxLength = maximumLength
        if (text.length > maximumLength) {
            text = text.substring(0, maximumLength)
            onChanged(text)
        }
    }

    private fun getMaxLength(): Int {
        return maxLength
    }

    fun getCursor(): Int {
        return selectionStart
    }

    private fun hasBorder(): Boolean {
        return focus
    }

    fun setHasBorder(hasBorder: Boolean) {
        focus = hasBorder
    }

    fun setEditableColor(color: Int) {
        editableColor = color
    }

    fun setUneditableColor(color: Int) {
        uneditableColor = color
    }

    override fun changeFocus(lookForwards: Boolean): Boolean {
        return if (visible && editable) super.changeFocus(lookForwards) else false
    }

    override fun isMouseOver(mouseX: Double, mouseY: Double): Boolean {
        return visible && mouseX >= x.toDouble() && mouseX < (x + width).toDouble() && mouseY >= y.toDouble() && mouseY < (y + height).toDouble()
    }

    fun onFocusedChanged(bl: Boolean) {
        if (bl) {
            focusedTicks = 0
        }
    }

    private fun isEditable(): Boolean {
        return editable
    }

    fun setEditable(editable: Boolean) {
        this.editable = editable
    }

    fun getInnerWidth(): Int {
        return if (hasBorder()) width - 8 else width
    }

    fun setSelectionEnd(i: Int) {
        val j = text.length
        selectionEnd = MathHelper.clamp(i, 0, j)
        if (textRenderer != null) {
            if (firstCharacterIndex > j) {
                firstCharacterIndex = j
            }
            val k = getInnerWidth()
            val string = textRenderer!!.trimToWidth(text.substring(firstCharacterIndex), k)
            val l = string.length + firstCharacterIndex
            if (selectionEnd == firstCharacterIndex) {
                firstCharacterIndex -= textRenderer!!.trimToWidth(text, k, true).length
            }
            if (selectionEnd > l) {
                firstCharacterIndex += selectionEnd - l
            } else if (selectionEnd <= firstCharacterIndex) {
                firstCharacterIndex -= firstCharacterIndex - selectionEnd
            }
            firstCharacterIndex = MathHelper.clamp(firstCharacterIndex, 0, j)
        }
    }

    fun setFocusUnlocked(focusUnlocked: Boolean) {
        this.focusUnlocked = focusUnlocked
    }

    fun setSuggestion(suggestion: String) {
        this.suggestion = suggestion
    }

    fun getCharacterX(index: Int): Int {
        return if (index > text.length) x else x + textRenderer!!.getWidth(
            text.substring(
                0,
                index
            )
        )
    }
}