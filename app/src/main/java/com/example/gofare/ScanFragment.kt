package com.example.gofare

import android.app.PendingIntent
import android.content.Intent
import android.content.IntentFilter
import android.graphics.Color
import android.nfc.NfcAdapter
import android.nfc.Tag
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.addCallback
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.example.gofare.databinding.FragmentScanBinding
import com.example.gofare.databinding.FragmentTopUpBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.Timestamp
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class ScanFragment : Fragment() {

    private var nfcAdapter: NfcAdapter? = null
    private lateinit var auth: FirebaseAuth

    private var transitAnimationJob: Job? = null
    private  lateinit var binding : FragmentScanBinding
    private var allowScan = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        nfcAdapter = NfcAdapter.getDefaultAdapter(requireContext())
        auth = FirebaseAuth.getInstance()

        val user = auth.currentUser
        if (user != null) {
            val rfidRef = FirebaseFirestore.getInstance().collection("UserRFID").document(user.uid)
            rfidRef.update("nfcActive", true)
                .addOnSuccessListener {
                    allowScan = true
                    Toast.makeText(requireContext(), "NFC Scanning!", Toast.LENGTH_SHORT).show()
                    Log.d("Firestore", "nfcActive set to true successfully")
                }
                .addOnFailureListener { e ->
                    Log.w("Firestore", "Error updating nfcActive", e)
                }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentScanBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner) {
            requireActivity().supportFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, HomeFragment())
                .commit()
        }

        val viewModel = ViewModelProvider(requireActivity())[SharedViewModel::class.java]
        viewModel.startLive()
        viewModel.nfcActive.observe(viewLifecycleOwner) { nfc ->
            var isActive = false
            if (nfc == false){
                stopScanningTextLoop()
                isActive = false
                if (allowScan){
                lifecycleScope.launch {
                    binding.scanningTv.visibility = View.GONE
                    binding.scanningSuccess.visibility = View.VISIBLE
                    delay(1000)
                    Toast.makeText(requireContext(), "Scan To Pay Successful!", Toast.LENGTH_SHORT).show()
                    requireActivity().supportFragmentManager.beginTransaction()
                        .replace(R.id.fragment_container, HomeFragment())
                        .commit()
                }
                }
            }
            else{
                isActive = true
                val texts = listOf("Scanning   ", "Scanning.  ", "Scanning.. ", "Scanning...")
                transitAnimationJob = lifecycleScope.launch {
                    while (isActive) {
                        for (text in texts) {
                            binding.scanningTv.text = text
                            delay(500)
                        }
                    }
                }
            }
        }
    }

    private fun stopScanningTextLoop() {
        transitAnimationJob?.cancel()
    }

    override fun onResume() {
        super.onResume()
        val intent = Intent(requireContext(), requireActivity()::class.java).apply {
            addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP)
        }

        val pendingIntent = PendingIntent.getActivity(
            requireContext(), 0, intent,
            PendingIntent.FLAG_MUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        val filters = arrayOf(IntentFilter(NfcAdapter.ACTION_TAG_DISCOVERED))
        nfcAdapter?.enableForegroundDispatch(requireActivity(), pendingIntent, filters, null)

        // Check if this fragment was triggered by a tag scan
        if (requireActivity().intent?.action == NfcAdapter.ACTION_TAG_DISCOVERED) {
//            handleNfcIntent(requireActivity().intent)
            // Clear the intent to avoid multiple triggers
            requireActivity().intent = Intent()
        }
    }

    override fun onPause() {
        super.onPause()
        nfcAdapter?.disableForegroundDispatch(requireActivity())
        val user = auth.currentUser
        if (user != null) {
            val rfidRef = FirebaseFirestore.getInstance().collection("UserRFID").document(user.uid)
            rfidRef.update("nfcActive", false)
                .addOnSuccessListener {
                    Log.d("Firestore", "nfcActive set to false successfully in onPause")
                }
                .addOnFailureListener { e ->
                    Log.w("Firestore", "Error updating nfcActive in onPause", e)
                }
        }
    }

    override fun onStop() {
        super.onStop()
        val user = auth.currentUser
        if (user != null) {
            val rfidRef = FirebaseFirestore.getInstance().collection("UserRFID").document(user.uid)
            rfidRef.update("nfcActive", false)
                .addOnSuccessListener {
                    Log.d("Firestore", "nfcActive set to false successfully in onPause")
                }
                .addOnFailureListener { e ->
                    Log.w("Firestore", "Error updating nfcActive in onPause", e)
                }
        }
    }

//    private fun handleNfcIntent(intent: Intent) {
//        val tag: Tag? = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
//            intent.getParcelableExtra(NfcAdapter.EXTRA_TAG, Tag::class.java)
//        } else {
//            intent.getParcelableExtra(NfcAdapter.EXTRA_TAG)
//        }
//
//        if (tag != null) {
//            val tagId = tag.id.joinToString("") { "%02X".format(it) }
//            val userId = auth.currentUser?.uid ?: return
//            val dynamicNfcRef = FirebaseFirestore.getInstance()
//                .collection("UserDynamicNFC")
//                .document(userId)
//
//            dynamicNfcRef.get().addOnSuccessListener { document ->
//                val previousId = document.getString("nfcId") ?: ""
//
//                val nfcUpdate = mapOf(
//                    "nfcId" to tagId,
//                    "previousId" to previousId,
//                    "updatedAt" to Timestamp.now()
//                )
//
//                dynamicNfcRef.set(nfcUpdate).addOnSuccessListener {
//                    Toast.makeText(requireContext(), "NFC scanned: $tagId", Toast.LENGTH_SHORT).show()
//                    Toast.makeText(requireContext(), "Proceeding to payment: $tagId", Toast.LENGTH_SHORT).show()
//                    requireActivity().supportFragmentManager.beginTransaction()
//                        .replace(R.id.fragment_container, HomeFragment())
//                        .commit()
//                }.addOnFailureListener {
//                    Toast.makeText(requireContext(), "Failed to update NFC", Toast.LENGTH_SHORT).show()
//                }
//            }.addOnFailureListener {
//                Toast.makeText(requireContext(), "Failed to read Firestore", Toast.LENGTH_SHORT).show()
//            }
//        } else {
//            Toast.makeText(requireContext(), "No NFC tag detected", Toast.LENGTH_SHORT).show()
//        }
//    }

}
