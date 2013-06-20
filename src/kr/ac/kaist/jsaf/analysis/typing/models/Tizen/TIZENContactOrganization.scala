/*******************************************************************************
    Copyright (c) 2013, S-Core.
    All rights reserved.

    Use is subject to license terms.

    This distribution may include materials developed by third parties.
  ******************************************************************************/

package kr.ac.kaist.jsaf.analysis.typing.models.Tizen

import kr.ac.kaist.jsaf.analysis.cfg.{CFG, CFGExpr}
import kr.ac.kaist.jsaf.analysis.typing.domain.{BoolFalse => F, BoolTrue => T, _}
import kr.ac.kaist.jsaf.analysis.typing.models._
import kr.ac.kaist.jsaf.analysis.typing.models.AbsConstValue
import kr.ac.kaist.jsaf.analysis.typing._
import java.lang.InternalError
import kr.ac.kaist.jsaf.analysis.typing.models.AbsInternalFunc
import kr.ac.kaist.jsaf.analysis.typing.models.AbsConstValue
import kr.ac.kaist.jsaf.analysis.typing.models.AbsInternalFunc
import kr.ac.kaist.jsaf.analysis.typing.models.AbsConstValue
import kr.ac.kaist.jsaf.analysis.typing.models.AbsInternalFunc
import kr.ac.kaist.jsaf.analysis.typing.models.AbsConstValue
import kr.ac.kaist.jsaf.analysis.typing.models.AbsInternalFunc
import kr.ac.kaist.jsaf.analysis.typing.models.AbsConstValue
import kr.ac.kaist.jsaf.analysis.typing.models.AbsInternalFunc
import kr.ac.kaist.jsaf.analysis.typing.models.AbsConstValue
import kr.ac.kaist.jsaf.analysis.typing.domain.UIntSingle
import kr.ac.kaist.jsaf.analysis.typing.domain.Context
import kr.ac.kaist.jsaf.analysis.typing.models.AbsInternalFunc
import kr.ac.kaist.jsaf.analysis.typing.models.AbsConstValue
import kr.ac.kaist.jsaf.analysis.typing.domain.Heap

object TIZENContactOrganization extends Tizen {
  private val name = "ContactOrganization"
  /* predefined locations */
  val loc_cons = newPredefLoc(name + "Cons")
  val loc_proto = newPredefLoc(name + "Proto")
  /* constructor or object*/
  private val prop_cons: List[(String, AbsProperty)] = List(
    ("@class", AbsConstValue(PropValue(AbsString.alpha("Function")))),
    ("@proto", AbsConstValue(PropValue(ObjectValue(Value(ObjProtoLoc), F, F, F)))),
    ("@extensible",                 AbsConstValue(PropValue(T))),
    ("@scope",                      AbsConstValue(PropValue(Value(NullTop)))),
    ("@construct",               AbsInternalFunc("tizen.ContactOrganization.constructor")),
    ("@hasinstance", AbsConstValue(PropValue(Value(NullTop)))),
    ("prototype", AbsConstValue(PropValue(ObjectValue(Value(loc_proto), F, F, F)))),
    ("name", AbsConstValue(PropValue(Value(UndefTop)))),
    ("department", AbsConstValue(PropValue(Value(UndefTop)))),
    ("title", AbsConstValue(PropValue(Value(UndefTop)))),
    ("role", AbsConstValue(PropValue(Value(UndefTop)))),
    ("logoURI", AbsConstValue(PropValue(Value(UndefTop))))
  )

  /* prototype */
  private val prop_proto: List[(String, AbsProperty)] = List(
    ("@class", AbsConstValue(PropValue(AbsString.alpha("CallbackObject")))),
    ("@proto", AbsConstValue(PropValue(ObjectValue(Value(ObjProtoLoc), F, F, F)))),
    ("@extensible", AbsConstValue(PropValue(T)))
  )

  override def getInitList(): List[(Loc, List[(String, AbsProperty)])] = List(
    (loc_cons, prop_cons), (loc_proto, prop_proto)
  )

