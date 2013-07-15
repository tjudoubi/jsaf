/*******************************************************************************
    Copyright (c) 2012-2013, KAIST, S-Core.
    All rights reserved.

    Use is subject to license terms.

    This distribution may include materials developed by third parties.
 ******************************************************************************/

package kr.ac.kaist.jsaf.bug_detector

import java.text.Format
import java.util.{HashMap=>JHashMap}
import java.util.{List => JList}
import scala.collection.mutable.{Map=>MMap, HashMap=>MHashMap}
import scala.collection.immutable.HashMap
import kr.ac.kaist.jsaf.ShellParameters
import kr.ac.kaist.jsaf.bug_detector._
import kr.ac.kaist.jsaf.analysis.cfg._
import kr.ac.kaist.jsaf.analysis.typing._
import kr.ac.kaist.jsaf.analysis.typing.domain._
import kr.ac.kaist.jsaf.analysis.typing.{SemanticsExpr => SE}
import kr.ac.kaist.jsaf.scala_src.useful.Lists._

class BugDetector(_params: ShellParameters, _cfg: CFG, _typing: TypingInterface, fileMap: JHashMap[String, String], quiet: Boolean, _shadowings: JList[BugInfo]) {
  val params        = _params
  val cfg           = _cfg
  val typing        = _typing
  val callGraph     = typing.computeCallGraph
  val shadowings    = toList(_shadowings)

  val errorOnly     = params.opt_ErrorOnly
  val locclone      = params.opt_LocClone
  val libMode       = params.opt_Library
  val devMode       = params.opt_DeveloperMode
   
  val semantics     = new Semantics(cfg, Worklist.computes(cfg, quiet), locclone)
  val varManager    = new VarManager(this)
  val stateManager  = new StateManager(this)
  val bugStorage    = new BugStorage(this, fileMap)
  val bugOption     = new BugOption(!devMode)
  val ExprDetect    = new ExprDetect(this)
  val InstDetect    = new InstDetect(this)
  val FinalDetect   = new FinalDetect(this)



  def detectBug() = {
    bugStorage.recordStartTime(System.nanoTime)
    traverseCFG; FinalDetect.check
    bugStorage.recordEndTime(System.nanoTime)
    bugStorage.reportDetectedBugs(params.opt_ErrorOnly, quiet)
  }

  /* Traverse all nodes in CFG */
  def traverseCFG() = {
    for(node <- cfg.getNodes) C(node, cfg.getCmd(node))
    /* Previous code
    cfg.getNodes.foreach((node) =>
      typing.readTable(node) match {
        case Some(map) => C(node, map, cfg.getCmd(node))
        case None => Unit
    })*/

    def C(node: Node, cmd: Cmd): Unit = {
      cmd match {
        case Block(insts) =>
          for(inst <- insts) {
            val cstate = stateManager.getCState(node, inst.getInstId, CallContext._MOST_SENSITIVE)
            I(node, inst, cstate)
          }
        case _ => Unit
      }
    }

    def I(node: Node, inst: CFGInst, stateMap: CState) = {
      inst match {
        case CFGAlloc(_, _ , x, e, a_new) => 
          e match {
            case Some(expr) => V(inst, expr, stateMap)
            case None => Unit
          }
        case CFGAllocArray(_, _, x, n, a_new) => Unit
        case CFGAllocArg(_, _, x, n, a_new) => Unit
        case CFGAssert(_, info, expr, _) => V(inst, expr, stateMap)
        case CFGAPICall(_, model, fun, args) => Unit
        case CFGCall(_, _, fun, base, arguments, a_new) => 
          V(inst, fun, stateMap)
          V(inst, base, stateMap)
          V(inst, arguments, stateMap)
        case CFGConstruct(_, _, cons, base, arguments, a_new) => 
          V(inst, cons, stateMap)
          V(inst, base, stateMap)
          V(inst, arguments, stateMap)
        case CFGCatch(_, _, name) => Unit
        case CFGDelete(_, _, lhs, expr) => V(inst, expr, stateMap)
        case CFGDeleteProp(_, _, lhs, obj, index) => 
          V(inst, obj, stateMap)
          V(inst, index, stateMap)
        case CFGExprStmt(_, _,x, expr) =>
          V(inst, expr, stateMap)
          expr match {
            case CFGString(x) => x match {
              case "safe_print_node"    => cfg.dump(node)
              case "safe_print_states"  => stateManager.dump(node, inst, stateMap)
              case _ => // pass
            } case _ => // pass
          } 
        case CFGFunExpr(_, _, lhs, name, fid, a_new1, a_new2, a_new3) => Unit
        case CFGInternalCall(_, _, lhs, fun, arguments, loc) => 
          (fun.toString, arguments, loc)  match {
            case ("<>Global<>toObject", List(expr), Some(a_new)) => V(inst, expr, stateMap)
            case ("<>Global<>toNumber", List(expr), None) => V(inst, expr, stateMap)
            case ("<>Global<>isObject", List(expr), None) => V(inst, expr, stateMap)
            case ("<>Global<>getBase", List(expr), None) => V(inst, expr, stateMap)
            case ("<>Global<>iteratorInit", List(expr), None) => V(inst, expr, stateMap)
            case ("<>Global<>iteratorHasNext", List(expr_2, expr_3), None) =>
              V(inst, expr_2, stateMap)
              V(inst, expr_3, stateMap)
            case ("<>Global<>iteratorNext", List(expr_2, expr_3), None) =>
              V(inst, expr_2, stateMap)
              V(inst, expr_3, stateMap)
            case _ => Unit
          }
        case CFGReturn(_, _, expr) => 
          expr match {
            case Some(e) => V(inst, e, stateMap)
            case None => Unit
          }
        case CFGStore(_, _, obj, index, rhs) => 
          V(inst, obj, stateMap)
          V(inst, index, stateMap)
          V(inst, rhs, stateMap)
        case CFGThrow(_, _, expr) => V(inst, expr, stateMap)
        case _ => Unit
      }
      InstDetect.check(inst, stateMap)
    }

    def V(inst: CFGInst, expr: CFGExpr, stateMap: CState): Unit = {
      ExprDetect.check(inst, expr, stateMap)
      expr match {
        case CFGBin(info, first, op, second) => 
          V(inst, first, stateMap)
          V(inst, second, stateMap)
        case CFGLoad(info, obj, index) => 
          V(inst, obj, stateMap)
          V(inst, index, stateMap)
        case CFGThis(info) => Unit
        case CFGUn(info, op, first) => V(inst, first, stateMap)
        case CFGVarRef(info, id) => Unit
        case _ => Unit
      }
    }
  }

  def traverseInsts(f: (Node, CFGInst) => Unit): Unit = {
    for(node <- cfg.getNodes) {
      cfg.getCmd(node) match {
        case Block(insts) => for(inst <- insts) f(node, inst)
        case _ =>
      }
    }
  }
}
