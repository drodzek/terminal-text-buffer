package org.example

import java.util.EnumSet

class TerminalBuffer (
    val width: Int,
    val height: Int,
    val scrollbackMax: Int
) {
    enum class TerminalStyle {
        BOLD, ITALIC, UNDERLINE
    }

    data class Cell(
        var character: Char = ' ',
        var foregroundColor: Short = -1,
        var backgroundColor: Short = -1,
        var style: EnumSet<TerminalStyle> = EnumSet.noneOf(TerminalStyle::class.java)
    ) {
        fun reset() {
            character = ' '
            foregroundColor = -1
            backgroundColor = -1
            style.clear()
        }
    }
    private var topIndex = 0
    private var cursorX = 0
    private var cursorY = 0

    private val screenContent: Array<Array<Cell>> = Array(height) { Array(width) { Cell() } }
    private val scrollbackContent: MutableList<Array<Cell>> = mutableListOf()

    private var currForegroundColor: Short = -1
    private var currBackgroundColor: Short = -1
    private var currStyle: EnumSet<TerminalStyle> = EnumSet.noneOf(TerminalStyle::class.java)

    // ================================================
    // Attribute manipulation
    fun setForegroundColor(color: Short) {
        currForegroundColor = color
    }

    fun setBackgroundColor(color: Short) {
        currBackgroundColor = color
    }

    fun addStyle(style: TerminalStyle) {
        currStyle.add(style)
    }

    fun resetAttributes() {
        currForegroundColor = -1
        currBackgroundColor = -1
        currStyle.clear()
    }

    // ================================================
    // Cursor manipulation
    fun getCursorPos(): Pair<Int,Int> {
        return Pair(cursorX, cursorY)
    }

    fun setCursor(x: Int, y: Int) {
        cursorX = x.coerceIn(0, width - 1)
        cursorY = y.coerceIn(0, height - 1)
    }

    fun moveCursor(x: Int, y: Int) {
        setCursor(cursorX + x, cursorY + y)
    }

    private fun getPhysicalY(logicalY: Int): Int {
        return (topIndex + logicalY) % height
    }

    // ================================================
    // Editing
    fun newLine() {
        cursorX = 0
        if (cursorY < height - 1) {
            cursorY++
        }
        else {
            // Cyclic buffer handling
            val physicalTopIndex = topIndex
            val rowScrollId = screenContent[physicalTopIndex]

            val rowToHistory = rowScrollId.map { cell->
                cell.copy(style = EnumSet.copyOf(cell.style))
            }.toTypedArray()
            scrollbackContent.add(rowToHistory)

            if (scrollbackContent.size > scrollbackMax) {
                scrollbackContent.removeAt(0)
            }

            for (cell in rowScrollId) {
                cell.reset()
            }

            topIndex = (topIndex + 1) % height
        }
    }

    private fun moveCursorForward() {
        cursorX++
        if (cursorX >= width) {
            newLine()
        }
    }

    fun fillCurrentLine(char: Char = ' ') {
        val physY = getPhysicalY(cursorY)
        for (cell in screenContent[physY]) {
            cell.character = char
            cell.foregroundColor = currForegroundColor
            cell.backgroundColor = currBackgroundColor
            cell.style = EnumSet.copyOf(currStyle)
        }
    }

    fun write(text: String) {
        for (char in text) {
            if (char == '\n') {
                newLine()
            }
            else {
                val physicalY = getPhysicalY(cursorY)
                val cell = screenContent[physicalY][cursorX]
                cell.character = char
                cell.foregroundColor = currForegroundColor
                cell.backgroundColor = currBackgroundColor
                cell.style = EnumSet.copyOf(currStyle)
                moveCursorForward()
            }
        }
    }

    fun insert(text: String) {
        for (char in text) {
            if (char == '\n') {
                newLine()
            }
            else {
                val physicalY = getPhysicalY(cursorY)
                val row = screenContent[physicalY]

                for (x in (width - 1) downTo (cursorX + 1)) {
                    val current = row[x]
                    val prev = row[x - 1]
                    current.character = prev.character
                    current.foregroundColor = prev.foregroundColor
                    current.backgroundColor = prev.backgroundColor
                    current.style = EnumSet.copyOf(prev.style)
                }

                val cell = row[cursorX]
                cell.character = char
                cell.foregroundColor = currForegroundColor
                cell.backgroundColor = currBackgroundColor
                cell.style = EnumSet.copyOf(currStyle)

                moveCursorForward()
            }
        }
    }

    fun clearScreen() {
        for (row in screenContent) {
            for (cell in row) {
                cell.reset()
            }
        }
        cursorX = 0
        cursorY = 0
    }

    fun clearAll(){
        scrollbackContent.clear()
        clearScreen()
        topIndex = 0
    }

    fun insertEmptyLine() {
        var oldX = cursorX
        var oldY = cursorY
        cursorY = height - 1
        newLine()
        cursorX = oldX.coerceIn(0, width - 1)
        cursorY = oldY.coerceIn(0, height - 1)
    }
}