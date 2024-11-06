package com.daniel.cineverse

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class FormFragment : Fragment() {

    private lateinit var auth: FirebaseAuth

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_form, container, false)

        auth = FirebaseAuth.getInstance()

        val btnSubmit: Button = view.findViewById(R.id.btnSubmit)
        val nameInput: EditText = view.findViewById(R.id.editTextName)
        val emailInput: EditText = view.findViewById(R.id.editTextEmail)
        val passwordInput: EditText = view.findViewById(R.id.editTextPassword)

        btnSubmit.setOnClickListener {
            val name = nameInput.text.toString()
            val email = emailInput.text.toString()
            val password = passwordInput.text.toString()

            if (name.isNotEmpty() && email.isNotEmpty() && password.isNotEmpty()) {
                Toast.makeText(requireContext(), "Nome: $name, Email: $email", Toast.LENGTH_SHORT)
                    .show()

                sendDataToDisplayFragment(name, email)

                createUserInFirebaseAuth(name, email, password)
            } else {
                Toast.makeText(
                    requireContext(),
                    "Por favor, preencha todos os campos",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }

        return view
    }

    private fun sendDataToDisplayFragment(name: String, email: String) {
        val bundle = Bundle().apply {
            putString("name", name)
            putString("email", email)
        }

        val displayFragment = DisplayFragment().apply {
            arguments = bundle
        }

        parentFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, displayFragment)
            .addToBackStack(null)
            .commit()
    }

    private fun createUserInFirebaseAuth(name: String, email: String, password: String) {
        auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    saveDataToFirebase(name, email, password)
                } else {
                    Toast.makeText(
                        requireContext(),
                        "Erro ao criar usuário: ${task.exception?.message}",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
    }

    private fun saveDataToFirebase(name: String, email: String, password: String) {
        val db = FirebaseFirestore.getInstance()
        val userData = hashMapOf(
            "name" to name,
            "email" to email,
            "password" to password
        )

        db.collection("usuarios")
            .add(userData)
            .addOnSuccessListener {
                Toast.makeText(
                    requireContext(),
                    "Dados salvos no Firebase com sucesso!",
                    Toast.LENGTH_SHORT
                ).show()
            }
            .addOnFailureListener { e ->
                Toast.makeText(
                    requireContext(),
                    "Erro ao salvar dados: ${e.message}",
                    Toast.LENGTH_SHORT
                ).show()
            }
    }
}
