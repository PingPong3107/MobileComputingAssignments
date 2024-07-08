package de.uni_s.ipvs.mcl.assignment5

/**
 * This class represents an observable mutable list.
 *
 * The list notifies its observers when an element is added or removed.
 *
 * @param E The type of the list.
 */
class ObservableMutableList<E> : ArrayList<E>() {
    private val observers = mutableListOf<() -> Unit>()

    /**
     * This function adds an observer to the list.
     * @param observer The observer to be added.
     */
    fun addObserver(observer: () -> Unit) {
        observers.add(observer)
    }

    /**
     * This function adds an element to the list.
     *
     * @param element The element to be added.
     */
    override fun add(element: E): Boolean {
        val result = super.add(element)
        observers.forEach { it.invoke() }
        return result
    }

    /**
     * This function removes an element from the list.
     *
     * @param element The element to be removed.
     */
    override fun remove(element: E): Boolean {
        val result = super.remove(element)
        observers.forEach { it.invoke() }
        return result
    }
}