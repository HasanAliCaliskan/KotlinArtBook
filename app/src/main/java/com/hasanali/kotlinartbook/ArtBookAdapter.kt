package com.hasanali.kotlinartbook

import android.content.Intent
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.hasanali.kotlinartbook.databinding.RecyclerRowBinding

class ArtBookAdapter(val artList: ArrayList<Art>): RecyclerView.Adapter<ArtBookAdapter.ArtBookHolder>() {

    class ArtBookHolder(val binding: RecyclerRowBinding): RecyclerView.ViewHolder(binding.root) {
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ArtBookHolder {
        val binding = RecyclerRowBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ArtBookHolder(binding)
    }

    override fun onBindViewHolder(holder: ArtBookHolder, position: Int) {
        holder.binding.recyclerViewTextView.text = artList[position].artName
        holder.itemView.setOnClickListener {
            val intent = Intent(holder.itemView.context, ArtActivity::class.java)
            intent.putExtra("id", artList[position].id)
            intent.putExtra("info", "old")
            holder.itemView.context.startActivity(intent)
        }
    }

    override fun getItemCount(): Int {
        return artList.size
    }
}