package com.example.videoplayer

import android.content.ContentUris
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.CodeBoy.MediaFacer.MediaFacer
import com.CodeBoy.MediaFacer.VideoGet
import com.CodeBoy.MediaFacer.mediaHolders.videoContent
class MainActivity : AppCompatActivity(),RecyclerAdapter.onItemClickListener {
    lateinit var videoContents: ArrayList<videoContent>
    val permissionOfExternalStorage = arrayOf<String>(android.Manifest.permission.READ_EXTERNAL_STORAGE)
    val EXTERNAL_STORAGE_PERMISSION_REQ_CODE = 2
    lateinit var recyclerView:RecyclerView
    lateinit var recyclerAdapter:RecyclerAdapter
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        checkForPermission();
    }

    private fun checkForPermission() {
        if (ActivityCompat.checkSelfPermission(
                this,
                permissionOfExternalStorage[0]
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            init();
        } else {
            ActivityCompat.requestPermissions(
                this@MainActivity,
                permissionOfExternalStorage,
                EXTERNAL_STORAGE_PERMISSION_REQ_CODE
            )
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == EXTERNAL_STORAGE_PERMISSION_REQ_CODE) {
            if (grantResults[0] == RESULT_OK) {
                init()
            } else {
                ActivityCompat.requestPermissions(
                    this@MainActivity,
                    permissionOfExternalStorage,
                    EXTERNAL_STORAGE_PERMISSION_REQ_CODE
                )
            }
        }
    }

    private fun init() {
        videoContents = MediaFacer.withVideoContex(this).getAllVideoContent(VideoGet.externalContentUri)


        recyclerView = findViewById(R.id.recycler_view)
        recyclerAdapter= RecyclerAdapter(this@MainActivity,videoContents)

        val layoutManager: RecyclerView.LayoutManager = LinearLayoutManager(this)
        recyclerView.layoutManager = layoutManager
        recyclerView.adapter = recyclerAdapter
        recyclerAdapter.setOnItemClickListener(this)

    }

    override fun onItemClick(video: videoContent) {
        val uri:Uri = ContentUris.withAppendedId(VideoGet.externalContentUri,video.videoId)
        val intent:Intent = Intent(this,VideoPlayerActivity::class.java)
            .putExtra(R.string.EXTRA_VIDEO_KEY.toString(),uri.toString())
        startActivity(intent)

    }
}