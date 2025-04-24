package com.example.gofare

import android.app.Activity.RESULT_OK
import android.app.DatePickerDialog
import android.content.Intent
import android.icu.util.Calendar
import android.net.Uri
import android.os.Bundle
import android.text.InputType
import android.util.Log
import android.util.Patterns
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.RadioButton
import android.widget.RadioGroup
import android.widget.TextView
import android.widget.Toast
import com.google.firebase.Firebase
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage

class RegisterFragment : Fragment() {

    lateinit var studentUpload: LinearLayout
    lateinit var toLogin: ImageButton
    lateinit var firstNameTxt: EditText
    lateinit var lastNameTxt: EditText
    lateinit var middleNameTxt: EditText
    lateinit var birthdayText: EditText
    lateinit var addressTxt: EditText
    lateinit var genderRadioGrp: RadioGroup
    lateinit var studentRadioGrp: RadioGroup
    lateinit var student: RadioButton
    lateinit var nonStudent: RadioButton
    lateinit var privacyPolicy: RadioButton
    lateinit var emailTxt: EditText
    lateinit var contactTxt: EditText
    lateinit var passwordTxt: EditText
    lateinit var confirmPasswordTxt: EditText
    lateinit var signUpBtn: com.google.android.material.button.MaterialButton
    lateinit var studentPrompt: TextView
    lateinit var termsOfService: TextView

    private lateinit var togglePasswordBtn: ImageButton
    private lateinit var toggleConfirmPasswordBtn: ImageButton
    private lateinit var imageView: ImageView
    private lateinit var buttonChoose: Button

    var imageUri: Uri? = null

    var isStudentInput: Boolean = false

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
        studentRadioGrp = view.findViewById(R.id.studentRadioGroup)
        contactTxt = view.findViewById(R.id.contactEditText)
        emailTxt = view.findViewById(R.id.emailEditText)
        passwordTxt = view.findViewById(R.id.passwordEditText)
        confirmPasswordTxt = view.findViewById(R.id.confirmPasswordEditText)
        signUpBtn = view.findViewById(R.id.signUpBtn)
        togglePasswordBtn = view.findViewById(R.id.togglePasswordBtn)
        toggleConfirmPasswordBtn = view.findViewById(R.id.toggleConfirmPasswordBtn)
        student = view.findViewById(R.id.student)
        nonStudent = view.findViewById(R.id.nonStudent)
        studentUpload = view.findViewById(R.id.studentUpload)
        studentPrompt = view.findViewById(R.id.studentPrompt)
        termsOfService = view.findViewById(R.id.termsOfService)
        privacyPolicy = view.findViewById(R.id.privacyPolicy)

        imageView = view.findViewById(R.id.image_view)
        buttonChoose = view.findViewById(R.id.button_choose_image)

        toLogin.setOnClickListener {
            val intent = Intent(
                requireContext(),
                MainActivity::class.java
            )
            startActivity(intent)
        }

        var isPasswordVisible = false
        var isConfirmPasswordVisible = false

        togglePasswordBtn.setOnClickListener {
            isPasswordVisible = !isPasswordVisible

            if (isPasswordVisible) {
                passwordTxt.inputType =
                    InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD
                togglePasswordBtn.setImageResource(R.drawable.eye)
            } else {
                passwordTxt.inputType =
                    InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD
                togglePasswordBtn.setImageResource(R.drawable.eye_closed)
            }
            passwordTxt.setSelection(passwordTxt.text.length)
        }

        toggleConfirmPasswordBtn.setOnClickListener {
            isConfirmPasswordVisible = !isConfirmPasswordVisible

            if (isConfirmPasswordVisible) {
                confirmPasswordTxt.inputType =
                    InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD
                toggleConfirmPasswordBtn.setImageResource(R.drawable.eye)
            } else {
                confirmPasswordTxt.inputType =
                    InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD
                toggleConfirmPasswordBtn.setImageResource(R.drawable.eye_closed)
            }
            confirmPasswordTxt.setSelection(confirmPasswordTxt.text.length)
        }

