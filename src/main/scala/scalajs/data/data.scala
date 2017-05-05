package pb.traces.data

import pb.traces.datatypes._
import scala.scalajs.js
import js.Dynamic.literal
import js.JSConverters._
import scala.util.matching.Regex


object Data {
  var all_traces: List[String] =  List()
  var all_etapes: Map[String, Etape] = Map()

  /* transforme une étape en item pour le formulaire à auto-complétion */
  def etape2autocItemJS(e: Etape): js.Object = {
    val Etape(code, shortname, name, bacplus, composante) = e
    val id = code
    val label = s"${name} ${code} ${composante}"
    val desc = s"${name} Bac + ${bacplus}, ${composante}, ${code}"
//  AutocItem(id, id, label, desc, bacplus)
    js.Dynamic.literal(id = id, value = id, label = label, desc = desc)
  }

  /* produit un graphe acyclique des traces qui concernent un code
  étape particulier TODO casser cette fonction en deux parties : la
  production du graphe proprement dite (et le calcul de ses
  dimensions) et la sélection des traces et
   leur réécriture */

  def select_group_traces(etapes: Set[String], groups: Map[String,String] = Map(), max: Int = 5) = {
    val lignes = all_traces.map(_.split(';').map(_.split('.')))
    lignes.filter(x => x.exists(etapes contains _.last)).map(_.map(
      y => y.updated(y.length - 1, groups.getOrElse(y.last, y.last))
    )).map(_.map(_.mkString("."))).filter(x => x(0).toInt >= max)

  }

  def traces2graph(traces: List[Array[String]]): (Int, js.Object) = {

    def levelling(a: Array[Array[String]]): Vector[(Int, String)] = {
      import scala.util.Try
        val b = a.map(v => (Try {v(0).toInt}.getOrElse(0), v(1)))
      for {
        i <- Range(0, b.length)
        j <- Range(i + 1, b.length)
        if (b(i)._1 >= b(j)._1)
          } {
        b(j) = (b(i)._1 + 1, b(j)._2)
      }
      b.toVector :+ (11,"alumni UP13")
    }

    /* arrange les traces par niveaux et les regroupe */
    val flowgraph = traces.map((x) => (x(0).toInt, levelling(x.drop(1).map(_.split('.').slice(0,2))))).groupBy(_._2).mapValues(_.map(_._1).sum).toList.zipWithIndex.map(t => (t._1._2, t._1._1, t._2))

    val height = flowgraph.map(_._1).sum
    val nodes = flowgraph.flatMap(_._2).toSet.toList.sorted

    def edges(xs:(Int, Vector[(Int,String)], Int)): List[(Int, (Int, String), (Int, String), Int)] = {
      val (n, chain, id) = xs
      val pairs = if (chain.length > 1) chain.sliding(2).toList else List()
      pairs.map((v) => (n, v(0), v(1), id))
    }

    val namedlinks = flowgraph.flatMap(edges)

    def namednode2index(s:(Int, String)): Int = {
      nodes.indexOf(s)
    }
    val links = namedlinks.map {
      case (n, v0, v1, id) => (n, namednode2index(v0), namednode2index(v1), id)
    }


    def jsgraph(nodes: List[(Int,String)], links:List[(Int, Int, Int, Int)]) : js.Object = {

      val nodesjs = nodes.map((p: (Int, String)) => {
        val desc = (all_etapes.get(p._2) match {
          case Some(Etape(c, s, n, b, co)) => s"${n}, Bac + ${b}, ${co}"
          case None => p._2
        })
        js.Dynamic.literal(name = p._2, level = p._1, legende = desc)
      }).toJSArray
      val linkslist = links.map{
        case (value, source, target, id) =>
          js.Dynamic.literal(source = source, target = target, value = value, id = id)
      }.toJSArray
      js.Dynamic.literal(nodes = nodesjs, links = linkslist)
    }

    def reflow(links:List[(Int, Int, Int)]): List[(Int, Int, Int)] = {
      links.groupBy(t => (t._2, t._3)).mapValues(_.map(t => t._1).sum).toList.map {
        case ( (a,b), c) => (c, a, b)
      }
    }
    (height, jsgraph(nodes, links))
  }
}
