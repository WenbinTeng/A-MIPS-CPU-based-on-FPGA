package core

import chisel3._
import chisel3.util._
import cons.{Bypass, MemOp}
import memo.SramDataPort
import utils.BubbleFifo

class S5 extends Module {
    val io = IO(new Bundle {
        val in = Input(new S4S5IO)
        val out = Output(new S5S6IO)

        val sramDataPort = Flipped(new SramDataPort)

        val bypass = Output(new Bypass)

        val flushSignal = Input(Bool())
        val nextAllowIn = Input(Bool())
        val prevReadyGo = Input(Bool())
        val prevMemOpGo = Input(UInt(MemOp.WIDTH.W))
        val currAllowIn = Output(Bool())
        val currReadyGo = Output(Bool())
    })

    val dout = io.sramDataPort.data_rdata
    val lb   = Wire(SInt(32.W))
    val lbu  = Wire(UInt(32.W))
    val lh   = Wire(SInt(32.W))
    val lhu  = Wire(UInt(32.W))

    when (io.in.memPack.aluData(1).asBool()) {
        when (io.in.memPack.aluData(0).asBool()) {
            lb  := dout(31, 24).asSInt()
            lbu := dout(31, 24).asUInt()
        }.otherwise {
            lb  := dout(23, 16).asSInt()
            lbu := dout(23, 16).asUInt()
        }
        lh  := dout(31, 16).asSInt()
        lhu := dout(31, 16).asUInt()
    }.otherwise {
        when (io.in.memPack.aluData(0).asBool()) {
            lb  := dout(15, 8).asSInt()
            lbu := dout(15, 8).asUInt()
        }.otherwise {
            lb  := dout(7, 0).asSInt()
            lbu := dout(7, 0).asUInt()
        }
        lh  := dout(15, 0).asSInt()
        lhu := dout(15, 0).asUInt()
    }

    val load = MuxLookup(io.in.memPack.memOp, 0.U(32.W), Seq(
        MemOp.lb  -> lb .asUInt(),
        MemOp.lbu -> lbu.asUInt(),
        MemOp.lh  -> lh .asUInt(),
        MemOp.lhu -> lhu.asUInt(),
        MemOp.lw  -> dout
    ))

    val fifo = Module(new BubbleFifo(32, 1))
    val flag = RegInit(false.B)
    val send = RegInit(false.B)

    when (io.in.memPack.memOp =/= MemOp.nop && !io.in.excPack.excFlag && !io.currReadyGo) {
        send := true.B
    }.elsewhen(io.currReadyGo) {
        send := false.B
    }

    when (!flag && !io.flushSignal) {
        when (io.currReadyGo && io.nextAllowIn) {
            when (!fifo.io.deq.empty) {
                fifo.io.enq.write      := false.B
                fifo.io.deq.read 	   := true.B
                fifo.io.enq.din		   := DontCare
                io.out.memPack.ramData := fifo.io.deq.dout
            }.otherwise {
                fifo.io.enq.write 	   := false.B
                fifo.io.deq.read	   := false.B
                fifo.io.enq.din		   := DontCare
                io.out.memPack.ramData := load
            }
        }.elsewhen(io.currReadyGo && !io.nextAllowIn) {
            when (!fifo.io.deq.empty) {
                fifo.io.enq.write      := false.B
                fifo.io.deq.read       := false.B
                fifo.io.enq.din        := DontCare
                io.out.memPack.ramData := DontCare
            }.otherwise {
                fifo.io.enq.write      := true.B
                fifo.io.deq.read	   := false.B
                fifo.io.enq.din		   := load
                io.out.memPack.ramData := DontCare
            }
        }.otherwise {
            fifo.io.enq.write 	   := false.B
            fifo.io.deq.read  	   := false.B
            fifo.io.enq.din   	   := DontCare
            io.out.memPack.ramData := DontCare
        }
    }.elsewhen (io.flushSignal) {
        when (io.prevReadyGo && io.prevMemOpGo =/= MemOp.nop || !io.currAllowIn && !io.currReadyGo) {
            flag := true.B
        }
        fifo.io.enq.write      := false.B
        fifo.io.deq.read       := true.B
        fifo.io.enq.din        := DontCare
        io.out.memPack.ramData := 0.U(32.W)
    }.otherwise {
        when (io.sramDataPort.data_data_ok) {
            flag := false.B
        }
        fifo.io.enq.write      := false.B
        fifo.io.deq.read       := true.B
        fifo.io.enq.din        := DontCare
        io.out.memPack.ramData := 0.U(32.W)
    }

    io.currAllowIn := io.nextAllowIn && io.currReadyGo
    io.currReadyGo := ((io.in.memPack.memOp === MemOp.nop || io.in.excPack.excFlag) && !send) || !fifo.io.deq.empty || io.sramDataPort.data_data_ok

    io.sramDataPort.data_req    := DontCare
    io.sramDataPort.data_wr     := DontCare
    io.sramDataPort.data_size   := DontCare
    io.sramDataPort.data_addr   := DontCare
    io.sramDataPort.data_wdata  := DontCare

    io.out.memPack.memOp := io.in.memPack.memOp
    io.out.memPack.regData := io.in.memPack.regData
    io.out.memPack.aluData := io.in.memPack.aluData

    io.out.regPack <> io.in.regPack
    io.out.cp0Pack <> io.in.cp0Pack
    io.out.mhiPack <> io.in.mhiPack
    io.out.mloPack <> io.in.mloPack
    io.out.excPack <> io.in.excPack

    io.bypass.valid := MuxCase(false.B, Seq(
        io.in.mhiPack.hiRe  -> true.B,
        io.in.mloPack.loRe  -> true.B,
        io.in.cp0Pack.cp0Re -> false.B,
        io.in.regPack.ramRe -> io.currReadyGo,
        io.in.regPack.regWe -> true.B
    ))
    io.bypass.cp0Re := io.in.cp0Pack.cp0Re
    io.bypass.ramRe := io.in.regPack.ramRe
    io.bypass.regWe := io.in.regPack.regWe
    io.bypass.regRd := io.in.regPack.regRd
    io.bypass.regDi := MuxCase(0.U(32.W), Seq(
        io.in.mhiPack.hiRe  -> io.in.mhiPack.hi,
        io.in.mloPack.loRe  -> io.in.mloPack.lo,
        io.in.cp0Pack.cp0Re -> DontCare,
        io.in.regPack.ramRe -> io.out.memPack.ramData,
        io.in.regPack.regWe -> io.out.memPack.aluData
    ))
    io.bypass.excFlag := io.out.excPack.excFlag
}

