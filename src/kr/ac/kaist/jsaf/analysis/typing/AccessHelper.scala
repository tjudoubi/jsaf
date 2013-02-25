/*******************************************************************************
    Copyright (c) 2012-2013, S-Core, KAIST.
    All rights reserved.

    Use is subject to license terms.

    This distribution may include materials developed by third parties.
 ***************************************************************************** */

package kr.ac.kaist.jsaf.analysis.typing
import kr.ac.kaist.jsaf.analysis.cfg.{CFGId, GlobalVar, PureLocalVar, CapturedVar, CapturedCatchVar}
import kr.ac.kaist.jsaf.analysis.typing.domain._
import kr.ac.kaist.jsaf.analysis.typing.Config.DEBUG
import kr.ac.kaist.jsaf.analysis.cfg.FunctionId
import kr.ac.kaist.jsaf.analysis.typing.domain.{BoolTrue => BT, BoolFalse => BF}
import com.sun.org.apache.xpath.internal.operations.Equals
import scala.collection.immutable.HashSet


object AccessHelper {
  var NewObject_def = Set("@class", "@proto", "@extensible", "@default_number", "@default_other")
  var NewArrayObject_def = Set("@class", "@proto", "length", "@extensible", "@default_number", "@default_other")
  var NewArgObject_def = Set("@class", "@proto", "length", "@extensible", "@default_number", "@default_other")
  var NewFunctionObject_def = Set("@class", "@proto", "@extensible", "@function", "@construct", "@scope", "@default_number", "@default_other", "prototype", "length")
  var NewDeclEnvRecord_def = Set("@outer", "@default_number", "@default_other")
  var NewBoolean_def = Set("@class", "@proto", "@extensible", "@primitive", "@default_number", "@default_other")
  var NewNumber_def = Set("@class", "@proto", "@extensible", "@primitive", "@default_number", "@default_other")
  var NewDate_def = Set("@class", "@proto", "@extensible", "@primitive", "@default_number", "@default_other")
  def NewString_def(v: AbsString): Set[String] = {
    val s = v
    val v_len = s.length()

    val P_1 = Set("@class", "@proto", "@extensible", "@primitive", "@default_number", "@default_other")
    val P_2 = v_len match {
      case UIntSingle(length) => {
        (0 until length.toInt).foldLeft[Set[String]](Set())((s, i) => s + (i.toString))
      }
      case _ => Set()
    }
    P_1 ++ P_2
  }

  def Oldify_def(h: Heap, ctx: Context, a: Address): LPSet = {
    val l_r: Loc = addrToLoc(a, Recent)
    val l_o: Loc = addrToLoc(a, Old)
    val LP_1 =
      if (h.domIn(l_r))
        h(l_r).map.foldLeft(LPBot)((S,v) => S + ((l_o, v._1)) + ((l_r, v._1)))
      else
        LPBot
    val LP_3 = LPSet(Set((ContextLoc, "3"), (ContextLoc, "4")))
    val LP_4 = h.map.keySet.foldLeft(LPBot)((S, l) => {
      S ++ h(l).map.keySet.foldLeft(LPBot)((Sp, x) =>
        if (h(l)(x)._1._1._1._2.contains(l_r) || h(l)(x)._1._2._2.contains(l_r))
          Sp + ((l,x))
        else Sp)
    })

    LP_1 ++ LP_3 ++ LP_4
  }

  def VarStore_def(h: Heap, env: LocSet, id: CFGId): LPSet = {
    val x = id.getText
    id.getVarKind match {
      case PureLocalVar =>
        LPSet((SinglePureLocalLoc, x))
      case CapturedVar =>
        env.foldLeft(LPBot)((S,l) => S ++ VarStoreL_def(h,l,x))
      case CapturedCatchVar =>
        LPSet((CollapsedLoc, x))
      case GlobalVar => {
        VarStoreG_def(h,x)
      }
    }
  }

  def VarStoreL_def(h: Heap, l: Loc, x: String): LPSet = {
    val env_obj = h(l)
    val L_outer = env_obj("@outer")._1._2._2
    val LP_1 =
      if (env_obj.dom(x))
        env_obj(x)._1._1._2 match {
          case BoolTrue =>
            LPSet((l,x))
          case BoolFalse =>
            LPBot
          case _ => 
            throw new InternalError("Writable attribute must be exact for variables in local env.") 
        }
      else LPBot
    val LP_2 =
      if (BoolFalse <= h(l).domIn(x))
        // TODO: infinite recursion.
        L_outer.foldLeft(LPBot)((S,l_outer) => S ++ VarStoreL_def(h, l_outer, x))
      else LPBot

    LP_1 ++ LP_2
  }

