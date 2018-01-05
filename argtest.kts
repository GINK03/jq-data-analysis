#!/usr/bin/env kscript
//DEPS com.offbytwo:docopt:0.6.0.20150202,log4j:log4j:1.2.14

import org.docopt.Docopt
import java.util.*


val usage = """
Use this cool tool to do cool stuff
Usage: cooltool.kts [options] <igenome> <fastq_files>...

Options:
 --gtf <gtfFile>     Custom gtf file instead of igenome bundled copy
 --pc-only           Use protein coding genes only for mapping and quantification
"""

val doArgs = Docopt(usage).parse(args.toList())
println(doArgs)
