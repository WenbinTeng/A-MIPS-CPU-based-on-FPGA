package memo

import chisel3._

class SramInstPort extends Bundle {
	val inst_req     = Input(Bool())
	val inst_wr      = Input(Bool())
	val inst_size    = Input(UInt(2.W))
	val inst_addr    = Input(UInt(32.W))
	val inst_wdata   = Input(UInt(32.W))
	val inst_rdata   = Output(UInt(32.W))
	val inst_addr_ok = Output(Bool())
	val inst_data_ok = Output(Bool())
}

class SramDataPort extends Bundle {
	val data_req     = Input(Bool())
	val data_wr      = Input(Bool())
	val data_size    = Input(UInt(2.W))
	val data_addr    = Input(UInt(32.W))
	val data_wdata   = Input(UInt(32.W))
	val data_rdata   = Output(UInt(32.W))
	val data_addr_ok = Output(Bool())
	val data_data_ok = Output(Bool())
}

class AxiPort extends Bundle {
	val arid    = Output(UInt(4.W))
	val araddr  = Output(UInt(32.W))
	val arlen   = Output(UInt(8.W))
	val arsize  = Output(UInt(3.W))
	val arburst = Output(UInt(2.W))
	val arlock  = Output(UInt(2.W))
	val arcache = Output(UInt(4.W))
	val arprot  = Output(UInt(3.W))
	val arvalid = Output(Bool())
	val arready = Input(Bool())

	val rid    = Input(UInt(4.W))
	val rdata  = Input(UInt(32.W))
	val rresp  = Input(UInt(2.W))
	val rlast  = Input(Bool())
	val rvalid = Input(Bool())
	val rready = Output(Bool())

	val awid    = Output(UInt(4.W))
	val awaddr  = Output(UInt(32.W))
	val awlen   = Output(UInt(8.W))
	val awsize  = Output(UInt(3.W))
	val awburst = Output(UInt(2.W))
	val awlock  = Output(UInt(2.W))
	val awcache = Output(UInt(4.W))
	val awprot  = Output(UInt(3.W))
	val awvalid = Output(Bool())
	val awready = Input(Bool())

	val wid    = Output(UInt(4.W))
	val wdata  = Output(UInt(32.W))
	val wstrb  = Output(UInt(4.W))
	val wlast  = Output(Bool())
	val wvalid = Output(Bool())
	val wready = Input(Bool())

	val bid    = Input(UInt(4.W))
	val bresp  = Input(UInt(2.W))
	val bvalid = Input(Bool())
	val bready = Output(Bool())
}

class cpu_axi_interface extends BlackBox {
	val io = IO(new Bundle {
		val clk    = Input(Clock())
		val resetn = Input(Reset())

		val inst_req     = Input(Bool())
		val inst_wr      = Input(Bool())
		val inst_size    = Input(UInt(2.W))
		val inst_addr    = Input(UInt(32.W))
		val inst_wdata   = Input(UInt(32.W))
		val inst_rdata   = Output(UInt(32.W))
		val inst_addr_ok = Output(Bool())
		val inst_data_ok = Output(Bool())

		val data_req     = Input(Bool())
		val data_wr      = Input(Bool())
		val data_size    = Input(UInt(2.W))
		val data_addr    = Input(UInt(32.W))
		val data_wdata   = Input(UInt(32.W))
		val data_rdata   = Output(UInt(32.W))
		val data_addr_ok = Output(Bool())
		val data_data_ok = Output(Bool())

		val arid    = Output(UInt(4.W))
		val araddr  = Output(UInt(32.W))
		val arlen   = Output(UInt(8.W))
		val arsize  = Output(UInt(3.W))
		val arburst = Output(UInt(2.W))
		val arlock  = Output(UInt(2.W))
		val arcache = Output(UInt(4.W))
		val arprot  = Output(UInt(3.W))
		val arvalid = Output(Bool())
		val arready = Input(Bool())

		val rid    = Input(UInt(4.W))
		val rdata  = Input(UInt(32.W))
		val rresp  = Input(UInt(2.W))
		val rlast  = Input(Bool())
		val rvalid = Input(Bool())
		val rready = Output(Bool())

		val awid    = Output(UInt(4.W))
		val awaddr  = Output(UInt(32.W))
		val awlen   = Output(UInt(8.W))
		val awsize  = Output(UInt(3.W))
		val awburst = Output(UInt(2.W))
		val awlock  = Output(UInt(2.W))
		val awcache = Output(UInt(4.W))
		val awprot  = Output(UInt(3.W))
		val awvalid = Output(Bool())
		val awready = Input(Bool())

		val wid    = Output(UInt(4.W))
		val wdata  = Output(UInt(32.W))
		val wstrb  = Output(UInt(4.W))
		val wlast  = Output(Bool())
		val wvalid = Output(Bool())
		val wready = Input(Bool())

		val bid    = Input(UInt(4.W))
		val bresp  = Input(UInt(2.W))
		val bvalid = Input(Bool())
		val bready = Output(Bool())
	})
}