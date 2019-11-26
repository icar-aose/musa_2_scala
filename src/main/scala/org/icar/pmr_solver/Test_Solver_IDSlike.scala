package org.icar.pmr_solver


import org.icar.pmr_solver.high_level_specification._
import org.icar.pmr_solver.symbolic_level.RawState


// TO fix: state [registered(doc),rejected(doc),unavailable(doc),worked(doc)] is considered Exit?
// Explanation: An exit node is (for definition) a node where next()=Empty.
// So the behavior is actually correct (the Finally holds). All successors will be ok by definition.

// BUT Another problem: if I use a goal like Globally(...)... it will be never be Empty!!!
// it is necessary to change the definition of Exit goal.

object Test_Solver_IDSlike extends App {

	/* the domain */
	//val axioms: Array[Assumption] =Array(Assumption("ready(X) :- available(X), registered(X)."))
	def qos(n:RawState):Float=0

	val dom_types : Array[DomainType] = Array(
		StringEnum_DomainType("doc_type",Array("issue_list","request","tech_rep")),
		StringEnum_DomainType("doc_state",Array("received","registered","worked","accepted","rejected","to_revise"))
	)

	val preds : Array[DomainPredicate] = Array(
		DomainPredicate("document",List(
			DomainVariable("TYPE","doc_type"),
			DomainVariable("STATE","doc_state")
		))
	)

	val my_domain = Domain(preds,dom_types,Array.empty)

	/* capability */
	val register = AbstractCapability(
		id = "register",
		params = List(DomainVariable("TYPE","doc_type")),

		pre = ExistQuantifier(
			List(VariableTerm("TYPE")),
			Predicate("document", List(VariableTerm("TYPE"), AtomTerm("received")))
		),

		post = ExistQuantifier(
			List(VariableTerm("TYPE")),
			Predicate("document", List(VariableTerm("TYPE"), AtomTerm("registered")))
		),

		effects = Array(
			EvolutionGrounding("base",Array[EvoOperator](
				AddOperator(Predicate("document", List( VariableTerm("TYPE"), AtomTerm("registered")))),
				RmvOperator(Predicate("document", List(VariableTerm("TYPE"), AtomTerm("received"))))
			))),

		future = List.empty
	)

	val work = AbstractCapability(
		id = "work",
		params = List(DomainVariable("TYPE","doc_type")),

		pre = Disjunction ( List(
			ExistQuantifier(
				List(VariableTerm("TYPE")),
				Predicate("document", List(VariableTerm("TYPE"), AtomTerm("registered")))
			),
			ExistQuantifier(
				List(VariableTerm("TYPE")),
				Predicate("document", List(VariableTerm("TYPE"), AtomTerm("to_revise")))
			))
		),

		post = ExistQuantifier(
			List(VariableTerm("TYPE")),
			Predicate("document", List(VariableTerm("TYPE"), AtomTerm("worked")))
		),

		effects = Array(
			high_level_specification.EvolutionGrounding("base",Array[EvoOperator](
				AddOperator(Predicate("document", List(VariableTerm("TYPE"), AtomTerm("worked")))),
				RmvOperator(Predicate("document", List(VariableTerm("TYPE"), AtomTerm("to_revise")))),
				RmvOperator(Predicate("document", List(VariableTerm("TYPE"), AtomTerm("registered"))))
			))),

		future = List.empty
	)

	val supervise = AbstractCapability(
		id = "supervise",
		params = List(DomainVariable("TYPE","doc_type")),

		pre = ExistQuantifier(
			List(VariableTerm("TYPE")),
			Predicate("document", List(VariableTerm("TYPE"), AtomTerm("worked")))
		),

		post = Disjunction ( List(
			ExistQuantifier(
				List(VariableTerm("TYPE")),
				Predicate("document", List(VariableTerm("TYPE"), AtomTerm("accepted")))
			),
			ExistQuantifier(
				List(VariableTerm("TYPE")),
				Predicate("document", List(VariableTerm("TYPE"), AtomTerm("rejected")))
			),
			ExistQuantifier(
				List(VariableTerm("TYPE")),
				Predicate("document", List(VariableTerm("TYPE"), AtomTerm("to_revise")))
			)
		)),

		effects = Array(
			high_level_specification.EvolutionGrounding("ok",Array[EvoOperator](
				AddOperator(Predicate("document", List(VariableTerm("TYPE"), AtomTerm("accepted")))),
				RmvOperator(Predicate("document", List(VariableTerm("TYPE"), AtomTerm("worked"))))
			)),
			high_level_specification.EvolutionGrounding("no",Array[EvoOperator](
				AddOperator(Predicate("document", List(VariableTerm("TYPE"), AtomTerm("rejected")))),
				RmvOperator(Predicate("document", List(VariableTerm("TYPE"), AtomTerm("worked"))))
			)),
			high_level_specification.EvolutionGrounding("change",Array[EvoOperator](
				AddOperator(Predicate("document", List(VariableTerm("TYPE"), AtomTerm("to_revise")))),
				RmvOperator(Predicate("document", List(VariableTerm("TYPE"), AtomTerm("worked"))))
			))
		),

		future = List.empty
	)

	val sys_action = Array(register,work,supervise)//,work,request_again,supervise) //


//
//	/* perturbations */
//	val evo_lose = Array(
//		ProbabilisticEvolutionGrounding("base",0.01f,Array[EvoOperator](RemoveEvoOperator(GroundPredicate("available", AtomTerm("doc"))), AddEvoOperator(GroundPredicate("unavailable", AtomTerm("doc")))))
//	)
//	val pre_lose = TweetyFormula.fromFormula(Literal(org.icar.fol.Predicate("available", AtomTerm("doc"))))
//	val lose_doc = EnvironmentAction("lose",pre_lose,evo_lose)
//
	val env_action : Array[AbstractCapability] = Array.empty// Array(lose_doc) //


	/* the problem */
	val initial = StateOfWorld(List(
		GroundPredicate("document", List(AtomTerm("tech_rep"),AtomTerm("received")))
	))
	val accepted = GroundPredicate("document", List(AtomTerm("tech_rep"),AtomTerm("accepted")))
	val rejected = GroundPredicate("document", List(AtomTerm("tech_rep"),AtomTerm("rejected")))

	val goalmodel = LTLGoalSet(Array(

		Finally(Disjunction(List(accepted,rejected)))

	))
	val available = AvailableActions(sys_action,env_action)
	val my_problem = Problem(initial,goalmodel,available)


	/* the solver */
	val solver = new Solver(my_problem,my_domain,qos)
	println("**Domain**")
	println("Number of predicates: "+solver.map.inverse.size)
	println("Number of goals: "+solver.specifications.length)
	println("Number of actions: "+solver.available_actions.length)
	println("Number of perturbations: "+solver.available_perturb.length)

	val its=solver.iterate_until_termination(SolverConfiguration(IterationTermination(20),SolutionConfiguration(allow_self_loop = false, allow_cap_multiple_instance = true, allow_loop = true, allow_parallel_action = true)))

	if (solver.opt_solution_set.isDefined) {


		println("**Planning**")
		println("Number of iterations: "+its)

		println("**Solutions**")
		println("Number of generated WTS: "+solver.opt_solution_set.get.wts_list.size)
		println("Number of full WTS: "+solver.opt_solution_set.get.full_wts.size)
		println("Number of partial WTS: "+solver.opt_solution_set.get.partial_wts.size)

		println( solver.opt_solution_set.get.all_solutions_to_graphviz(node => node.toString) )
	}

}
