/*******************************************************************************
    Copyright (c) 2013, S-Core.
    All rights reserved.

    Use is subject to license terms.

    This distribution may include materials developed by third parties.
  ***************************************************************************** */

package kr.ac.kaist.jsaf.analysis.typing.models

import kr.ac.kaist.jsaf.analysis.cfg._
import kr.ac.kaist.jsaf.analysis.typing.domain._
import kr.ac.kaist.jsaf.analysis.typing.{SemanticsExpr => SE, PreSemanticsExpr => PSE, AccessHelper => AH, InternalError, PreHelper, Helper, Access}
import kr.ac.kaist.jsaf.nodes_util.IRFactory

trait ModelData {
  def getInitList(): List[(Loc, List[(String, AbsProperty)])]
  def getSemanticMap(): Map[String, SemanticFun]
  def getPreSemanticMap(): Map[String, SemanticFun]
  def getDefMap(): Map[String, AccessFun]
  def getUseMap(): Map[String, AccessFun]

  /* helper function for semantics */
  val dummyInfo = IRFactory.makeInfo(IRFactory.dummySpan("Model"))
  protected def getArgValue(h : Heap, ctx: Context, args: CFGExpr, x : String):Value = {
    SE.V(CFGLoad(dummyInfo, args, CFGString(x)), h, ctx)._1
  }
  protected def getArgValueAbs(h : Heap, ctx: Context, args: CFGExpr, s : AbsString):Value = {
    val lset = SE.V(args,h,ctx)._1._2
    val v = lset.foldLeft(ValueBot)((v_1, l) => v_1 + Helper.Proto(h,l,s))
    v
  }
  protected def getArgValue_pre(h: Heap, ctx: Context, args: CFGExpr, x: String, PureLocalLoc: Loc): Value = {
    PSE.V(CFGLoad(dummyInfo, args, CFGString(x)), h, ctx, PureLocalLoc)._1
  }
  protected def getArgValueAbs_pre(h: Heap, ctx: Context, args: CFGExpr, s: AbsString, PureLocalLoc: Loc): Value = {
    val lset = PSE.V(args,h,ctx, PureLocalLoc)._1._2
    val v = lset.foldLeft(ValueBot)((v_1, l) => v_1 + PreHelper.Proto(h,l,s))
    v
  }
  protected def getArgValue_use(h : Heap, ctx: Context, args: CFGExpr, x : String): LPSet = {
    Access.V_use(CFGLoad(dummyInfo, args, CFGString(x)), h, ctx)
  }
  protected def getArgValueAbs_use(h : Heap, ctx: Context, args: CFGExpr, s : AbsString): LPSet = {
    val lset = SE.V(args, h, ctx)._1._2
    val LP1 = Access.V_use(args, h, ctx)
    val LP2 = lset.foldLeft(LPBot)((lpset, l) => lpset ++ AH.Proto_use(h,l,s))
    LP1 ++ LP2
  }

  protected def getAddrList(h : Heap, cfg: CFG): List[Address] = {
    val lset_env = h(SinglePureLocalLoc)("@env")._1._2._2
    val set_addr = lset_env.foldLeft[Set[Address]](Set())((a, l) => a + locToAddr(l))
    if (set_addr.size > 1) throw new InternalError("API heap allocation: Size of env address is " + set_addr.size)
    cfg.getAPIAddress(set_addr.head)
  }
  protected def getAddrList_pre(h : Heap, cfg: CFG, PureLocalLoc: Loc): List[Address] = {
    val lset_env = h(PureLocalLoc)("@env")._1._2._2
    val set_addr = lset_env.foldLeft[Set[Address]](Set())((a, l) => a + locToAddr(l))
    if (set_addr.size > 1) throw new InternalError("API heap allocation: Size of env address is " + set_addr.size)
    cfg.getAPIAddress(set_addr.head)
  }
  protected def getAddrList_use(): LPSet = {
    LPSet(SinglePureLocalLoc, "@env")
  }
}
