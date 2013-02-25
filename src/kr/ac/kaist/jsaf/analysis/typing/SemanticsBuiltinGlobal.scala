/*******************************************************************************
    Copyright (c) 2012-2013, S-Core, KAIST.
    All rights reserved.

    Use is subject to license terms.

    This distribution may include materials developed by third parties.
 ***************************************************************************** */

package kr.ac.kaist.jsaf.analysis.typing

import kr.ac.kaist.jsaf.analysis.cfg._
import kr.ac.kaist.jsaf.analysis.typing.Operator._
import kr.ac.kaist.jsaf.analysis.typing.domain._
import kr.ac.kaist.jsaf.nodes_util.IRFactory
import kr.ac.kaist.jsaf.analysis.typing.{SemanticsExpr => SE}

import scala.math.{min,max,floor, abs}

object SemanticsBuiltinGlobal {
  def builtinGlobal(sem: Semantics, h: Heap, ctx: Context, he: Heap, ctxe: Context, cp: ControlPoint, cfg: CFG,
                  fun: String, args: CFGExpr): ((Heap, Context),(Heap, Context)) = {
    val dummyInfo = IRFactory.makeInfo(IRFactory.dummySpan("CFGBuilder"))    
    def getArgValue(h : Heap, ctx: Context, x : String):Value = SE.V(CFGLoad(dummyInfo, args, CFGString(x)), h, ctx)._1
        
    fun match {
      case "Global.parseInt" => {
        // 15.1.2.2 parseInt(string, radix)
        val v_1 = getArgValue(h, ctx,"0") /* string */
        val v_2 = getArgValue(h, ctx, "1") /* radix */

        val inputString = Helper.toString(Helper.toPrimitive(v_1))
        // TODO: Simple implementation. Must be revised. Not the same as the original.
        val r = Operator.ToInt32(v_2)

        val value = Operator.parseInt(inputString, r)
        val rtn = Value(value)

        ((Helper.ReturnStore(h, rtn), ctx), (he, ctx))
      }
      case "Global.encodeURIComponent" => {
        // TODO
        val value = Value(StrTop)
        val es = Set[Exception](URIError)
        val (h_e, ctx_e) = Helper.RaiseException(h, ctx, es)

        ((Helper.ReturnStore(h, value), ctx), (he + h_e, ctxe + ctx_e))
      }
      case "Global.isNaN" => {
        val n = Helper.toNumber(Helper.toPrimitive(getArgValue(h, ctx, "0")))
        val b =
          if (NaN == n)
            BoolTrue
          else if (NaN </ n)
            BoolFalse
          else if (NaN <= n)
            BoolTop
          else
            BoolBot
        ((Helper.ReturnStore(h, Value(b)), ctx), (he, ctxe))
      }
      case "Global.isFinite" => {
        val n = Helper.toNumber(Helper.toPrimitive(getArgValue(h, ctx, "0")))
        val b =
          if (NaN == n || PosInf == n || NegInf == n)
            BoolFalse
          else if (NaN </ n && PosInf </ n && NegInf </ n)
            BoolTrue
          else if (NaN <= n || PosInf <= n || NegInf <= n)
            BoolTop
          else
            BoolBot
        ((Helper.ReturnStore(h, Value(b)), ctx), (he, ctxe))
      }
      case "Global.alert" => {
        ((Helper.ReturnStore(h, Value(UndefTop)), ctx), (he, ctxe))
      }
      case _ =>
        System.err.println("* Warning: Semantics of built-in function '"+fun+"' are not defined.")
        ((h,ctx), (he, ctxe))
    }
  }

}
