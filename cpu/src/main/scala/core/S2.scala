package core

import chisel3._
import chisel3.util._
import cons._
import memo.Regfile

import scala.collection.immutable.Nil

class S2 extends Module {
	val io = IO(new Bundle {
		val in  = Input(new S1S2IO)
		val out = Output(new S2S3IO)

		val pc = Input(UInt(32.W))

		val bypass3 = Input(new Bypass)
		val bypass4 = Input(new Bypass)
		val bypass5 = Input(new Bypass)
		val bypass6 = Input(new Bypass)

		val branchFlag = Output(Bool())
		val branchMiss = Output(Bool())
		val branchAddr = Output(UInt(32.W))

		val nextAllowIn = Input(Bool())
		val prevReadyGo = Input(Bool())
		val currAllowIn = Output(Bool())
		val currReadyGo = Output(Bool())
	})
	
	val inst = io.in.decPack.inst

	val rs = inst(25, 21)
	val rt = inst(20, 16)
	val rd = inst(15, 11)

	val srcSelA :: srcSelB :: immSel :: regSel :: regWe :: ramRe :: bpuOp :: aluOp :: memOp :: excOp :: Nil = ListLookup(inst, OpDecode.default, OpDecode.table)

	val immI = WireInit(SInt(32.W), inst(15, 0).asSInt())
	val immZ = WireInit(UInt(32.W), inst(15, 0).asUInt())
	val immB = WireInit(SInt(32.W), Cat(inst(15, 0), 0.U(2.W)).asSInt())
	val immJ = WireInit(SInt(32.W), Cat(inst(25, 0), 0.U(2.W)).asSInt())
	val immS = WireInit(UInt(32.W), inst(10, 6))
	val imm = MuxLookup(immSel, 0.U(32.W), Seq(
		ImmSel.immI -> immI.asUInt(),
		ImmSel.immZ -> immZ.asUInt(),
		ImmSel.immS -> immS.asUInt(),
		ImmSel.immB -> immB.asUInt(),
		ImmSel.immJ -> immJ.asUInt(),
		ImmSel.zero -> 0.U(32.W)
	))

	val regfile = Module(new Regfile)
	regfile.io.rio.rs := rs
	regfile.io.rio.rt := rt
	regfile.io.wio.we := io.bypass6.regWe
	regfile.io.wio.rd := io.bypass6.regRd
	regfile.io.wio.di := io.bypass6.regDi

	val qs = MuxCase(regfile.io.rio.qs, Seq(
		(io.bypass3.regWe && io.bypass3.regRd === rs) -> io.bypass3.regDi,
		(io.bypass4.regWe && io.bypass4.regRd === rs) -> io.bypass4.regDi,
		(io.bypass5.regWe && io.bypass5.regRd === rs) -> io.bypass5.regDi,
		(io.bypass6.regWe && io.bypass6.regRd === rs) -> io.bypass6.regDi
		))
	val qt = MuxCase(regfile.io.rio.qt, Seq(
		(io.bypass3.regWe && io.bypass3.regRd === rt) -> io.bypass3.regDi,
		(io.bypass4.regWe && io.bypass4.regRd === rt) -> io.bypass4.regDi,
		(io.bypass5.regWe && io.bypass5.regRd === rt) -> io.bypass5.regDi,
		(io.bypass6.regWe && io.bypass6.regRd === rt) -> io.bypass6.regDi
		))

	val hazardFlag =
		((srcSelA === SrcSel.qs || srcSelB === SrcSel.qs) && io.bypass3.regRd === rs && io.bypass3.regWe && !io.bypass3.valid) ||
		((srcSelA === SrcSel.qt || srcSelB === SrcSel.qt) && io.bypass3.regRd === rt && io.bypass3.regWe && !io.bypass3.valid) ||
		((srcSelA === SrcSel.qs || srcSelB === SrcSel.qs) && io.bypass4.regRd === rs && io.bypass4.regWe && !io.bypass4.valid) ||
		((srcSelA === SrcSel.qt || srcSelB === SrcSel.qt) && io.bypass4.regRd === rt && io.bypass4.regWe && !io.bypass4.valid) ||
		((srcSelA === SrcSel.qs || srcSelB === SrcSel.qs) && io.bypass5.regRd === rs && io.bypass5.regWe && !io.bypass5.valid) ||
		((srcSelA === SrcSel.qt || srcSelB === SrcSel.qt) && io.bypass5.regRd === rt && io.bypass5.regWe && !io.bypass5.valid) ||
		false.B

