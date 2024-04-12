package com.unistuttgart.taschenrechner

import net.objecthunter.exp4j.ExpressionBuilder

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
}