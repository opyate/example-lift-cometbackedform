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

/**
 *
 * 1. Render an AJAX select or text box for the CI name, along with a Comet section on the page
 * that starts out empty (or with a message like "please choose CI env". If you need more than
 * one input to determine the CI env you can use ajaxForm.
 *
 * 2. When someone submits a change on the AJAX element (onchange with ajaxSelect, onblur for ajaxText),
 * the result is sending a message to the Comet actor to run processing. At that point you can have the
 * Comet actor re-render with a "processing..." result, as well as firing off a message to the real worker
 * actor. When the real worker actor finishes, it just notifies the Comet actor, which re-renders the page
 * with a form for selecting the list of RPMs, etc
 *
 * @author juanuys
 */

case object FormUpdate

class FormState(var a: Box[String] = Empty, var b: Box[String] = Empty) {
  override def toString = "FormState=[a=["+a+"], b=["+b+"]]"
}

object FormStateSessionVar extends SessionVar[FormState](new FormState)

/**
 * This form DOES NOT depend on Alpha's value, but DOES depend on Alpha at least being captured.
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
 * This form DOES depend on Alpha's value
 */
class DeltaCometActor extends CometActor with Loggable {

  override def render =
    <p>Waiting... (If Alpha is "magic", I'll be happy. I don't care about Beta)</p>

  /**
   * TODO figure out a way for the Comet actor to poll periodically...
   */
  override def lowPriority = {
    case FormUpdate => {

      logger.info("In BetaCometActor, and the formState is now: " + FormStateSessionVar.is.toString)

      // render the Beta form on condition that 'a' is not Empty

      FormStateSessionVar.is.a match {
        case Full(a) if (!a.isEmpty) => {
          logger.info("Rendering Beta")
          partialUpdate(
            JqSetHtml(uniqueId, <span>Render DeltaForm, state is now: {FormStateSessionVar.is.toString}</span>) &
            Hide(uniqueId) & FadeIn(uniqueId, TimeSpan(0),TimeSpan(500))
          )
        }
        case _ =>
      }
    }
  }
}