  def VarStoreG_def(h: Heap, x: String): LPSet = {
    val l_g = GlobalLoc
    val LP_1 =
      if (BoolFalse <= h(l_g).domIn(x))
        PropStore_def(h,l_g, AbsString.alpha(x))
      else LPBot
    val LP_2 =
      if (BoolTrue <= h(l_g).domIn(x))
        LPSet((l_g,x))
      else
        LPBot

    LP_1 ++ LP_2
  }

  def PropStore_def(h: Heap, l: Loc, s: AbsString): LPSet = {
    absPair(h,l,s)
  }

  def Delete_def(h: Heap, l: Loc, s: AbsString): LPSet = {
    if (((BoolTrue <= Helper.HasOwnProperty(h, l, s)) && (BoolFalse <= h(l)(s)._1._1._4)) ||
        (BoolFalse <= Helper.HasOwnProperty(h, l, s)))
      absPair(h, l, s)
    else LPBot
  }

  def CreateMutableBinding_def(h: Heap, env: LocSet, id: CFGId): LPSet = {
    val x = id.getText
    id.getVarKind match {
      case PureLocalVar =>
        LPSet((SinglePureLocalLoc, x))
      case CapturedVar =>
        env.foldLeft(LPBot)((S,l) => S + (l, x))  //VarStoreL_def(h,l,x))
      case CapturedCatchVar =>
        LPSet((CollapsedLoc, x))
      case GlobalVar => {
        LPSet((GlobalLoc, x))
      }
    }
  }

  def toObject_def(h: Heap, ctx: Context, v: Value, a: Address): LPSet = {
    val o_1 = 
      if (!(v._1._5 <= StrBot)) NewString_def(v._1._5)
      else Set[String]()
    val o_2 = 
      if (!(v._1._3 <= BoolBot)) NewBoolean_def
      else Set[String]()
    val o_3 = 
      if (!(v._1._4 <= NumBot)) NewNumber_def
      else Set[String]()
    val o = o_1 ++ o_2 ++ o_3

    val l_r = addrToLoc(a, Recent)
    if (!o.isEmpty)
      Oldify_def(h,ctx,a) ++ o.foldLeft(LPBot)((S,p) => S + ((l_r, p)))
    else LPBot
  }

  def RaiseException_def(es:Set[Exception]): LPSet = {
    if (es.isEmpty) {
      (LPBot)
    } else {
      LPSet(Set((SinglePureLocalLoc, "@exception_all"), (SinglePureLocalLoc, "@exception")))
    }
  }
  
  /* built-in helper */
  def DefineProperties_def(h: Heap, l_1: Loc, l_2: Loc): LPSet = {
    val props = h(l_2).getProps
    props.foldLeft(LPBot)((lpset, p) => {
      val prop = AbsString.alpha(p)
      val v_1 = Helper.Proto(h, l_2, prop)
      lpset ++ v_1._2.foldLeft(LPBot)((_lpset, l) => _lpset ++ DefineProperty_def(h, l_1, prop, l))
    })
  }

  /* built-in helper */
  def DefineProperty_def(h: Heap, l_1: Loc, s: AbsString, l_2: Loc) : LPSet = {
    absPair(h, l_1, s)
  }
  
  
  def lookup(h: Heap, l: Loc, x: String): LPSet = {
    val LP_1 = 
      AbsString.alpha(x) match {
        case NumStrSingle(v) => LPSet((l, "@default_number"))
        case OtherStrSingle(v) => LPSet((l, "@default_other"))
        case _ =>{
          System.err.println("impossible case?")
          throw new InternalError("impossible case.") 
        }
      }

    val LP_2 = LPSet((l, x))

    LP_1 ++ LP_2
  }

