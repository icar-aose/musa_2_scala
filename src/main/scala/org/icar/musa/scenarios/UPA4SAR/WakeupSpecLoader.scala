package org.icar.musa.scenarios.UPA4SAR

import org.icar.fol.AssumptionSet
import org.icar.musa.context.StateOfWorld
import org.icar.musa.main_entity._
import org.icar.musa.pmr.QualityAsset
import org.icar.musa.scenarios.WakeUpScenario
import org.icar.musa.specification._

class UPA4SAR_spec_loader(path: String) extends SpecificationLoader {
  override def domains: Array[DomainLoader] = {
    Array(new UPA4SAR_domain_loader(path))
  }
}

class UPA4SAR_domain_loader(general_path: String) extends DomainLoader {
  val path: String = general_path+"/prin_data"
  val sc = new WakeUpScenario(path)
  val mk = new WakeUpConcreteRepository(sc.capabilities)

  override def name: String = "WakeUp"

  override def initial_state: StateOfWorld = sc.initial_state

  override def assumption: AssumptionSet = sc.assumption_set

  override def goal: LTLGoal = sc.goal_specification

  override def quality_asset: QualityAsset = sc.quality_asset

  override def abstract_repository: Array[AbstractCapability] = sc.capabilities

  override def concrete_repository: Array[ConcreteCapabilityFactory] = mk.load_concrete_capabilty

  override def grounder_type: GrounderProperty = OnDemand()
  override def solution_type: SolutionProperty = EarlyDecisionWorkflow()

  override def active = false
}

