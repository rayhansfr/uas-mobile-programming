package com.example.uasrayhansafarti4a

import android.content.Intent
import android.os.Bundle
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class LoginActivity : AppCompatActivity() {
    private lateinit var dbReference: DatabaseReference

    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_login)

        val usernameText = findViewById<EditText>(R.id.userLogin)
        val passText = findViewById<EditText>(R.id.passwordLogin)

        val btnToRegister = findViewById<TextView>(R.id.btnToRegister)
        val btnLogin = findViewById<TextView>(R.id.btnLogin)

        val dbURL = "https://uas-mobile-programming-e74b0-default-rtdb.asia-southeast1.firebasedatabase.app/"
        dbReference = FirebaseDatabase.getInstance(dbURL).reference.child("users")

        btnToRegister.setOnClickListener{
            startActivity(Intent(this, RegisterActivity::class.java))
            finish()
        }

        btnLogin.setOnClickListener{

            val username = usernameText.text.toString()
            val password = passText.text.toString()

            if (username.isEmpty() || password.isEmpty()) {
                Toast.makeText(applicationContext, "Please fill username and password", Toast.LENGTH_SHORT).show()
            } else {
                // Check Firebase for user login credentials
                dbReference.addListenerForSingleValueEvent(object : ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {
                        if (snapshot.child(username).exists()) {
                            val storedPassword = snapshot.child(username).child("password").getValue(String::class.java)
                            if (storedPassword == password) {
                                Toast.makeText(applicationContext, "Login Success", Toast.LENGTH_SHORT).show()
                                val berhasil = Intent(applicationContext, MainActivity::class.java)
                                startActivity(berhasil)
                            } else {
                                Toast.makeText(applicationContext, "Incorrect Password", Toast.LENGTH_SHORT).show()
                            }
                        } else {
                            Toast.makeText(applicationContext, "User Not Found", Toast.LENGTH_SHORT).show()
                        }
                    }
                    override fun onCancelled(error: DatabaseError) {
                        Toast.makeText(applicationContext, "Error: ${error.message}", Toast.LENGTH_SHORT).show()
                    }
                })
            }
        }
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
    }
}