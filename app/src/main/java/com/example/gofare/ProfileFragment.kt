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
import com.google.firebase.database.FirebaseDatabase

class ProfileFragment : Fragment() {

    var isEditing = false

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
    private lateinit var pfGenderRadioGrp : RadioGroup

    private lateinit var editBtn : com.google.android.material.button.MaterialButton

    private lateinit var editLayout : androidx.constraintlayout.widget.ConstraintLayout
    private lateinit var saveBtn : com.google.android.material.button.MaterialButton
    private lateinit var cancelBtn : com.google.android.material.button.MaterialButton

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
        pfGenderRadioGrp = view.findViewById(R.id.pfGenderRadioGrp)
        radioMale = view.findViewById(R.id.maleRadio)
        radioFemale = view.findViewById(R.id.femaleRadio)
        saveBtn = view.findViewById(R.id.confirmButton)
        editBtn = view.findViewById(R.id.editButton)
        cancelBtn = view.findViewById(R.id.cancelButton)
        editLayout = view.findViewById(R.id.editMode)

        editBtn.setOnClickListener{
            isEditing = true
            setEnabled()
            editBtn.visibility = View.GONE
            editLayout.visibility = View.VISIBLE
        }

        saveBtn.setOnClickListener{
            updateProfile()
            isEditing = false
            setEnabled()
            editBtn.visibility = View.VISIBLE
            editLayout.visibility = View.GONE
        }

        cancelBtn.setOnClickListener{
            isEditing = false
            setEnabled()
            editBtn.visibility = View.VISIBLE
            editLayout.visibility = View.GONE
        }


        displayUserData()
    }

    private fun setEnabled(){
        pfFirstName.isEnabled = isEditing
        pfMiddleName.isEnabled = isEditing
        pfLastName.isEnabled = isEditing
        pfContactNumber.isEnabled = isEditing
        pfAddress.isEnabled = isEditing
        pfAge.isEnabled = isEditing
        radioMale.isEnabled = isEditing
        radioFemale.isEnabled = isEditing
        saveBtn.isEnabled = isEditing
        editBtn.isEnabled = isEditing
        cancelBtn.isEnabled = isEditing
        editLayout.isEnabled = isEditing
    }

    private fun updateProfile() {
        val auth = FirebaseAuth.getInstance()
        val userId = auth.currentUser?.uid

        if (userId != null){
            val dbRef = FirebaseDatabase.getInstance().getReference("ClientReference").child(userId)

            if (dbRef != null){

                val firstName = pfFirstName.text.toString().trim()
                val lastName = pfLastName.text.toString().trim()
                val middleName = pfMiddleName.text.toString().trim()
                val age = pfAge.text.toString().trim()
                val address = pfAddress.text.toString().trim()
                val contactNumber = pfContactNumber.text.toString().replace("+", "").replace("-", "").replace(" ", "")

                val selectedIndex = pfGenderRadioGrp.indexOfChild(pfGenderRadioGrp.findViewById(pfGenderRadioGrp.checkedRadioButtonId))
                val gender = when (selectedIndex) {
                    0 -> "Male"
                    1 -> "Female"
                    else -> ""
                }

                val userData = mapOf(
                    "firstName" to firstName,
                    "lastName" to lastName,
                    "middleName" to middleName,
                    "age" to age,
                    "address" to address,
                    "gender" to gender,
                    "contactNumber" to contactNumber,
                )

                if (firstName.isEmpty() || lastName.isEmpty() || age.isEmpty() || address.isEmpty() || gender.isEmpty() || contactNumber.isEmpty()) {
                    Toast.makeText(requireContext(), "Please fill all required fields!", Toast.LENGTH_SHORT).show()
                }
                else{
                    dbRef.updateChildren(userData)
                        .addOnSuccessListener {
                            Toast.makeText(requireContext(), "Profile updated successfully!", Toast.LENGTH_SHORT).show()
                        }
                        .addOnFailureListener {
                            Toast.makeText(requireContext(), "Failed to update profile.", Toast.LENGTH_SHORT).show()
                        }
                }

            }

        }
        else{
            Toast.makeText(requireContext(), "Error Fetching Current User", Toast.LENGTH_SHORT).show()
        }

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