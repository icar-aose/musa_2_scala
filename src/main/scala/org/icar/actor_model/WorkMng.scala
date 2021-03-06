package org.icar.actor_model

import akka.actor.{ActorRef, Props}
import org.icar.actor_model.core.{ApplicationConfig, ConcreteCapability, MUSAActor}
import org.icar.actor_model.protocol.{GroundingProtocol, OrchestrationProtocol}
import org.icar.actor_model.role.{GroundingAuctionParticipant, OrchestrationWorker}
import org.icar.pmr_solver.high_level_specification.ConstantTerm

import scala.org.icar.high_level_specification.SolutionTask

class WorkMng(config:ApplicationConfig,concrete:ConcreteCapability) extends MUSAActor
	with GroundingAuctionParticipant
	with OrchestrationWorker {

	val my_log_area = config.logfactory.register_actor(self.path.name)

	var jobs : Map[SolutionTask,Boolean] = Map.empty


	override def role__received_call_for_grounding_auction(sender: ActorRef, msg: GroundingProtocol.CallForProposals): Unit = {
		if (matching(msg.task))
			sender ! msg.participate(concrete.capability_description)
	}
	override def role__win_task_auction(sender: ActorRef, msg: GroundingProtocol.AssignTask): Unit = {
		jobs += msg.task->false
	}
	override def role__received_start_for_concrete(sender: ActorRef, msg: OrchestrationProtocol.RequestConcreteExecution): Unit = {
		jobs += msg.task->true
	}

	override def role__received_concrete_pause(sender: ActorRef, msg: OrchestrationProtocol.PauseConcrete): Unit = {
		jobs += msg.task->false
	}

	override def role__received_concrete_restart(sender: ActorRef, msg: OrchestrationProtocol.RestartConcrete): Unit = {
		jobs += msg.task->false
	}


	def grounding_is_compatible(ground: Map[String, ConstantTerm]): Boolean = {
		var ret = true
		for (k<-ground.keys)
			if (!concrete.constraint.contains(k) || ground(k)!=concrete.constraint(k))
				ret=false

		ret
	}


	private def matching(task:SolutionTask) : Boolean = {
		var ret =false
		if (task.grounding.capability.id == concrete.ref_abstract)
			if (grounding_is_compatible(task.grounding.grounding))
				ret=true

		ret
	}

}


object WorkMng {
	def instance(config:ApplicationConfig,concrete:ConcreteCapability) : Props = Props.create(classOf[WorkMng],config, concrete)

}
