package com.unistuttgart.taschenrechner

import net.objecthunter.exp4j.ExpressionBuilder

/**
 * A class to calculate the result of a given expression
 */
class Calculation() {

    /**
     * Calculate the result of a given expression
     * @param expression The expression to calculate
     * @return The result of the calculation
     */
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

    /**
     * Test if the parentheses in the expression are correct
     * @param expression The expression to test
     * @return True if the parentheses are correct, false otherwise
     */
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