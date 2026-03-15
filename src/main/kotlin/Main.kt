package org.example

import java.util.EnumSet

enum class TerminalStyle {
    BOLD, ITALIC, UNDERLINE
}

data class Cell(
    var character: Char = ' ',
    var foregroundColor: Short = -1,
    var backgroundColor: Short = -1,
    var style: EnumSet<TerminalStyle> = EnumSet.noneOf(TerminalStyle::class.java)
)

class TerminalBuffer (
    val width: Int,
    val height: Int,
    val scrollbackMax: Int
) {
    private var cursorX: Int = 0
    private var cursorY: Int = 0

    private val screenContent: Array<Array<Cell>> = Array(height) { Array(width) { Cell() } }
    private val scrollback: MutableList<Array<Cell>> = mutableListOf()

}