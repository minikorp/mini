package mini.compiler

import mini.MiniGen
import org.amshove.kluent.`should contain all`
import org.amshove.kluent.`should contain`
import org.junit.Test

class CompilerActionsTests {

    @Test
    fun `simple action has correct type`() {
        val types = MiniGen.actionTypes[SimpleAction::class] ?: error("Should not be null")
        types.`should contain`(SimpleAction::class)
    }


    @Test
    fun `action interface is propagated with base classes`() {
        val types = MiniGen.actionTypes[ChildBaseActionNotAnnotated::class]
            ?: error("Should not be null")
        types.`should contain all`(listOf(ChildBaseActionNotAnnotated::class, ParentActionClass::class))
    }

    @Test
    fun `multiple level actions are detected`() {
        val types = MiniGen.actionTypes[MultipleLevelsAction::class]
            ?: error("Should not be null")
        types.`should contain all`(listOf(MultipleLevelsAction::class,
            ParentA::class,
            ParentB::class,
            ParentC::class
        ))
    }
}