package com.biancaputri.pos.activity

import android.content.Intent
import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.biancaputri.pos.R
import com.biancaputri.pos.model.ModelAkun
import com.google.firebase.database.*

class LoginActivity : AppCompatActivity() {

    private lateinit var etEmail: EditText
    private lateinit var etPassword: EditText
    private lateinit var btnLogin: Button
    private lateinit var tvRegister: TextView

    private val database = FirebaseDatabase.getInstance(
        "https://cafebianca-default-rtdb.asia-southeast1.firebasedatabase.app/"
    )
    private val akunRef = database.getReference("akun")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val prefs = getSharedPreferences("session", MODE_PRIVATE)
        val savedEmail = prefs.getString("email", null)
        val idAkun = prefs.getString("idAkun", null)
        if (savedEmail != null && !idAkun.isNullOrEmpty()) {
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
            finish()
            return
        } else {
            prefs.edit().clear().apply()
        }

        setContentView(R.layout.activity_login)

        etEmail = findViewById(R.id.etEmail)
        etPassword = findViewById(R.id.etPassword)
        btnLogin = findViewById(R.id.btnLogin)
        tvRegister = findViewById(R.id.tvRegister)

        tvRegister.setOnClickListener {
            val intent = Intent(this, RegisterActivity::class.java)
            startActivity(intent)
        }

        btnLogin.setOnClickListener {
            val email = etEmail.text.toString().trim()
            val password = etPassword.text.toString().trim()

            if (email.isEmpty()) {
                etEmail.error = getString(R.string.email_kosong)
                etEmail.requestFocus()
                return@setOnClickListener
            }

            if (password.isEmpty()) {
                etPassword.error = getString(R.string.password_kosong)
                etPassword.requestFocus()
                return@setOnClickListener
            }

            loginProcess(email, password)
        }
    }

    private fun loginProcess(email: String, password: String) {
        btnLogin.isEnabled = false
        btnLogin.text = getString(R.string.proses_masuk)

        akunRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                var found = false
                var akunLogin: ModelAkun? = null

                for (data in snapshot.children) {
                    val akun = data.getValue(ModelAkun::class.java)
                    if (akun != null && akun.email == email && akun.password == password) {
                        found = true
                        val finalId = if (akun.idAkun.isNullOrEmpty()) data.key else akun.idAkun
                        akunLogin = akun.copy(idAkun = finalId)
                        break
                    }
                }

                if (found && akunLogin != null) {
                    val prefs = getSharedPreferences("session", MODE_PRIVATE)
                    prefs.edit()
                        .putString("idAkun", akunLogin.idAkun)
                        .putString("nama", akunLogin.nama)
                        .putString("email", akunLogin.email)
                        .putString("nomor", akunLogin.nomor)
                        .putString("github", akunLogin.github)
                        .apply()

                    Toast.makeText(this@LoginActivity, getString(R.string.login_berhasil), Toast.LENGTH_SHORT).show()

                    val intent = Intent(this@LoginActivity, MainActivity::class.java)
                    intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                    startActivity(intent)
                    finish()
                } else {
                    Toast.makeText(
                        this@LoginActivity,
                        getString(R.string.login_gagal),
                        Toast.LENGTH_SHORT
                    ).show()
                    btnLogin.isEnabled = true
                    btnLogin.text = getString(R.string.masuk)
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(
                    this@LoginActivity,
                    "Error: ${error.message}",
                    Toast.LENGTH_SHORT
                ).show()
                btnLogin.isEnabled = true
                btnLogin.text = getString(R.string.masuk)
            }
        })
    }
}