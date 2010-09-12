package code.snippet

import net.liftweb.actor.LiftActor
import net.liftweb.http.js.jquery.JqJsCmds.FadeIn
import net.liftweb.http.js.JsCmds.Noop
import net.liftweb.http.js.jquery.JqJsCmds.{Hide, FadeIn, AppendHtml}
import net.liftweb.util.Helpers.TimeSpan
import xml.{Text, NodeSeq}
import net.liftweb.http._
import js.JsCmd
import net.liftweb.http.SHtml._
import net.liftweb.util.Helpers._
import code.comet.{FormUpdate, FormStateSessionVar}
import net.liftweb.common.{Full, Loggable}
import code.lib.{LongRunningWorker, NewJob}

/**
 * TODO Javadoc here...
 *
 * @author juanuys
 */

class AlphaForm extends Loggable {

  private[this] def doOnSubmit(a: String): JsCmd = {
    FormStateSessionVar.is.a = Full(a)

    // send the new value to a long-running process
    LongRunningWorker ! NewJob(a)

    // while the long-running process is running, render the rest of the form
    S.session.open_!.findComet("BetaCometActor").map(beta => {
      beta ! FormUpdate
    })
    Noop
  }

  def ajaxRender(in: NodeSeq) = {
    logger.info("In AlphaForm, and the formState is now: " + FormStateSessionVar.is.toString)

    bind("f", in,
      "a" -> ajaxText(FormStateSessionVar.is.a openOr "", false, a => doOnSubmit(a))
    )

  }
}

