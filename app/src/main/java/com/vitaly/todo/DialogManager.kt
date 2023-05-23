package com.vitaly.todo

import android.app.Dialog
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.view.Gravity
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import androidx.room.Room
import com.vitaly.todo.data.AppDatabase
import com.vitaly.todo.data.Todo
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch


object DialogManager {

    @OptIn(DelicateCoroutinesApi::class)
    fun showTaskDialog(context: Context, recyclerAdapter: TodoRecyclerAdapter, isAdd: Boolean, editTask: Todo?){ // add or edit task

        val db = Room.databaseBuilder(context, AppDatabase::class.java, "todos").build()
        val todoDao = db.todoDao()

        val dialog = Dialog(context)
        dialog.setContentView(R.layout.task_dialog)
        dialog.window!!.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
        dialog.window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        dialog.window!!.setGravity(Gravity.BOTTOM)

        val title: TextView = dialog.findViewById(R.id.title)
        val editText: EditText = dialog.findViewById(R.id.editText)
        val dialogBtn: Button = dialog.findViewById(R.id.dialogBtn)

        if (!isAdd)
            editText.setText(editTask!!.title)

        // show keyboard
        editText.requestFocus()
        val imm = context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager?
        editText.postDelayed({
            editText.requestFocus()
            imm!!.showSoftInput(editText, 0)
        }, 100)

        title.text = context.getText(if(isAdd) R.string.add_a_new_task else R.string.edit_the_task)
        dialogBtn.text = context.getText(if(isAdd) R.string.add else R.string.edit)

        dialogBtn.setOnClickListener {
            if (editText.text.isNotEmpty()) {
                GlobalScope.launch(Dispatchers.Main) {
                    if (isAdd) todoDao.insert(Todo(title = editText.text.toString(), isChecked = false))
                    else if (editText.text.toString() != editTask!!.title) todoDao.update(Todo(editTask.id ,editText.text.toString(), editTask.isChecked))

                    recyclerAdapter.update()
                    dialog.dismiss()
                }
            }
        }

        dialog.show()
    }

}