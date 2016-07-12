// WITH_REFLECT
// FILE: I.java

public class I {
    public static void publicStaticI() {}
    public void publicMemberI() {}
    private static void privateStaticI() {}
    private void privateMemberI() {}
}

// FILE: J.java

public class J extends I {
    public static void publicStaticJ() {}
    public void publicMemberJ() {}
    private static void privateStaticJ() {}
    private void privateMemberJ() {}
}

// FILE: K.kt

import kotlin.reflect.declaredMembers
import kotlin.test.assertEquals

open class K : J() {
    open fun publicK() {}
    private fun privateK() {}
}

class L : K() {
    fun publicL() {}
    private fun privateL() {}
}

inline fun <reified T> test(vararg names: String) {
    assertEquals(names.toSet(), T::class.declaredMembers.map { it.name }.toSet())
}

fun box(): String {
    test<I>("publicStaticI", "publicMemberI", "privateStaticI", "privateMemberI")
    test<J>("publicStaticJ", "publicMemberJ", "privateStaticJ", "privateMemberJ")
    test<K>("publicK", "privateK")
    test<L>("publicL", "privateL")

    return "OK"
}
