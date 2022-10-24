package co.matheusmartins.fitnesstracker;

import co.matheusmartins.fitnesstracker.model.Calc

interface OnListClickListener {
	fun onClick(id: Int, type: String)
	fun onLongClick(position: Int, calc: Calc)
}