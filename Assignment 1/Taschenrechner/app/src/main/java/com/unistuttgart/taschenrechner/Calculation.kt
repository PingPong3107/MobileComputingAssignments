package com.unistuttgart.taschenrechner

import net.objecthunter.exp4j.ExpressionBuilder

class Calculation {

    fun calculate(expression: String): String {

        if(!testParentheses(expression)){
            return "Error in parentheses"
        }

        return try {
            val result = ExpressionBuilder(expression).build().evaluate()
            result.toString()
        } catch (e: Exception) {
            e.toString()
        }
    }

    private fun testParentheses(expression: String): Boolean {
        var test=0
        for (s in expression) {
            if (s=='(') {
                test++
            } else if (s==')') {
                test--
            }
            if (test<0) {
                return false
            }
        }
        return test==0
    }
}