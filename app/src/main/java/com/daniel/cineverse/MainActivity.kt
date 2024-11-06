package com.daniel.cineverse

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.FrameLayout
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.FragmentTransaction

class MainActivity : AppCompatActivity() {

    private val savedMovies = mutableListOf<Movie>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        if (savedInstanceState != null) {
            val restoredMovies = savedInstanceState.getParcelableArrayList<Movie>("saved_movies")
            if (restoredMovies != null) {
                savedMovies.clear()
                savedMovies.addAll(restoredMovies)
            }
        }

        val imgForm: ImageView = findViewById(R.id.salvar)
        val imgDisplay: ImageView = findViewById(R.id.menu)

        imgForm.setOnClickListener {
            openFormFragment()
        }

        imgDisplay.setOnClickListener {
            openSavedMoviesActivity()
        }

        configureMovieList()
    }

    private fun configureMovieClick(imageView: ImageView, movie: Movie) {
        imageView.setOnClickListener {
            imageView.animate()
                .scaleX(1.2f)
                .scaleY(1.2f)
                .setDuration(400)
                .withEndAction {
                    imageView.animate()
                        .scaleX(1.0f)
                        .scaleY(1.0f)
                        .setDuration(400)
                        .withEndAction {
                            openDetailActivity(movie)
                        }
                        .start()
                }
                .start()
        }
    }

    private fun openFormFragment() {
        val fragmentContainer = findViewById<FrameLayout>(R.id.fragment_container)
        fragmentContainer.visibility = View.VISIBLE

        val fragment = FormFragment()
        supportFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, fragment)
            .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE)
            .addToBackStack(null)
            .commit()
    }

    private fun openSavedMoviesActivity() {
        val intent = Intent(this, DisplayActivity::class.java)
        intent.putParcelableArrayListExtra("saved_movies", ArrayList(savedMovies))
        startActivity(intent)
    }

    private fun openDetailActivity(movie: Movie) {
        val intent = Intent(this, DetalheSalvos::class.java).apply {
            putExtra("movie", movie)
        }
        startActivity(intent)
    }

    private fun configureMovieList() {

        val movies = listOf(
            Movie("Coringa 2", R.drawable.capa),
            Movie("Interestelar", R.drawable.ficcao),
            Movie("Passageiros", R.drawable.ficcao3),
            Movie("Moonfall", R.drawable.ficcao2),
            Movie("Perdido em Marte", R.drawable.ficcao4),
            Movie("Gravidade", R.drawable.ficcao5),
            Movie("Avatar", R.drawable.ficcao6),
            Movie("Tomb Raider", R.drawable.acao),
            Movie("John Wick 4", R.drawable.acao2),
            Movie("Furiosa", R.drawable.acao3),
            Movie("Batman", R.drawable.acao4),
            Movie("Deadpool Wolverine", R.drawable.acao5),
            Movie("Resgate 2", R.drawable.acao6)
        )

        val imageViews = listOf(
            R.id.coringa_2_img,
            R.id.interstellar_img,
            R.id.passengers_img,
            R.id.moonfall_img,
            R.id.perdido_em_marte_img,
            R.id.gravidade_img,
            R.id.avatar_img,
            R.id.tombraider_img,
            R.id.johnwick4_img,
            R.id.furiosa_img,
            R.id.batman_img,
            R.id.deadpool_wolverine_img,
            R.id.resgate_2_img
        )

        for (i in movies.indices) {
            val imageView: ImageView = findViewById(imageViews[i])
            configureMovieClick(imageView, movies[i])
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)

        outState.putParcelableArrayList("saved_movies", ArrayList(savedMovies))
    }

    override fun onRestoreInstanceState(savedInstanceState: Bundle) {
        super.onRestoreInstanceState(savedInstanceState)

        val restoredMovies = savedInstanceState.getParcelableArrayList<Movie>("saved_movies")
        if (restoredMovies != null) {
            savedMovies.clear()
            savedMovies.addAll(restoredMovies)
        }
    }
}
