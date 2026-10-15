package com.serie.app

import android.app.Application
import com.serie.app.data.SerieRepository
import com.serie.app.data.local.SerieDatabase

/**
 * Dono do banco e do repositorio enquanto o processo vive.
 *
 * E injecao de dependencia manual: com um repositorio so, um container de DI
 * custaria mais em configuracao do que resolve. Se a lista crescer, este e o
 * ponto onde um Hilt entraria.
 */
class SerieApplication : Application() {

    val repository: SerieRepository by lazy {
        SerieRepository(SerieDatabase.get(this))
    }
}
