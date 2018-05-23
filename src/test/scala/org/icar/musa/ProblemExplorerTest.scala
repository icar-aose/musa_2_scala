package org.icar.musa

import junit.framework.Assert.assertEquals
import junit.framework.TestCase
import org.icar.fol._
import org.icar.ltl.{Finally, Globally, LogicAtom, LogicConjunction}
import org.icar.musa.context.{AddEvoOperator, EvoOperator, RemoveEvoOperator, StateOfWorld}
import org.icar.musa.pmr._
import org.icar.musa.spec.{AbstractCapability, EvolutionScenario, GroundedAbstractCapability, LTLGoal}
import org.icar.petrinet.{AcceptedState, WaitErrorState}

class ProblemExplorerTest extends TestCase {

  def test1Iteration (): Unit = {
    val w = StateOfWorld.create(GroundPredicate("receipt",AtomTerm("doc")))

    val f1 = Globally(LogicAtom("document",AtomTerm("doc")))
    val f2 = Finally(LogicAtom("ready",AtomTerm("doc")))
    val f3 = LogicConjunction(f1,f2)
    val goal = LTLGoal(f3)

    val ass = AssumptionSet( Assumption("document(X) :- receipt(X).")  )
    val quality = new EmptyQualityAsset(ass)

    val ps = SingleGoalProblemSpecification(ass,goal,quality)

    val pre = FOLCondition(Literal(Predicate("document", AtomTerm("doc"))))
    val post = FOLCondition(Literal(Predicate("ready", AtomTerm("doc"))))
    val evo = EvolutionScenario(Array[EvoOperator](AddEvoOperator(GroundPredicate("ready", AtomTerm("doc")))))
    val c1 = GroundedAbstractCapability("working_the_doc",pre,post,Map("1"-> evo))
    val cap_set = Array[AbstractCapability](c1)

    val expl = new SingleGoalProblemExploration(ps, w, cap_set)
    expl.execute_iteration()

    val exp = expl.highest_expansion
    assertEquals(1,expl.cap_that_applied.size)
    assertEquals("working_the_doc",expl.cap_that_applied.head.name)
    assertEquals(1,expl.expansions.size)
    assertEquals(AcceptedState,expl.expansions.head.asInstanceOf[SimpleWTSExpansion].end.su.current_state)
    //expl.log_iteration
  }

  def test2Iteration (): Unit = {
    val w = StateOfWorld.create(GroundPredicate("receipt",AtomTerm("doc")))

    val f1 = Globally(LogicAtom("document",AtomTerm("doc")))
    val f2 = Finally(LogicAtom("ready",AtomTerm("doc")))
    val f3 = LogicConjunction(f1,f2)
    val goal = LTLGoal(f3)

    val ass = AssumptionSet( Assumption("document(X) :- receipt(X).")  )
    val quality = new EmptyQualityAsset(ass)

    val ps = SingleGoalProblemSpecification(ass,goal,quality)

    val pre = FOLCondition(Literal(Predicate("document", AtomTerm("doc"))))
    val post = FOLCondition(Literal(Predicate("worked", AtomTerm("doc"))))
    val evo = EvolutionScenario(Array[EvoOperator]( AddEvoOperator(GroundPredicate("worked", AtomTerm("doc"))) ))
    val c1 = GroundedAbstractCapability("working_the_doc",pre,post,Map("1"-> evo))

    val pre2 = FOLCondition(Literal(Predicate("worked", AtomTerm("doc"))))
    val post2 = FOLCondition(Literal(Predicate("ready", AtomTerm("doc"))))
    val evo2 = EvolutionScenario(Array[EvoOperator](AddEvoOperator(GroundPredicate("ready", AtomTerm("doc"))),RemoveEvoOperator(GroundPredicate("worked", AtomTerm("doc")))))
    val c2 = GroundedAbstractCapability("terminating_the_doc",pre2,post2,Map("1"-> evo2))

    val cap_set = Array[AbstractCapability](c1,c2)
    val expl = new SingleGoalProblemExploration(ps, w, cap_set)
    expl.execute_iteration()
    assertEquals(1,expl.cap_that_applied.size)
    assertEquals("working_the_doc",expl.cap_that_applied.head.name)
    assertEquals(1,expl.expansions.size)
    assertEquals(WaitErrorState,expl.expansions.head.asInstanceOf[SimpleWTSExpansion].end.su.current_state)

    //expl.log_iteration

    val exp = expl.highest_expansion

    if (exp.isDefined)
      exp.get match {
        case x : SimpleWTSExpansion => expl.new_node( x.end ); expl.pick_expansion(x)
      }

    expl.execute_iteration()
    val exp2 = expl.highest_expansion
    assertEquals(1,expl.cap_that_applied.size)
    assertEquals("terminating_the_doc",expl.cap_that_applied.head.name)
    assertEquals(1,expl.expansions.size)
    assertEquals(AcceptedState,expl.expansions.head.asInstanceOf[SimpleWTSExpansion].end.su.current_state)

    //expl.log_iteration

  }

