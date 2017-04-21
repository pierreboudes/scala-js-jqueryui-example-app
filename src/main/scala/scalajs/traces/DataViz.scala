package pb.traces.viz

import scala.scalajs.js
import scala.scalajs.js.JSApp
import js.Dynamic.literal
import pb.traces.datatypes._
import org.scalajs.jquery._
import pb.scalajs.jqueryui._
import pb.scalajs.jqueryui.JQueryUi._
import pb.scalajs.sankey._
import js.JSConverters._
import pb.traces.data.Data._


// Pour XmlHttpRequest
import org.scalajs.dom
import dom.html

import scala.scalajs.js.annotation.JSExport
import scala.util.Random

//@js.native
// object JsPage extends Sankey

object TracesViz extends JSApp {

  def main(): Unit = {
    // Création des éléments de la page
    jQuery(s"""<div id='content'></div>""").appendTo("body")

    /*
    private def successaj = (data: Any, textStatus: String, xhr: JQueryXHR) =>
    println("Status: " + textStatus)

    jQuery.ajax("data/etapes.csv", literal(
      crossDomain = false,
      success = successaj))
     */

/*
    jQuery.ajax("data/etapes.csv", new JQueryAjaxSettings {
      override val crossDomain: js.UndefOr[Boolean] = false
      override val success: js.UndefOr[js.Function3[Any, String, JQueryXHR, _]] = {
        js.defined { (data: Any, textStatus: String, xhr: JQueryXHR) =>
          println("Status: " + textStatus)
        }
      }
    })
 */
    val xhr = new dom.XMLHttpRequest()

    xhr.open("GET",
      "/tracesdeps/data/etapes.csv"
    )

    xhr.onload = { (e: dom.Event) =>
      if (xhr.status == 200) {
        appendForm(xhr.responseText)
      }
    }
    xhr.send()
  }

  var etapes: List[Etape] = List()
  var traces: List[String] = List()

  private def appendForm(r: String): Boolean = {
    // paramètres du questionnaire
    etapes = r.split('\n').map(_.split(';')).map(v => Etape(v(0), v(1), v(2), v(3), v(4))).toList
    val autocItems = etapes.map(etape2autocItemJS)
    val autocItemsJS : js.Array[js.Object] = autocItems.toJSArray
    //        val autocItemsJS : js.Array[AutocItem] = autocItems.toJSArray

    // ajout du questionnaire
    jQuery(s"""<div id='autocomplete'><div id="AutocItem-label">Choisir une étape : <input id="AutocItem"></div>
<input type="hidden" id="AutocItem-id">
<p id="AutocItem-description"></p>
</div>""").appendTo("#content")

    jQuery("#AutocItem").autocomplete(literal(
      minLength = 2,
      source = autocItemsJS,
      focus = focusAutocomplete,
      select = selectAutocomplete
    ))
    false
  }

  private def focusAutocomplete = (event: JQueryEventObject, ui: JQueryUiAutocomplete) => {
    // is equals to $(this)
    jQuery("#AutocItem").value(ui.item.desc)
    false
  }

  private def selectAutocomplete = (event: JQueryEventObject, ui: JQueryUiAutocomplete) => {
    println(ui.item.id)
    // is equals to $(this)
    jQuery("#AutocItem").value(ui.item.label)
    jQuery( "#AutocItem-id" ).value(ui.item.id)
//    jQuery( "#AutocItem-description" ).append(ui.item.desc)
    val xhr = new dom.XMLHttpRequest()

    xhr.open("GET",
      "/tracesdeps/data/up13_traces.csv"
    )

    xhr.onload = { (e: dom.Event) =>
      if (xhr.status == 200) {
        traces = xhr.responseText.split('\n').toList
        makeSankey(xhr.responseText, ui.item.id)
      }
    }
    xhr.send()
    false
  }

  private def makeSankey(tracescsv: String, etape: String) {
    jQuery("#chart").remove()
    jQuery(s"""<div id="chart"></div>""").appendTo("body")
    val etapesmap: Map[String, Etape] = etapes.groupBy({case Etape(a, b, c, d, e) => a}).mapValues(_.apply(0))
    val (height, graph) = csv2sankey(tracescsv, etape, etapesmap)
    val realheight = math.max((height * 3).toInt + 50, 100)
    jQuery("#chart").attr("height",realheight)
    js.Dynamic.global.drawSankey(graph, realheight, 800)
  }
}
