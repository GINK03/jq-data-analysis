#!/usr/bin/env kscript

//DEPS com.github.holgerbrandl:kscript:1.2 com.google.code.gson:gson:2.8.2 org.apache.commons:commons-csv:1.5
import kscript.text.*
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import org.apache.commons.csv.*
import kotlin.system.exitProcess
import java.io.File
//val iter = lines.iterator()

val args_ = args.toList()

if( args_ == listOf<String>() ) { 
  println("not specified argments for regex.")
  exitProcess(0)
}

val file = File(args_.first())
if( file.isFile() && file.canRead()) {
  println("arg 1st is file.")
  // implimentation of kgrep
} else {
  val iter = lines.iterator()
  
  val regex = """${args_.first()}""".toRegex()
  iter.forEach { 
    if( regex.find(it) != null ) 
      println(it)
  }
}
