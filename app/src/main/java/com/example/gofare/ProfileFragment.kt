package com.example.gofare

import android.content.Intent
import android.os.Bundle
import android.util.TypedValue
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.RadioButton
import android.widget.RadioGroup
import android.widget.TextView
import android.widget.Toast
import androidx.lifecycle.ViewModelProvider
import com.google.firebase.auth.FirebaseAuth

class ProfileFragment : Fragment() {

    private lateinit var userProfile : TextView
    private lateinit var pfFirstName : EditText
    private lateinit var pfMiddleName : EditText
    private lateinit var pfLastName : EditText
    private lateinit var pfEmail : EditText
    private lateinit var pfContactNumber : EditText
    private lateinit var pfAddress : EditText
    private lateinit var pfAge : EditText
    private lateinit var radioMale : RadioButton
    private lateinit var radioFemale : RadioButton


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_profile, container, false) // Inflate layout
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        userProfile = view.findViewById(R.id.userProfile)
        pfFirstName = view.findViewById(R.id.pfFirstName)
        pfMiddleName = view.findViewById(R.id.pfMiddleName)
        pfLastName = view.findViewById(R.id.pfLastName)
        pfEmail = view.findViewById(R.id.pfEmail)
        pfContactNumber = view.findViewById(R.id.pfContactNumber)
        pfAddress = view.findViewById(R.id.pfAddress)
        pfAge = view.findViewById(R.id.pfAge)
        radioMale = view.findViewById(R.id.maleRadio)
        radioFemale = view.findViewById(R.id.femaleRadio)


        displayUserData()
    }

    private fun displayUserData() {
        val viewModel = ViewModelProvider(requireActivity())[SharedViewModel::class.java]

        viewModel.fullName.observe(viewLifecycleOwner) { name ->
            userProfile.setText(name)
        }
        viewModel.firstName.observe(viewLifecycleOwner) { name ->
            pfFirstName.setText(name)
        }
        viewModel.middleName.observe(viewLifecycleOwner) { name ->
            pfMiddleName.setText(name)
        }
        viewModel.lastName.observe(viewLifecycleOwner) { name ->
            pfLastName.setText(name)
        }
        viewModel.email.observe(viewLifecycleOwner) { name ->
            pfEmail.setText(name)
        }
        viewModel.contactNumber.observe(viewLifecycleOwner) { name ->
            pfContactNumber.setText(name)
        }
        viewModel.address.observe(viewLifecycleOwner) { name ->
            pfAddress.setText(name)
        }
        viewModel.age.observe(viewLifecycleOwner) { name ->
            pfAge.setText(name)
        }
        viewModel.gender.observe(viewLifecycleOwner) { gender ->
            if (gender.trim() == "Male" || gender.trim() == "male"){
                radioMale.isChecked = true
            }
            else {
                radioFemale.isChecked = true
            }
        }



    }

}