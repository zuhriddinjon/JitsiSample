package uz.instat.jitsisample

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import org.jitsi.meet.sdk.*
import timber.log.Timber
import uz.instat.jitsisample.databinding.ActivityMainBinding
import java.net.MalformedURLException
import java.net.URL

class MainActivity : AppCompatActivity(), View.OnClickListener {

    private lateinit var binding: ActivityMainBinding

    private val broadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(p0: Context?, p1: Intent?) {
            onBroadcastReceived(intent)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setupViews()


        // Initialize default options for Jitsi Meet conferences.
        val serverURL: URL = try {
            URL("https://meet.jit.si")
        } catch (e: MalformedURLException) {
            e.printStackTrace()
            throw RuntimeException("Invalid server URL!")
        }
        val defaultOptions = JitsiMeetConferenceOptions.Builder()
            .setServerURL(serverURL)
            // When using JaaS, set the obtained JWT here
            //.setToken("MyJWT")
            // Different features flags can be set
            //.setFeatureFlag("toolbox.enabled", false)
            //.setFeatureFlag("filmstrip.enabled", false)
            .setWelcomePageEnabled(false)
            .build()
        JitsiMeet.setDefaultConferenceOptions(defaultOptions)

        registerForBroadcastMessages()
    }

    private fun setupViews() {
        binding.btnJoin.setOnClickListener(this)
    }


    private fun registerForBroadcastMessages() {
        val intentFilter = IntentFilter()

        /* This registers for every possible event sent from JitsiMeetSDK
           If only some of the events are needed, the for loop can be replaced
           with individual statements:
           ex:  intentFilter.addAction(BroadcastEvent.Type.AUDIO_MUTED_CHANGED.action);
                intentFilter.addAction(BroadcastEvent.Type.CONFERENCE_TERMINATED.action);
                ... other events
         */
        for (type in BroadcastEvent.Type.values()) {
            intentFilter.addAction(type.action)
        }

        LocalBroadcastManager.getInstance(this).registerReceiver(broadcastReceiver, intentFilter)
    }

    private fun onBroadcastReceived(intent: Intent?) {
        if (intent != null) {
            val event = BroadcastEvent(intent)
            when (event.type) {
                BroadcastEvent.Type.CONFERENCE_JOINED -> Timber.i(
                    "Conference Joined with url%s",
                    event.data.get("url")
                )
                BroadcastEvent.Type.PARTICIPANT_JOINED -> Timber.i(
                    "Participant joined%s",
                    event.data.keys
                )
                BroadcastEvent.Type.CONFERENCE_TERMINATED -> {
                }
                BroadcastEvent.Type.CONFERENCE_WILL_JOIN -> {
                }
                BroadcastEvent.Type.AUDIO_MUTED_CHANGED -> {
                }
                BroadcastEvent.Type.PARTICIPANT_LEFT -> {
                }
                BroadcastEvent.Type.ENDPOINT_TEXT_MESSAGE_RECEIVED -> {
                }
                BroadcastEvent.Type.SCREEN_SHARE_TOGGLED -> {
                }
                BroadcastEvent.Type.PARTICIPANTS_INFO_RETRIEVED -> {
                }
                BroadcastEvent.Type.CHAT_MESSAGE_RECEIVED -> {
                }
                BroadcastEvent.Type.CHAT_TOGGLED -> {
                }
                BroadcastEvent.Type.VIDEO_MUTED_CHANGED -> {
                }
            }
        }

    }

    override fun onClick(v: View?) {
        when (v?.id) {
            binding.btnJoin.id -> {
                launchJitsiMeet()
            }
        }
    }

    private fun launchJitsiMeet() {
        val text = binding.etConferenceAme.text.toString()
        if (text.isNotEmpty()) {
            // Build options object for joining the conference. The SDK will merge the default
            // one we set earlier and this one when joining.
            val options = JitsiMeetConferenceOptions.Builder()
                .setRoom(text)
                // Settings for audio and video
                //.setAudioMuted(true)
                //.setVideoMuted(true)
                .build()
            // Launch the new activity with the given options. The launch() method takes care
            // of creating the required Intent and passing the options.
            JitsiMeetActivity.launch(this, options)
        }

    }

    private fun hangUp() {
        val hangupBroadcastIntent: Intent = BroadcastIntentHelper.buildHangUpIntent()
        LocalBroadcastManager.getInstance(applicationContext)
            .sendBroadcast(hangupBroadcastIntent)
    }

    override fun onDestroy() {
        LocalBroadcastManager.getInstance(this).unregisterReceiver(broadcastReceiver)
        super.onDestroy()
    }

}