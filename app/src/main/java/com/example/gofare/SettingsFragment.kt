package com.example.gofare

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.LinearLayout
import android.widget.Switch
import android.widget.TextView
import android.widget.Toast
import androidx.activity.addCallback
import androidx.lifecycle.ViewModelProvider
import com.google.firebase.auth.FirebaseAuth

class SettingsFragment : Fragment() {
    private lateinit var logoutButton : com.google.android.material.button.MaterialButton
    private lateinit var editProfile : LinearLayout
    private lateinit var contactButton : com.google.android.material.button.MaterialButton
    private lateinit var topUpHistoryButton : com.google.android.material.button.MaterialButton
    private lateinit var privacyButton: com.google.android.material.button.MaterialButton
    private lateinit var aboutButton: com.google.android.material.button.MaterialButton
    private lateinit var swNotification: Switch
    private lateinit var swVibrate: Switch
    private lateinit var stFullName : TextView
    private lateinit var stEmail : TextView
    private lateinit var userVerified : TextView


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_settings, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner) {
            requireActivity().supportFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, HomeFragment())
                .commit()
        }

        logoutButton = view.findViewById(R.id.logoutButton)
        topUpHistoryButton = view.findViewById(R.id.topUpHistoryBtn)
        editProfile = view.findViewById(R.id.editProfileButton)
        stFullName = view.findViewById(R.id.stFullName)
        stEmail = view.findViewById(R.id.stEmail)
        userVerified = view.findViewById(R.id.userVerified)
        contactButton = view.findViewById(R.id.contactButton)

        val viewModel = ViewModelProvider(requireActivity())[SharedViewModel::class.java]
        viewModel.startLive()

        viewModel.rfid.observe(viewLifecycleOwner) { rfid ->
            if (rfid.isNullOrEmpty()) {
                userVerified.text = "RFID UNREGISTERED"
                userVerified.setTextColor(Color.RED)
            }
            else{
                userVerified.text = "Verified"
                userVerified.setTextColor(Color.BLACK)
            }
        }

        editProfile.setOnClickListener {
            switchFragment(ProfileFragment())
        }

        contactButton.setOnClickListener {
            switchFragment(ContactFragment())
        }

        topUpHistoryButton.setOnClickListener {
            switchFragment(TopUpHistoryFragment())
        }

        logoutButton.setOnClickListener(View.OnClickListener {
            logoutUser()
        })
        displayUserData()
    }

    private fun displayUserData() {
        val viewModel = ViewModelProvider(requireActivity())[SharedViewModel::class.java]

        viewModel.fullName.observe(viewLifecycleOwner) { name ->
            stFullName.setText(name)
        }
        viewModel.email.observe(viewLifecycleOwner) {email ->
            stEmail.setText(email)
        }

    }

    fun switchFragment(fragment: Fragment) {
        requireActivity().supportFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, fragment)
            .commit()
    }

    private fun logoutUser() {
        FirebaseAuth.getInstance().signOut()

        val intent = Intent(requireContext(), MainActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)

    }

}