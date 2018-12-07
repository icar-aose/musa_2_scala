package org.icar.musa.scenarios

import org.icar.fol._
import org.icar.ltl.{Finally, LogicConjunction, ltlFormula}
import org.icar.musa.context.{AddEvoOperator, EvoOperator, RemoveEvoOperator, StateOfWorld}
import org.icar.musa.pmr._
import org.icar.musa.scenarios.sps.{Circuit, Mission, ReconfigurationScenario}
import org.icar.musa.main_entity.{AbstractCapability, EvolutionScenario, GroundedAbstractCapability, LTLGoal}

import scala.collection.mutable.ArrayBuffer

class SPSScenario(path:String) extends Scenario {

  var circuit = Circuit.load_from_file(path+"/circuit3.txt")
  var scenario = ReconfigurationScenario.scenario_circuit3_parsed_1
  var mission = Mission.circuit3_file_mission_1

  override def assumption_set : AssumptionSet = {
    /* standard assumptions */
    val list = ArrayBuffer[Assumption]()
    list += Assumption("off(X) :- load(X), not on(X).")
    list += Assumption("off(X) :- generator(X), not on(X).")
    list += Assumption("down(X) :- node(X), not up(X).")
    list += Assumption("open(X) :- sw(X), not closed(X).")

    for (n<-circuit.nodes)
      list += Assumption("node(n"+n.id+").")

    for (c<-circuit.connections) {
      list += Assumption(c.source.up+":-"+c.dest.up+","+"not "+c.failure+".")
      list += Assumption(c.dest.up+":-"+c.source.up+","+"not "+c.failure+".")
    }
    for (s<-circuit.switcher) {
      list += Assumption("sw("+s.id+").")
      list += Assumption(s.source.up+":-"+s.dest.up+", "+s.closed+".")
      list += Assumption(s.dest.up+":-"+s.source.up+", "+s.closed+".")
    }
    for (g<-circuit.generators) {
      list += Assumption("generator("+g.id+").")
      list += Assumption(g.node.up+":-"+g.up+".")
    }
    for (l<-circuit.loads) {
      list += Assumption("load("+l.id+").")
      list += Assumption(l.up+":-"+l.node.up+".")
    }

    AssumptionSet(list: _*)
  }

  override def goal_specification: LTLGoal = {
    var vitals = ArrayBuffer[ltlFormula]()
    for (v <- circuit.loads if mission.vitals.contains(v.id))
      vitals += v.atom
    val conj = LogicConjunction(vitals)
    LTLGoal(Finally(conj))
  }

  override def quality_asset: QualityAsset = {

    lazy val assumptions = assumption_set

    class SPSQualityAsset extends QualityAsset {
      val entail = Entail
      override def evaluate_node(w: StateOfWorld, goal_sat: Float): Float = evaluate_state(w).get
      override def evaluate_state(w: StateOfWorld): Option[Float] = {
        val cond_map = circuit.cond_map
        val res_map : Map[String, Boolean] = entail.condition_map(w,assumptions,cond_map)

        var supplied_pow : Float = 0
        for (g <- circuit.generators if res_map(g.id)) supplied_pow += mission.gen_pow(g.id)

        var residue_pow : Float = supplied_pow
        var absorbed_pow : Float = 0
        var not_enough_pow : Float = 0
        var digits : String = ""

        for (l_name <- mission.vitals) {

          if (res_map(l_name)) {
            absorbed_pow += mission.vital_pow
            if (residue_pow>mission.vital_pow) {
              digits += "1"
              residue_pow -= mission.vital_pow
            } else {
              digits += "0"
              not_enough_pow += mission.vital_pow
            }
          } else digits += "0"

        }

        for (l_name <- mission.semivitals) {

          if (res_map(l_name)) {
            absorbed_pow += mission.semivital_pow
            if (residue_pow>mission.semivital_pow) {
              digits += "1"
              residue_pow -= mission.semivital_pow
            } else {
              digits += "0"
              not_enough_pow += mission.semivital_pow
            }
          } else digits += "0"

        }

        for (l_name <- mission.nonvitals) {

          if (res_map(l_name)) {
            absorbed_pow += mission.nonvital_pow
            if (residue_pow>mission.nonvital_pow) {
              digits += "1"
              residue_pow -= mission.nonvital_pow
            } else {
              digits += "0"
              not_enough_pow += mission.nonvital_pow
            }
          } else digits += "0"

        }

        //println(digits)
        Some(Integer.parseInt(digits, 2)-not_enough_pow-residue_pow)
      }

      override def max_score: Float = math.pow(2, circuit.loads.length).toFloat

      override def pretty_string(w: StateOfWorld): String = {
        val cond_map = circuit.cond_map
        val res_map : Map[String, Boolean] = entail.condition_map(w,assumptions,cond_map)
        var digits : String = "["
        for (g <- circuit.generators)
          if (res_map(g.id)) digits+="1" else digits+="0"
        digits += " | "
        for (l <- circuit.loads)
          if (res_map(l.id)) digits+="1" else digits+="0"
        digits += "]"
        digits
      }

      override def pretty_string(node: WTSStateNode): String = {
        "n("+pretty_string(node.w)+","+node.qos+")"
      }

      override def pretty_string(exp: WTSExpansion): String = {
        exp match {
          case s : SimpleWTSExpansion =>
            "x("+pretty_string(s.start.w)+"->"+pretty_string(s.end.w)+","+s.order+","+s.cap.name+")"
          case m : MultiWTSExpansion =>
            m.toString
        }
      }
    }

    new SPSQualityAsset()
  }

