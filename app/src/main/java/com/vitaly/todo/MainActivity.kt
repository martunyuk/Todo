package com.vitaly.todo

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.vitaly.todo.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    @SuppressLint("NotifyDataSetChanged")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.recyclerView.layoutManager = LinearLayoutManager(this)
        val recyclerAdapter = TodoRecyclerAdapter(this, listOf(), binding.recyclerView)
        binding.recyclerView.adapter = recyclerAdapter
        recyclerAdapter.update()

        recyclerAdapter.itemSizeLiveData().observe(this){
            binding.emptyText.visibility = if (it == 0) View.VISIBLE else View.GONE
        }

        binding.addBtn.setOnClickListener { DialogManager.showTaskDialog(this, recyclerAdapter, true, null) }

    }
}