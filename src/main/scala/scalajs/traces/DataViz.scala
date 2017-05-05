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

  var sel_etapes: Set[String] = Set()
  var groups: Map[String,String] = Map()
  var seuil: Int = 1

  def main(): Unit = {
    // Création des éléments de la page
    jQuery(s"""<div id='content'></div>""").appendTo("body")
    jQuery(s"""<p>Une <em>trace</em> est la succession des inscriptions d'un·e étudiant·e pour suivre un diplôme, étape par étape, année après année. Les traces présentées ici sont celles laissées par les étudiant·e·s de l'université Paris 13 entre septembre 2006 et juillet 2016. Choisir des étapes pour observer toutes les traces qui y passent. Les traces identiques sont regroupées en cohortes.</p>""").appendTo("#content")
    jQuery(s"""<div id="formulaire">Chargement des étapes…</div>""").appendTo("#content")
    val xhr = new dom.XMLHttpRequest()

    xhr.open("GET",
      "/viz/traces/data/etapes.csv"
    )

    xhr.onload = { (e: dom.Event) =>
      if (xhr.status == 200) {
        all_etapes = xhr.responseText.split('\n').map(_.split(';')).map(v => Etape(v(0), v(1), v(2), v(3), v(4))).toList.groupBy({case Etape(a, b, c, d, e) => a}).mapValues(_.apply(0))
        appendForm()
      }
    }
    xhr.send()
  }


  var traces: List[String] = List()

  private def appendForm(): Boolean = {
    // paramètres du questionnaire
    val autocItems = all_etapes.values.map(etape2autocItemJS)
    val autocItemsJS : js.Array[js.Object] = autocItems.toJSArray
    //        val autocItemsJS : js.Array[AutocItem] = autocItems.toJSArray

    // ajout du questionnaire
    jQuery("#formulaire").html(s"""
<div id="AutocItem-label">Choisir une étape à observer : <input id="AutocItem">
<span id="selectionEtapes"></span></div>
<div id="Seuil-label">Observer uniquement les cohortes de <input id="Seuil" value="${seuil}"> individu(s) et plus.</div>
<div id="Regrouper-label">Regrouper l'étape <input id="FusionSource"> avec l'étape <input id="FusionBut">
<span id="FusionItems"></span>
</div>
<input type="hidden" id="AutocItem-id">
""")

    jQuery("#AutocItem").autocomplete(literal(
      minLength = 2,
      source = autocItemsJS,
      focus = focusAutocomplete,
      select = selectAutocomplete
    ))

    jQuery("#FusionSource").autocomplete(literal(
      minLength = 2,
      source = autocItemsJS,
      select = selectFusionSource
    ))

    jQuery("#FusionBut").autocomplete(literal(
      minLength = 2,
      source = autocItemsJS,
      select = selectFusionBut
    ))

    jQuery("#Seuil").focusout( (e:JQueryEventObject) => {
      seuil = jQuery("#Seuil").value().toString.toInt
      println(seuil)
      if (!all_traces.isEmpty && !sel_etapes.isEmpty) {
        makeSankey()
      }
      jQuery("#Seuil").value() // un truc du bon type, à régler TODO
    })


    false
  }

  private def removeSource() = (e:JQueryEventObject) => {
    val id = jQuery(e.currentTarget).attr("id").toString.split('_')(1)
    println(id)
    groups = groups - (id)
    showGroups()
    makeSankey()
  }


  private def removeEtape() = (e:JQueryEventObject) => {
    val id = jQuery(e.currentTarget).attr("id").toString.split('_')(1)
    println(id)
    sel_etapes = sel_etapes - (id)
    showSelection()
    makeSankey()
  }

  private def showSelection() {
    jQuery("#selectionEtapes").html(sel_etapes.toList.sorted.reverse.map(y => s"""<div class="etape" id="etape_${y}">${y}</div>""").mkString(""))
    jQuery("#selectionEtapes .etape").click(removeEtape)
  }

  private def showGroups() {
    val rgroups = groups.toList.groupBy(_._2).mapValues(_.map(_._1).sorted).toList.sortBy(_._1).reverse
    val rg = rgroups.map(x => {
      val inside = x._2.map(y => s"""<span class="source" id="source_${y}">${y}</span>""").mkString("")
      s"""<div class="group" id="group_${x._1}"><span class="but">${x._1}</span> ${inside}</div>"""
    })
    jQuery("#FusionItems").html(
      rg.mkString(" ")
    )
    jQuery("#FusionItems .source").click(removeSource)
  }

  private def addFusion(): Boolean = {
    val source = jQuery("#FusionSource").value.toString
    val but = jQuery("#FusionBut").value.toString

    if ((source != "") && (but != "")) {
      println(source + " -> " + but)
      groups = groups + ((source, but))
      showGroups()
      makeSankey()
      true
    } else {
      false
    }
  }

  private def selectFusionSource = (event: JQueryEventObject, ui: JQueryUiAutocomplete) => {
    println("Fusion source :" + ui.item.id)
    jQuery("#FusionSource").value(ui.item.id)
    val clean = addFusion()
    jQuery("#FusionBut").trigger("focus")
    if (clean) jQuery("#FusionSource").value("")
  }

  private def selectFusionBut = (event: JQueryEventObject, ui: JQueryUiAutocomplete) => {
    println("Fusion but :" + ui.item.id)
    jQuery("#FusionBut").value(ui.item.id)
    val clean = addFusion()
    if (clean) jQuery("#FusionSource").value("")
  }

  /* ce truc est useless */
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
    sel_etapes += ui.item.id
    println(sel_etapes)
    showSelection()
    //    jQuery( "#AutocItem-description" ).append(ui.item.desc)
    if (all_traces.isEmpty) {
      val xhr = new dom.XMLHttpRequest()

      xhr.open("GET",
        "/viz/traces/data/up13_traces.csv"
      )

      xhr.onload = { (e: dom.Event) =>
        if (xhr.status == 200) {
          all_traces = xhr.responseText.split('\n').toList
          makeSankey()
        }
      }
      xhr.send()
    } else {
      makeSankey()
    }
    false
  }

  private def makeSankey() {
    jQuery("#chart").remove()
    jQuery(s"""<div id="chart"></div>""").appendTo("body")
    println(groups)
    println(seuil)
    val traces = select_group_traces(sel_etapes, groups, seuil)
    val (height, graph) = traces2graph(traces)
    val realheight = math.max((height * 3).toInt + 50, 100)
    val realwidth = 1200
    jQuery("#chart").attr("height", realheight)
    jQuery("#chart").attr("width", realwidth)
    js.Dynamic.global.drawSankey(graph, realheight, realwidth)
  }
}
