package ru.otus.homework

class SomeClass {
    fun someMethod() {
        println("Some method")
    }

    private fun somePrivateMethod() {
        println("Some Private method")
    }

    companion object {
        fun callPrivateMethod(sc: SomeClass) {
            sc.somePrivateMethod() // OK
        }
    }
}

const val topLevelProperty: String = "Top level property"

fun topLevelFunction(sc: SomeClass) {
    sc.someMethod()
    // sc.somePrivateMethod() // Compilation error
}