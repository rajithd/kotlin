enum class A(val v: A) {
    A1(<!UNINITIALIZED_ENUM_ENTRY!>A2<!>),
    A2(A1)
}

enum class B(val x: Int) {
    B1(1),
    B2(2);

    companion object {
        val SUM = B1.x + B2.x
        val COPY = B1
    }
}

enum class C(val x: Int) {
    C1(<!UNINITIALIZED_VARIABLE!>SUM<!>),
    C2(1);

    companion object {
        val COPY = C2
        val SUM = C1.x + COPY.x
    }
}

enum class D(val x: Int) {
    D1(<!UNINITIALIZED_ENUM_ENTRY!>D2<!>.x),
    D2(D1.x)
}