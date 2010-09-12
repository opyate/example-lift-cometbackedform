package code.lib

import net.liftweb.common.Loggable
import net.liftweb.actor.LiftActor
import xml.NodeSeq
import net.liftweb.http.{ReRender, CometListener, ListenerManager}

/**
 * This worker should not know about my CometActors.
 *
 * @author juanuys
 */
object LongRunningWorker extends LiftActor with Loggable {

  var jobs = Map.empty[String, JobStatus]

  override def messageHandler = {

    case NewJob(jobName, actor) => {
      if (jobs.contains(jobName)) {
        logger.warn("Job [" + jobName +"] was already processed.")
      } else {
        jobs += jobName -> JobStatusPending
        actor ! JobReply(jobName, JobStatusPending)

        // emulating something that takes a while
        logger.info("["+jobName+"]: Job received")
        Thread.sleep(10000)
        logger.info("["+jobName+"]: Job completed")

        jobs += jobName -> JobStatusSuccess
        actor ! JobReply(jobName, JobStatusSuccess)
      }
    }
    case _ => {
      logger.warn("LongRunningWorker received unidentified message.")
    }
  }
}

case class NewJob(jobName: String, actor: LiftActor)
case class GetJobStatus(jobName: String)
case class JobReply(jobName: String, jobStatus: JobStatus)

sealed abstract class JobStatus
case object JobStatusPending extends JobStatus
case object JobStatusSuccess extends JobStatus
case object JobStatusFailure extends JobStatus