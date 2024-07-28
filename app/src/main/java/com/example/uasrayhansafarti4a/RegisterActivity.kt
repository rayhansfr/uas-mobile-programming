package com.example.uasrayhansafarti4a

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase

class RegisterActivity : AppCompatActivity() {

    private lateinit var dbReference: DatabaseReference

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_register)

        val usernameText = findViewById<EditText>(R.id.userReg)
        val passText = findViewById<EditText>(R.id.passwordReg)
        val btnRegister = findViewById<Button>(R.id.btnRegister)
        val btnToLogin = findViewById<TextView>(R.id.btnToLogin)

        val dbURL = "https://uas-mobile-programming-e74b0-default-rtdb.asia-southeast1.firebasedatabase.app/"
        dbReference = FirebaseDatabase.getInstance(dbURL).reference.child("users")

        btnRegister.setOnClickListener {
            val username = usernameText.text.toString()
            val password = passText.text.toString()

            if (username.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Please fill username and password", Toast.LENGTH_SHORT).show()
            } else {
                dbReference.child(username).child("username").setValue(username)
                dbReference.child(username).child("password").setValue(password)

                Toast.makeText(this, "Registration Success", Toast.LENGTH_SHORT).show()
                startActivity(Intent(this, MainActivity::class.java))
                finish()
            }
        }

        btnToLogin.setOnClickListener{
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
        }

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
    }
}