  def absPair(h: Heap, l: Loc, s: AbsString): LPSet = {
    if (!h.domIn(l)) LPBot
    else {
      s match {
        case StrTop =>
          val pset = h(l).map.keySet.filter(x => !x.take(1).equals("@"))
        val is = LPSet(Set((l,"@default_number"),(l,"@default_other")))
        pset.foldLeft(is)((S,x) => S + ((l,x)))
        case StrBot => LPBot
        case NumStr =>
          val pset = h(l).map.keySet.filter(x => !x.take(1).equals("@") && AbsString.alpha(x) <= NumStr)
        val is = LPSet((l,"@default_number"))
        pset.foldLeft(is)((S,x) => S + ((l,x)))
        case OtherStr =>
          val pset = h(l).map.keySet.filter(x => !x.take(1).equals("@") && AbsString.alpha(x) <= OtherStr)
        val is = LPSet((l,"@default_other"))
        pset.foldLeft(is)((S,x) => S + ((l,x)))
        case NumStrSingle(v) => lookup(h,l,v) + ((l,"@default_number"))
        case OtherStrSingle(v) => lookup(h,l,v) + ((l,"@default_other"))
      }
    }
  }    

  def Proto_use(h: Heap, l: Loc, s: AbsString): LPSet = {
    var visited = LocSetBot

    def iter(h: Heap, l: Loc, s: AbsString): LPSet = {
      if (!visited.contains(l)) {
        visited += l
        val LP =
          if (BoolFalse <= h(l).domIn(s)) {
            val lset_proto = h(l)("@proto")._1._1._1._2
            lset_proto.foldLeft(LPBot)((S,l_proto) => S ++ iter(h,l_proto,s))
          } else LPBot

        LP ++ absPair(h,l,s) + ((l, "@proto"))
      } else {
        LPBot
      }
    }

    iter(h, l, s)
  }

  def Lookup_use(h: Heap, env: LocSet, id: CFGId): LPSet = {
    // System.out.println("Lookup_use("+id.getText+")")
    val x = id.getText
    // System.out.println(id.getVarKind)
    val lpset = id.getVarKind match {
      case PureLocalVar =>
        lookup(h,SinglePureLocalLoc,x)
      case CapturedVar =>
        env.foldLeft(LPBot)((S, l) => S ++ LookupL_use(h,l,x))
      case CapturedCatchVar =>
        lookup(h, CollapsedLoc, x)
      case GlobalVar => {
        LookupG_use(h,x)
      }
    }
    // System.out.println(lpset.toString)
    lpset
  }

  def LookupL_use(h: Heap, l: Loc, x: String): LPSet = {
    var visited = LocSetBot

    def iter(h: Heap, l: Loc, x: String): LPSet = {
      if (!visited.contains(l)) {
        visited += l
        val env_obj = h(l)
        if (env_obj.dom(x))
          lookup(h, l, x)
        else {
          val lset_outer = env_obj("@outer")._1._2._2
          LPSet((l, "@outer")) ++
          lset_outer.foldLeft(LPBot)((S, l_outer) => S ++ iter(h,l_outer,x))
        }
      } else {
        LPBot
      }
    }

    iter(h, l, x)
  }

  def LookupG_use(h: Heap, x: String): LPSet = {
    val lset_proto = h(GlobalLoc)("@proto")._1._1._1._2
    val ax = AbsString.alpha(x)
    val LP_1 = lookup(h, GlobalLoc, x) ++ LPSet((GlobalLoc, "@proto"))
    val LP_2 = lset_proto.foldLeft(LPBot)((S, l_proto) => S + ((l_proto, x)))
    val LP_3 =
      lset_proto.foldLeft(LPBot)((S, l_proto) => {
        val L =
          if (BoolTrue <= Helper.HasProperty(h, l_proto, ax))
            Proto_use(h,l_proto,ax)
          else LPBot
        S ++ L
      })
    LP_1 ++ LP_2 ++ LP_3
  }

  def TypeTag_use(h: Heap, v: Value): LPSet = {
    v._2.foldLeft(LPBot)((S, l) => S ++ IsCallable_use(h,l))
  }

  def IsCallable_use(h: Heap, l: Loc): LPSet = {
    LPSet((l, "@function"))
  }

  def HasConstruct_use(h: Heap, l: Loc): LPSet = {
    LPSet((l, "@construct"))
  }

  def inherit_use(h: Heap, l_1: Loc, l_2: Loc): LPSet = {
    var visited = LocSetBot

    def iter(h: Heap, l_1: Loc, l_2: Loc): LPSet = {
      if (!visited.contains(l_1)) {
        visited += l_1
        val LP =
          if (l_1 == l_2)
            LPBot
          else {
            val lset_proto = h(l_1)("@proto")._1._1._1._2
            lset_proto.foldLeft(LPBot)((S, l) => S ++ iter(h,l,l_2))
          }
        LP + ((l_1, "@proto"))
      } else {
        LPBot
      }
    }

    iter(h, l_1, l_2)
  }

