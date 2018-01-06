
require 'json'

def is_int(it)
  it.to_i.to_s == it.to_s
end
def is_float(it)
  it.to_f.to_s == it.to_s
end

buff = []
STDIN.each_line { |line|
  h = JSON.load(line)
  buff.push(h)
  #if( buff.size >= 100 ) then
  #  break
  #end
}

keys = buff[0].keys()
p keys

infers = keys.map { |key| 
  y = buff.slice(1...100).map { |it| 
    val = it[key] 
    [is_int(val), is_float(val)]
  }.group_by { |it|
    it
  }.map { |key, values|
    [key, values.size]
  }.max_by { |it|
    it[1]
  }
  type = y[0]
  p key, type
  if( type  == [false, false] ) then 
    infer = "String"
  elsif(  type  == [true, false] ) then 
    infer = "Int"
  else
    infer = "Double"
  end
  [key, infer]
}.to_h

infers.map { |key, val|
  buff.map { |h|
    if( val == "Int" ) then 
      h[key] = h[key].to_i 
    elsif( val == "Double" ) then
      h[key] = h[key].to_f
    end
  }
}


buff.map { |it|
  p it
}
