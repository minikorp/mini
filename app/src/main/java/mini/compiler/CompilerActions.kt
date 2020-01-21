package mini.compiler

import mini.Action

/*

Test actions to verify compiler is generating proper code for actions.

 */


@Action
class SimpleAction


@Action
abstract class ParentActionClass

class ChildBaseActionNotAnnotated : ParentActionClass()


interface ParentA
interface ParentB : ParentA
interface ParentC : ParentB

@Action
class MultipleLevelsAction : ParentC