  override def initial_state : StateOfWorld = {
    var list = ArrayBuffer[GroundPredicate]()

    for (r <- circuit.switcher) {
      var state = if (scenario.open_switchers.contains(r.id)) "open" else "closed"
      list += GroundPredicate(state,AtomTerm(r.id))
    }

    for (r<- circuit.generators) {
      val state = if (scenario.up_generators.contains(r.id)) "on" else if (scenario.generator_malfunctioning.contains(r.id)) "error" else "off"
      list += GroundPredicate(state,AtomTerm(r.id))
    }

    for (r <- scenario.failures) {
      list += GroundPredicate("f",AtomTerm(r))
    }

    StateOfWorld.create(list.toArray)
  }

  override def capabilities : Array[AbstractCapability] = {
    var cap_list = ArrayBuffer[AbstractCapability]()

    for (g <- circuit.generators if !scenario.generator_malfunctioning.contains(g)) {
      cap_list += generate_switch_on_generator(g.id)
      cap_list += generate_switch_off_generator(g.id)
    }

    for (g <- circuit.switcher if !scenario.switcher_malfunctioning.contains(g)) {
      if (circuit.sw_map.contains(g.id)) {
        val g2_name = circuit.sw_map(g.id)
        cap_list += generate_combinated_on_off_switcher(g.id,g2_name)
      } else {
        cap_list += generate_close_switcher(g.id)
        cap_list += generate_open_switcher(g.id)
      }
    }


    cap_list.toArray
  }

  private def generate_switch_on_generator(name : String) : GroundedAbstractCapability = {
    val generator_name = "switch_ON_"+name
    val generator = AtomTerm(name)
    val pre = FOLCondition(Literal(Predicate("off", generator )))
    val post = FOLCondition(Literal(Predicate("on", generator )))
    val evo_1 = EvolutionScenario(Array[EvoOperator](RemoveEvoOperator(GroundPredicate("off", generator)),AddEvoOperator(GroundPredicate("on", generator))))
    GroundedAbstractCapability(generator_name,pre,post,Map("1"-> evo_1))
  }
  private def generate_switch_off_generator(name : String) : GroundedAbstractCapability = {
    val generator_name = "switch_OFF_"+name
    val generator = AtomTerm(name)
    val pre = FOLCondition(Literal(Predicate("on", generator )))
    val post = FOLCondition(Literal(Predicate("off", generator )))
    val evo_1 = EvolutionScenario(Array[EvoOperator](RemoveEvoOperator(GroundPredicate("on", generator)),AddEvoOperator(GroundPredicate("off", generator))))
    GroundedAbstractCapability(generator_name,pre,post,Map("1"-> evo_1))
  }
  private def generate_close_switcher(name : String) : GroundedAbstractCapability = {
    val capname = "CLOSE_"+name
    val switcher = AtomTerm(name)
    val pre = FOLCondition(Literal(Predicate("open", switcher )))
    val post = FOLCondition(Literal(Predicate("closed", switcher )))
    val evo_1 = EvolutionScenario(Array[EvoOperator](RemoveEvoOperator(GroundPredicate("open", switcher)),AddEvoOperator(GroundPredicate("closed", switcher))))
    GroundedAbstractCapability(capname,pre,post,Map("1"-> evo_1))
  }
  private def generate_open_switcher(name : String) : GroundedAbstractCapability = {
    val capname = "OPEN_"+name
    val switcher = AtomTerm(name)
    val pre = FOLCondition(Literal(Predicate("closed", switcher )))
    val post = FOLCondition(Literal(Predicate("open", switcher )))
    val evo_1 = EvolutionScenario(Array[EvoOperator](RemoveEvoOperator(GroundPredicate("closed", switcher)),AddEvoOperator(GroundPredicate("open", switcher))))
    GroundedAbstractCapability(capname,pre,post,Map("1"-> evo_1))
  }
  private def generate_combinated_on_off_switcher(name1 : String, name2 : String) : GroundedAbstractCapability = {
    val capname = "CLOSE_"+name1+"_&_OPEN_"+name2
    val switcher1 = AtomTerm(name1)
    val switcher2 = AtomTerm(name2)
    val pre = FOLCondition(Conjunction(Literal(Predicate("open", switcher1 )),Literal(Predicate("closed", switcher2 ))))
    val post = FOLCondition(Conjunction(Literal(Predicate("closed", switcher1 )),Literal(Predicate("open", switcher2 ))))
    val evo_1 = EvolutionScenario(Array[EvoOperator](
      RemoveEvoOperator(GroundPredicate("open", switcher1)),
      AddEvoOperator(GroundPredicate("closed", switcher1)),
      RemoveEvoOperator(GroundPredicate("closed", switcher2)),
      AddEvoOperator(GroundPredicate("open", switcher2))
    ))
    GroundedAbstractCapability(capname,pre,post,Map("1"-> evo_1))
  }


  override def termination : TerminationDescription = MaxEmptyIterationTermination(10)
}
