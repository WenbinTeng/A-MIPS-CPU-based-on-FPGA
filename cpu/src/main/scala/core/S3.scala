package core

import chisel3._
import chisel3.util._
import cons.{AluOp, Bypass}

class S3 extends Module {
	val io = IO(new Bundle {
		val in  = Input(new S2S3IO)
		val out = Output(new S3S4IO)

		val excFlag4 = Input(Bool())
		val excFlag5 = Input(Bool())
		val excFlag6 = Input(Bool())

		val bypass = Output(new Bypass)

		val nextAllowIn = Input(Bool())
		val currAllowIn = Output(Bool())
		val currReadyGo = Output(Bool())
	})

	val alu = Module(new Alu)
	alu.io.op   := io.in.aluPack.aluOp
	alu.io.numA := io.in.aluPack.operandA
	alu.io.numB := io.in.aluPack.operandB

	val mulCtrl = Module(new MulCtrl)
	mulCtrl.io.en 			 := (io.in.aluPack.aluOp === AluOp.mult || io.in.aluPack.aluOp === AluOp.multu) && io.nextAllowIn
	mulCtrl.io.signed        := io.in.aluPack.aluOp === AluOp.mult
	mulCtrl.io.multiplicand  := io.in.aluPack.operandA
	mulCtrl.io.multiplicator := io.in.aluPack.operandB

	val divCtrl = Module(new DivCtrl)
	divCtrl.io.en 		:= (io.in.aluPack.aluOp === AluOp.div || io.in.aluPack.aluOp === AluOp.divu) && io.nextAllowIn
	divCtrl.io.signed   := io.in.aluPack.aluOp === AluOp.div
	divCtrl.io.dividend := io.in.aluPack.operandA
	divCtrl.io.divisor  := io.in.aluPack.operandB

	val hi = RegInit(0.U(32.W))
	val lo = RegInit(0.U(32.W))
	hi := MuxCase(hi, Seq(
		(io.out.excPack.excFlag || io.excFlag4 || io.excFlag5 || io.excFlag6) -> hi,
		(mulCtrl.io.en && mulCtrl.io.ready) -> mulCtrl.io.hi,
		(divCtrl.io.en && divCtrl.io.ready) -> divCtrl.io.hi,
		io.in.mhiPack.hiWe -> io.in.mhiPack.hi
	))
	lo := MuxCase(lo, Seq(
		(io.out.excPack.excFlag || io.excFlag4 || io.excFlag5 || io.excFlag6) -> lo,
		(mulCtrl.io.en && mulCtrl.io.ready) -> mulCtrl.io.lo,
		(divCtrl.io.en && divCtrl.io.ready) -> divCtrl.io.lo,
		io.in.mloPack.loWe -> io.in.mloPack.lo
	))

	val calculateFinished = (!mulCtrl.io.en && !divCtrl.io.en) || (mulCtrl.io.en && mulCtrl.io.ready) || (divCtrl.io.en && divCtrl.io.ready)

	io.currAllowIn := calculateFinished && io.nextAllowIn
	io.currReadyGo := calculateFinished

	io.out.mhiPack.hi := MuxCase(hi, Seq(
		(mulCtrl.io.en && mulCtrl.io.ready) -> mulCtrl.io.hi,
		(divCtrl.io.en && divCtrl.io.ready) -> divCtrl.io.hi
	))
	io.out.mhiPack.hiRe := io.in.mhiPack.hiRe
	io.out.mhiPack.hiWe := io.in.mhiPack.hiWe
	io.out.mloPack.lo := MuxCase(lo, Seq(
		(mulCtrl.io.en && mulCtrl.io.ready) -> mulCtrl.io.lo,
		(divCtrl.io.en && divCtrl.io.ready) -> divCtrl.io.lo
	))
	io.out.mloPack.loRe := io.in.mloPack.loRe
	io.out.mloPack.loWe := io.in.mloPack.loWe

	io.out.memPack.memOp   := io.in.memPack.memOp
	io.out.memPack.aluData := alu.io.data
	io.out.memPack.regData := io.in.memPack.regData
	io.out.memPack.ramData := io.in.memPack.ramData

