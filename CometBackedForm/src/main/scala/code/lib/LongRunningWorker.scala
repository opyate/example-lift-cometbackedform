package code.lib

import net.liftweb.common.Loggable
import net.liftweb.actor.LiftActor
import net.liftweb.http.ListenerManager

/**
 * This worker should not know about my CometActors.
 *
 * @author juanuys
 */

object LongRunningWorker extends LiftActor with ListenerManager with Loggable {

  var jobs = Map.empty[String, JobStatus]

  def createUpdate = jobs

  override def lowPriority = {
    case GetJobStatus(jobName) => {
      jobs.get(jobName)
    }
    case _ => {
      logger.warn("Worker received unidentified message.")
    }
  }

  override def highPriority = {
    case NewJob(jobName) => {
      jobs += jobName -> JobStatusPending
      // emulating something that takes a while
      logger.info("["+jobName+"]: Job received")
      Thread.sleep(10000)
      logger.info("["+jobName+"]: Job completed")
      jobs += jobName -> JobStatusSuccess
    }
    case _ => {
      logger.warn("Worker received unidentified message.")
    }
  }
}

case class NewJob(jobName: String)
case class GetJobStatus(jobName: String)

sealed abstract class JobStatus
case object JobStatusPending extends JobStatus
case object JobStatusSuccess extends JobStatus
case object JobStatusFailure extends JobStatus