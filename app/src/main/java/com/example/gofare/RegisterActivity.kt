package com.example.gofare

import android.app.DatePickerDialog
import android.content.Intent
import android.icu.util.Calendar
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.EditText
import android.widget.RadioButton
import android.widget.RadioGroup
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import at.favre.lib.crypto.bcrypt.BCrypt
import com.google.android.gms.common.internal.Objects
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.firestore.*
import java.time.LocalDate
import java.time.LocalDate.now
import java.util.Date
import java.util.Locale
import com.google.firebase.Timestamp


class RegisterActivity : AppCompatActivity() {

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
        enableEdgeToEdge()
        setContentView(R.layout.activity_register)

        toLogin = findViewById(R.id.toLogin)
        firstNameTxt = findViewById(R.id.firstNameEditText)
        lastNameTxt = findViewById(R.id.lastNameEditText)
        middleNameTxt = findViewById(R.id.middleNameEditText)
        birthdayText = findViewById(R.id.birthdayEditText)
        addressTxt = findViewById(R.id.addressEditText)
        genderRadioGrp = findViewById(R.id.genderRadioGrp)
        contactTxt = findViewById(R.id.contactEditText)
        emailTxt = findViewById(R.id.emailEditText)
        passwordTxt = findViewById(R.id.passwordEditText)
        confirmPasswordTxt = findViewById(R.id.confirmPasswordEditText)
        signUpBtn = findViewById(R.id.signUpBtn)

        toLogin.setOnClickListener(View.OnClickListener {
            val intent = Intent(
                this@RegisterActivity,
                MainActivity::class.java
            )
            startActivity(intent)
        });

        birthdayText.setOnClickListener {
            val calendar = Calendar.getInstance()
            val year = calendar.get(Calendar.YEAR)
            val month = calendar.get(Calendar.MONTH)
            val day = calendar.get(Calendar.DAY_OF_MONTH)

            val datePickerDialog = DatePickerDialog(this,
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
                findViewById<RadioButton>(selectedGenderId).text.toString()
            } else {
                ""
            }

            if (firstName.isEmpty() || lastName.isEmpty() || email.isEmpty() || password.isEmpty() || confirmPassword.isEmpty() || selectedGenderId == 1) {
                Toast.makeText(this, "Please fill all required fields!", Toast.LENGTH_SHORT).show()
                return@OnClickListener
            }
            if (password != confirmPassword) {
                Toast.makeText(this, "Passwords do not match!", Toast.LENGTH_SHORT).show()
                return@OnClickListener
            }
            if (password.length < 3 || password.length > 20)
            {
                Toast.makeText(this, "Invalid Password!", Toast.LENGTH_SHORT).show()
                return@OnClickListener
            }

            var auth: FirebaseAuth = FirebaseAuth.getInstance()

            auth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        val userId = auth.currentUser?.uid
                        if (userId != null) {
                            saveUserToDatabase(userId, firstName, lastName, middleName, birthday,  age, address, gender, contactNumber, email, password)
                        }
                        Toast.makeText(this, "Registration Successful!", Toast.LENGTH_SHORT).show()
                    } else {
                        Toast.makeText(this, "Registration Failed: ${task.exception?.message}", Toast.LENGTH_LONG).show()
                    }
                }
        })
    }
    private fun saveUserToDatabase(userId: String, firstName: String, lastName: String, middleName: String, birthday : String, age: Int, address: String, gender: String, contactNo : String, email : String, password : String) {
        val db = FirebaseFirestore.getInstance()
        val userRef = db.collection("Users").document(userId)
        val walletRef = db.collection("UserWallet").document(userId)
        val rfidRef = db.collection("UserRFID").document(userId)
        val transactionsRef = db.collection("UserTransaction").document(userId)

        val userData = mapOf(
            "firstName" to firstName,
            "lastName" to lastName,
            "middleName" to middleName,
            "age" to age,
            "birthday" to birthday,
            "address" to address,
            "gender" to gender,
            "contactNumber" to contactNo,
            "email" to email,
            "creationDate" to Timestamp.now(),
            "updateDate" to Timestamp.now(),
            "enabled" to true
        )

        val walletData = mapOf(
            "balance" to 0,
            "currency" to "PHP",
            "loaned" to false,
            "loanedAmount" to 0,
        )

        val rfidData = mapOf(
            "registeredAt" to "",
            "renewedAt" to "",
            "rfid" to "",
        )

        userRef.set(userData).addOnSuccessListener {
            walletRef.set(walletData)
            rfidRef.set(rfidData)
            transactionsRef.set(emptyMap<String, Any>())

            Toast.makeText(this, "Success", Toast.LENGTH_SHORT).show()
            Log.d("Register", "User Registration Successful")
            startActivity(Intent(this, MainActivity::class.java))
        }.addOnFailureListener { e ->
            Toast.makeText(this, "Failed: ${e.message}", Toast.LENGTH_SHORT).show()
            Log.e("Register", "User Registration Failed", e)
        }

    }
}