  def Oldify_use(h: Heap, ctx: Context, a: Address): LPSet = {
    val l_r: Loc = addrToLoc(a, Recent)
    val l_o: Loc = addrToLoc(a, Old)
    val LP_1 =
      if (h.domIn(l_r))
        h(l_r).map.foldLeft(LPBot)((S,v) => S ++ lookup(h, l_r, v._1))// ++ lookup(h, l_o, v._1))
      else
        LPBot
    val LP_3 = h.map.keySet.foldLeft(LPBot)((S, l) => {
      S ++ h(l).map.keySet.foldLeft(LPBot)((Sp, x) =>
        if (h(l)(x)._1._1._1._2.contains(l_r) || h(l)(x)._1._2._2.contains(l_r))
          Sp ++ lookup(h,l,x)
        else Sp)
    })
    val LP_4 = absPair(h, l_r, StrTop)

    LP_1 ++ LP_3 ++ LP_4
  }

  def VarStore_use(h: Heap, env: LocSet, id: CFGId): LPSet = {
    val x = id.getText

    id.getVarKind match {
      case PureLocalVar =>
        lookup(h, SinglePureLocalLoc, x)
      case CapturedVar =>
        env.foldLeft(LPBot)((S,l) => S ++ VarStoreL_use(h,l,x))
      case CapturedCatchVar =>
        lookup(h, CollapsedLoc, x)
      case GlobalVar => {
        VarStoreG_use(h,x) ++ CanPutVar_use(h,x)
      }
    }
  }

  def VarStoreL_use(h: Heap, l: Loc, x: String): LPSet = {
    if (h(l).dom(x))
      lookup(h,l,x)
    else {
      val lset_outer = h(l)("@outer")._1._2._2
      lset_outer.foldLeft(LPBot)((S, l_outer) => S ++ VarStoreL_use(h,l_outer,x) + ((l,"@outer")))
    }
  }

  def VarStoreG_use(h: Heap, x: String): LPSet = {
    val l_g = GlobalLoc
    val LP =
      if (BoolFalse <= h(l_g).domIn(x))
        PropStore_use(h,l_g, AbsString.alpha(x))
      else LPBot
    LP ++ lookup(h, l_g, x)
  }

  def PropStore_use(h: Heap, l: Loc, s: AbsString): LPSet = {
    absPair(h, l, s)
  }

  def CanPutVar_use(h: Heap, x: String): LPSet = {
    val l_g = GlobalLoc
    val LP =
      if (BoolFalse <= h(l_g).domIn(x))
        CanPut_use(h,GlobalLoc,AbsString.alpha(x))
      else
        LPBot
    
    LP ++ lookup(h, l_g, x)
  }

  def CanPut_use(h: Heap, l: Loc, s: AbsString): LPSet = {
    CanPutHelp_use(h,l,s,l)
  }

  def CanPutHelp_use(h: Heap, l_1: Loc, s: AbsString, l_2: Loc): LPSet = {
    var visited = LocSetBot

    def iter(h: Heap, l_1: Loc, s: AbsString, l_2: Loc): LPSet = {
      if (!visited.contains(l_1)) {
        visited += l_1
        val v_proto = h(l_1)("@proto")._1._1._1
        val lset_proto = v_proto._2
        val LP_1 =
          if (BoolFalse <= h(l_1).domIn(s))
            lset_proto.foldLeft(LPBot)((S, l_proto) => S ++ iter(h,l_proto,s,l_2))
          else
            LPBot
        val LP_2 =
          if (v_proto._1._2 </ NullBot)
            LPSet((l_2, "@extensible"))
          else
            LPBot

        LP_1 ++ LP_2 ++ absPair(h,l_1,s) + ((l_1, "@proto"))
      } else {
        LPBot
      }
    }

    iter(h, l_1, s, l_2)
  }

  def LookupBase_use(h: Heap, env: LocSet, id: CFGId): LPSet = {
    val x = id.getText
    id.getVarKind match {
      case PureLocalVar =>
        LPBot
      case CapturedVar =>
        env.foldLeft(LPBot)((S, l) => S ++ LookupBaseL_use(h,l,x))
      case CapturedCatchVar =>
        LPBot
      case GlobalVar => {
        LookupBaseG_use(h,x)
      }
    }
  }

