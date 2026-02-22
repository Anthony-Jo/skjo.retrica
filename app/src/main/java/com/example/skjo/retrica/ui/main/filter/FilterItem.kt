package com.example.skjo.retrica.ui.main.filter

import com.example.skjo.retrica.model.FilterData

data class FilterItem(
    val name: Int,
    val type: FilterData,
    val thumbnail: Int
) {
    companion object {
        fun getFilters(): List<FilterItem> {
            return FilterData.entries.map {
                FilterItem(
                    name = it.title,
                    type = it,
                    thumbnail = it.thumbnail
                )
            }
        }
    }
}
