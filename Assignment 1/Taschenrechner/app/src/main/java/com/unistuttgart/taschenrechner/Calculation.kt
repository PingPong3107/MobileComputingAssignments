package com.unistuttgart.taschenrechner

import android.content.Context
import android.widget.Toast
import net.objecthunter.exp4j.ExpressionBuilder
import com.unistuttgart.taschenrechner.MainActivity

class Calculation {

    public fun calculate(expression: String): String {

        if(!testParantheses(expression)){
            return "Error in parentheses"
        }

        try {
            val result = ExpressionBuilder(expression).build().evaluate()
            return result.toString()
        } catch (e: Exception) {
            return e.toString()
        }
    }

    fun testParantheses(expression: String): Boolean {
        var test=0
        for (s in expression) {
            if (s=='(') {
                test++
            } else if (s==')') {
                test--
            }
        }
        return test==0
    }
    
    fun Context.toast(message: CharSequence)=
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
}