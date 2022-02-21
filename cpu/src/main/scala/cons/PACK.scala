package cons

import chisel3._

class FetPack extends Bundle {
	val send = Bool()
}

class DecPack extends Bundle {
	val inst = UInt(32.W)
}

class AluPack extends Bundle {
	val aluOp    = UInt(AluOp.WIDTH.W)
	val operandA = UInt(32.W)
	val operandB = UInt(32.W)
}

class MemPack extends Bundle {
	val memOp   = UInt(MemOp.WIDTH.W)
	val regData = UInt(32.W)
	val aluData = UInt(32.W)
	val ramData = UInt(32.W)
}

class RegPack extends Bundle {
	val ramRe = Bool()
	val regWe = Bool()
	val regRd = UInt(5.W)
}

class Cp0Pack extends Bundle {
	val excOp = UInt(ExcOp.WIDTH.W)
	val cp0Re = Bool()
	val cp0We = Bool()
	val cp0Rd = UInt(5.W)
}

class ExcPack extends Bundle {
	val excFlag = Bool()
	val excSlot = Bool()
	val excCode = UInt(5.W)
	val excAddr = UInt(32.W)
}

class MhiPack extends Bundle {
	val hi   = UInt(32.W)
	val hiRe = Bool()
	val hiWe = Bool()
}

class MloPack extends Bundle {
	val lo   = UInt(32.W)
	val loRe = Bool()
	val loWe = Bool()
}

class DebugPack extends Bundle {
	val debugPc = UInt(32.W)
	val debugWe = Bool()
	val debugRd = UInt(5.W)
	val debugDi = UInt(32.W)
}

class Bypass extends Bundle {
	val valid   = Bool()
	val cp0Re   = Bool()
	val ramRe   = Bool()
	val regWe   = Bool()
	val regRd   = UInt(5.W)
	val regDi   = UInt(32.W)
	val excFlag = Bool()
}

