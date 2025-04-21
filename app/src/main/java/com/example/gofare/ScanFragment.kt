package com.example.gofare

import android.app.PendingIntent
import android.content.Intent
import android.content.IntentFilter
import android.nfc.NfcAdapter
import android.nfc.Tag
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.Timestamp

class ScanFragment : Fragment() {

    private var nfcAdapter: NfcAdapter? = null
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        nfcAdapter = NfcAdapter.getDefaultAdapter(requireContext())
        auth = FirebaseAuth.getInstance()
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
            handleNfcIntent(requireActivity().intent)
            // Clear the intent to avoid multiple triggers
            requireActivity().intent = Intent()
        }
    }

    override fun onPause() {
        super.onPause()
        nfcAdapter?.disableForegroundDispatch(requireActivity())
    }

    private fun handleNfcIntent(intent: Intent) {
        val tag: Tag? = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            intent.getParcelableExtra(NfcAdapter.EXTRA_TAG, Tag::class.java)
        } else {
            @Suppress("DEPRECATION")
            intent.getParcelableExtra(NfcAdapter.EXTRA_TAG)
        }

        if (tag != null) {
            val tagId = tag.id.joinToString("") { "%02X".format(it) }
            val userId = auth.currentUser?.uid ?: return
            val dynamicNfcRef = FirebaseFirestore.getInstance()
                .collection("UserDynamicNFC")
                .document(userId)

            dynamicNfcRef.get().addOnSuccessListener { document ->
                val previousId = document.getString("nfcId") ?: ""

                val nfcUpdate = mapOf(
                    "nfcId" to tagId,
                    "previousId" to previousId,
                    "updatedAt" to Timestamp.now()
                )

                dynamicNfcRef.set(nfcUpdate).addOnSuccessListener {
                    Toast.makeText(requireContext(), "NFC scanned: $tagId", Toast.LENGTH_SHORT).show()
                    Toast.makeText(requireContext(), "Proceeding to payment: $tagId", Toast.LENGTH_SHORT).show()
                    requireActivity().supportFragmentManager.beginTransaction()
                        .replace(R.id.fragment_container, HomeFragment())
                        .commit()
                }.addOnFailureListener {
                    Toast.makeText(requireContext(), "Failed to update NFC", Toast.LENGTH_SHORT).show()
                }
            }.addOnFailureListener {
                Toast.makeText(requireContext(), "Failed to read Firestore", Toast.LENGTH_SHORT).show()
            }
        } else {
            Toast.makeText(requireContext(), "No NFC tag detected", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_scan, container, false)
    }
}
