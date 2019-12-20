package org.icar.musa.scenarios

import junit.framework.TestCase
import org.icar.musa.pmr._

class PRINEntertainmentTest extends TestCase {

  val domain = new EntertainmentScenario
  val sol_builder = new LateDecisionSolutionBuilder

  def testDomain (): Unit = {
    val wtsbuilder = new WTSLocalBuilder(domain.problem,domain.initial_state,domain.capabilities,domain.termination,sol_builder)
    wtsbuilder.build_wts()

    //println("TERMINATED")

    //wtsbuilder.wts.print_for_graphviz(domain.problem.asset.pretty_string)

    //println("PARTIAL")
    /*for (sol <- sol_builder.partial_solution_stack)
      sol.print_for_graphviz()*/



    println("COMPLETE")
    /*for (sol <- sol_builder.complete_solution)
      sol.print_for_graphviz()*/

    sol_builder.solution.print_for_graphviz()

  }

}
