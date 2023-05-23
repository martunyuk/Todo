package com.vitaly.todo

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView
import androidx.room.Room
import com.google.android.material.snackbar.Snackbar
import com.vitaly.todo.data.AppDatabase
import com.vitaly.todo.data.Todo
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

class TodoRecyclerAdapter(private val context: Context, private var tasks: List<Todo>, private val recyclerView: RecyclerView) : RecyclerView.Adapter<TodoRecyclerAdapter.ViewHolder>() {

    private val db = Room.databaseBuilder(context, AppDatabase::class.java, "todos").build()
    private val todoDao = db.todoDao()

    private val _itemSizeLiveData = MutableLiveData<Int>()
    fun itemSizeLiveData(): LiveData<Int> {
        return _itemSizeLiveData
    }

    @OptIn(DelicateCoroutinesApi::class)
    @SuppressLint("NotifyDataSetChanged")
    fun update(){
        GlobalScope.launch(Dispatchers.Main) {
            tasks = todoDao.getAllTodos()
            _itemSizeLiveData.value = tasks.size

            ItemTouchHelper(object : ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT or ItemTouchHelper.RIGHT) {
                override fun onMove(
                    recyclerView: RecyclerView,
                    viewHolder: RecyclerView.ViewHolder,
                    target: RecyclerView.ViewHolder
                ): Boolean {
                    return false
                }

                override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                    val position = viewHolder.adapterPosition
                    val deletedTask: Todo = tasks[position]

                    (tasks as MutableList).removeAt(position)
                    GlobalScope.launch(Dispatchers.Main) {
                        todoDao.deleteById(deletedTask.id)
                    }
                    this@TodoRecyclerAdapter.notifyItemRemoved(position)
                    _itemSizeLiveData.value = tasks.size

                    Snackbar.make(recyclerView, "Deleted '${deletedTask.title}' task", Snackbar.LENGTH_LONG)
                        .setActionTextColor(context.getColor(R.color.blue))
                        .setAction("Undo") {
                            (tasks as MutableList<Todo>).add(position, deletedTask)
                            GlobalScope.launch(Dispatchers.Main) {
                                todoDao.insert(deletedTask)
                            }
                            this@TodoRecyclerAdapter.notifyItemInserted(position)
                            _itemSizeLiveData.value = tasks.size
                        }.show()
                }
            }).attachToRecyclerView(recyclerView)

            notifyDataSetChanged()
        }
    }

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val checkbox: CheckBox = itemView.findViewById(R.id.checkbox)
        val title: TextView = itemView.findViewById(R.id.title)
        val cardView: CardView = itemView.findViewById(R.id.cardView)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(LayoutInflater.from(context).inflate(R.layout.todo_item, parent, false))
    }

    override fun getItemCount(): Int {
        return tasks.size
    }

    @OptIn(DelicateCoroutinesApi::class)
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val task: Todo = tasks[position]

        holder.title.text = task.title
        holder.checkbox.isChecked = task.isChecked

        holder.checkbox.setOnCheckedChangeListener { _, isChecked ->
            task.isChecked = isChecked

            GlobalScope.launch(Dispatchers.Main) {
                todoDao.update(task)
            }
        }

        holder.cardView.setOnClickListener { DialogManager.showTaskDialog(context, this, false, task) }

    }

    override fun onViewRecycled(holder: ViewHolder) {
        super.onViewRecycled(holder)
        holder.checkbox.setOnCheckedChangeListener(null)
    }
}