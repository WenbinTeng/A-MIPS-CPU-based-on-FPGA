package cons

import chisel3._
import chisel3.util._
import MIPS._

object BpuOp {
	val TOTAL = 13
	val WIDTH = log2Ceil(this.TOTAL)
	val nop :: beq :: bne :: bgez :: bgtz :: blez :: bltz :: bgezal :: bltzal :: j :: jal :: jr :: jalr :: Nil = Enum(this.TOTAL)
}

object AluOp {
	val TOTAL = 19
	val WIDTH = log2Ceil(this.TOTAL)
	val nop :: add :: addu :: sub :: subu :: slt :: sltu :: div :: divu :: mult :: multu :: and :: lui :: nor :: or :: xor :: sll :: sra :: srl :: Nil = Enum(this.TOTAL)
}

object MemOp {
	val TOTAL = 9
	val WIDTH = log2Ceil(this.TOTAL)
	val nop :: lb :: lbu :: lh :: lhu :: lw :: sb :: sh :: sw :: Nil = Enum(this.TOTAL)
}

object ExcOp {
	val TOTAL = 11
	val WIDTH = log2Ceil(this.TOTAL)
	val nop :: eret :: mfc0 :: mtc0 :: break :: syscall :: ri :: mfhi :: mthi :: mflo :: mtlo :: Nil = Enum(this.TOTAL)
}

object SrcSel {
	val qs :: qt :: imm :: zero :: Nil = Enum(4)
}

object ImmSel {
	val immI :: immZ :: immB :: immJ :: immS :: zero :: Nil = Enum(6)
}

object RegSel {
	val rd :: rt :: ra :: zero :: Nil = Enum(4)
}

object OpDecode {
	val N = 0.B
	val Y = 1.B

