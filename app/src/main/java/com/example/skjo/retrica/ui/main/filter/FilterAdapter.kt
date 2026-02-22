package com.example.skjo.retrica.ui.main.filter

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.skjo.retrica.databinding.ItemFilterBinding

/**
 * Camera Filter 변경 UI RecyclerView Adapter.
 */
class FilterAdapter(
    private val context: Context,
    private val onItemClicked: (Int) -> Unit
) : RecyclerView.Adapter<FilterAdapter.FilterViewHolder>() {

    private val filterItems = FilterItem.getFilters()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FilterViewHolder {
        val binding = ItemFilterBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return FilterViewHolder(binding)
    }

    override fun onBindViewHolder(holder: FilterViewHolder, position: Int) {
        val actualPosition = position % filterItems.size
        holder.bind(filterItems[actualPosition])
    }

    override fun getItemCount() = Integer.MAX_VALUE

    fun getFilterItemAt(position: Int): FilterItem {
        val actualPosition = position % filterItems.size
        return filterItems[actualPosition]
    }

    inner class FilterViewHolder(private val binding: ItemFilterBinding) : RecyclerView.ViewHolder(binding.root) {
        init {
            itemView.setOnClickListener {
                if (adapterPosition != RecyclerView.NO_POSITION) {
                    onItemClicked(adapterPosition)
                }
            }
        }

        fun bind(filterItem: FilterItem) {
            binding.tvFilterName.text = context.getString(filterItem.name)
            binding.ivThumbnail.setImageResource(filterItem.thumbnail)
        }
    }
}
