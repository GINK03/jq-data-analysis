#!/usr/bin/env kscript

//DEPS com.github.holgerbrandl:kscript:1.2 com.google.code.gson:gson:2.8.2 org.apache.commons:commons-csv:1.5
import kscript.text.*
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import org.apache.commons.csv.*

val iter = lines.iterator()
val heads = CSVParser.parse(iter.next(), CSVFormat.RFC4180).map { 
	val vals = mutableListOf<String>()
	it.iterator().forEach { vals.add( it.toString() ) }
	vals
}.first()

val gson = Gson()
iter.forEach { 
	val vals = mutableListOf<String>()
	it.iterator().forEach { vals.add( it.toString() ) }
	val map = heads.zip(vals).toMap()

	println(gson.toJson(map))
}
