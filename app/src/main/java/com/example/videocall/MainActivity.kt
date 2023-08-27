package com.example.videocall
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.SurfaceView
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.example.videocall.databinding.ActivityMainBinding
import io.agora.rtc2.ChannelMediaOptions
import io.agora.rtc2.Constants
import io.agora.rtc2.IRtcEngineEventHandler
import io.agora.rtc2.RtcEngine
import io.agora.rtc2.RtcEngineConfig
import io.agora.rtc2.video.VideoCanvas


class MainActivity : AppCompatActivity() {
    lateinit var binding: ActivityMainBinding

    private val appId = "022b2df540de45e1b58fac346268b1b5"
    private val channelName = "asifrezan"
    private val token = "007eJxTYNC8Jrhj31xV+1O2RuIXAywCnjmpb+vVUrO0nV28O86gPUKBwcDIKMkoJc3UxCAl1cQ01TDJ1CItMdnYxMzIzCIJyEuQfJ3SEMjI0KZjxMjIAIEgPidDYnFmWlFqVWIeAwMA/1EeIw=="
    private val uid = 0

    private var isJoined = false
    private var agoraEngine: RtcEngine? = null
    private var localSurfacreView: SurfaceView?= null
    private var remoteSurfacreView: SurfaceView?= null


    private val PERMISSION_ID = 12
    private val REQUESTED_PERMISSION =
        arrayOf(
            android.Manifest.permission.RECORD_AUDIO,
            android.Manifest.permission.CAMERA
        )

    private fun checkSelfPermission() : Boolean
    {
        return !(ContextCompat.checkSelfPermission(
            this, REQUESTED_PERMISSION[0]
        )!=PackageManager.PERMISSION_GRANTED || ContextCompat.checkSelfPermission(
            this, REQUESTED_PERMISSION[1]
        )!= PackageManager.PERMISSION_GRANTED)
    }

    private fun showMessage(message:String)
    {
        runOnUiThread{
            Toast.makeText(this,message,Toast.LENGTH_SHORT).show()
        }

    }

    private fun setupVideoSDKEngine() {
        try {
            val config = RtcEngineConfig()
            config.mContext = baseContext
            config.mAppId = appId
            config.mEventHandler = mRtcEventHandler
            agoraEngine = RtcEngine.create(config)
            agoraEngine!!.enableVideo()
        } catch (e: Exception) {
            showMessage(e.toString())
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        if (!checkSelfPermission())
        {
            ActivityCompat.requestPermissions(
                this,REQUESTED_PERMISSION,PERMISSION_ID
            )
        }
        setupVideoSDKEngine()

        binding.JoinButton.setOnClickListener {
            joinCall()
        }
        binding.LeaveButton.setOnClickListener {
            leaveCall()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        agoraEngine!!.stopPreview()
        agoraEngine!!.leaveChannel()

        Thread {
            RtcEngine.destroy()
            agoraEngine = null
        }.start()
    }



    private fun leaveCall() {

        if (!isJoined)
        {
            showMessage("Join a channel first")
        }
        else
        {
            agoraEngine!!.leaveChannel()
            showMessage("You left the channel")
            if (remoteSurfacreView!=null) remoteSurfacreView!!.visibility = View.GONE
            if (localSurfacreView!=null) localSurfacreView!!.visibility = View.GONE
            isJoined = false
        }


    }

    private fun joinCall() {
      if (checkSelfPermission())
      {
           val option = ChannelMediaOptions()
          option.channelProfile = Constants.CHANNEL_PROFILE_COMMUNICATION
          option.clientRoleType = Constants.CLIENT_ROLE_BROADCASTER
          setupLocalVideo()
          localSurfacreView!!.visibility = View.VISIBLE
          agoraEngine!!.startPreview()
          agoraEngine!!.joinChannel(token,channelName,uid,option)
      }

      else{
          Toast.makeText(this, "Permission Not granted", Toast.LENGTH_SHORT).show()

      }
    }

    private val mRtcEventHandler: IRtcEngineEventHandler = object : IRtcEngineEventHandler() {
        override fun onUserJoined(uid: Int, elapsed: Int) {
            showMessage("Remote user joined $uid")

            runOnUiThread{
                setupRemoteVideo(uid)
            }
        }
        override fun onJoinChannelSuccess(channel: String, uid: Int, elapsed: Int) {
            isJoined = true
            showMessage("Joined Channel $channel")
        }
        override fun onUserOffline(uid: Int, reason: Int) {
            showMessage("Remote user offline $uid $reason")


            runOnUiThread{
                remoteSurfacreView!!.visibility = View.GONE
            }

        }
    }

    private fun setupRemoteVideo(uid: Int) {
        remoteSurfacreView = SurfaceView(baseContext)
        remoteSurfacreView!!.setZOrderMediaOverlay(true)
        binding.remoteVideoViewContainer.addView(remoteSurfacreView)
        agoraEngine!!.setupRemoteVideo(
            VideoCanvas(
                remoteSurfacreView,
                VideoCanvas.RENDER_MODE_FIT,
                uid
            )
        )
        remoteSurfacreView!!.setVisibility(View.VISIBLE)
    }

    private fun setupLocalVideo() {
        localSurfacreView = SurfaceView(baseContext)
        binding.localVideoViewContainer.addView(localSurfacreView)
        agoraEngine!!.setupLocalVideo(
            VideoCanvas(
                localSurfacreView,
                VideoCanvas.RENDER_MODE_FIT,
                0
            )
        )
        localSurfacreView!!.setVisibility(View.VISIBLE)
    }


}