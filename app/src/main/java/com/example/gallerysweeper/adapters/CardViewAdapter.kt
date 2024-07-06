package com.example.gallerysweeper.adapters

import android.content.Context
import android.media.tv.TvTrackInfo.TYPE_VIDEO
import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.MediaController
import android.widget.VideoView
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.exoplayer.drm.ExoMediaDrm
import androidx.media3.ui.PlayerView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.gallerysweeper.R
import com.example.gallerysweeper.data.MediaItem


class CardViewAdapter(private val context: Context) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    var list: List<MediaItem> = listOf()
    private var playingVideoPosition: Int = RecyclerView.NO_POSITION


    companion object {
        private const val TYPE_IMAGE = 0
        private const val TYPE_VIDEO = 1
    }

    inner class ImageViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val img: ImageView = itemView.findViewById(R.id.item_card_img)
//        init {
//            itemView.background = null
//        }
    }

    inner class VideoViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val playerView: PlayerView = itemView.findViewById(R.id.item_card_video)
        var player: ExoPlayer? = null


        fun initializePlayer(uri: Uri) {
            if (player == null) {
                player = ExoPlayer.Builder(context).build().apply {
                    playerView.player = this
                    setMediaItem(androidx.media3.common.MediaItem.fromUri(uri))
                    prepare()
                    playWhenReady = false
                }
            }
        }


        fun releasePlayer() {
            player?.release()
            player = null
            playerView.player = null
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
            TYPE_IMAGE -> {
                val view = inflater.inflate(R.layout.item_card, parent, false)
                ImageViewHolder(view)
            }
            else -> {
                val view = inflater.inflate(R.layout.item_card_video, parent, false)
                VideoViewHolder(view)
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
                holder.img.setImageDrawable(null)
                Glide.with(holder.img.context)
                    .load(currentItem.uri)
                    .into(holder.img)
            }
            is VideoViewHolder -> {
                holder.playerView.visibility = View.VISIBLE
                }


            }
        }



    override fun onViewRecycled(holder: RecyclerView.ViewHolder) {
        when (holder) {
            is VideoViewHolder -> {
                holder.releasePlayer()
                holder.playerView.visibility = View.GONE
            }
            is ImageViewHolder -> {
                holder.img.setImageDrawable(null)
            }
        }
        super.onViewRecycled(holder)
    }

    fun initializeVideoIfNeeded(holder: RecyclerView.ViewHolder, position: Int) {
        if (holder is VideoViewHolder && list[position].isVideo) {
            holder.initializePlayer(list[position].uri)
        }
    }

    fun releaseVideoIfNeeded(holder: RecyclerView.ViewHolder) {
        if (holder is VideoViewHolder) {
            holder.releasePlayer()
        }
    }

    fun setData(list: List<MediaItem>) {
        this.list = list
        notifyDataSetChanged()
    }

    fun getItem(position: Int): MediaItem = list[position]
}