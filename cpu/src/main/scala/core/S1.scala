package core

import chisel3._
import memo.SramInstPort
import utils.BubbleFifo

class S1 extends Module {
	val io = IO(new Bundle {
		val in  = Input(new S0S1IO)
		val out = Output(new S1S2IO)

		val sramInstPort = Flipped(new SramInstPort)

		val branchFlag = Input(Bool())

		val flushSignal = Input(Bool())
		val nextAllowIn = Input(Bool())
		val prevReadyGo = Input(Bool())
		val currAllowIn = Output(Bool())
		val currReadyGo = Output(Bool())
	})

	val fifo = Module(new BubbleFifo(32, 1))
	val flag = RegInit(false.B)
	val send = RegInit(false.B)

	when (io.in.fetPack.send && !io.currReadyGo) {
		send := true.B
	}.elsewhen(io.currReadyGo) {
		send := false.B
	}

	when (!flag && !io.flushSignal) {
		when (io.currReadyGo && io.nextAllowIn) {
			when (!fifo.io.deq.empty) {
				fifo.io.enq.write 	:= false.B
				fifo.io.deq.read 	:= true.B
				fifo.io.enq.din		:= DontCare
				io.out.decPack.inst	:= fifo.io.deq.dout
			}.otherwise {
				fifo.io.enq.write 	:= false.B
				fifo.io.deq.read	:= false.B
				fifo.io.enq.din		:= DontCare
				io.out.decPack.inst	:= io.sramInstPort.inst_rdata
			}
		}.elsewhen(io.currReadyGo && !io.nextAllowIn) {
			when (!fifo.io.deq.empty) {
				fifo.io.enq.write 	:= false.B
				fifo.io.deq.read 	:= false.B
				fifo.io.enq.din   	:= DontCare
				io.out.decPack.inst	:= DontCare
			}.otherwise {
				fifo.io.enq.write	:= true.B
				fifo.io.deq.read	:= false.B
				fifo.io.enq.din		:= io.sramInstPort.inst_rdata
				io.out.decPack.inst	:= DontCare
			}
		}.otherwise {
			fifo.io.enq.write 	:= false.B
			fifo.io.deq.read  	:= false.B
			fifo.io.enq.din   	:= DontCare
			io.out.decPack.inst	:= DontCare
		}
	}.elsewhen (io.flushSignal) {
		when (io.prevReadyGo || !io.currAllowIn && !io.currReadyGo) {
			flag := true.B
		}
		fifo.io.enq.write 	:= false.B
		fifo.io.deq.read 	:= true.B
		fifo.io.enq.din		:= DontCare
		io.out.decPack.inst := 0.U(32.W)
	}.otherwise {
		when (io.sramInstPort.inst_data_ok) {
			flag := false.B
		}
		fifo.io.enq.write 	:= false.B
		fifo.io.deq.read 	:= true.B
		fifo.io.enq.din 	:= DontCare
		io.out.decPack.inst := 0.U(32.W)
	}

	io.sramInstPort.inst_req   := DontCare
	io.sramInstPort.inst_wr    := DontCare
	io.sramInstPort.inst_size  := DontCare
	io.sramInstPort.inst_addr  := DontCare
	io.sramInstPort.inst_wdata := DontCare

	io.currAllowIn := io.nextAllowIn && (!io.in.fetPack.send && !send || io.currReadyGo) || (!io.in.fetPack.send && !send && io.branchFlag)
	io.currReadyGo := !fifo.io.deq.empty || io.sramInstPort.inst_data_ok

	io.out.excPack.excFlag := io.in.excPack.excFlag
	io.out.excPack.excSlot := io.in.excPack.excSlot
	io.out.excPack.excCode := io.in.excPack.excCode
	io.out.excPack.excAddr := io.in.excPack.excAddr
}

