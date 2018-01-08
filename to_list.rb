#! /usr/bin/env ruby
require 'json'

se = []
STDIN.each_line { |line|
  json = JSON.load(line)
  se.push(json)
}

puts JSON.pretty_generate(se)
