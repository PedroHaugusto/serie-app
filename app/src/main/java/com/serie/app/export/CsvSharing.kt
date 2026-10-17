package com.serie.app.export

import android.content.Context
import android.content.Intent
import androidx.core.content.FileProvider
import com.serie.app.domain.CsvExport
import java.io.File

/**
 * Grava os CSV e entrega ao compartilhamento do Android.
 *
 * O app nao escolhe onde salvar: abre a folha de compartilhamento e deixa o
 * usuario mandar para o Drive, o e-mail ou os Arquivos. Escrever direto em
 * Downloads exigiria permissao de armazenamento para resolver menos.
 */
object CsvSharing {

    private const val FOLDER = "exportacoes"

    /** Devolve falso quando nao ha nenhum app capaz de receber os arquivos. */
    fun share(context: Context, documents: List<CsvExport.Document>): Boolean {
        val folder = File(context.cacheDir, FOLDER).apply { mkdirs() }
        val uris = documents.map { document ->
            val file = File(folder, document.name)
            // O BOM e para o Excel: sem ele, ele le o arquivo no code page do
            // Windows e "Repetições" chega como "Repetiçµes".
            file.writeText("﻿" + document.content)
            FileProvider.getUriForFile(context, context.packageName + ".arquivos", file)
        }

        val send = Intent(Intent.ACTION_SEND_MULTIPLE).apply {
            type = "text/csv"
            putParcelableArrayListExtra(Intent.EXTRA_STREAM, ArrayList(uris))
            putExtra(Intent.EXTRA_SUBJECT, "Dados da série")
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        val chooser = Intent.createChooser(send, "Exportar dados")
            .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            // A permissao de leitura tambem vai no seletor: ele e quem lanca o
            // aplicativo escolhido, e sem ela o destino recebe um content://
            // que nao pode abrir.
            .addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        return try {
            context.startActivity(chooser)
            true
        } catch (e: android.content.ActivityNotFoundException) {
            false
        }
    }
}
