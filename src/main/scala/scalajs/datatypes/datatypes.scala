package pb.traces.datatypes
import scala.scalajs.js.annotation._
case class Etape(code: String,  shortname: String, name: String, bacplus: String, composante: String)
// @JSExport
case class AutocItem(id: String, value: String, label: String, desc: String, bacplus: String)
case class Node(name: String, level: Int, legende: String)
case class Link(source: Int, target: Int, value: Int, id: Int)
