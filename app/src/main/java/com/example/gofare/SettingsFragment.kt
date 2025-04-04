package com.example.gofare

import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import com.google.firebase.auth.FirebaseAuth

class SettingsFragment : Fragment() {
    private lateinit var logoutButton : com.google.android.material.button.MaterialButton
    private lateinit var editProfile : com.google.android.material.button.MaterialButton

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
        logoutButton = view.findViewById(R.id.logoutButton)
        editProfile = view.findViewById(R.id.editProfileButton)

        editProfile.setOnClickListener {
            switchFragment(ProfileFragment())
        }

        logoutButton.setOnClickListener(View.OnClickListener {
            logoutUser()
        })
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