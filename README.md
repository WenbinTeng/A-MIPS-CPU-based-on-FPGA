# A-MIPS-CPU-based-on-FPGA
This is a 32-bit MIPS CPU in NSCSCC

## Directory Structure
* `nscscc`: game test file
* `doc`: document and report
* `src`: source code

## Build Chisel Project
* Download and install `sbt & scala-2.11.12`
* Execute `sbt run` under chisel directory in where the `build.sbt` file locates
* Files generated are in `/generated` (alternative in Main)

## Run Test
* Open the test project (local official test files are recommanded) in vivado, then
* Use the `gcc-4.3-ls232` cross-compile tools to run `make` under `*/soft/*/`
* Run chisel project to generate verilog code, then
* Copy the `mycpu.v` to `*/rtl/mycpu` in test folder
* Run in vivado and get the result in Tcl Console

## Current Progress
### Func Test
Test point 89/89, all pass.
### Perf Test
Score 1.2 & 99MHz, all pass.
### System Test
All pass.
