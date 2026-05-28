package com.biancaputri.pos.activity

import android.content.Intent
import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.biancaputri.pos.R
import com.biancaputri.pos.model.ModelAkun
import com.google.firebase.database.FirebaseDatabase

class RegisterActivity : AppCompatActivity() {

    private lateinit var etNama: EditText
    private lateinit var etNomor: EditText
    private lateinit var etEmail: EditText
    private lateinit var etGithub: EditText
    private lateinit var etPassword: EditText
    private lateinit var btnDaftar: Button
    private lateinit var tvLogin: TextView

    private val database = FirebaseDatabase.getInstance(
        "https://cafebianca-default-rtdb.asia-southeast1.firebasedatabase.app/"
    )
    private val akunRef = database.getReference("akun")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)

        etNama = findViewById(R.id.etNama)
        etNomor = findViewById(R.id.etNomor)
        etEmail = findViewById(R.id.etEmail)
        etGithub = findViewById(R.id.etGithub)
        etPassword = findViewById(R.id.etPassword)
        btnDaftar = findViewById(R.id.btnDaftar)
        tvLogin = findViewById(R.id.tvLogin)

        btnDaftar.setOnClickListener {
            val nama = etNama.text.toString().trim()
            val nomor = etNomor.text.toString().trim()
            val email = etEmail.text.toString().trim()
            val github = etGithub.text.toString().trim()
            val password = etPassword.text.toString().trim()

            if (nama.isEmpty()) {
                etNama.error = getString(R.string.nama_kosong)
                etNama.requestFocus()
                return@setOnClickListener
            }
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
            if (password.length < 6) {
                etPassword.error = getString(R.string.password_minimal)
                etPassword.requestFocus()
                return@setOnClickListener
            }

            daftarAkun(nama, nomor, email, github, password)
        }

        tvLogin.setOnClickListener { finish() }
    }

    private fun daftarAkun(nama: String, nomor: String, email: String, github: String, password: String) {
        btnDaftar.isEnabled = false
        btnDaftar.text = getString(R.string.proses_daftar)

        val id = akunRef.push().key ?: return

        val akun = ModelAkun(
            idAkun = id,
            nama = nama,
            nomor = nomor,
            email = email,
            github = github,
            password = password
        )

        akunRef.child(id).setValue(akun).addOnSuccessListener {
            Toast.makeText(this, getString(R.string.daftar_berhasil), Toast.LENGTH_SHORT).show()

            val intent = Intent(this, LoginActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
            finish()
        }.addOnFailureListener {
            Toast.makeText(this, getString(R.string.daftar_gagal), Toast.LENGTH_SHORT).show()
            btnDaftar.isEnabled = true
            btnDaftar.text = getString(R.string.daftar)
        }
    }
}