  override def getSemanticMap(): Map[String, SemanticFun] = {
    Map(
       ("tizen.ContactOrganization.constructor" -> (
         (sem: Semantics, h: Heap, ctx: Context, he: Heap, ctxe: Context, cp: ControlPoint, cfg: CFG, fun: String, args: CFGExpr) => {
           val lset_env = h(SinglePureLocalLoc)("@env")._1._2._2
           val set_addr = lset_env.foldLeft[Set[Address]](Set())((a, l) => a + locToAddr(l))
           if (set_addr.size > 1) throw new InternalError("API heap allocation: Size of env address is " + set_addr.size)
           val addr_env = set_addr.head
           val addr1 = cfg.getAPIAddress(addr_env, 0)
           val l_r1 = addrToLoc(addr1, Recent)
           val (h_1, ctx_1) = Helper.Oldify(h, ctx, addr1)
           val n_arglen = Operator.ToUInt32(getArgValue(h_1, ctx_1, args, "length"))

           val o_new = ObjEmpty.
             update("@class", PropValue(AbsString.alpha("Object"))).
             update("@proto", PropValue(ObjectValue(Value(TIZENContactOrganization.loc_proto), F, F, F))).
             update("@extensible", PropValue(T)).
             update("@scope", PropValue(Value(NullTop))).
             update("@hasinstance", PropValue(Value(NullTop)))

           val (h_2, es_1) = n_arglen match {
             case UIntSingle(n) if n == 0 =>
                val o_new2 = o_new.
                 update("name", PropValue(ObjectValue(Value(NullTop), T, T, T))).
                 update("department", PropValue(ObjectValue(Value(NullTop), T, T, T))).
                 update("title", PropValue(ObjectValue(Value(NullTop), T, T, T))).
                 update("role", PropValue(ObjectValue(Value(NullTop), T, T, T))).
                 update("logoURI", PropValue(ObjectValue(Value(NullTop), T, T, T)))
               val h_2 = h_1.update(l_r1, o_new2)
               (h_2, TizenHelper.TizenExceptionBot)
             case UIntSingle(n) if n >= 1 =>
               val v = getArgValue(h_1, ctx_1, args, "0")
               val es =
                 if (v._1 </ PValueBot)
                   Set[WebAPIException](TypeMismatchError)
                 else TizenHelper.TizenExceptionBot
               val (obj, ess) = v._2.foldLeft((o_new, TizenHelper.TizenExceptionBot))((_o, l) => {
                 val v_1 = Helper.Proto(h_1, l, AbsString.alpha("name"))
                 val v_2 = Helper.Proto(h_1, l, AbsString.alpha("department"))
                 val v_3 = Helper.Proto(h_1, l, AbsString.alpha("title"))
                 val v_4 = Helper.Proto(h_1, l, AbsString.alpha("role"))
                 val v_5 = Helper.Proto(h_1, l, AbsString.alpha("logoURI"))
                 val es_1 =
                   if (v_1._1._1 </ UndefTop && v_1._1._5 </ StrTop)
                     Set[WebAPIException](TypeMismatchError)
                   else TizenHelper.TizenExceptionBot
                 val o_1 =
                   if (v_1._1._5 </ StrBot)
                     _o._1.update("name", PropValue(ObjectValue(Value(v_1._1._5), T, T, T)))
                   else _o._1.update("name", PropValue(ObjectValue(Value(NullTop), T, T, T)))
                 val es_2 =
                   if (v_2._1._1 </ UndefTop && v_2._1._5 </ StrTop)
                     Set[WebAPIException](TypeMismatchError)
                   else TizenHelper.TizenExceptionBot
                 val o_2 =
                   if (v_2._1._5 </ StrBot)
                     _o._1.update("department", PropValue(ObjectValue(Value(v_2._1._5), T, T, T)))
                   else _o._1.update("department", PropValue(ObjectValue(Value(NullTop), T, T, T)))
                 val es_3 =
                   if (v_3._1._1 </ UndefTop && v_3._1._5 </ StrTop)
                     Set[WebAPIException](TypeMismatchError)
                   else TizenHelper.TizenExceptionBot
                 val o_3 =
                   if (v_3._1._5 </ StrBot)
                     _o._1.update("title", PropValue(ObjectValue(Value(v_3._1._5), T, T, T)))
                   else _o._1.update("title", PropValue(ObjectValue(Value(NullTop), T, T, T)))
                 val es_4 =
                   if (v_4._1._1 </ UndefTop && v_4._1._5 </ StrTop)
                     Set[WebAPIException](TypeMismatchError)
                   else TizenHelper.TizenExceptionBot
                 val o_4 =
                   if (v_4._1._5 </ StrBot)
                     _o._1.update("role", PropValue(ObjectValue(Value(v_4._1._5), T, T, T)))
                   else _o._1.update("role", PropValue(ObjectValue(Value(NullTop), T, T, T)))
                 val es_5 =
                   if (v_5._1._1 </ UndefTop && v_5._1._5 </ StrTop)
                     Set[WebAPIException](TypeMismatchError)
                   else TizenHelper.TizenExceptionBot
                 val o_5 =
                   if (v_5._1._5 </ StrBot)
                     _o._1.update("logoURI", PropValue(ObjectValue(Value(v_5._1._5), T, T, T)))
                   else _o._1.update("logoURI", PropValue(ObjectValue(Value(NullTop), T, T, T)))
                 (o_1 + o_2 + o_3 + o_4 + o_5, _o._2 ++ es_1 ++ es_2 ++ es_3 ++ es_4 ++ es_5)
               })
               val h_2 = h_1.update(l_r1, obj)
               (h_2, es ++ ess)
             case _ => {
               (h_1, TizenHelper.TizenExceptionBot)
             }
           }
           val (h_e, ctx_e) = TizenHelper.TizenRaiseException(h, ctx, es_1)
           ((Helper.ReturnStore(h_2, Value(l_r1)), ctx_1), (he + h_e, ctxe + ctx_e))
         }
         ))
    )
  }

  override def getPreSemanticMap(): Map[String, SemanticFun] = {
    Map()
  }
  override def getDefMap(): Map[String, AccessFun] = {
    Map()
  }
  override def getUseMap(): Map[String, AccessFun] = {
    Map()
  }
}