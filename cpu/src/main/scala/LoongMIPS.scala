import chisel3._
import _root_.core.Core
import memo.cpu_axi_interface

class LoongMIPS extends MultiIOModule {

	override def desiredName: String = "mycpu_top"

	this.clock.suggestName("aclk")
	this.reset.suggestName("aresetn")

	val ext_int = IO(Input(UInt(6.W))).suggestName("ext_int")

	val arid    = IO(Output(UInt(4.W)))	.suggestName("arid")
	val araddr  = IO(Output(UInt(32.W))).suggestName("araddr")
	val arlen   = IO(Output(UInt(8.W)))	.suggestName("arlen")
	val arsize  = IO(Output(UInt(3.W)))	.suggestName("arsize")
	val arburst = IO(Output(UInt(2.W)))	.suggestName("arburst")
	val arlock  = IO(Output(UInt(2.W)))	.suggestName("arlock")
	val arcache = IO(Output(UInt(4.W)))	.suggestName("arcache")
	val arprot  = IO(Output(UInt(3.W)))	.suggestName("arprot")
	val arvalid = IO(Output(Bool()))	.suggestName("arvalid")
	val arready = IO(Input(Bool()))		.suggestName("arready")

	val rid    = IO(Input(UInt(4.W)))	.suggestName("rid")
	val rdata  = IO(Input(UInt(32.W)))	.suggestName("rdata")
	val rresp  = IO(Input(UInt(2.W)))	.suggestName("rresp")
	val rlast  = IO(Input(Bool()))		.suggestName("rlast")
	val rvalid = IO(Input(Bool()))		.suggestName("rvalid")
	val rready = IO(Output(Bool()))		.suggestName("rready")

	val awid    = IO(Output(UInt(4.W)))	.suggestName("awid")
	val awaddr  = IO(Output(UInt(32.W))).suggestName("awaddr")
	val awlen   = IO(Output(UInt(8.W)))	.suggestName("awlen")
	val awsize  = IO(Output(UInt(3.W)))	.suggestName("awsize")
	val awburst = IO(Output(UInt(2.W)))	.suggestName("awburst")
	val awlock  = IO(Output(UInt(2.W)))	.suggestName("awlock")
	val awcache = IO(Output(UInt(4.W)))	.suggestName("awcache")
	val awprot  = IO(Output(UInt(3.W)))	.suggestName("awprot")
	val awvalid = IO(Output(Bool()))	.suggestName("awvalid")
	val awready = IO(Input(Bool()))		.suggestName("awready")

	val wid    = IO(Output(UInt(4.W)))	.suggestName("wid")
	val wdata  = IO(Output(UInt(32.W)))	.suggestName("wdata")
	val wstrb  = IO(Output(UInt(4.W)))	.suggestName("wstrb")
	val wlast  = IO(Output(Bool()))		.suggestName("wlast")
	val wvalid = IO(Output(Bool()))		.suggestName("wvalid")
	val wready = IO(Input(Bool()))		.suggestName("wready")

	val bid    = IO(Input(UInt(4.W)))	.suggestName("bid")
	val bresp  = IO(Input(UInt(2.W)))	.suggestName("bresp")
	val bvalid = IO(Input(Bool()))		.suggestName("bvalid")
	val bready = IO(Output(Bool()))		.suggestName("bready")

	val debug_wb_pc       = IO(Output(UInt(32.W)))  .suggestName("debug_wb_pc")
	val debug_wb_rf_wen   = IO(Output(SInt(4.W)))   .suggestName("debug_wb_rf_wen")
	val debug_wb_rf_wnum  = IO(Output(UInt(5.W)))   .suggestName("debug_wb_rf_wnum")
	val debug_wb_rf_wdata = IO(Output(UInt(32.W)))  .suggestName("debug_wb_rf_wdata")

	val core = Module(new Core)
	core.clock := clock
	core.reset := !reset.asBool()
	core.io.extInt := ext_int

	val axiBridge = Module(new cpu_axi_interface)
	axiBridge.io.clk  	:= clock
	axiBridge.io.resetn	:= reset.asBool()
	axiBridge.io.inst_req 		:= core.io.sramInstPort.inst_req
	axiBridge.io.inst_wr		:= core.io.sramInstPort.inst_wr
	axiBridge.io.inst_size		:= core.io.sramInstPort.inst_size
	axiBridge.io.inst_addr		:= core.io.sramInstPort.inst_addr
	axiBridge.io.inst_wdata		:= core.io.sramInstPort.inst_wdata
	core.io.sramInstPort.inst_rdata		:= axiBridge.io.inst_rdata
	core.io.sramInstPort.inst_addr_ok	:= axiBridge.io.inst_addr_ok
	core.io.sramInstPort.inst_data_ok	:= axiBridge.io.inst_data_ok
	axiBridge.io.data_req 		:= core.io.sramDataPort.data_req
	axiBridge.io.data_wr		:= core.io.sramDataPort.data_wr
	axiBridge.io.data_size		:= core.io.sramDataPort.data_size
	axiBridge.io.data_addr		:= core.io.sramDataPort.data_addr
	axiBridge.io.data_wdata		:= core.io.sramDataPort.data_wdata
	core.io.sramDataPort.data_rdata		:= axiBridge.io.data_rdata
	core.io.sramDataPort.data_addr_ok	:= axiBridge.io.data_addr_ok
	core.io.sramDataPort.data_data_ok	:= axiBridge.io.data_data_ok
	arid 	:= axiBridge.io.arid
	araddr	:= axiBridge.io.araddr
	arlen	:= axiBridge.io.arlen
	arsize	:= axiBridge.io.arsize
	arburst	:= axiBridge.io.arburst
	arlock	:= axiBridge.io.arlock
	arcache	:= axiBridge.io.arcache
	arprot	:= axiBridge.io.arprot
	arvalid := axiBridge.io.arvalid
	axiBridge.io.arready := arready
	axiBridge.io.rid	:= rid
	axiBridge.io.rdata	:= rdata
	axiBridge.io.rresp	:= rresp
	axiBridge.io.rlast	:= rlast
	axiBridge.io.rvalid := rvalid
	rready := axiBridge.io.rready
	awid	:= axiBridge.io.awid
	awaddr	:= axiBridge.io.awaddr
	awlen	:= axiBridge.io.awlen
	awsize	:= axiBridge.io.awsize
	awburst	:= axiBridge.io.awburst
	awlock	:= axiBridge.io.awlock
	awcache	:= axiBridge.io.awcache
	awprot	:= axiBridge.io.awprot
	awvalid	:= axiBridge.io.awvalid
	axiBridge.io.awready := awready
	wid		:= axiBridge.io.wid
	wdata	:= axiBridge.io.wdata
	wstrb	:= axiBridge.io.wstrb
	wlast	:= axiBridge.io.wlast
	wvalid	:= axiBridge.io.wvalid
	axiBridge.io.wready := wready
	axiBridge.io.bid	:= bid
	axiBridge.io.bresp	:= bresp
	axiBridge.io.bvalid	:= bvalid
	bready := axiBridge.io.bready

	debug_wb_pc         := core.io.debugPack.debugPc.asUInt()
	debug_wb_rf_wen     := core.io.debugPack.debugWe.asSInt()
	debug_wb_rf_wnum    := core.io.debugPack.debugRd.asUInt()
	debug_wb_rf_wdata   := core.io.debugPack.debugDi.asUInt()
}

object Main extends App {
	Driver.execute(Array[String]("--target-dir", "./generated"), () => new LoongMIPS)
}

