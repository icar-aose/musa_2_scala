package org.icar.pmr_solver.high_level_specification

/******* PLANNING PROBLEM ********/
case class Problem(I : StateOfWorld, goal_model : LTLGoalSet, actions : AvailableActions)


case class StateOfWorld (statements : List[GroundPredicate]) {
    override def toString: String = {
        var a_string: String = ""
        a_string += "["
        for (i <- statements.indices) {
            a_string += statements(i).toString
            if (i<statements.length-1)
                a_string += ","
        }
        a_string + "]"
    }
}


/******* GOAL: LTL SYNTAX DEFINITION ********/
case class LTLGoalSet(goals:Array[HL_LTLFormula])



/******* PLANNING ACTIONS ********/
case class AvailableActions(sys_action : Array[SystemAction], env_action : Array[EnvironmentAction])


abstract class PlanningAction

case class SystemAction(
                           id : String,
                           params: List[DomainPredArguments],
                           pre : HL_PredicateFormula,
                           post : HL_PredicateFormula,
                           effects : Array[EvolutionGrounding],
                           invariants : List[HL_PredicateFormula]
           ) extends PlanningAction

case class EnvironmentAction(
                                id : String,
                                params: List[DomainPredArguments],
                                pre : HL_PredicateFormula,
                                post : HL_PredicateFormula,
                                effects : Array[ProbabilisticEvolutionGrounding],
                                invariants : List[HL_PredicateFormula]
            ) extends PlanningAction


/*
    improvement: EVOLUTION CONSTRAINTS
    add to (Probabilistic)EvolutionGrounding a set of invariants
    i.e. constraints for future evolutions
    example: close_sw_1 implies that the condition 'closed(sw1)' holds for all the successors
*/

case class EvolutionGrounding(name : String, evo : Array[EvoOperator])
case class ProbabilisticEvolutionGrounding(
                                              name : String,
                                              probability : Float,
                                              evo : Array[EvoOperator]
                                          )



sealed abstract class EvoOperator
case class Deprec_AddEvoOperator(add : GroundPredicate) extends EvoOperator
case class Deprec_RemoveEvoOperator(rmv : GroundPredicate) extends EvoOperator
case class Deprec_RemoveAllEvoOperator(rmv_all : String) extends EvoOperator
case class AddOperator(p : Predicate) extends EvoOperator
case class RmvOperator(p : Predicate) extends EvoOperator








