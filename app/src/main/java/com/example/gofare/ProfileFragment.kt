package com.example.gofare

import android.app.DatePickerDialog
import android.content.Intent
import android.icu.util.Calendar
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
import androidx.activity.addCallback
import androidx.lifecycle.ViewModelProvider
import androidx.transition.Visibility
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.Timestamp
import java.util.Date

class ProfileFragment : Fragment() {

    var isEditing = false

    private lateinit var userProfile : TextView
    private lateinit var ageText : TextView
    private lateinit var pfFirstName : EditText
    private lateinit var pfMiddleName : EditText
    private lateinit var pfLastName : EditText
    private lateinit var emailLabel : TextView
    private lateinit var isStudent : TextView
    private lateinit var pfEmail : EditText
    private lateinit var pfContactNumber : EditText
    private lateinit var pfAddress : EditText
    private lateinit var pfBirthday : EditText
    private lateinit var pfAge : EditText
    private lateinit var radioMale : RadioButton
    private lateinit var radioFemale : RadioButton
    private lateinit var pfGenderRadioGrp : RadioGroup
    private lateinit var student : RadioButton
    private lateinit var nonStudent : RadioButton
    private lateinit var studentRadioGroup : RadioGroup

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

        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner) {
            requireActivity().supportFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, SettingsFragment())
                .commit()
        }

        userProfile = view.findViewById(R.id.userProfile)
        pfFirstName = view.findViewById(R.id.pfFirstName)
        pfMiddleName = view.findViewById(R.id.pfMiddleName)
        pfLastName = view.findViewById(R.id.pfLastName)
        pfEmail = view.findViewById(R.id.pfEmail)
        isStudent = view.findViewById(R.id.isStudent)
        emailLabel = view.findViewById(R.id.emailLabel)
        pfContactNumber = view.findViewById(R.id.pfContactNumber)
        pfAddress = view.findViewById(R.id.pfAddress)
        pfBirthday = view.findViewById(R.id.pfBirthday)
        pfAge = view.findViewById(R.id.pfAge)
        ageText = view.findViewById(R.id.ageLabel)
        pfGenderRadioGrp = view.findViewById(R.id.pfGenderRadioGrp)
        radioMale = view.findViewById(R.id.maleRadio)
        radioFemale = view.findViewById(R.id.femaleRadio)
        studentRadioGroup = view.findViewById(R.id.studentRadioGroup)
        student = view.findViewById(R.id.student)
        nonStudent = view.findViewById(R.id.nonStudent)
        saveBtn = view.findViewById(R.id.confirmButton)
        editBtn = view.findViewById(R.id.editButton)
        cancelBtn = view.findViewById(R.id.cancelButton)
        editLayout = view.findViewById(R.id.editMode)

        editBtn.setOnClickListener{
            isEditing = true
            setEnabled(true)
            pfAge.visibility = View.GONE
            ageText.visibility = View.GONE
            editBtn.visibility = View.GONE
            editLayout.visibility = View.VISIBLE
            pfEmail.visibility = View.GONE
            emailLabel.visibility = View.GONE
            studentRadioGroup.visibility = View.GONE
            isStudent.visibility = View.GONE
        }

        saveBtn.setOnClickListener{
            updateProfile()
            isEditing = false
            setEnabled(false)
            pfAge.visibility = View.VISIBLE
            ageText.visibility = View.VISIBLE
            editBtn.visibility = View.VISIBLE
            editLayout.visibility = View.GONE
            pfEmail.visibility = View.VISIBLE
            emailLabel.visibility = View.VISIBLE
            studentRadioGroup.visibility = View.VISIBLE
            isStudent.visibility = View.VISIBLE
        }

        cancelBtn.setOnClickListener{
            isEditing = false
            setEnabled(false)
            editBtn.visibility = View.VISIBLE
            editLayout.visibility = View.GONE
            pfEmail.visibility = View.VISIBLE
            emailLabel.visibility = View.VISIBLE
            studentRadioGroup.visibility = View.VISIBLE
            isStudent.visibility = View.VISIBLE
            displayUserData()
        }

        pfBirthday.setOnClickListener {
            val calendar = Calendar.getInstance()
            val year = calendar.get(Calendar.YEAR)
            val month = calendar.get(Calendar.MONTH)
            val day = calendar.get(Calendar.DAY_OF_MONTH)

            val datePickerDialog = DatePickerDialog(requireContext(),
                { _, selectedYear, selectedMonth, selectedDay ->
                    // Format and set the selected date
                    val dateString = "${selectedMonth + 1}/$selectedDay/$selectedYear"
                    pfBirthday.setText(dateString)
                },
                year, month, day)

            datePickerDialog.show()
        }

        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner) {
            requireActivity().supportFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, HomeFragment())
                .commit()
        }

        displayUserData()
    }

    private fun setEnabled( arg: Boolean ){
        pfFirstName.isEnabled = arg
        pfMiddleName.isEnabled = arg
        pfLastName.isEnabled = arg
        pfContactNumber.isEnabled = arg
        pfAddress.isEnabled = arg
        pfBirthday.isEnabled = arg
        radioMale.isEnabled = arg
        radioFemale.isEnabled = arg
        editLayout.isEnabled = arg
    }

    private fun updateProfile() {
        val auth = FirebaseAuth.getInstance()
        val userId = auth.currentUser?.uid

        if (userId != null){
            val dbRef = FirebaseFirestore.getInstance().collection("Users").document(userId)

            if (dbRef != null){

                val firstName = pfFirstName.text.toString().trim()
                val lastName = pfLastName.text.toString().trim()
                val middleName = pfMiddleName.text.toString().trim()
                val birthday = pfBirthday.text.toString().trim()
                val currentYear = Calendar.getInstance().get(Calendar.YEAR)
                val birthYear = birthday.substring(birthday.length - 4).toInt()

                val age = currentYear - birthYear
                val address = pfAddress.text.toString().trim()
                val contactNumber = pfContactNumber.text.toString().replace("+", "").replace("-", "").replace(" ", "")

                val selectedIndex = pfGenderRadioGrp.indexOfChild(pfGenderRadioGrp.findViewById(pfGenderRadioGrp.checkedRadioButtonId))
                val gender = when (selectedIndex) {
                    0 -> "Male"
                    1 -> "Female"
                    else -> ""
                }

                if (birthYear > Calendar.getInstance().get(Calendar.YEAR)){
                    Toast.makeText(requireContext(), "Invalid Birthday! Please enter a valid year of birth", Toast.LENGTH_SHORT).show()
                    return
                }
                if (age < 7 || age > 120) {
                    Toast.makeText(requireContext(), "Age must be between 7 and 120!", Toast.LENGTH_SHORT).show()
                    return
                }

                if (!contactNumber.matches(Regex("^\\d{10,11}$"))) {
                    Toast.makeText(requireContext(), "Invalid Contact Number. Only 10-11 digits allowed.", Toast.LENGTH_SHORT).show()
                    return
                }

                val userData = mapOf(
                    "firstName" to firstName,
                    "lastName" to lastName,
                    "middleName" to middleName,
                    "birthday" to birthday,
                    "age" to age,
                    "address" to address,
                    "gender" to gender,
                    "contactNumber" to contactNumber,
                    "updateDate" to Timestamp.now(),
                )

                if (firstName.isEmpty() || lastName.isEmpty() || birthday.isEmpty() || address.isEmpty() || gender.isEmpty() || contactNumber.isEmpty()) {
                    Toast.makeText(requireContext(), "Please fill all required fields!", Toast.LENGTH_SHORT).show()
                }
                else{
                    dbRef.update(userData)
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
        viewModel.birthday.observe(viewLifecycleOwner) { name ->
            pfBirthday.setText(name)
        }
        viewModel.age.observe(viewLifecycleOwner) { name ->
            pfAge.setText(name)
        }
        viewModel.studentStatus.observe(viewLifecycleOwner) { name ->
            if (name == true){
                student.isChecked = true
            }
            else {
                nonStudent.isChecked = true
            }
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