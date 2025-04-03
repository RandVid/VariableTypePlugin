package org.randvid.variabletypeplugin

import com.intellij.openapi.editor.Editor
import com.intellij.openapi.editor.event.CaretListener
import com.intellij.openapi.fileEditor.FileEditorManager
import com.intellij.openapi.project.Project
import com.intellij.openapi.wm.StatusBar
import com.intellij.openapi.wm.StatusBarWidget
import com.intellij.openapi.wm.impl.status.TextPanel
import com.jetbrains.python.psi.PyExpression
import com.jetbrains.python.psi.types.PyType
import com.jetbrains.python.psi.types.TypeEvalContext
import java.util.concurrent.atomic.AtomicReference

class VariableTypeWidget(private val project: Project) : StatusBarWidget, CaretListener {
    private val currentType = AtomicReference("TEST")
    private val textPanel = TextPanel().apply {
        isOpaque = false
    }

    init {
        println("[DEBUG] Widget created")
    }

    override fun ID(): String = "VariableTypeWidget"

    override fun getPresentation() = object : StatusBarWidget.TextPresentation {
        override fun getTooltipText(): String = "Variable type under caret"
        override fun getText(): String = currentType.get()
        override fun getAlignment(): Float = 1f
    }

    fun updateType(editor: Editor?) {
        println("[DEBUG] updateType called")
        if (editor == null || project.isDisposed) {
            currentType.set("")
            return
        }

        val offset = editor.caretModel.offset
        val file = com.intellij.psi.util.PsiUtilBase.getPsiFileInEditor(editor, project) ?: run {
            currentType.set("")
            return
        }

        val element = file.findElementAt(offset) ?: run {
            currentType.set("")
            return
        }

        val expression = element.parent as? PyExpression ?: run {
            currentType.set("")
            return
        }

        val type: PyType? = TypeEvalContext.codeAnalysis(project, file).getType(expression)
        println("[DEBUG] Detected type: ${type?.name} for expression:")

        currentType.set(type?.name ?: "Any")
        textPanel.text = currentType.get()
    }

    override fun install(statusBar: StatusBar) {
        val editor = FileEditorManager.getInstance(project).selectedTextEditor
        editor?.caretModel?.addCaretListener(VariableTypeCaretListener())
    }
}