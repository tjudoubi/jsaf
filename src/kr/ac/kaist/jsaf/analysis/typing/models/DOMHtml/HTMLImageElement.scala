/*******************************************************************************
    Copyright (c) 2013, KAIST, S-Core.
    All rights reserved.

    Use is subject to license terms.

    This distribution may include materials developed by third parties.
 ******************************************************************************/

package kr.ac.kaist.jsaf.analysis.typing.models.DOMHtml

import scala.collection.mutable.{Map=>MMap, HashMap=>MHashMap}
import kr.ac.kaist.jsaf.analysis.typing.domain._
import kr.ac.kaist.jsaf.analysis.typing.domain.{BoolFalse => F, BoolTrue => T}
import kr.ac.kaist.jsaf.analysis.typing.models._
import org.w3c.dom.Node
import org.w3c.dom.Element
import kr.ac.kaist.jsaf.analysis.typing.Helper
import kr.ac.kaist.jsaf.analysis.cfg.CFG
import kr.ac.kaist.jsaf.analysis.typing.models.AbsConstValue
import scala.Some

object HTMLImageElement extends DOM {
  private val name = "HTMLImageElement"

  /* predefined locatoins */
  val loc_cons = newPredefLoc(name + "Cons")
  val loc_proto = newPredefLoc(name + "Proto")

  /* constructor */
  private val prop_cons: List[(String, AbsProperty)] = List(
    ("@class", AbsConstValue(PropValue(AbsString.alpha("Function")))),
    ("@proto", AbsConstValue(PropValue(ObjectValue(Value(ObjProtoLoc), F, F, F)))),
    ("@extensible", AbsConstValue(PropValue(BoolTrue))),
    ("@hasinstance", AbsConstValue(PropValue(Value(NullTop)))),
    ("length", AbsConstValue(PropValue(ObjectValue(Value(AbsNumber.alpha(0)), F, F, F)))),
    ("prototype", AbsConstValue(PropValue(ObjectValue(Value(loc_proto), F, F, F))))
  )
  
  /* prorotype */
  private val prop_proto: List[(String, AbsProperty)] = List(
    ("@class", AbsConstValue(PropValue(AbsString.alpha("Object")))),
    ("@proto", AbsConstValue(PropValue(ObjectValue(Value(HTMLElement.loc_proto), F, F, F)))),
    ("@extensible", AbsConstValue(PropValue(BoolTrue)))
  )

  /* global */
  private val prop_global: List[(String, AbsProperty)] = List(
      (name, AbsConstValue(PropValue(ObjectValue(loc_cons, T, F, T))))
    )

  def getInitList(): List[(Loc, List[(String, AbsProperty)])] = List(
    (loc_cons, prop_cons), (loc_proto, prop_proto), (GlobalLoc, prop_global)
  )

  def getSemanticMap(): Map[String, SemanticFun] = {
    Map()
  }

  def getPreSemanticMap(): Map[String, SemanticFun] = {
    Map()
  }

  def getDefMap(): Map[String, AccessFun] = {
    Map()
  }

  def getUseMap(): Map[String, AccessFun] = {
    Map()
  }

  /* semantics */
  // no function
     
  /* instance */
  override def getInstance(cfg: CFG): Option[Loc] = Some(addrToLoc(cfg.newProgramAddr, Recent))
  /* list of properties in the instance object */
  override def getInsList(node: Node): List[(String, PropValue)] = node match {
    case e: Element => 
      // This object has all properties of the HTMLElement object 
      HTMLElement.getInsList(node) ++ List(
      ("@class",    PropValue(AbsString.alpha("Object"))),
      ("@proto",    PropValue(ObjectValue(loc_proto, F, F, F))),
      ("@extensible", PropValue(BoolTrue)),
      // DOM Level 1
      ("name", PropValue(ObjectValue(AbsString.alpha(e.getAttribute("name")), T, T, T))),
      ("align", PropValue(ObjectValue(AbsString.alpha(e.getAttribute("align")), T, T, T))),
      ("alt", PropValue(ObjectValue(AbsString.alpha(e.getAttribute("alt")), T, T, T))),
      ("border", PropValue(ObjectValue(AbsString.alpha(e.getAttribute("border")), T, T, T))),
      ("isMap",   PropValue(ObjectValue((if(e.getAttribute("isMap")=="true") T else F), T, T, T))),
      ("longDesc", PropValue(ObjectValue(AbsString.alpha(e.getAttribute("longDesc")), T, T, T))),
      ("src", PropValue(ObjectValue(AbsString.alpha(e.getAttribute("src")), T, T, T))),
      ("useMap", PropValue(ObjectValue(AbsString.alpha(e.getAttribute("useMap")), T, T, T))),
      // Modified in DOM Level 2
      ("height",     PropValue(ObjectValue(Helper.toNumber(PValue(AbsString.alpha(e.getAttribute("height")))), T, T, T))),
      ("hspace",     PropValue(ObjectValue(Helper.toNumber(PValue(AbsString.alpha(e.getAttribute("hspace")))), T, T, T))),
      ("vspace",     PropValue(ObjectValue(Helper.toNumber(PValue(AbsString.alpha(e.getAttribute("vspace")))), T, T, T))),
      ("width",     PropValue(ObjectValue(Helper.toNumber(PValue(AbsString.alpha(e.getAttribute("width")))), T, T, T))))
    case _ => {
      System.err.println("* Warning: " + node.getNodeName + " cannot have instance objects.")
      List()
    }
  }
   
  def getInsList(name: PropValue, align: PropValue, alt: PropValue, border: PropValue, isMap: PropValue, 
                 longDesc: PropValue, src: PropValue, useMap: PropValue, height: PropValue, hspace: PropValue, 
                 vspace: PropValue, width: PropValue): List[(String, PropValue)] = List(
    ("@class",    PropValue(AbsString.alpha("Object"))),
    ("@proto",    PropValue(ObjectValue(loc_proto, F, F, F))),
    ("@extensible", PropValue(BoolTrue)),
    // DOM Level 1
    ("name", name),
    ("align", align),
    ("alt", alt),
    ("border", border),
    ("isMap", isMap),
    ("longDesc", longDesc),
    ("src", src),
    ("useMap", useMap),
    // Modified in DOM Level 2
    ("height", height),
    ("hspace", hspace),
    ("vspace", vspace),
    ("width",  width)
  )
  
  override def default_getInsList(): List[(String, PropValue)] = {    
    val name = PropValue(ObjectValue(AbsString.alpha(""), T, T, T))
    val align = PropValue(ObjectValue(AbsString.alpha(""), T, T, T))
    val alt = PropValue(ObjectValue(AbsString.alpha(""), T, T, T))
    val border = PropValue(ObjectValue(AbsString.alpha(""), T, T, T))
    val isMap = PropValue(ObjectValue(BoolFalse, T, T, T))
    val longDesc = PropValue(ObjectValue(AbsString.alpha(""), T, T, T))
    val src = PropValue(ObjectValue(AbsString.alpha(""), T, T, T))
    val useMap = PropValue(ObjectValue(AbsString.alpha(""), T, T, T))
    val height = PropValue(ObjectValue(UInt, T, T, T))
    val hspace = PropValue(ObjectValue(NumTop, T, T, T))
    val vspace = PropValue(ObjectValue(NumTop, T, T, T))
    val width = PropValue(ObjectValue(UInt, T, T, T))
    // This object has all properties of the HTMLElement object 
    HTMLElement.default_getInsList ::: 
      getInsList(name, align, alt, border, isMap, longDesc, src, useMap, height, hspace, vspace, width)
  }

}