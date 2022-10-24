package co.matheusmartins.fitnesstracker

import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.inputmethod.InputMethodManager
import android.widget.*
import androidx.annotation.StringRes
import androidx.appcompat.app.AlertDialog
import co.matheusmartins.fitnesstracker.model.Calc

class RcqActivity : AppCompatActivity() {

    private lateinit var lifestyle: AutoCompleteTextView
    private lateinit var editWaist: EditText
    private lateinit var editHip: EditText

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_rcq)

        editWaist = findViewById(R.id.edit_tmb_weight)
        editHip = findViewById(R.id.edit_tmb_height)

        lifestyle = findViewById(R.id.auto_lifestyle)
        val items = resources.getStringArray(R.array.rcq_gender)
        lifestyle.setText(items.first())
        val adapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, items)
        lifestyle.setAdapter(adapter)

        val btnSend: Button = findViewById(R.id.btn_rcq_send)
        btnSend.setOnClickListener {
            if (!validate()) {
                Toast.makeText(this, R.string.fields_messages, Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

                val waist = editWaist.text.toString().toInt()
                val hip = editHip.text.toString().toInt()

                val result = calculateRcq(waist, hip)
                val response = rcqRequest(result)

                AlertDialog.Builder(this)
                    .setMessage(getString(R.string.rcq_response, response))
                    .setPositiveButton(android.R.string.ok) { dialog, which ->
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
                                dao.update(Calc(id = updateId, type = "rcq", res = response))
                            } else {
                                dao.insert(Calc(type = "rcq", res = response))
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
            intent.putExtra("type", "rcq")
            startActivity(intent)
        }

        private fun rcqRequest(rcq: Double): Double {
            val items = resources.getStringArray(R.array.rcq_gender)
            return when {
                lifestyle.text.toString() == items[0] -> rcq - 0
                lifestyle.text.toString() == items[1] -> rcq - 0.12
                else -> 0.0
            }
        }

        private fun calculateRcq(waist: Int, hip: Int): Double {
            return (waist / hip).toDouble()
        }
        @StringRes
        private fun rcqResponse(rcq: Double): Int {
            return when {
                rcq < 0.83 -> R.string.rcq_low_risk
                rcq < 0.88 -> R.string.rcq_moderate_risk
                rcq < 0.94 -> R.string.rcq_high_risk
                else -> R.string.rcq_very_high_risk
            }
        }
        private fun validate(): Boolean {
            return (editWaist.text.toString().isNotEmpty()
                    && editHip.text.toString().isNotEmpty()
                    && !editWaist.text.toString().startsWith("0")
                    && !editHip.text.toString().startsWith("0"))
        }
    }