	val branchFlag = bpuOp =/= BpuOp.nop
	val branchSlot = RegNext(branchFlag)
	val branchMiss = MuxLookup(bpuOp, false.B, Seq(
		BpuOp.beq       -> (qs === qt),
		BpuOp.bne       -> (qs =/= qt),
		BpuOp.bgez      -> (qs.asSInt() >= 0.S(32.W)),
		BpuOp.bgtz      -> (qs.asSInt() >  0.S(32.W)),
		BpuOp.blez      -> (qs.asSInt() <= 0.S(32.W)),
		BpuOp.bltz      -> (qs.asSInt() <  0.S(32.W)),
		BpuOp.bgezal    -> (qs.asSInt() >= 0.S(32.W)),
		BpuOp.bltzal    -> (qs.asSInt() <  0.S(32.W)),
		BpuOp.j         -> true.B,
		BpuOp.jal       -> true.B,
		BpuOp.jr        -> true.B,
		BpuOp.jalr      -> true.B,
		BpuOp.nop       -> false.B
	))
	io.branchFlag := branchFlag
	io.branchMiss := branchMiss && io.prevReadyGo
	io.branchAddr := MuxLookup(bpuOp, io.pc + imm + 4.U(32.W), Seq(
		BpuOp.j     -> Cat((io.pc + 4.U(32.W))(31, 28), imm(27, 0)),
		BpuOp.jal   -> Cat((io.pc + 4.U(32.W))(31, 28), imm(27, 0)),
		BpuOp.jr    -> qs,
		BpuOp.jalr  -> qs,
		BpuOp.nop   -> 0.U(32.W)
	))

	io.currAllowIn := !hazardFlag && (!branchFlag || branchFlag && io.prevReadyGo) && io.nextAllowIn
	io.currReadyGo := !hazardFlag && (!branchFlag || branchFlag && io.prevReadyGo)

	def SelectOperand(sel: UInt): UInt = {
		MuxLookup(sel, 0.U(32.W), Seq(
			SrcSel.qs -> qs,
			SrcSel.qt -> qt,
			SrcSel.imm -> imm,
			SrcSel.zero -> 0.U(32.W)
		))
	}

	when (MuxLookup(bpuOp, false.B, Seq(
		BpuOp.bgezal -> true.B,
		BpuOp.bltzal -> true.B,
		BpuOp.jal    -> true.B,
		BpuOp.jalr   -> true.B
	))) {
		io.out.aluPack.aluOp    := AluOp.add
		io.out.aluPack.operandA := io.pc
		io.out.aluPack.operandB := 8.U(32.W)
	}.otherwise {
		io.out.aluPack.aluOp    := aluOp
		io.out.aluPack.operandA := SelectOperand(srcSelA)
		io.out.aluPack.operandB := SelectOperand(srcSelB)
	}

	io.out.memPack.memOp   := memOp
	io.out.memPack.regData := qt
	io.out.memPack.aluData := DontCare
	io.out.memPack.ramData := DontCare

	io.out.regPack.ramRe := ramRe
	io.out.regPack.regWe := regWe.asBool() && io.out.regPack.regRd =/= 0.U(5.W)
	io.out.regPack.regRd := MuxLookup(regSel, 0.U(5.W), Seq(
		RegSel.zero -> 0.U(5.W),
		RegSel.rd -> rd,
		RegSel.rt -> rt,
		RegSel.ra -> 31.U(5.W)
		))

	io.out.mhiPack.hi   := qs
	io.out.mhiPack.hiRe := excOp === ExcOp.mfhi
	io.out.mhiPack.hiWe := excOp === ExcOp.mthi
	io.out.mloPack.lo   := qs
	io.out.mloPack.loRe := excOp === ExcOp.mflo
	io.out.mloPack.loWe := excOp === ExcOp.mtlo

	io.out.cp0Pack.excOp := excOp
	io.out.cp0Pack.cp0Re := excOp === ExcOp.mfc0
	io.out.cp0Pack.cp0We := excOp === ExcOp.mtc0
	io.out.cp0Pack.cp0Rd := rd

	when (io.in.excPack.excFlag) {
		io.out.excPack.excFlag := io.in.excPack.excFlag
		io.out.excPack.excSlot := branchSlot
		io.out.excPack.excCode := io.in.excPack.excCode
		io.out.excPack.excAddr := io.in.excPack.excAddr
	}.otherwise {
		when (excOp === ExcOp.ri) {
			io.out.excPack.excFlag := true.B
			io.out.excPack.excSlot := branchSlot
			io.out.excPack.excCode := ExcCode.RI
			io.out.excPack.excAddr := 0.U(32.W)
		}.elsewhen(excOp === ExcOp.syscall) {
			io.out.excPack.excFlag := true.B
			io.out.excPack.excSlot := branchSlot
			io.out.excPack.excCode := ExcCode.Sys
			io.out.excPack.excAddr := 0.U(32.W)
		}.elsewhen(excOp === ExcOp.break) {
			io.out.excPack.excFlag := true.B
			io.out.excPack.excSlot := branchSlot
			io.out.excPack.excCode := ExcCode.Bp
			io.out.excPack.excAddr := 0.U(32.W)
		}.elsewhen(excOp === ExcOp.eret) {
			io.out.excPack.excFlag := true.B
			io.out.excPack.excSlot := branchSlot
			io.out.excPack.excCode := ExcCode.Int
			io.out.excPack.excAddr := 0.U(32.W)
		}.otherwise {
			io.out.excPack.excFlag := false.B
			io.out.excPack.excSlot := branchSlot
			io.out.excPack.excCode := ExcCode.Int
			io.out.excPack.excAddr := 0.U(32.W)
		}
	}
}

