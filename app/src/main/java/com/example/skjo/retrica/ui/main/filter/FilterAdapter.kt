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
    private val onFilterSelected: (FilterItem) -> Unit
) : RecyclerView.Adapter<FilterAdapter.FilterViewHolder>() {

    private val filterItemList = mutableListOf<FilterItem>()

    init {
        filterItemList.clear()
        filterItemList.addAll(FilterItem.getFilters())
        notifyItemRangeChanged(0, filterItemList.size)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FilterViewHolder {
        val binding = ItemFilterBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return FilterViewHolder(binding)
    }

    override fun onBindViewHolder(holder: FilterViewHolder, position: Int) {
        holder.bind(filterItemList[position])
    }

    override fun getItemCount() = filterItemList.size

    inner class FilterViewHolder(private val binding: ItemFilterBinding) : RecyclerView.ViewHolder(binding.root) {
        init {
            itemView.setOnClickListener {
                if (adapterPosition != RecyclerView.NO_POSITION) {
                    onFilterSelected(filterItemList[adapterPosition])
                }
            }
        }

        fun bind(filterItem: FilterItem) {
            binding.tvFilterName.text = context.getString(filterItem.name)
            binding.ivThumbnail.setImageResource(filterItem.thumbnail)
        }
    }
}