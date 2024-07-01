package com.example.gallerysweeper.adapters

import android.content.Context
import android.media.tv.TvTrackInfo.TYPE_VIDEO
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.MediaController
import android.widget.VideoView
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.exoplayer.drm.ExoMediaDrm
import androidx.media3.ui.PlayerView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.gallerysweeper.R
import com.example.gallerysweeper.data.MediaItem


class CardViewAdapter(val context: Context) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    var list: List<MediaItem> = listOf()

    companion object {
        private const val TYPE_IMAGE = 0
        private const val TYPE_VIDEO = 1
    }

    inner class ImageViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val img: ImageView = itemView.findViewById(R.id.item_card_img)
        init {
            itemView.background = null
        }
    }

    inner class VideoViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        val playerView: PlayerView = itemView.findViewById(R.id.item_card_video)
        val player: ExoPlayer = ExoPlayer.Builder(context).build().apply {
            playerView.player = this
        }

        init {
            itemView.background = null
        }

    }

    override fun getItemViewType(position: Int): Int {
        return if (list[position].isVideo) {
            TYPE_VIDEO
        } else {
            TYPE_IMAGE
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        return when (viewType) {
            TYPE_VIDEO -> {
                val view = inflater.inflate(R.layout.item_card_video, parent, false)
                VideoViewHolder(view)
            }
            else -> {
                val view = inflater.inflate(R.layout.item_card, parent, false)
                ImageViewHolder(view)
            }
        }
    }

    override fun getItemCount(): Int {
        return list.size
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val currentItem = list[position]
        when (holder) {
            is ImageViewHolder -> {
                Glide.with(holder.img.context)
                    .load(currentItem.uri)
                    .into(holder.img)
            }
            is VideoViewHolder -> {
                val mediaItem = androidx.media3.common.MediaItem.fromUri(currentItem.uri)
                holder.player.setMediaItem(mediaItem)
                holder.player.prepare()
                holder.player.playWhenReady = false

            }
        }
    }

    override fun onViewRecycled(holder: RecyclerView.ViewHolder) {
        if (holder is VideoViewHolder) {
            holder.player.release()
        }
        super.onViewRecycled(holder)
    }

    fun setData(list: List<MediaItem>) {
        this.list = list
        notifyDataSetChanged()
    }

    fun getItem(position: Int): MediaItem = list[position]
}