	val table = Array(
		//              SrcSelA         SrcSelB         ImmSel          RegSel          regWe   ramRe   bpuOp           aluOp           memOp       excOp
		NOP     -> List(SrcSel.zero,    SrcSel.zero,    ImmSel.zero,    RegSel.zero,    N,      N,      BpuOp.nop,      AluOp.nop,      MemOp.nop,  ExcOp.nop),

		ADD     -> List(SrcSel.qs,      SrcSel.qt,      ImmSel.zero,    RegSel.rd,      Y,      N,      BpuOp.nop,      AluOp.add,      MemOp.nop,  ExcOp.nop),
		ADDI    -> List(SrcSel.qs,      SrcSel.imm,     ImmSel.immI,    RegSel.rt,      Y,      N,      BpuOp.nop,      AluOp.add,      MemOp.nop,  ExcOp.nop),
		ADDU    -> List(SrcSel.qs,      SrcSel.qt,      ImmSel.zero,    RegSel.rd,      Y,      N,      BpuOp.nop,      AluOp.addu,     MemOp.nop,  ExcOp.nop),
		ADDIU   -> List(SrcSel.qs,      SrcSel.imm,     ImmSel.immI,    RegSel.rt,      Y,      N,      BpuOp.nop,      AluOp.addu,     MemOp.nop,  ExcOp.nop),
		SUB     -> List(SrcSel.qs,      SrcSel.qt,      ImmSel.zero,    RegSel.rd,      Y,      N,      BpuOp.nop,      AluOp.sub,      MemOp.nop,  ExcOp.nop),
		SUBU    -> List(SrcSel.qs,      SrcSel.qt,      ImmSel.zero,    RegSel.rd,      Y,      N,      BpuOp.nop,      AluOp.subu,     MemOp.nop,  ExcOp.nop),
		SLT     -> List(SrcSel.qs,      SrcSel.qt,      ImmSel.zero,    RegSel.rd,      Y,      N,      BpuOp.nop,      AluOp.slt,      MemOp.nop,  ExcOp.nop),
		SLTI    -> List(SrcSel.qs,      SrcSel.imm,     ImmSel.immI,    RegSel.rt,      Y,      N,      BpuOp.nop,      AluOp.slt,      MemOp.nop,  ExcOp.nop),
		SLTU    -> List(SrcSel.qs,      SrcSel.qt,      ImmSel.zero,    RegSel.rd,      Y,      N,      BpuOp.nop,      AluOp.sltu,     MemOp.nop,  ExcOp.nop),
		SLTIU   -> List(SrcSel.qs,      SrcSel.imm,     ImmSel.immI,    RegSel.rt,      Y,      N,      BpuOp.nop,      AluOp.sltu,     MemOp.nop,  ExcOp.nop),
		DIV     -> List(SrcSel.qs,      SrcSel.qt,      ImmSel.zero,    RegSel.zero,    N,      N,      BpuOp.nop,      AluOp.div,      MemOp.nop,  ExcOp.nop),
		DIVU    -> List(SrcSel.qs,      SrcSel.qt,      ImmSel.zero,    RegSel.zero,    N,      N,      BpuOp.nop,      AluOp.divu,     MemOp.nop,  ExcOp.nop),
		MULT    -> List(SrcSel.qs,      SrcSel.qt,      ImmSel.zero,    RegSel.zero,    N,      N,      BpuOp.nop,      AluOp.mult,     MemOp.nop,  ExcOp.nop),
		MULTU   -> List(SrcSel.qs,      SrcSel.qt,      ImmSel.zero,    RegSel.zero,    N,      N,      BpuOp.nop,      AluOp.multu,    MemOp.nop,  ExcOp.nop),

		AND     -> List(SrcSel.qs,      SrcSel.qt,      ImmSel.zero,    RegSel.rd,      Y,      N,      BpuOp.nop,      AluOp.and,      MemOp.nop,  ExcOp.nop),
		ANDI    -> List(SrcSel.qs,      SrcSel.imm,     ImmSel.immZ,    RegSel.rt,      Y,      N,      BpuOp.nop,      AluOp.and,      MemOp.nop,  ExcOp.nop),
		LUI     -> List(SrcSel.zero,    SrcSel.imm,     ImmSel.immZ,    RegSel.rt,      Y,      N,      BpuOp.nop,      AluOp.lui,      MemOp.nop,  ExcOp.nop),
		NOR     -> List(SrcSel.qs,      SrcSel.qt,      ImmSel.zero,    RegSel.rd,      Y,      N,      BpuOp.nop,      AluOp.nor,      MemOp.nop,  ExcOp.nop),
		OR      -> List(SrcSel.qs,      SrcSel.qt,      ImmSel.zero,    RegSel.rd,      Y,      N,      BpuOp.nop,      AluOp.or,       MemOp.nop,  ExcOp.nop),
		ORI     -> List(SrcSel.qs,      SrcSel.imm,     ImmSel.immZ,    RegSel.rt,      Y,      N,      BpuOp.nop,      AluOp.or,       MemOp.nop,  ExcOp.nop),
		XOR     -> List(SrcSel.qs,      SrcSel.qt,      ImmSel.zero,    RegSel.rd,      Y,      N,      BpuOp.nop,      AluOp.xor,      MemOp.nop,  ExcOp.nop),
		XORI    -> List(SrcSel.qs,      SrcSel.imm,     ImmSel.immZ,    RegSel.rt,      Y,      N,      BpuOp.nop,      AluOp.xor,      MemOp.nop,  ExcOp.nop),
		SLLV    -> List(SrcSel.qs,      SrcSel.qt,      ImmSel.zero,    RegSel.rd,      Y,      N,      BpuOp.nop,      AluOp.sll,      MemOp.nop,  ExcOp.nop),
		SLL     -> List(SrcSel.imm,     SrcSel.qt,      ImmSel.immS,    RegSel.rd,      Y,      N,      BpuOp.nop,      AluOp.sll,      MemOp.nop,  ExcOp.nop),
		SRAV    -> List(SrcSel.qs,      SrcSel.qt,      ImmSel.zero,    RegSel.rd,      Y,      N,      BpuOp.nop,      AluOp.sra,      MemOp.nop,  ExcOp.nop),
		SRA     -> List(SrcSel.imm,     SrcSel.qt,      ImmSel.immS,    RegSel.rd,      Y,      N,      BpuOp.nop,      AluOp.sra,      MemOp.nop,  ExcOp.nop),
		SRLV    -> List(SrcSel.qs,      SrcSel.qt,      ImmSel.zero,    RegSel.rd,      Y,      N,      BpuOp.nop,      AluOp.srl,      MemOp.nop,  ExcOp.nop),
		SRL     -> List(SrcSel.imm,     SrcSel.qt,      ImmSel.immS,    RegSel.rd,      Y,      N,      BpuOp.nop,      AluOp.srl,      MemOp.nop,  ExcOp.nop),

		BEQ     -> List(SrcSel.qs,      SrcSel.qt,      ImmSel.immB,    RegSel.zero,    N,      N,      BpuOp.beq,      AluOp.nop,      MemOp.nop,  ExcOp.nop),
		BNE     -> List(SrcSel.qs,      SrcSel.qt,      ImmSel.immB,    RegSel.zero,    N,      N,      BpuOp.bne,      AluOp.nop,      MemOp.nop,  ExcOp.nop),
		BGEZ    -> List(SrcSel.qs,      SrcSel.zero,    ImmSel.immB,    RegSel.zero,    N,      N,      BpuOp.bgez,     AluOp.nop,      MemOp.nop,  ExcOp.nop),
		BGTZ    -> List(SrcSel.qs,      SrcSel.zero,    ImmSel.immB,    RegSel.zero,    N,      N,      BpuOp.bgtz,     AluOp.nop,      MemOp.nop,  ExcOp.nop),
		BLEZ    -> List(SrcSel.qs,      SrcSel.zero,    ImmSel.immB,    RegSel.zero,    N,      N,      BpuOp.blez,     AluOp.nop,      MemOp.nop,  ExcOp.nop),
		BLTZ    -> List(SrcSel.qs,      SrcSel.zero,    ImmSel.immB,    RegSel.zero,    N,      N,      BpuOp.bltz,     AluOp.nop,      MemOp.nop,  ExcOp.nop),
		BGEZAL  -> List(SrcSel.qs,      SrcSel.zero,    ImmSel.immB,    RegSel.ra,      Y,      N,      BpuOp.bgezal,   AluOp.nop,      MemOp.nop,  ExcOp.nop),
		BLTZAL  -> List(SrcSel.qs,      SrcSel.zero,    ImmSel.immB,    RegSel.ra,      Y,      N,      BpuOp.bltzal,   AluOp.nop,      MemOp.nop,  ExcOp.nop),
		J       -> List(SrcSel.zero,    SrcSel.zero,    ImmSel.immJ,    RegSel.zero,    N,      N,      BpuOp.j,        AluOp.nop,      MemOp.nop,  ExcOp.nop),
		JAL     -> List(SrcSel.zero,    SrcSel.zero,    ImmSel.immJ,    RegSel.ra,      Y,      N,      BpuOp.jal,      AluOp.nop,      MemOp.nop,  ExcOp.nop),
		JR      -> List(SrcSel.qs,      SrcSel.zero,    ImmSel.zero,    RegSel.zero,    N,      N,      BpuOp.jr,       AluOp.nop,      MemOp.nop,  ExcOp.nop),
		JALR    -> List(SrcSel.qs,      SrcSel.zero,    ImmSel.zero,    RegSel.rd,      Y,      N,      BpuOp.jalr,     AluOp.nop,      MemOp.nop,  ExcOp.nop),

		LB      -> List(SrcSel.qs,      SrcSel.imm,     ImmSel.immI,    RegSel.rt,      Y,      Y,      BpuOp.nop,      AluOp.add,      MemOp.lb ,  ExcOp.nop),
		LBU     -> List(SrcSel.qs,      SrcSel.imm,     ImmSel.immI,    RegSel.rt,      Y,      Y,      BpuOp.nop,      AluOp.add,      MemOp.lbu,  ExcOp.nop),
		LH      -> List(SrcSel.qs,      SrcSel.imm,     ImmSel.immI,    RegSel.rt,      Y,      Y,      BpuOp.nop,      AluOp.add,      MemOp.lh ,  ExcOp.nop),
		LHU     -> List(SrcSel.qs,      SrcSel.imm,     ImmSel.immI,    RegSel.rt,      Y,      Y,      BpuOp.nop,      AluOp.add,      MemOp.lhu,  ExcOp.nop),
		LW      -> List(SrcSel.qs,      SrcSel.imm,     ImmSel.immI,    RegSel.rt,      Y,      Y,      BpuOp.nop,      AluOp.add,      MemOp.lw ,  ExcOp.nop),
		SB      -> List(SrcSel.qs,      SrcSel.imm,     ImmSel.immI,    RegSel.zero,    N,      N,      BpuOp.nop,      AluOp.add,      MemOp.sb ,  ExcOp.nop),
		SH      -> List(SrcSel.qs,      SrcSel.imm,     ImmSel.immI,    RegSel.zero,    N,      N,      BpuOp.nop,      AluOp.add,      MemOp.sh ,  ExcOp.nop),
		SW      -> List(SrcSel.qs,      SrcSel.imm,     ImmSel.immI,    RegSel.zero,    N,      N,      BpuOp.nop,      AluOp.add,      MemOp.sw ,  ExcOp.nop),

		MFHI    -> List(SrcSel.zero,	SrcSel.zero,	ImmSel.zero, 	RegSel.rd, 		Y, 		N, 		BpuOp.nop, 		AluOp.nop, 		MemOp.nop, 	ExcOp.mfhi),
		MFLO    -> List(SrcSel.zero,	SrcSel.zero,	ImmSel.zero, 	RegSel.rd, 		Y, 		N, 		BpuOp.nop, 		AluOp.nop, 		MemOp.nop,	ExcOp.mflo),
		MTHI    -> List(SrcSel.zero, 	SrcSel.zero, 	ImmSel.zero, 	RegSel.zero, 	N, 		N, 		BpuOp.nop, 		AluOp.nop, 		MemOp.nop,	ExcOp.mthi),
		MTLO    -> List(SrcSel.zero, 	SrcSel.zero, 	ImmSel.zero, 	RegSel.zero, 	N, 		N, 		BpuOp.nop, 		AluOp.nop, 		MemOp.nop,	ExcOp.mtlo),

		BREAK   -> List(SrcSel.zero,    SrcSel.zero,    ImmSel.zero,    RegSel.zero,    N,      N,      BpuOp.nop,      AluOp.nop,      MemOp.nop,  ExcOp.break),
		SYSCALL -> List(SrcSel.zero,    SrcSel.zero,    ImmSel.zero,    RegSel.zero,    N,      N,      BpuOp.nop,      AluOp.nop,      MemOp.nop,  ExcOp.syscall),
		ERET    -> List(SrcSel.zero,    SrcSel.zero,    ImmSel.zero,    RegSel.zero,    N,      N,      BpuOp.nop,      AluOp.nop,      MemOp.nop,  ExcOp.eret),
		MFC0    -> List(SrcSel.zero,    SrcSel.zero,    ImmSel.zero,    RegSel.rt,    	Y,      N,      BpuOp.nop,      AluOp.nop,      MemOp.nop,  ExcOp.mfc0),
		MTC0    -> List(SrcSel.zero,    SrcSel.zero,    ImmSel.zero,    RegSel.zero,    N,      N,      BpuOp.nop,      AluOp.nop,      MemOp.nop,  ExcOp.mtc0),

		RI      -> List(SrcSel.zero,    SrcSel.zero,    ImmSel.zero,    RegSel.zero,    N,      N,      BpuOp.nop,      AluOp.nop,      MemOp.nop,  ExcOp.ri)
	)

	val default =  List(SrcSel.zero,    SrcSel.zero,    ImmSel.zero,    RegSel.zero,    N,      N,      BpuOp.nop,      AluOp.nop,      MemOp.nop,  ExcOp.nop)
}

