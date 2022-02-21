package core

import chisel3._
import chisel3.util.MuxCase
import cons.Bypass

class S6 extends Module {
	val io = IO(new Bundle {
		val in = Input(new S4S5IO)

		val pc = Input(UInt(32.W))

		val ehdlFlag = Output(Bool())
		val ehdlAddr = Output(UInt(32.W))
		val eretFlag = Output(Bool())
		val eretAddr = Output(UInt(32.W))

		val extInt = Input(UInt(6.W))

		val bypass = Output(new Bypass)
	})

	val cp0 = Module(new CP0)
	cp0.io.pc := io.pc
	cp0.io.excFlag := io.in.excPack.excFlag
	cp0.io.excSlot := io.in.excPack.excSlot
	cp0.io.excCode := io.in.excPack.excCode
	cp0.io.excAddr := io.in.excPack.excAddr
	cp0.io.externalInt := io.extInt
	cp0.io.excOp := io.in.cp0Pack.excOp
	cp0.io.cp0Re := io.in.cp0Pack.cp0Re
	cp0.io.cp0We := io.in.cp0Pack.cp0We
	cp0.io.cp0Rd := io.in.cp0Pack.cp0Rd
	cp0.io.cp0Di := io.in.memPack.regData

	io.ehdlFlag := cp0.io.ehdlFlag
	io.ehdlAddr := cp0.io.ehdlAddr
	io.eretFlag := cp0.io.eretFlag
	io.eretAddr := cp0.io.eretAddr

	io.bypass.valid := true.B
	io.bypass.cp0Re := io.in.cp0Pack.cp0We
	io.bypass.ramRe := io.in.regPack.ramRe
	io.bypass.regWe := io.in.regPack.regWe && !io.bypass.excFlag
	io.bypass.regRd := io.in.regPack.regRd
	io.bypass.regDi := MuxCase(0.U(32.W), Seq(
		io.in.mhiPack.hiRe  -> io.in.mhiPack.hi,
		io.in.mloPack.loRe  -> io.in.mloPack.lo,
		io.in.cp0Pack.cp0Re -> cp0.io.cp0Do,
		io.in.regPack.ramRe -> io.in.memPack.ramData,
		io.in.regPack.regWe -> io.in.memPack.aluData
		))
	io.bypass.excFlag := io.in.excPack.excFlag || io.extInt.orR()
}

