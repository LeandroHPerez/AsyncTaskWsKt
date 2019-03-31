package br.edu.ifsp.scl.asynctaskwskt

import android.os.AsyncTask
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import br.edu.ifsp.scl.asynctaskwskt.MainActivity.constantes.URL_BASE
import kotlinx.android.synthetic.main.activity_main.*
import org.jetbrains.anko.toast
import java.io.BufferedInputStream
import java.io.IOException
import java.lang.Thread.sleep
import java.net.HttpURLConnection
import java.net.URL

/*
obs:
http://www.nobile.pro.br/sdm/texto.php irá retornar: Teste de recuperação de texto

http://www.nobile.pro.br/sdm/data.php  irá retornar:
{"seconds":30,"minutes":27,"hours":19,"mday":30,"wday":6,"mon":3,"year":2019,"yday":88,"weekday":"Saturday","month":"March","0":1553984850}

*/



class MainActivity : AppCompatActivity() {
    object constantes {
        val URL_BASE = "http://www.nobile.pro.br/sdm/"
            }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        // Seta o Listener para o botão usando uma função lambda
        buscarInformacoesBt.setOnClickListener {
            // Disparando AsyncTask para buscar texto
            val buscarTextoAt = BuscarTextoAt()
            buscarTextoAt.execute(URL_BASE + "texto.php")
        }
    }
    // AsyncTask que fará acesso ao WebService
    private inner class BuscarTextoAt : AsyncTask<String, Int, String>() {
        // Executa na mesma Thread de UI
        override fun onPreExecute() {
            super.onPreExecute()
            toast("Buscando String no Web Service")
            // Mostrando a barra de progresso
            progressBar.visibility = View.VISIBLE
        }
        // Executa numa outra Thread em background
        override fun doInBackground(vararg params: String?): String {
            // Pegando URL na primeira posição do params
            val url = params[0]
            // Criando um StringBuffer para receber a resposta do Web Service
            val stringBufferResposta: StringBuffer = StringBuffer()
            try {
                // Criando uma conexão HTTP a partir da URL
                val conexao = URL(url).openConnection() as HttpURLConnection

                if (conexao.responseCode == HttpURLConnection.HTTP_OK) {
                    // Caso a conexão seja bem sucedida, resgata o InputStream da mesma
                    val inputStream = conexao.inputStream
                    // Cria um BufferedReader a partir do InputStream
                    val bufferedReader = BufferedInputStream(inputStream).bufferedReader()
                    // Lê o bufferedReader para uma lista de Strings
                    val respostaList = bufferedReader.readLines()
                    // "Appenda" cada String da lista ao StringBuffer
                    respostaList.forEach { stringBufferResposta.append(it) }
                }
            } catch (ioe: IOException) {
                toast("Erro na conexão!")
            }
            // Simulando notificação do progresso para Thread de UI
            for (i in 1..10) {
                /* Envia um inteiro para ser publicado como progresso. Esse valor é recebido pela função
                callback onProgressUpdate*/
                publishProgress(i)
                // Dormindo por 0.5 s para simular o atraso de rede
                sleep(500)
            }
            // Retorna a String formada a partir do StringBuffer
            return stringBufferResposta.toString()
        }
        // Executa na mesma Thread de UI
        override fun onPostExecute(result: String?) {
            super.onPostExecute(result)
            toast("Texto recuperado com sucesso")
            // Altera o TextView com o texto recuperado
            textoTv.text = result
            // Tornando a barra de progresso invisível
            progressBar.visibility = View.GONE
        }
        // Executa na Thread de UI, é chamado sempre após uma publishProgress
        override fun onProgressUpdate(vararg values: Int?) {
            // Se o valor de progresso não for nulo, atualiza a barra de progresso
            values[0]?.apply { progressBar.progress = this }
        }
    }
}
