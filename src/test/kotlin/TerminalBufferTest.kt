package org.example

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class TerminalBufferTest {
    private lateinit var buffer: TerminalBuffer

    @BeforeEach
    fun setUp() {
        buffer = TerminalBuffer(10,3,2)
    }

    @Test
    fun `Test of cursor movement`() {
        buffer.write("ABCD")
        val pos = buffer.getCursorPos()
        assertEquals(4, pos.first)
        assertEquals(0, pos.second)
        assertEquals('A', buffer.getCharacter(0, 0))
        assertEquals('B', buffer.getCharacter(1, 0))
        assertEquals('C', buffer.getCharacter(2, 0))
        assertEquals('D', buffer.getCharacter(3, 0))
    }

    @Test
    fun `Boundaries test for cursor`() {
        buffer.setCursor(100, 100)
        val pos = buffer.getCursorPos()
        assertEquals(9, pos.first)
        assertEquals(2, pos.second)

        buffer.setCursor(-10, -10)
        val posZero = buffer.getCursorPos()
        assertEquals(0, posZero.first)
        assertEquals(0, posZero.second)


        buffer.setCursor(5, 1)
        buffer.moveCursor(10, 0)
        assertEquals(9, buffer.getCursorPos().first)

        buffer.moveCursor(-20, 0)
        assertEquals(0, buffer.getCursorPos().first)
    }

    @Test
    fun `Test of line wrapping`(){
        buffer.write("0123456789ABCDEF")
        assertEquals(6, buffer.getCursorPos().first)
        assertEquals(1, buffer.getCursorPos().second)
        assertEquals('A', buffer.getCharacter(0, 1))
        assertEquals('B', buffer.getCharacter(1, 1))
        assertEquals('C', buffer.getCharacter(2, 1))
    }

    @Test
    fun `Test of insert operation`() {
        buffer.write("ABCDEF")
        buffer.setCursor(2,0)
        buffer.insert("123")
        val line = buffer.getLineAsString(0)
        assertEquals("AB123CDEF ", line)
    }

    @Test
    fun `Insert text with full line`() {
        buffer.write("ABCDEFGHIJ")
        buffer.setCursor(0, 0)
        buffer.insert("1")

        val line = buffer.getLineAsString(0)
        assertEquals("1ABCDEFGHI", line)
    }

    @Test
    fun `Scrollback test`() {
        buffer.write("Line 1\n")
        buffer.write("Line 2\n")
        buffer.write("Line 3\n")
        buffer.write("Line 4\n")
        buffer.write("Line 5\n")

        assertTrue(buffer.getLineAsString(0).contains("Line 4"))
        assertTrue(buffer.getLineAsString(-1).contains("Line 3"))
        assertTrue(buffer.getLineAsString(-2).contains("Line 2"))
        assertFalse(buffer.getLineAsString(-3).contains("Line 1"))
    }

    @Test
    fun `Clear all test`(){
        buffer.write("Line 1\n")
        buffer.write("Line 2\n")
        buffer.write("Line 3\n")
        buffer.write("Line 4\n")
        buffer.clearAll()
        assertEquals(0, buffer.getCursorPos().first)
        assertEquals(0, buffer.getCursorPos().second)
        assertEquals(' ', buffer.getCharacter(0, 0))
        assertNull(buffer.getCell(0, -1))
    }

    @Test
    fun `Test of attributes`() {
        buffer.addStyle(TerminalBuffer.TerminalStyle.ITALIC)
        buffer.setBackgroundColor(11)
        buffer.write("ABCD")
        val cell = buffer.getCell(1, 0)
        assertEquals(11.toShort(), cell?.backgroundColor)
        assertTrue(cell?.style?.contains(TerminalBuffer.TerminalStyle.ITALIC) == true)

        buffer.resetAttributes()
        buffer.write("E")
        val cellE = buffer.getCell(4, 0)
        assertEquals((-1).toShort(), cellE?.backgroundColor)
        assertTrue(cellE?.style?.isEmpty() == true)
    }


}