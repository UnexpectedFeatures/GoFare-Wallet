package com.example.gofare

import android.annotation.SuppressLint
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import com.example.gofare.databinding.FragmentContactBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class ContactFragment : Fragment() {

    private  var _binding : FragmentContactBinding? = null
    private val binding get() = _binding!!

    private lateinit var submitBtn : com.google.android.material.button.MaterialButton
    private lateinit var backBtn : com.google.android.material.button.MaterialButton
    private lateinit var typeMenu : com.google.android.material.textfield.TextInputLayout
    private lateinit var reasonMenu : com.google.android.material.textfield.TextInputLayout
    private lateinit var ctOtherType : EditText
    private lateinit var specifyRequestLabel : TextView
    private lateinit var ctDescription : EditText

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        _binding = FragmentContactBinding.inflate(inflater, container, false)

        val requestTypes = resources.getStringArray(R.array.requestType)
        val requestReason = resources.getStringArray(R.array.requestReason)
        val arrayAdapterType = ArrayAdapter(requireContext(), R.layout.request_item, requestTypes)
        val arrayAdapterReason = ArrayAdapter(requireContext(), R.layout.reason_item, requestReason)
        binding.typeAutoCompleteView.setAdapter(arrayAdapterType)
        binding.reasonAutoCompleteView.setAdapter(arrayAdapterReason)

        return binding.root
    }
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        submitBtn = view.findViewById(R.id.submitBtn)
        backBtn = view.findViewById(R.id.backBtn)
        typeMenu = view.findViewById(R.id.typeMenu)
        reasonMenu = view.findViewById(R.id.reasonMenu)
        ctOtherType = view.findViewById(R.id.ctOtherType)
        ctDescription = view.findViewById(R.id.ctDescription)
        specifyRequestLabel = view.findViewById(R.id.specifyRequestLabel)

        ctOtherType.visibility = View.GONE
        specifyRequestLabel.visibility = View.GONE

        binding.typeAutoCompleteView.setOnItemClickListener { _, _, position, _ ->
            val selected = binding.typeAutoCompleteView.adapter.getItem(position).toString()
            Log.d("DropdownSelection", "Selected item: $selected")
            ctOtherType.visibility = if (selected == "Others (Specify)") View.VISIBLE else View.GONE
            specifyRequestLabel.visibility = if (selected == "Others (Specify)") View.VISIBLE else View.GONE
        }

        binding.reasonAutoCompleteView.setOnItemClickListener { _, _, position, _ ->
            val selected = binding.reasonAutoCompleteView.adapter.getItem(position).toString()
            Log.d("DropdownSelection", "Selected item: $selected")
        }

        backBtn.setOnClickListener {
            switchFragment(SettingsFragment())
        }

        submitBtn.setOnClickListener {
            createRequest()
        }
    }

    fun switchFragment(fragment: Fragment) {
        requireActivity().supportFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, fragment)
            .commit()
    }

    @SuppressLint("DefaultLocale")
    private fun createRequest() {
        val auth = FirebaseAuth.getInstance()
        val userId = auth.currentUser?.uid

        val currentDate = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
        val currentTime = SimpleDateFormat("hh:mm a", Locale.getDefault()).format(Date())

        val case = binding.typeAutoCompleteView.text.toString()
        val reason = binding.reasonAutoCompleteView.text.toString()
        val otherType = ctOtherType.text.toString()
        val description = ctDescription.text.toString()

        if (case.isEmpty() || reason.isEmpty() || reason == "Reason") {
            Toast.makeText(requireContext(), "Please choose a case and a reason", Toast.LENGTH_SHORT).show()
            return
        }

        if (userId != null) {
            val requestRef = FirebaseFirestore.getInstance()
                .collection("UserRequests")
                .document(userId)

            requestRef.get().addOnSuccessListener { snapshot ->
                val existingRequests = snapshot.data ?: emptyMap<String, Any>()
                val count = existingRequests.size
                val requestId = "UR" + String.format("%04d", count + 1)

                val requestMap = mapOf(
                    "requestId" to requestId,
                    "type" to case,
                    "reason" to reason,
                    "otherType" to if (case == "Others (Specify)") otherType else "",
                    "description" to description,
                    "date" to currentDate,
                    "time" to currentTime,
                    "status" to "Pending"
                )

                val updateMap = mapOf(requestId to requestMap)

                requestRef.set(updateMap, SetOptions.merge())
                    .addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            Toast.makeText(requireContext(), "Request Sent Successfully", Toast.LENGTH_SHORT).show()
                            switchFragment(SettingsFragment())
                        } else {
                            Toast.makeText(requireContext(), "Request Sending Failed", Toast.LENGTH_SHORT).show()
                        }
                    }
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()

        _binding = null
    }

}