  def testLocalBuilder() : Unit = {
    val w = StateOfWorld.create(GroundPredicate("receipt",AtomTerm("doc")))

    val f1 = Globally(LogicAtom("document",AtomTerm("doc")))
    val f2 = Finally(LogicAtom("ready",AtomTerm("doc")))
    val f3 = LogicConjunction(f1,f2)
    val goal = LTLGoal(f3)

    val ass = AssumptionSet( Assumption("document(X) :- receipt(X).")  )
    val quality = new EmptyQualityAsset(ass)

    val ps = SingleGoalProblemSpecification(ass,goal,quality)

    val pre = FOLCondition(Literal(Predicate("document", AtomTerm("doc"))))
    val post = FOLCondition(Literal(Predicate("worked", AtomTerm("doc"))))
    val evo = EvolutionScenario(Array[EvoOperator]( AddEvoOperator(GroundPredicate("worked", AtomTerm("doc"))) ))
    val c1 = GroundedAbstractCapability("working_the_doc",pre,post,Map("1"-> evo))

    val pre2 = FOLCondition(Literal(Predicate("worked", AtomTerm("doc"))))
    val post2 = FOLCondition(Literal(Predicate("ready", AtomTerm("doc"))))
    val evo2 = EvolutionScenario(Array[EvoOperator](AddEvoOperator(GroundPredicate("ready", AtomTerm("doc"))),RemoveEvoOperator(GroundPredicate("worked", AtomTerm("doc")))))
    val c2 = GroundedAbstractCapability("terminating_the_doc",pre2,post2,Map("1"-> evo2))

    val cap_set = Array[AbstractCapability](c1,c2)

    val local_builder = new WTSLocalBuilder(ps,w,cap_set,IterationTermination(2))
    local_builder.build_wts()

    //local_builder.wts.print_for_graphviz(quality.pretty_string)

    //println("exit="+local_builder.num_exit_node)
    //println("total="+local_builder.wts.nodes.size)
    assertEquals(1,local_builder.num_exit_node)
    assertEquals(3,local_builder.wts.nodes.size)

  }

  def testLocalBuilderEmptyIteration() : Unit = {
    val w = StateOfWorld.create(GroundPredicate("receipt",AtomTerm("doc")))

    val f1 = Globally(LogicAtom("document",AtomTerm("doc")))
    val f2 = Finally(LogicAtom("ready",AtomTerm("doc")))
    val f3 = LogicConjunction(f1,f2)
    val goal = LTLGoal(f3)

    val ass = AssumptionSet( Assumption("document(X) :- receipt(X).")  )
    val quality = new EmptyQualityAsset(ass)

    val ps = SingleGoalProblemSpecification(ass,goal,quality)

    val pre = FOLCondition(Literal(Predicate("document", AtomTerm("doc"))))
    val post = FOLCondition(Literal(Predicate("worked", AtomTerm("doc"))))
    val evo = EvolutionScenario(Array[EvoOperator]( AddEvoOperator(GroundPredicate("worked", AtomTerm("doc"))) ))
    val c1 = GroundedAbstractCapability("working_the_doc",pre,post,Map("1"-> evo))

    val pre2 = FOLCondition(Literal(Predicate("worked", AtomTerm("doc"))))
    val post2 = FOLCondition(Literal(Predicate("ready", AtomTerm("doc"))))
    val evo2 = EvolutionScenario(Array[EvoOperator](AddEvoOperator(GroundPredicate("ready", AtomTerm("doc"))),RemoveEvoOperator(GroundPredicate("worked", AtomTerm("doc")))))
    val c2 = GroundedAbstractCapability("terminating_the_doc",pre2,post2,Map("1"-> evo2))

    val cap_set = Array[AbstractCapability](c1,c2)

    val local_builder = new WTSLocalBuilder(ps,w,cap_set,MaxEmptyIterationTermination(10))
    local_builder.build_wts()

    //local_builder.wts.print_for_graphviz(quality.pretty_string)

    //println("exit="+local_builder.num_exit_node)
    //println("total="+local_builder.wts.nodes.size)
    //println(local_builder.num_empty_its)
    assertEquals(1,local_builder.num_exit_node)
    assertEquals(3,local_builder.wts.nodes.size)
    assertEquals(10,local_builder.num_empty_its)

  }

