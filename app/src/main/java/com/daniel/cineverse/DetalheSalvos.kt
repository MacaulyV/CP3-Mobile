package com.daniel.cineverse

import android.content.Context
import android.os.Bundle
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import android.net.Uri
import com.google.common.reflect.TypeToken
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.google.gson.Gson

class DetalheSalvos : AppCompatActivity() {

    companion object {
        private const val SHARED_PREFS_NAME = "movie_prefs"
        private const val SAVED_MOVIES_KEY = "saved_movies"
        val savedMovies = mutableListOf<Movie>()
    }

    private val selectedMovies = mutableListOf<Movie>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_detalhe_salvos)

        restoreSavedMovies()

        val movie: Movie? = intent.getParcelableExtra("movie")

        val movieNameTextView: TextView = findViewById(R.id.movie_name)
        val movieImageView: ImageView = findViewById(R.id.movie_image)
        val btnSalvar: Button = findViewById(R.id.btnSalvar)
        val btnVoltar: Button = findViewById(R.id.btnVoltar)
        val btnExcluir: Button = findViewById(R.id.btnExcluir)
        val btnSendFormData: Button = findViewById(R.id.btnFormulario)
        val editTextUserComment: EditText = findViewById(R.id.editTextUserComment)
        val spinnerMovieGenre: Spinner = findViewById(R.id.spinnerMovieGenre)

        val genres = arrayOf("Ação", "Comédia", "Drama", "Ficção Científica", "Terror")
        val spinnerAdapter =
            ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, genres)
        spinnerMovieGenre.adapter = spinnerAdapter

        movie?.let { selectedMovie ->
            movieNameTextView.text = selectedMovie.name
            movieImageView.setImageResource(selectedMovie.imageResId)

            btnSalvar.setOnClickListener {
                val movieAlreadySaved =
                    savedMovies.any { savedMovie -> savedMovie.name == selectedMovie.name }
                if (!movieAlreadySaved) {
                    selectedMovie.comment = editTextUserComment.text.toString()
                    savedMovies.add(selectedMovie)
                    addMovieToSavedList(selectedMovie)
                    saveMovieToFirebase(selectedMovie)
                    saveMoviesToSharedPreferences()
                    Toast.makeText(this, "Filme salvo com comentário!", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(this, "Esse filme já está salvo!", Toast.LENGTH_SHORT).show()
                }
            }
        }

        spinnerMovieGenre.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>,
                view: View?,
                position: Int,
                id: Long
            ) {
                val selectedGenre = genres[position]
                Toast.makeText(
                    this@DetalheSalvos,
                    "Gênero selecionado: $selectedGenre",
                    Toast.LENGTH_SHORT
                ).show()
            }

            override fun onNothingSelected(parent: AdapterView<*>) {}
        }

        btnSendFormData.setOnClickListener {
            val comment = editTextUserComment.text.toString().trim()
            if (comment.isNotEmpty()) {
                Toast.makeText(this, "Comentário enviado: $comment", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(this, "Por favor, insira um comentário.", Toast.LENGTH_SHORT).show()
            }
        }

        btnExcluir.setOnClickListener {
            deleteSelectedMoviesFromFirebase()
        }

        btnVoltar.setOnClickListener {
            finish()
        }

        showSavedMovies()
    }

    private fun showSavedMovies() {
        val savedMoviesLayout: LinearLayout = findViewById(R.id.saved_movies_layout)
        savedMoviesLayout.removeAllViews()

        for (movie in savedMovies) {
            addMovieToSavedList(movie)
        }
    }

    private fun addMovieToSavedList(movie: Movie) {
        val savedMoviesLayout: LinearLayout = findViewById(R.id.saved_movies_layout)

        val movieLayout = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                setMargins(16, 0, 16, 0)
            }
        }

        val movieImageView = ImageView(this).apply {
            setImageResource(movie.imageResId)
            layoutParams = LinearLayout.LayoutParams(200, 300)
        }

        val movieNameTextView = TextView(this).apply {
            text = movie.name
            setTextColor(resources.getColor(android.R.color.white))
            textAlignment = TextView.TEXT_ALIGNMENT_CENTER
        }

        val movieCommentTextView = TextView(this).apply {
            text = movie.comment ?: "Sem comentário"
            setTextColor(resources.getColor(android.R.color.darker_gray))
            textAlignment = TextView.TEXT_ALIGNMENT_CENTER
        }

        val movieCheckBox = CheckBox(this).apply {
            setOnCheckedChangeListener { _, isChecked ->
                if (isChecked) {
                    selectedMovies.add(movie)
                } else {
                    selectedMovies.remove(movie)
                }
            }
        }

        movieLayout.addView(movieImageView)
        movieLayout.addView(movieNameTextView)
        movieLayout.addView(movieCommentTextView)
        movieLayout.addView(movieCheckBox)

        savedMoviesLayout.addView(movieLayout)
    }

    private fun saveMoviesToSharedPreferences() {
        val sharedPreferences = getSharedPreferences(SHARED_PREFS_NAME, Context.MODE_PRIVATE)
        val editor = sharedPreferences.edit()
        val gson = Gson()

        val moviesJson = gson.toJson(savedMovies)
        editor.putString(SAVED_MOVIES_KEY, moviesJson)
        editor.apply()
    }

    private fun restoreSavedMovies() {
        val sharedPreferences = getSharedPreferences(SHARED_PREFS_NAME, Context.MODE_PRIVATE)
        val gson = Gson()


        val moviesJson = sharedPreferences.getString(SAVED_MOVIES_KEY, null)
        if (moviesJson != null) {
            val movieListType = object : TypeToken<MutableList<Movie>>() {}.type
            val restoredMovies: MutableList<Movie> = gson.fromJson(moviesJson, movieListType)
            savedMovies.clear()
            savedMovies.addAll(restoredMovies)
        }
    }

    private fun deleteSelectedMoviesFromFirebase() {
        if (selectedMovies.isEmpty()) {
            Toast.makeText(this, "Nenhum filme selecionado para exclusão.", Toast.LENGTH_SHORT)
                .show()
            return
        }

        val db = FirebaseFirestore.getInstance()
        val storage = FirebaseStorage.getInstance()
        val storageRef: StorageReference = storage.reference

        for (movie in selectedMovies) {
            db.collection("movies")
                .whereEqualTo("name", movie.name)
                .get()
                .addOnSuccessListener { documents ->
                    for (document in documents) {
                        db.collection("movies").document(document.id).delete()
                            .addOnSuccessListener {
                                val imageRef = storageRef.child("movies/${movie.name}.jpg")
                                imageRef.delete().addOnSuccessListener {
                                    Toast.makeText(
                                        this,
                                        "Filme '${movie.name}' excluído com sucesso!",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                }.addOnFailureListener {
                                    Toast.makeText(
                                        this,
                                        "Erro ao excluir imagem do Storage",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                }
                            }.addOnFailureListener {
                                Toast.makeText(
                                    this,
                                    "Erro ao excluir filme no Firebase Firestore",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                    }
                }.addOnFailureListener {
                    Toast.makeText(this, "Erro ao encontrar filme no Firestore", Toast.LENGTH_SHORT)
                        .show()
                }
        }

        savedMovies.removeAll(selectedMovies)
        selectedMovies.clear()
        saveMoviesToSharedPreferences()
        showSavedMovies()
    }

    private fun saveMovieToFirebase(movie: Movie) {
        val db = FirebaseFirestore.getInstance()
        val storage = FirebaseStorage.getInstance()
        val storageRef: StorageReference = storage.reference

        val imageUri = Uri.parse("android.resource://$packageName/${movie.imageResId}")

        val imageRef = storageRef.child("movies/${movie.name}.jpg")

        val uploadTask = imageRef.putFile(imageUri)
        uploadTask.addOnSuccessListener {
            imageRef.downloadUrl.addOnSuccessListener { uri ->
                val movieData = hashMapOf(
                    "name" to movie.name,
                    "imageUrl" to uri.toString(),
                    "comment" to movie.comment
                )

                db.collection("movies").add(movieData)
                    .addOnSuccessListener {
                        Toast.makeText(
                            this,
                            "Filme salvo no Firebase com sucesso!",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                    .addOnFailureListener { e ->
                        Toast.makeText(
                            this,
                            "Erro ao salvar o filme no Firebase",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
            }
        }.addOnFailureListener {
            Toast.makeText(this, "Erro ao fazer upload da imagem", Toast.LENGTH_SHORT).show()
        }
    }
}
