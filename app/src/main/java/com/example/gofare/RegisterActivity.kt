package com.example.gofare

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.EditText
import android.widget.RadioButton
import android.widget.RadioGroup
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import at.favre.lib.crypto.bcrypt.BCrypt
import com.google.android.gms.common.internal.Objects
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase


class RegisterActivity : AppCompatActivity() {

    lateinit var toLogin : com.google.android.material.button.MaterialButton
    lateinit var firstNameTxt : EditText
    lateinit var lastNameTxt : EditText
    lateinit var middleNameTxt : EditText
    lateinit var ageTxt : EditText
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
        ageTxt = findViewById(R.id.ageEditText)
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

        signUpBtn.setOnClickListener(View.OnClickListener {
            val firstName = firstNameTxt.text.toString().trim()
            val lastName = lastNameTxt.text.toString().trim()
            val middleName = middleNameTxt.text.toString().trim()
            val age = ageTxt.text.toString().trim()
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

            var auth: FirebaseAuth = FirebaseAuth.getInstance()
            auth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        val userId = auth.currentUser?.uid
                        if (userId != null) {
                            saveUserToDatabase(userId, firstName, lastName, middleName, age, address, gender, contactNumber, email, password)
                        }
                        Toast.makeText(this, "Registration Successful!", Toast.LENGTH_SHORT).show()
                    } else {
                        Toast.makeText(this, "Registration Failed: ${task.exception?.message}", Toast.LENGTH_LONG).show()
                    }
                }
        })
    }
    private fun saveUserToDatabase(userId: String, firstName: String, lastName: String, middleName: String, age: String, address: String, gender: String, contactNo : String, email : String, password : String) {
        val databaseRef = FirebaseDatabase.getInstance().getReference("ClientReference").child(userId)

        val walletData = mapOf(
            "balance" to 0,
            "currency" to "PHP",
            "status" to "default",
            "loanedAmount" to 0
        )

        val userData = mapOf(
            "firstName" to firstName,
            "lastName" to lastName,
            "middleName" to middleName,
            "age" to age,
            "address" to address,
            "gender" to gender,
            "rfid" to null,
            "contactNumber" to contactNo,
            "email" to email,
            "accountStatus" to "active",
            "wallet" to walletData
        )

        databaseRef.setValue(userData)
            .addOnSuccessListener {
                Toast.makeText(this, "Success", Toast.LENGTH_SHORT).show()
                Log.d("Register", "User Registration Successful")
                startActivity(Intent(this, MainActivity::class.java))
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Failed", Toast.LENGTH_SHORT).show()
                Log.d("Register", "User Registration Successful")
            }

    }
}