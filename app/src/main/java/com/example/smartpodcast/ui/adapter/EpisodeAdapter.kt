package com.example.smartpodcast.ui.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.core.text.HtmlCompat
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.smartpodcast.R
import com.example.smartpodcast.data.local.EpisodeEntity

class EpisodeAdapter(
    private val onItemClick: (EpisodeEntity) -> Unit,
    private val onDownloadClick: (EpisodeEntity) -> Unit
) : RecyclerView.Adapter<EpisodeAdapter.EpisodeViewHolder>() {

    private var episodes: List<EpisodeEntity> = emptyList()

    fun updateData(newList: List<EpisodeEntity>) {
        this.episodes = newList
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): EpisodeViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_episode, parent, false)
        return EpisodeViewHolder(view)
    }

    override fun onBindViewHolder(holder: EpisodeViewHolder, position: Int) {
        val episode = episodes[position]
        holder.bind(episode)
        holder.itemView.setOnClickListener { onItemClick(episode) }
        holder.btnDownload.setOnClickListener { onDownloadClick(episode) }
    }

    override fun getItemCount(): Int = episodes.size

    class EpisodeViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        private val title: TextView = view.findViewById(R.id.tvTitle)
        private val date: TextView = view.findViewById(R.id.tvPubDate)
        // private val desc: TextView = view.findViewById(R.id.tvDescription)
        private val image: ImageView = view.findViewById(R.id.imgPodcast)
        val btnDownload: ImageView = view.findViewById(R.id.btnDownload)

        fun bind(episode: EpisodeEntity) {
            title.text = episode.title
            date.text = episode.pubDate
            // desc.text = HtmlCompat.fromHtml(episode.description, HtmlCompat.FROM_HTML_MODE_LEGACY)
            
            if (episode.isDownloaded) {
                btnDownload.setImageResource(android.R.drawable.stat_sys_download_done)
                btnDownload.setColorFilter(android.graphics.Color.GREEN)
            } else {
                btnDownload.setImageResource(android.R.drawable.stat_sys_download)
                btnDownload.setColorFilter(android.graphics.Color.parseColor("#6441A5"))
            }

            Glide.with(itemView.context).load(episode.imageUrl).into(image)
        }
    }
}