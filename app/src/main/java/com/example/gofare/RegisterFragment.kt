package com.example.gofare

import android.app.DatePickerDialog
import android.content.Intent
import android.icu.util.Calendar
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.RadioButton
import android.widget.RadioGroup
import android.widget.Toast
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class RegisterFragment : Fragment() {

    lateinit var toLogin : com.google.android.material.button.MaterialButton
    lateinit var firstNameTxt : EditText
    lateinit var lastNameTxt : EditText
    lateinit var middleNameTxt : EditText
    lateinit var birthdayText : EditText
    lateinit var addressTxt : EditText
    lateinit var genderRadioGrp: RadioGroup
    lateinit var emailTxt : EditText
    lateinit var contactTxt : EditText
    lateinit var passwordTxt : EditText
    lateinit var confirmPasswordTxt : EditText
    lateinit var signUpBtn : com.google.android.material.button.MaterialButton

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)


    }
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        toLogin = view.findViewById(R.id.toLogin)
        firstNameTxt = view.findViewById(R.id.firstNameEditText)
        lastNameTxt = view.findViewById(R.id.lastNameEditText)
        middleNameTxt = view.findViewById(R.id.middleNameEditText)
        birthdayText = view.findViewById(R.id.birthdayEditText)
        addressTxt = view.findViewById(R.id.addressEditText)
        genderRadioGrp = view.findViewById(R.id.genderRadioGrp)
        contactTxt = view.findViewById(R.id.contactEditText)
        emailTxt = view.findViewById(R.id.emailEditText)
        passwordTxt = view.findViewById(R.id.passwordEditText)
        confirmPasswordTxt = view.findViewById(R.id.confirmPasswordEditText)
        signUpBtn = view.findViewById(R.id.signUpBtn)

        toLogin.setOnClickListener {
            val intent = Intent(
                requireContext(),
                MainActivity::class.java
            )
            startActivity(intent)
        }

        birthdayText.setOnClickListener {
            val calendar = Calendar.getInstance()
            val year = calendar.get(Calendar.YEAR)
            val month = calendar.get(Calendar.MONTH)
            val day = calendar.get(Calendar.DAY_OF_MONTH)

            val datePickerDialog = DatePickerDialog(requireContext(),
                { _, selectedYear, selectedMonth, selectedDay ->
                    // Format and set the selected date
                    val dateString = "${selectedMonth + 1}/$selectedDay/$selectedYear"
                    birthdayText.setText(dateString)
                },
                year, month, day)

            datePickerDialog.show()
        }

        signUpBtn.setOnClickListener(View.OnClickListener {

            val firstName = firstNameTxt.text.toString().trim()
            val lastName = lastNameTxt.text.toString().trim()
            val middleName = middleNameTxt.text.toString().trim()

            val birthday = birthdayText.text.toString().trim()

            val currentYear = Calendar.getInstance().get(Calendar.YEAR)
            val birthYear = birthday.substring(birthday.length - 4).toInt()
            val age = currentYear - birthYear
            val address = addressTxt.text.toString().trim()
            val contactNumber = contactTxt.text.toString().replace("+", "").replace("-", "").replace(" ", "")
            val email = emailTxt.text.toString().trim()
            val password = passwordTxt.text.toString().trim()
            val confirmPassword = confirmPasswordTxt.text.toString().trim()

            val selectedGenderId = genderRadioGrp.checkedRadioButtonId
            val gender = if (selectedGenderId != -1) {
                view.findViewById<RadioButton>(selectedGenderId).text.toString()
            } else {
                ""
            }

            if (firstName.isEmpty() || lastName.isEmpty() || email.isEmpty() || password.isEmpty() || confirmPassword.isEmpty() || selectedGenderId == 1) {
                Toast.makeText(requireContext(), "Please fill all required fields!", Toast.LENGTH_SHORT).show()
                return@OnClickListener
            }
            if (password != confirmPassword) {
                Toast.makeText(requireContext(), "Passwords do not match!", Toast.LENGTH_SHORT).show()
                return@OnClickListener
            }
            if (password.length < 3 || password.length > 20)
            {
                Toast.makeText(requireContext(), "Invalid Password!", Toast.LENGTH_SHORT).show()
                return@OnClickListener
            }

            var auth: FirebaseAuth = FirebaseAuth.getInstance()

            auth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        val user = auth.currentUser
                        user?.sendEmailVerification()?.addOnCompleteListener { verifyTask ->
                            if (verifyTask.isSuccessful) {
                                Toast.makeText(requireContext(), "Verification email sent. Please check your inbox.", Toast.LENGTH_LONG).show()

                                val bundle = Bundle().apply {
                                    putString("firstName", firstName)
                                    putString("lastName", lastName)
                                    putString("middleName", middleName)
                                    putString("birthday", birthday)
                                    putInt("age", age)
                                    putString("address", address)
                                    putString("gender", gender)
                                    putString("contact", contactNumber)
                                    putString("email", email)
                                    putString("password", password)
                                }


                                val registerEmailFragment = RegisterEmailFragment()
                                registerEmailFragment.arguments = bundle

                                parentFragmentManager.beginTransaction()
                                    .replace(R.id.register_frame, registerEmailFragment)
                                    .commit()

                                auth.signOut()
                            } else {
                                user.delete()
                                Toast.makeText(requireContext(), "Failed to send verification email: ${verifyTask.exception?.message}", Toast.LENGTH_LONG).show()
                            }
                        }
                    } else {
                        Toast.makeText(requireContext(), "Registration Failed: ${task.exception?.message}", Toast.LENGTH_LONG).show()
                    }
                }
        })
    }



    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_register, container, false)

    }

}