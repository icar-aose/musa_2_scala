package org.icar.pmr_solver

object Test_Solver_SPS extends App {

//	val domain = new SPSScenario("./data/sps_data")
//
//	val force = new ForceField(domain.circuit, domain.mission)
//	val mission_layer = ForceFieldLayer.merge_layers_by_sum(force.layers.values.toArray)
//
//	val axioms: Array[Assumption] = domain.axioms_set
//	val my_domain = Domain(Array.empty,axioms,qos)
//
//	var action_list : List[SystemAction] = List.empty
//	for (c<-domain.capabilities) {
//		action_list = MUSA_entities.capability_to_system_actions(c.asInstanceOf[GroundedAbstractCapability]) ::: action_list
//	}
//	val sys_action = action_list.toArray
//
//	val env_action : Array[EnvironmentAction] = Array()
//
//
//	val initial= domain.initial_state
//	val goalmodel = LTLGoalSet(Array(MUSA_entities.ltlFormula_to_LTLformula(domain.goal_specification.ltl)))
//	val available = AvailableActions(sys_action,env_action)
//
//	val my_problem = Problem(initial,goalmodel,available)
//
//	/* the solver */
//	val solver = new Solver(my_problem,my_domain)
//
//	val its=solver.iterate_until_termination(SolverConfiguration(IterationTermination(10),SolutionConfiguration(allow_self_loop = false, allow_cap_multiple_instance = false, allow_loop = false, allow_parallel_action = true)))
////val its=solver.iterate_until_termination(SolverConfiguration(IterationTermination(30),SolutionConfiguration(allow_self_loop = true, allow_cap_multiple_instance = true, allow_loop = true, allow_parallel_action = true)))
//
//	if (solver.opt_solution_set.isDefined) {
//		println("Number of iterations: "+its)
//		println("Number of generated WTS: "+solver.opt_solution_set.get.wts_list.size)
//		println("Number of full WTS: "+solver.opt_solution_set.get.full_wts.size)
//		println("Number of partial WTS: "+solver.opt_solution_set.get.partial_wts.size)
//
//		for (s <- solver.opt_solution_set.get.full_wts)
//			println(s.to_graphviz(circuit_state_interpretation))
//	}
//
//
//	//println( solver.solution_set.all_solutions_to_graphviz(circuit_state_interpretation) )
//
//
//
//	def qos(wts_node : Node):Float={
//		var sum : Float = 0
//		for (circuit_node<-domain.circuit.nodes) {
//			val node_value = circuit_node_to_potential(wts_node,circuit_node)
//			//if (node_value>max)
//				sum += node_value
//		}
//		sum
//	}
//
//	def circuit_node_to_potential(node : Node, circuit_node : org.icar.musa.scenarios.sps.Node) : Float = {
//		val up_cond = circuit_node.up_cond
//		val bool = node.satisfies(up_cond)
//		if (bool)
//			mission_layer.map(circuit_node)
//		else
//			0
//	}
//
//	def circuit_state_interpretation(node : Node) : String = {
//		var digits : String = "["
//		for (g <- domain.circuit.generators)
//			if (node.satisfies(g.up_cond)) digits+="1" else digits+="0"
//		digits += " | "
//
//		val load_map : Map[String, Boolean] = for (c<-domain.circuit.cond_map) yield (c._1->node.satisfies(c._2))
//
//		for (vital: String <- domain.mission.vitals)
//			if (load_map(vital)) digits+="1" else digits+="0"
//		digits += " "
//		for (semivital <- domain.mission.semivitals)
//			if (load_map(semivital)) digits+="1" else digits+="0"
//		digits += " "
//		for (nonvital <- domain.mission.nonvitals)
//			if (load_map(nonvital)) digits+="1" else digits+="0"
//
//		digits += "]"
//		digits
//	}



}

