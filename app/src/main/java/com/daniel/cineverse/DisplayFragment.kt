package com.daniel.cineverse

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment

class DisplayFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        parentFragmentManager.popBackStack()

        val view = inflater.inflate(R.layout.fragment_display, container, false)

        val textViewDisplay: TextView = view.findViewById(R.id.textViewDisplay)

        val fragmentContainer = requireActivity().findViewById<View>(R.id.fragment_container)
        fragmentContainer?.visibility = View.GONE

        val name = arguments?.getString("name") ?: "Nome não disponível"
        val email = arguments?.getString("email") ?: "Email não disponível"
        val gender = arguments?.getString("gender") ?: "Gênero não disponível"

        textViewDisplay.text = "Nome: $name\nEmail: $email\nGênero: $gender"

        return view
    }
}