	io.out.regPack <> io.in.regPack
	io.out.cp0Pack <> io.in.cp0Pack

	when (!io.in.excPack.excFlag) {
		when (alu.io.of) {
			io.out.excPack.excFlag := true.B
			io.out.excPack.excSlot := io.in.excPack.excSlot
			io.out.excPack.excCode := ExcCode.Ov
			io.out.excPack.excAddr := 0.U(32.W)
		}.otherwise {
			io.out.excPack.excFlag := false.B
			io.out.excPack.excSlot := io.in.excPack.excSlot
			io.out.excPack.excCode := ExcCode.Int
			io.out.excPack.excAddr := 0.U(32.W)
		}
	}.otherwise {
		io.out.excPack <> io.in.excPack
	}

	io.bypass.valid := MuxCase(false.B, Seq(
		io.in.mhiPack.hiRe  -> io.currReadyGo,
		io.in.mloPack.loRe  -> io.currReadyGo,
		io.in.cp0Pack.cp0Re -> false.B,
		io.in.regPack.ramRe -> false.B,
		io.in.regPack.regWe -> true.B
	))
	io.bypass.cp0Re := io.in.cp0Pack.cp0Re
	io.bypass.ramRe := io.in.regPack.ramRe
	io.bypass.regWe := io.in.regPack.regWe
	io.bypass.regRd := io.in.regPack.regRd
	io.bypass.regDi := MuxCase(0.U(32.W), Seq(
		io.in.mhiPack.hiRe  -> io.out.mhiPack.hi,
		io.in.mloPack.loRe  -> io.out.mloPack.lo,
		io.in.cp0Pack.cp0Re -> DontCare,
		io.in.regPack.ramRe -> DontCare,
		io.in.regPack.regWe -> io.out.memPack.aluData
	))
	io.bypass.excFlag := io.out.excPack.excFlag
}

class Alu extends Module {
	val io = IO(new Bundle {
		val op   = Input(UInt(AluOp.WIDTH.W))
		val numA = Input(UInt(32.W))
		val numB = Input(UInt(32.W))
		val data = Output(UInt(32.W))
		val of   = Output(Bool())
	})

	val A = WireInit(SInt(33.W), io.numA.asSInt())
	val B = WireInit(SInt(33.W), io.numB.asSInt())
	val addRes = WireInit(SInt(33.W), A + B)
	val subRes = WireInit(SInt(33.W), A - B)

	io.data := MuxLookup(io.op, 0.U(32.W), Seq(
		AluOp.add   ->  addRes(31, 0),
		AluOp.addu  ->  addRes(31, 0),
		AluOp.sub   ->  subRes(31, 0),
		AluOp.subu  ->  subRes(31, 0),
		AluOp.xor   ->  (io.numA ^ io.numB),
		AluOp.nor   -> ~(io.numA | io.numB),
		AluOp.or    ->  (io.numA | io.numB),
		AluOp.and   ->  (io.numA & io.numB),
		AluOp.slt   ->  (io.numA.asSInt() < io.numB.asSInt()).asUInt(),
		AluOp.sltu  ->  (io.numA.asUInt() < io.numB.asUInt()).asUInt(),
		AluOp.sll   ->  (io.numB.asUInt() << io.numA(4, 0).asUInt()).asUInt(),
		AluOp.srl   ->  (io.numB.asUInt() >> io.numA(4, 0).asUInt()).asUInt(),
		AluOp.sra   ->  (io.numB.asSInt() >> io.numA(4, 0).asUInt()).asUInt(),
		AluOp.lui   ->  Cat(io.numB(15, 0), 0.U(16.W))
	))

	io.of := MuxLookup(io.op, false.B, Seq(
		AluOp.add -> (addRes(32) && !addRes(31) || !addRes(32) && addRes(31)),
		AluOp.sub -> (subRes(32) && !subRes(31) || !subRes(32) && subRes(31))
	))
}

