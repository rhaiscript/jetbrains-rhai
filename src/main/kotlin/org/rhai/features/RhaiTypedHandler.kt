package org.rhai.features

import com.intellij.codeInsight.editorActions.TypedHandlerDelegate
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiFile
import org.rhai.lang.RhaiFileType

class RhaiTypedHandler : TypedHandlerDelegate() {

    override fun charTyped(c: Char, project: Project, editor: Editor, file: PsiFile): Result {
        if (file.fileType != RhaiFileType.INSTANCE) {
            return Result.CONTINUE
        }

        // When user types '{' after '$' in an interpolated string, add closing '}'
        if (c == '{') {
            val offset = editor.caretModel.offset
            val document = editor.document
            val text = document.charsSequence

            // Check if there's a '$' before the '{'
            if (offset >= 2 && text[offset - 2] == '$') {
                // Check if we're in an interpolated string context
                if (isInInterpolatedString(text, offset - 2)) {
                    document.insertString(offset, "}")
                    return Result.STOP
                }
            }
        }

        return Result.CONTINUE
    }

    private fun isInInterpolatedString(text: CharSequence, offset: Int): Boolean {
        // Count backticks before this position
        // If odd number of backticks, we're inside an interpolated string
        var backtickCount = 0
        var i = 0
        while (i < offset) {
            val ch = text[i]
            if (ch == '`') {
                // Check if escaped
                var backslashCount = 0
                var j = i - 1
                while (j >= 0 && text[j] == '\\') {
                    backslashCount++
                    j--
                }
                if (backslashCount % 2 == 0) {
                    backtickCount++
                }
            }
            i++
        }
        return backtickCount % 2 == 1
    }
}
