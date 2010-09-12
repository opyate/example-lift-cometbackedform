package code.comet

import net.liftweb.actor.LiftActor
import net.liftweb.http.js.jquery.JqJsCmds.FadeIn
import net.liftweb.http.js.jquery.JqJsCmds._
import net.liftweb.util.Helpers.TimeSpan
import net.liftweb.http._
import js.JE.Str
import js.JsCmd
import js.JsCmds.{SetHtml, Noop}
import net.liftweb.http.SHtml._
import net.liftweb.util.Helpers._
import net.liftweb.common.{Full, Box, Empty, Loggable}
import code.lib._
import net.liftweb.util.ActorPing
import java.util.concurrent.ScheduledFuture
import xml.{Elem, Text, NodeSeq}

case object FormUpdate

class FormState(
        var foo: Box[String] = Empty,
        var bar: Box[String] = Empty,
        var thud: Box[String] = Empty) {
  override def toString = "FormState=[foo=["+foo+"], bar=["+bar+"], thud=["+thud+"]]"
}

object FormStateSessionVar extends SessionVar[FormState](new FormState)

/**
 * I'm using 'foo', 'bar' and 'thud' to denote 'initial', 'subsequent' and 'last' values captured.
 * 'thud' depends on processing done on 'foo'.
 * 'bar' depends on 'foo' to, at least, have a value (i.e. be submitted)
 *
 * http://www.faqs.org/rfcs/rfc3092.html
 */
class CometForm extends CometActor with Loggable {

  override def render =
    <div id={uniqueId+"_foo"}>
      Foo: {ajaxText(FormStateSessionVar.is.foo openOr "", false, it => fooSubmit(it))}
    </div> ++ <div id={uniqueId+"_therest"}>
      <div id={uniqueId+"_bar"}/>
      <div id={uniqueId+"_thud"}/>
    </div>

  override def lowPriority = {
    case JobReply(jobName, jobStatus) if (FormStateSessionVar.is.foo == jobName) => {

      logger.info("CometForm: formState=" + FormStateSessionVar.is.toString)

      jobStatus match {
        case JobStatusPending => {

        }
        case JobStatusSuccess | JobStatusFailure => {
          partialUpdate(thudForm)
        }
      }
    }
  }

  private[this] def fooSubmit(it: String): JsCmd = {
    FormStateSessionVar.is.foo = Full(it)

    // send the new value to a long-running process
    LongRunningWorker ! NewJob(it, this)

    // render bar form
    // and set the spinny wheel on 'thud'
    this ! PartialUpdateMsg(() =>
      JqSetHtml(uniqueId+"_foo", <span>Foo captured!</span>) &
        Hide(uniqueId+"_foo") & FadeIn(uniqueId+"_foo", TimeSpan(0),TimeSpan(500)) &
      barForm &
      JqSetHtml(uniqueId+"_thud", pleaseWaitSnippet) &
        Hide(uniqueId+"_thud") & FadeIn(uniqueId+"_thud", TimeSpan(0),TimeSpan(500)))

    Noop
  }

  private[this] val pleaseWaitSnippet: NodeSeq = {
    <span id={uniqueId+"_wait"}>Please wait...
      <img alt="" id="ajax-loader" src="/images/ajax-loader.gif" width="15" height="15"/>
    </span>
  }

  // bar stuff

  private[this] def barForm: JsCmd = {
    JqSetHtml(uniqueId+"_bar", barSnippet) &
    Hide(uniqueId+"_bar") & FadeIn(uniqueId+"_bar", TimeSpan(0),TimeSpan(500))
  }

  private[this] val barSnippet: NodeSeq = {
    <div id={uniqueId + "_bar"}>
      Bar: {ajaxText(FormStateSessionVar.is.bar openOr "", false, it => barSubmit(it))}
    </div>
  }

  private[this] def barSubmit(it: String): JsCmd = {
    FormStateSessionVar.is.bar = Full(it)

    this ! PartialUpdateMsg(() => JqSetHtml(uniqueId+"_bar", <span>Bar captured!</span>) &
      Hide(uniqueId+"_bar") & FadeIn(uniqueId+"_bar", TimeSpan(0),TimeSpan(500)))
    Noop
  }

  // thud stuff

  private[this] def thudForm: JsCmd = {
    JqSetHtml(uniqueId+"_thud", thudSnippet) &
    Hide(uniqueId+"_thud") & FadeIn(uniqueId+"_thud", TimeSpan(0),TimeSpan(500))
  }

  private[this] val thudSnippet: NodeSeq = {
    <div id={uniqueId + "_thud"}>
      Thud: {ajaxText(FormStateSessionVar.is.thud openOr "", false, it => thudSubmit(it))}
    </div>
  }

  private[this] def thudSubmit(it: String): JsCmd = {
    FormStateSessionVar.is.thud = Full(it)

    this ! PartialUpdateMsg(() => JqSetHtml(uniqueId+"_thud", <span>Thud captured!</span>) &
      Hide(uniqueId+"_thud") & FadeIn(uniqueId+"_thud", TimeSpan(0),TimeSpan(500)))
    Noop
  }
}