        birthdayText.setOnClickListener {
            val calendar = Calendar.getInstance()
            val year = calendar.get(Calendar.YEAR)
            val month = calendar.get(Calendar.MONTH)
            val day = calendar.get(Calendar.DAY_OF_MONTH)

            val datePickerDialog = DatePickerDialog(
                requireContext(),
                { _, selectedYear, selectedMonth, selectedDay ->
                    // Format and set the selected date
                    val dateString = "${selectedMonth + 1}/$selectedDay/$selectedYear"
                    birthdayText.setText(dateString)
                },
                year, month, day
            )

            datePickerDialog.show()
        }

        student.setOnClickListener {
            isStudentInput = true
            studentPrompt.setText("Insert your valid Student ID")
        }
        nonStudent.setOnClickListener {
            isStudentInput = false
            studentPrompt.setText("Insert a valid ID")
        }

        termsOfService.setOnClickListener {
            requireActivity().supportFragmentManager.beginTransaction()
                .replace(R.id.register_frame, PolicyFragment())
                .commit()
        }

        buttonChoose.setOnClickListener {
            openFileChooser()
        }

        signUpBtn.setOnClickListener(View.OnClickListener {

            val firstName = firstNameTxt.text.toString().trim()
            val lastName = lastNameTxt.text.toString().trim()
            val middleName = middleNameTxt.text.toString().trim()

            val birthday = birthdayText.text.toString().trim()

            val birthYear = try {
                birthday.takeLast(4).toInt()
            } catch (e: Exception) {
                Toast.makeText(requireContext(), "Invalid birthday format!", Toast.LENGTH_SHORT)
                    .show()
                return@OnClickListener
            }

            val currentYear = Calendar.getInstance().get(Calendar.YEAR)
            val age = currentYear - birthYear

            if (age < 7 || age > 120) {
                Toast.makeText(
                    requireContext(),
                    "Age must be between 7 and 120!",
                    Toast.LENGTH_SHORT
                ).show()
                return@OnClickListener
            }

            val address = addressTxt.text.toString().trim()
            val contactNumber =
                contactTxt.text.toString().replace("+", "").replace("-", "").replace(" ", "")
            val email = emailTxt.text.toString().trim()
            val password = passwordTxt.text.toString().trim()
            val confirmPassword = confirmPasswordTxt.text.toString().trim()

            if (!contactNumber.matches(Regex("^\\d{10,11}$"))) {
                Toast.makeText(
                    requireContext(),
                    "Invalid Contact Number. Only 10-11 digits allowed.",
                    Toast.LENGTH_SHORT
                ).show()
                return@OnClickListener
            }

            val selectedGenderId = genderRadioGrp.checkedRadioButtonId
            val gender = if (selectedGenderId != -1) {
                view.findViewById<RadioButton>(selectedGenderId).text.toString()
            } else {
                ""
            }


            val isStudentSelection = studentRadioGrp.checkedRadioButtonId
            if (isStudentSelection == -1) {
                Toast.makeText(requireContext(), "Student Identification Required!", Toast.LENGTH_SHORT)
                    .show()
                return@OnClickListener
            }

            val studentChoice = studentRadioGrp.checkedRadioButtonId
            val isStudent: Boolean
            if (studentChoice != -1) {
                if (studentChoice == 1) {
                    isStudent = true
                } else {
                    isStudent = false
                }
            } else {
                isStudent = false
            }

            val passwordRegex = Regex("^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d).{8,20}\$")
            if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                Toast.makeText(requireContext(), "Enter a Valid Email!", Toast.LENGTH_SHORT).show()
                return@OnClickListener
            }
            if (!password.matches(passwordRegex)) {
                Toast.makeText(
                    requireContext(),
                    "Password must be 8–20 characters long and include uppercase, lowercase, digit, and special character.",
                    Toast.LENGTH_SHORT
                ).show()
                return@OnClickListener
            }
            if (firstName.isEmpty() || lastName.isEmpty() || email.isEmpty() || password.isEmpty() || confirmPassword.isEmpty() || selectedGenderId == 1) {
                Toast.makeText(
                    requireContext(),
                    "Please fill all required fields!",
                    Toast.LENGTH_SHORT
                ).show()
                return@OnClickListener
            }
            if (password != confirmPassword) {
                Toast.makeText(requireContext(), "Passwords do not match!", Toast.LENGTH_SHORT)
                    .show()
                return@OnClickListener
            }
            if (password.length < 3 || password.length > 20) {
                Toast.makeText(requireContext(), "Invalid Password!", Toast.LENGTH_SHORT).show()
                return@OnClickListener
            }

            if (!privacyPolicy.isChecked) {
                Toast.makeText(requireContext(), "Please Read the Terms and Policy!", Toast.LENGTH_SHORT).show()
                return@OnClickListener
            }

            var auth: FirebaseAuth = FirebaseAuth.getInstance()

            auth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        val user = auth.currentUser
                        user?.sendEmailVerification()?.addOnCompleteListener { verifyTask ->
                            if (verifyTask.isSuccessful) {
                                Toast.makeText(
                                    requireContext(),
                                    "Verification email sent. Please check your inbox.",
                                    Toast.LENGTH_LONG
                                ).show()
                                uploadImageToFirebase()
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
                                    putBoolean("studentStatus", isStudent)
                                }

                                val registerEmailFragment = RegisterEmailFragment()
                                registerEmailFragment.arguments = bundle

                                parentFragmentManager.beginTransaction()
                                    .replace(R.id.register_frame, registerEmailFragment)
                                    .commit()

                                auth.signOut()
                            } else {
                                val storage =
                                    FirebaseStorage.getInstance().reference.child("studentIds/${user.uid}")
                                storage.listAll()
                                    .addOnSuccessListener { listResult ->
                                        listResult.items.forEach { fileRef ->
                                            fileRef.delete()
                                                .addOnSuccessListener {
                                                    Log.d("Delete", "Deleted: ${fileRef.name}")
                                                }
                                                .addOnFailureListener { e ->
                                                    Log.e(
                                                        "Delete",
                                                        "Error deleting ${fileRef.name}",
                                                        e
                                                    )
                                                }
                                        }
                                    }
                                    .addOnFailureListener { e ->
                                        Log.e("Delete", "Error listing user files", e)
                                    }
                                user.delete()
                                Toast.makeText(
                                    requireContext(),
                                    "Failed to send verification email: ${verifyTask.exception?.message}",
                                    Toast.LENGTH_LONG
                                ).show()
                            }
                        }
                    } else {
                        Toast.makeText(
                            requireContext(),
                            "Registration Failed: ${task.exception?.message}",
                            Toast.LENGTH_LONG
                        ).show()
                    }
                }
        })
    }

    val PICK_IMAGE_REQUEST = 1

    fun openFileChooser() {
        val intent = Intent()
        intent.type = "image/*"
        intent.action = Intent.ACTION_GET_CONTENT
        startActivityForResult(intent, PICK_IMAGE_REQUEST)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null && data.data != null) {
            imageUri = data.data
        }
        imageView.setImageURI(imageUri)
    }

    fun uploadImageToFirebase() {
        val auth = FirebaseAuth.getInstance()
        val user = auth.currentUser
        if (user != null) {

            if (imageUri != null) {
                val storageRef = FirebaseStorage.getInstance().reference
                val folder = if (isStudentInput) "studentId" else "validIds"

                val fileRef =
                    storageRef.child("${folder}/${user.uid}/${System.currentTimeMillis()}.jpg")

                val uploadTask = fileRef.putFile(imageUri!!)
                uploadTask.addOnSuccessListener {
                    fileRef.downloadUrl.addOnSuccessListener { uri ->
                        if (!isAdded) return@addOnSuccessListener
                        val downloadUrl = uri.toString()
                        context?.let {
                            Toast.makeText(it, "Upload successful", Toast.LENGTH_SHORT).show()
                        }
                    }
                }.addOnFailureListener {
                    Toast.makeText(
                        requireContext(),
                        "Upload failed: ${it.message}",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            } else {
                Toast.makeText(requireContext(), "No image selected", Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_register, container, false)

    }

}