class MulCtrl extends Module {
	val io = IO(new Bundle {
		val en 			  = Input(Bool())
		val signed        = Input(Bool())
		val multiplicand  = Input(UInt(32.W))
		val multiplicator = Input(UInt(32.W))
		val hi            = Output(UInt(32.W))
		val lo            = Output(UInt(32.W))
		val ready 		  = Output(Bool())
	})

	val multsIP = Module(new mults)
	val multuIP = Module(new multu)

	val send = RegNext(io.en)

	multsIP.io.CLK  := this.clock
	multsIP.io.A    := io.multiplicand
	multsIP.io.B    := io.multiplicator

	multuIP.io.CLK  := this.clock
	multuIP.io.A    := io.multiplicand
	multuIP.io.B    := io.multiplicator

	io.hi := Mux(io.signed, multsIP.io.P(63, 32), multuIP.io.P(63, 32))
	io.lo := Mux(io.signed, multsIP.io.P(31,  0), multuIP.io.P(31,  0))
	io.ready := io.en && send
}

class mults extends BlackBox {
	val io = IO(new Bundle {
		val CLK  = Input(Clock())
		val A    = Input(UInt(32.W))
		val B    = Input(UInt(32.W))
		val P    = Output(UInt(64.W))
	})
}

class multu extends BlackBox {
	val io = IO(new Bundle {
		val CLK  = Input(Clock())
		val A    = Input(UInt(32.W))
		val B    = Input(UInt(32.W))
		val P    = Output(UInt(64.W))
	})
}

class DivCtrl extends Module {
	val io = IO(new Bundle {
		val en       = Input(Bool())
		val signed   = Input(Bool())
		val dividend = Input(UInt(32.W))
		val divisor  = Input(UInt(32.W))
		val hi       = Output(UInt(32.W))
		val lo       = Output(UInt(32.W))
		val ready 	 = Output(Bool())
	})

	val divsIP = Module(new divs)
	val divuIP = Module(new divu)

	val send = RegNext(io.en)

	divsIP.io.aclk := this.clock
	divsIP.io.s_axis_divisor_tvalid  := io.en && io.signed && !send
	divsIP.io.s_axis_divisor_tdata   := io.divisor
	divsIP.io.s_axis_dividend_tvalid := io.en && io.signed && !send
	divsIP.io.s_axis_dividend_tdata  := io.dividend

	divuIP.io.aclk := this.clock
	divuIP.io.s_axis_divisor_tvalid  := io.en && !io.signed && !send
	divuIP.io.s_axis_divisor_tdata   := io.divisor
	divuIP.io.s_axis_dividend_tvalid := io.en && !io.signed && !send
	divuIP.io.s_axis_dividend_tdata  := io.dividend

	io.hi := Mux(io.signed, divsIP.io.m_axis_dout_tdata(31,  0), divuIP.io.m_axis_dout_tdata(31,  0))
	io.lo := Mux(io.signed, divsIP.io.m_axis_dout_tdata(63, 32), divuIP.io.m_axis_dout_tdata(63, 32))
	io.ready := (io.en && io.signed && divsIP.io.m_axis_dout_tvalid) || (io.en && !io.signed && divuIP.io.m_axis_dout_tvalid)
}

class divs extends BlackBox {
	val io = IO(new Bundle {
		val aclk                   = Input(Clock())
		val s_axis_divisor_tvalid  = Input(Bool())
		val s_axis_divisor_tdata   = Input(UInt(32.W))
		val s_axis_dividend_tvalid = Input(Bool())
		val s_axis_dividend_tdata  = Input(UInt(32.W))
		val m_axis_dout_tvalid     = Output(Bool())
		val m_axis_dout_tdata      = Output(UInt(64.W))
	})
}

class divu extends BlackBox {
	val io = IO(new Bundle {
		val aclk                   = Input(Clock())
		val s_axis_divisor_tvalid  = Input(Bool())
		val s_axis_divisor_tdata   = Input(UInt(32.W))
		val s_axis_dividend_tvalid = Input(Bool())
		val s_axis_dividend_tdata  = Input(UInt(32.W))
		val m_axis_dout_tvalid     = Output(Bool())
		val m_axis_dout_tdata      = Output(UInt(64.W))
	})
}

