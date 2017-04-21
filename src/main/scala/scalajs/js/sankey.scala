package pb.scalajs.sankey

import pb.traces.datatypes._
import scala.scalajs.js

import scala.language.implicitConversions

@js.native
trait Sankey extends js.Object {
  def blahdrawSankey(data: js.Any, height: Int, width: Int) = js.native
}
