package org.randvid.variabletypeplugin

import com.intellij.openapi.editor.Editor
import com.intellij.openapi.editor.event.CaretListener
import com.intellij.openapi.fileEditor.FileEditorManager
import com.intellij.openapi.project.Project
import com.intellij.openapi.wm.StatusBar
import com.intellij.openapi.wm.StatusBarWidget
import com.jetbrains.python.psi.PyExpression
import com.jetbrains.python.psi.PyReferenceExpression
import com.jetbrains.python.psi.PyTargetExpression
import com.jetbrains.python.psi.types.PyType
import com.jetbrains.python.psi.types.TypeEvalContext
import java.util.concurrent.atomic.AtomicReference

class VariableTypeWidget(private val project: Project) : StatusBarWidget, CaretListener {
    // AtomicReference is used to ensure thread-safe updates of displayed type
    private val currentType = AtomicReference("")
    private var caretListener: VariableTypeCaretListener? = null

    override fun ID(): String = "VariableTypeWidget"

    override fun getPresentation() = object : StatusBarWidget.TextPresentation {
        override fun getTooltipText(): String = "Variable type under caret"
        override fun getText(): String = currentType.get()
        override fun getAlignment(): Float = 1f // Align the widget to the right
    }

    /**
     * updateType
     * Updates the currentType to the type of variable the caret at
     * Parameters:
     * editor: Editor? - The active editor, or `null` if no file is open.
     */
    fun updateType(editor: Editor?) {
        if (editor == null || project.isDisposed) {
            currentType.set("")
            return
        }

        val file = com.intellij.psi.util.PsiUtilBase.getPsiFileInEditor(editor, project) ?: run {
            currentType.set("")
            return
        }

        val offset = editor.caretModel.offset
        val element = file.findElementAt(offset) ?: run {
            currentType.set("")
            return
        }

        val expression = element.parent as? PyExpression ?: run {
            currentType.set("")
            return
        }

        // if the end user wants some non-variable expression types to be shown
        // The following if-statement may be deleted or overwritten
        if (!(expression is PyTargetExpression || expression is PyReferenceExpression)) {
            currentType.set("") // Ignore non-variable expressions.
            return
        }

        val type: PyType? = TypeEvalContext.codeAnalysis(project, file).getType(expression)

        currentType.set(type?.name ?: "Any")
    }

    override fun install(statusBar: StatusBar) {
        val editor = FileEditorManager.getInstance(project).selectedTextEditor
        caretListener = VariableTypeCaretListener()
        editor?.caretModel?.addCaretListener(caretListener!!)
    }

    override fun dispose() {
        // Clean up to prevent memory leaks.
        val editor = FileEditorManager.getInstance(project).selectedTextEditor
        caretListener?.let { listener ->
            editor?.caretModel?.removeCaretListener(listener)
        }
    }
}