  def LookupBaseL_use(h: Heap, l: Loc, x: String): LPSet = {
    val LP = 
      if (h(l).dom(x))
        LPBot
      else {
        val lset_outer = h(l)("@outer")._1._2._2
        lset_outer.foldLeft(LPBot)((S, l_outer) => S ++ LookupBaseL_use(h,l_outer,x))
      }

    LP ++ lookup(h, l, x) ++ LPSet((l, "@outer"))
  }

  def LookupBaseG_use(h: Heap, x: String): LPSet = {
    val lset_proto = h(GlobalLoc)("@proto")._1._1._1._2
    val ax = AbsString.alpha(x)
    val LP_1 = lset_proto.foldLeft(LPBot)((S, l_proto) => S + ((l_proto, x)))
    val LP_2 =
      lset_proto.foldLeft(LPBot)((S, l_proto) => {
        val L =
          if (BoolTrue <= Helper.HasProperty(h, l_proto, ax))
            Proto_use(h,l_proto,ax)
          else LPBot
        S ++ L
      })
    LP_1 ++ LP_2 ++ lookup(h, GlobalLoc, x) ++ LPSet((GlobalLoc, "@proto"))
  }

  def Delete_use(h: Heap, l: Loc, s: AbsString): LPSet = {
    absPair(h,l,s)
  }

  def getThis_use(h: Heap, v: Value): LPSet = {
    v._2.foldLeft(LPBot)((lpset, l) => lpset + (l, "@class"))
  }
  
  def CreateMutableBinding_use(h: Heap, env: LocSet, id: CFGId): LPSet = {
    val x = id.getText
    id.getVarKind match {
      case PureLocalVar =>
        LPBot
      case CapturedVar =>
        env.foldLeft(LPBot)((S,l) => S + ((l, x)))
      case CapturedCatchVar =>
        lookup(h, CollapsedLoc, x)
      case GlobalVar => {
        LPBot
      }
    }
  }

  def toObject_use(h: Heap, ctx: Context, v: Value, a: Address): LPSet = {
    if ((!(v._1._5 <= StrBot)) || (!(v._1._3 <= BoolBot)) || (!(v._1._4 <= NumBot)))
      Oldify_use(h, ctx, a)
    else LPBot
  }

  def HasOwnProperty_use(h: Heap, l: Loc, s: AbsString): LPSet = {
    absPair(h,l,s)
  }

  def RaiseException_use(es:Set[Exception]): LPSet = {
    if (es.isEmpty) {
      (LPBot)
    } else {
      LPSet((SinglePureLocalLoc, "@exception_all"))
    }
  }

  def HasProperty_use(h: Heap, l: Loc, s: AbsString): LPSet = {
    val LP_1 = HasOwnProperty_use(h, l, s)
    val LP_2 = LPSet((l, "@proto"))
    val LP_3 =
      if (BoolFalse <= Helper.HasOwnProperty(h, l, s)) {
        val lset_proto = h(l)("@proto")._1._1._1._2
        lset_proto.foldLeft(LPBot)((S, l_proto) => S ++ HasProperty_use(h, l_proto, s))
      } else {
        LPBot
      }
    LP_1 ++ LP_2 ++ LP_3
  }
  
  /* built-in helper */
  def DefineProperties_use(h: Heap, l_1: Loc, l_2: Loc): LPSet = {
    val props = h(l_2).getProps
    props.foldLeft(LPBot)((lpset, p) => {
      val prop = AbsString.alpha(p)
      val v_1 = Helper.Proto(h, l_2, prop)
      lpset ++
      Proto_use(h, l_2, prop) ++ 
      v_1._2.foldLeft(LPBot)((_lpset, l) => _lpset ++ DefineProperty_use(h, l_1, prop, l))
    })
  }

  /* built-in helper */
  def DefineProperty_use(h: Heap, l_1: Loc, s: AbsString, l_2: Loc) : LPSet = {
    Proto_use(h, l_2, OtherStrSingle("value")) ++
    Proto_use(h, l_2, OtherStrSingle("writable")) ++
    Proto_use(h, l_2, OtherStrSingle("enumerable")) ++
    Proto_use(h, l_2, OtherStrSingle("configurable"))
  }
  
  def IsArray_use(h: Heap, l: Loc) : LPSet =  {
    LPSet((l, "@class"))
  }
}
