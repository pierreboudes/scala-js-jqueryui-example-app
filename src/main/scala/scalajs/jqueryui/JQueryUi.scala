package pb.scalajs.jqueryui

import pb.traces.datatypes._
import scala.scalajs.js
import org.scalajs.jquery._

import scala.language.implicitConversions

object JQueryUi {
  implicit def jquery2ui(jquery: JQuery): JQueryUi =
    jquery.asInstanceOf[JQueryUi]
}

@js.native
trait JQueryUi extends JQuery {
  def draggable(options: js.Any): this.type = js.native

  def droppable(options: js.Any): this.type = js.native

  def position(options: js.Any): this.type = js.native

  def autocomplete(options: js.Any): this.type = js.native
}

@js.native
trait JQueryUiObject extends js.Object {
  var helper: JQueryUi = js.native
  var position: JQueryUiCoordinate = js.native
  var offset: JQueryUiCoordinate = js.native
}

@js.native
trait JQueryUiDropObject extends JQueryUiObject {
  var draggable: JQuery = js.native
}

@js.native
trait JQueryUiCoordinate extends js.Object {
  var left: Double = js.native
  var top: Double = js.native
}

@js.native
trait AutocompleteItem extends js.Object {
  var label: String = js.native
  var id: String = js.native
  var desc: String = js.native
  var value: String = js.native
}

@js.native
trait JQueryUiAutocomplete extends js.Object {
    var item:  AutocompleteItem = js.native
}
