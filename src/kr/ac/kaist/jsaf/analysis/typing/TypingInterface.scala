/*******************************************************************************
    Copyright (c) 2012-2013, KAIST, S-Core.
    All rights reserved.

    Use is subject to license terms.

    This distribution may include materials developed by third parties.
 ******************************************************************************/

package kr.ac.kaist.jsaf.analysis.typing

import kr.ac.kaist.jsaf.analysis.cfg._
import kr.ac.kaist.jsaf.analysis.typing.domain._
import kr.ac.kaist.jsaf.analysis.typing.models.BuiltinModel

trait TypingInterface {
  def cfg: CFG
  def getMergedState(): State
  def analyze(model: BuiltinModel): Unit = {}
  def analyze(model: BuiltinModel, duset: DUSet): Unit = {}
  def dump(): Unit
  def dump_callgraph(): Unit = {}
  def statistics(statdump: Boolean): Unit = {}

  def builtinFset(): Map[FunctionId, String]
  def readTable(cp: ControlPoint): State = StateBot
  def readTable(node: Node): Option[CState] = None
  def getStateBeforeProgram: State = StateBot
  def getStateAfterProgram: State = StateBot
  def getExcStateAfterProgram: State = StateBot
  def getStateAfterFile(file: String): State = StateBot 
  def getStateAtFunctionEntry(fid: FunctionId): CState = Map()
  def getStateAtFunctionExit(fid: FunctionId): CState = Map()
  def getExcStateAtFunctionExit(fid: FunctionId): CState = Map()
  def getStateBeforeNode(node: Node): CState = Map()
  def getStateAfterNode(node: Node): CState = Map()
  def getStateAfterLine(file: String, line: Int): CState = Map()
  def getStateBeforeInst(inst: CFGInst): CState = Map()
  def getStateAfterInst(inst: CFGInst): CState = Map()
  def getExcStateAfterInst(inst: CFGInst): CState = Map()
  def mergeState(cstate: CState): State = StateBot
  def chooseState(cstate: CState, cc: CallContext): State = StateBot
  def computeCallGraph(): Map[CFGInst, Set[FunctionId]]
  def computePrototypeHierarchy(state: State): Map[Loc, Set[Loc]] = Map()
  def getFuncNameByLoc(state:State, loc: Loc): Set[String] = Set()
  def integrateRecentState(s:State) : State = StateBot
}