  def testLocalBuilderMultiExpansion() : Unit = {
    val w = StateOfWorld.create(GroundPredicate("receipt",AtomTerm("doc")))

    val f1 = Globally(LogicAtom("document",AtomTerm("doc")))
    val f2 = Finally(LogicAtom("ready",AtomTerm("doc")))
    val f3 = LogicConjunction(f1,f2)
    val goal = LTLGoal(f3)

    val ass = AssumptionSet( Assumption("document(X) :- receipt(X).")  )
    val quality = new EmptyQualityAsset(ass)

    val ps = SingleGoalProblemSpecification(ass,goal,quality)

    val pre = FOLCondition(Literal(Predicate("document", AtomTerm("doc"))))
    val post = FOLCondition(Literal(Predicate("worked", AtomTerm("doc"))))
    val evo = EvolutionScenario(Array[EvoOperator]( AddEvoOperator(GroundPredicate("worked", AtomTerm("doc"))) ))
    val c1 = GroundedAbstractCapability("working_the_doc",pre,post,Map("1"-> evo))

    val pre2 = FOLCondition(Literal(Predicate("worked", AtomTerm("doc"))))
    val post2 = FOLCondition(Disjunction(  Literal(Predicate("ready", AtomTerm("doc"))), Literal(Predicate("to_refine", AtomTerm("doc"))) ) )
    val evo2_1 = EvolutionScenario(Array[EvoOperator](AddEvoOperator(GroundPredicate("ready", AtomTerm("doc"))),RemoveEvoOperator(GroundPredicate("worked", AtomTerm("doc")))))
    val evo2_2 = EvolutionScenario(Array[EvoOperator](AddEvoOperator(GroundPredicate("to_refine", AtomTerm("doc"))),RemoveEvoOperator(GroundPredicate("worked", AtomTerm("doc")))))
    val c2 = GroundedAbstractCapability("supervising_the_doc",pre2,post2,Map("accept"-> evo2_1,"reject"-> evo2_2))

    val pre3 = FOLCondition(Literal(Predicate("to_refine", AtomTerm("doc"))))
    val post3 = FOLCondition(Literal(Predicate("worked", AtomTerm("doc"))))
    val evo3 = EvolutionScenario(Array[EvoOperator]( AddEvoOperator(GroundPredicate("worked", AtomTerm("doc"))), RemoveEvoOperator(GroundPredicate("to_refine", AtomTerm("doc")) ) ))
    val c3 = GroundedAbstractCapability("revising_the_doc",pre3,post3,Map("1"-> evo3))

    val cap_set = Array[AbstractCapability](c1,c2,c3)

    val local_builder = new WTSLocalBuilder(ps,w,cap_set,MaxEmptyIterationTermination(10))
    local_builder.build_wts()

    //local_builder.wts.print_for_graphviz(quality.pretty_string)

    assertEquals(1,local_builder.num_exit_node)
    assertEquals(5,local_builder.wts.nodes.size)
    assertEquals(10,local_builder.num_empty_its)

  }
}
