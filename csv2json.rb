
require 'csv'
require 'json'

csvs = []
STDIN.each_line { |line| 
  begin
    csv = CSV.parse(line)[0]
    #p csv
    csv
  rescue Exception => e 
    csv = nil
  end
  csvs.push(csv) 
}
csvs = csvs.select { |x| x != nil }

head = csvs[0]; csvs.delete(0)

csvs.map { |csv|
  h = head.zip(csv).to_h
  puts h.to_json
}

