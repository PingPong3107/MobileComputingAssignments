package de.uni_s.ipvs.mcl.assignment5

class ObservableMutableList<E> : ArrayList<E>() {
    private val observers = mutableListOf<() -> Unit>()

    fun addObserver(observer: () -> Unit) {
        observers.add(observer)
    }

    override fun add(element: E): Boolean {
        val result = super.add(element)
        observers.forEach { it.invoke() }
        return result
    }

    override fun remove(element: E): Boolean {
        val result = super.remove(element)
        observers.forEach { it.invoke() }
        return result
    }
}