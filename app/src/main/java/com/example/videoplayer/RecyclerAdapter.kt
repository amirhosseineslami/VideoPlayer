package com.example.videoplayer

import android.content.ContentUris
import android.content.Context
import android.net.Uri
import android.os.Build
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.annotation.RequiresApi
import androidx.recyclerview.widget.RecyclerView
import com.CodeBoy.MediaFacer.VideoGet
import com.CodeBoy.MediaFacer.mediaHolders.videoContent
import com.bumptech.glide.Glide
import java.util.*
import java.util.concurrent.TimeUnit

class RecyclerAdapter(var context: Context, var videoContents: List<videoContent>) : RecyclerView.Adapter<RecyclerAdapter.Holder>() {
lateinit var setOnItemClickListener: onItemClickListener
    interface onItemClickListener{
        fun onItemClick(video:videoContent)
    }


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): Holder {
        val rootView: View =
            LayoutInflater.from(context).inflate(R.layout.item_video, parent, false)
        return Holder(rootView)
    }

    @RequiresApi(Build.VERSION_CODES.Q)
    override fun onBindViewHolder(holder: Holder, position: Int) {
        val videoContent:videoContent = videoContents[position]
        var videoName:String = videoContent.videoName.substring(0,videoContent.videoName.length-4)
        holder.nameTextView.text = videoName
        holder.durationTextView.text = longToTime(videoContent.videoDuration)
        val uri:Uri = ContentUris.withAppendedId(VideoGet.externalContentUri,videoContent.videoId)

        holder.imageView?.let {
            Glide.with(context)
                .load(uri)
                .error(android.R.drawable.stat_notify_error)
                .into(it)
        }

        //val bitMap:Bitmap = context.contentResolver.loadThumbnail(uri, Size(R.dimen.item_imageView_size,R.dimen.item_imageView_size),null)
        //holder.imageView?.setImageBitmap(bitMap)
    }
    override fun getItemCount(): Int {
        return videoContents.size
    }

    inner class Holder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var nameTextView: TextView
        var durationTextView: TextView
        var imageView: ImageView? = null

        init {
            nameTextView = itemView.findViewById(R.id.textView_itemName)
            imageView = itemView.findViewById(R.id.imageView_item)
            durationTextView = itemView.findViewById(R.id.textView_itemDuration)
                itemView.setOnClickListener(View.OnClickListener {
                    setOnItemClickListener.onItemClick(videoContents[adapterPosition])
                })

        }
    }

    private fun longToTime(duration: Long):String{

        return String.format(Locale.ENGLISH,"%02d:%02d:%02d",
            TimeUnit.MILLISECONDS.toHours(duration),
            TimeUnit.MILLISECONDS.toMinutes(duration)-TimeUnit.HOURS.toMinutes(TimeUnit.MILLISECONDS.toHours(duration)),
            TimeUnit.MILLISECONDS.toSeconds(duration)-TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(duration))
        )
    }
   public fun setOnItemClickListener(setOnItemClickListener:onItemClickListener){
       this.setOnItemClickListener = setOnItemClickListener
   }
}