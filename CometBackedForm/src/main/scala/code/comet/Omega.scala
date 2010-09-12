package code.comet

import net.liftweb.actor.LiftActor
import net.liftweb.http.js.jquery.JqJsCmds.FadeIn
import net.liftweb.http.js.JsCmds.Noop
import net.liftweb.http.js.jquery.JqJsCmds._
import net.liftweb.util.Helpers.TimeSpan
import xml.{Text, NodeSeq}
import net.liftweb.http._
import net.liftweb.http.SHtml._
import net.liftweb.util.Helpers._
import net.liftweb.common.{Full, Box, Empty, Loggable}
import code.lib._

case object FormUpdate

class FormState(var a: Box[String] = Empty, var b: Box[String] = Empty) {
  override def toString = "FormState=[a=["+a+"], b=["+b+"]]"
}

object FormStateSessionVar extends SessionVar[FormState](new FormState)

/**
 * I want Alpha to be submitted with a value, but I don't care what that value is.
 */
class BetaCometActor extends CometActor with Loggable {

  override def render =
    <p>Waiting... (for any value to be inputted into Alpha)</p>

  override def lowPriority = {
    case FormUpdate => {

      logger.info("In BetaCometActor, and the formState is now: " + FormStateSessionVar.is.toString)

      // render the Beta form on condition that 'a' is not Empty

      FormStateSessionVar.is.a match {
        case Full(a) if (!a.isEmpty) => {
          logger.info("Rendering Beta")
          partialUpdate(
            JqSetHtml(uniqueId, theForm) &
            Hide(uniqueId) & FadeIn(uniqueId, TimeSpan(0),TimeSpan(500))
          )
        }
        case _ =>
      }
    }
  }

  val theForm: NodeSeq =
     <span>Render BetaForm, state is now: {FormStateSessionVar.is.toString}</span>
}

/**
 * I want Alpha to be submitted with a value AND I do something different depending on Alpha's submitted value.
 *
 * TODO do something else with FormState.a == "magic", but only when JobStatus != JobStatusPending.
 */
class DeltaCometActor extends CometActor with Loggable {

  override def render =
    <p>Waiting... (If Alpha is "magic", I'll be happy. I don't care about Beta)</p>

  /**
   * TODO figure out a way for the Comet actor to poll periodically... instead of waiting to be contacted.
   */
  override def lowPriority = {
    case FormUpdate => {
      logger.info("In DeltaCometActor, and the formState is now: " + FormStateSessionVar.is.toString)

      LongRunningWorker !! GetJobStatus(FormStateSessionVar.is.a openOr "") match {
        case Full(Some(status)) => {
          status match {
            case JobStatusPending => {
              // pending (worker still working), don't render
            }
            case JobStatusSuccess | JobStatusFailure => {
              // finally, a result. Render!!!
              logger.info("Rendering Delta")
              partialUpdate(
                JqSetHtml(uniqueId, <span>Render DeltaForm, state is now: {FormStateSessionVar.is.toString}</span>) &
                Hide(uniqueId) & FadeIn(uniqueId, TimeSpan(0),TimeSpan(500))
              )
            }
          }
        }
        case _ => {
          // no status, don't render.
        }
      }
    }
  }
}