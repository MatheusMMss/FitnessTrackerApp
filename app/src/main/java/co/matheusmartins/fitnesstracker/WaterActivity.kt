package co.matheusmartins.fitnesstracker

import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.inputmethod.InputMethodManager
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.annotation.StringRes
import androidx.appcompat.app.AlertDialog
import co.matheusmartins.fitnesstracker.model.Calc

class WaterActivity : AppCompatActivity() {

    private lateinit var editWeight: EditText

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_water)

        editWeight = findViewById(R.id.edit_water_weight)

        val btnSend: Button = findViewById(R.id.btn_water_send)

        btnSend.setOnClickListener {
            if (!validate()) {
                Toast.makeText(this, R.string.fields_messages, Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val weight = editWeight.text.toString().toInt()

            val result = calculateWater(weight)

            AlertDialog.Builder(this)
                .setTitle(getString(R.string.water_response, result))
                .setPositiveButton(android.R.string.ok) { dialog, which ->
                    // aqui vai rodar depois do click
                }
                .setNegativeButton(R.string.save) { dialog, which ->
                    Thread {
                        val app = application as App
                        val dao = app.db.calcDao()

                        // FIXME: checa se tem um updateId, se tiver, significa
                        // FIXME: que viemos da tela da lista de itens e devemos
                        // FIXME: editar ao inves de inserir
                        val updateId = intent.extras?.getInt("updateId")
                        if (updateId != null) {
                            dao.update(Calc(id = updateId, type = "water", res = result))
                        } else {
                            dao.insert(Calc(type = "water", res = result))
                        }

                        runOnUiThread {
                            openListActivity()
                        }
                    }.start()

                }
                .create()
                .show()

            val service = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            service.hideSoftInputFromWindow(currentFocus?.windowToken, 0)
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == R.id.menu_search) {
            finish()
            openListActivity()
        }
        return super.onOptionsItemSelected(item)
    }

    private fun openListActivity() {
        val intent = Intent(this, ListCalcActivity::class.java)
        intent.putExtra("type", "water")
        startActivity(intent)
    }


    private fun calculateWater(weight: Int): Double {
        // peso * 35ml
        return weight * 35.0
    }

    private fun validate(): Boolean {
        return (editWeight.text.toString().isNotEmpty()
                && !editWeight.text.toString().startsWith("0"))
    }
}
