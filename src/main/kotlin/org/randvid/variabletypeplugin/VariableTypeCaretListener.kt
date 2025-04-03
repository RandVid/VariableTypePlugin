package org.randvid.variabletypeplugin

import com.intellij.openapi.editor.event.CaretEvent
import com.intellij.openapi.editor.event.CaretListener
import com.intellij.openapi.wm.WindowManager

class VariableTypeCaretListener : CaretListener {
    override fun caretPositionChanged(event: CaretEvent) {
        println("[DEBUG] Caret moved to offset: ${event.editor.caretModel.offset}")

        val project = event.editor.project ?: return
        val statusBar = WindowManager.getInstance().getStatusBar(project) ?: return

        (statusBar.getWidget("VariableTypeWidget") as? VariableTypeWidget)?.let { widget ->
            widget.updateType(event.editor)
            statusBar.updateWidget("VariableTypeWidget")
        }
    }
}