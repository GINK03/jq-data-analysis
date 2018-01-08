# jqとrubyでshellでデータ分析する


## Ruby製のCSV to JSON変換機
```console
$ cat vehicles.csv | ruby csv2json.rb 
```
これは、行志向のJSONなので、このままjqで処理するには色々と制限が多いのと、型推定ができていない(CSVは型情報がないのじゃ。。。)

## 型がないJSONを適切な型にキャストする
```console
$ cat ${SOME_ROW_JSONS} | ruby  type_infer.rb
```

## リストにラップアップしてjqで処理できる様にする
```console
$ cat ${ANY_ROW_JSONS} | ruby to_list.rb
```

### 例
燃料代を全てreduce関数で足し合わせてたたみこむ
```cosnole
$ cat vehicles.csv | ruby csv2json.rb  | ruby type_infer.rb | ruby to_list.rb | jq 'reduce .[].fuelCost08 as $fc (0; . + $fc)'
```

### 例: jqでreduce時にarrayにpushする
```cosnole
$ cat vehicles.csv | ruby csv2json.rb  | ruby type_infer.rb | ruby to_list.rb | jq 'reduce .[].fuelCost08 as $fc ([]; . + [$fc] )'
```

## head vs jq
### head
```console
$ cat vehicles.csv | head -n 10  
```
### jq
スライシングの指定の仕方では途中を切り取ることもできる
```console
$ cat vehicles.csv | conv | jq '.[:10]'
```

## cut vs jq
### cut
```console
$ cat vehicles.csv | cut -f1  
```
### jq
フィールドをリテラルを指定できる
```console
$ cat vehicles.csv | conv | jq '.[].barrels08
```

## wc vs jq
#### wc
```console
$ cat vehicles.csv | wc -l
```
#### jq
```console
$ cat vehicles.csv | conv | jq '. | length'
```

## sort vs jq
#### sort
```console
$ cat vehicles.csv | sort -k,k
```
#### jq
```console
$ cat vehicles.csv | conv | jq 'sort_by(.fuelCost08)'
```

## grep vs jq
#### grep
```console
$ cat vehicles.csv | egrep T...ta
```
#### jq
```console
$ cat vehicles.csv | conv | jq '.[] | .make | select(test("T....a"))'
```

## group by
これができれば最強
```console
$ cat vehicles.csv | ./csv2json.rb | ./type_infer.rb | ./to_list.rb | jq 'group_by(.make)[] | {(.[0].make): [.[] | .]}' | less 
```
複数のソースを混ぜて、直積したいキーでgroup_byすればSQLにおける直積みたいなことができる

## ex)counting uniq key frequency
キーの出現回数をカウントする
```console
$ head -n 1000 vehicles.csv | conv | jq 'map(.make)' | jq 'group_by(.) | map({(.[0]): length}) | add'
```

## オブジェクトのキーを限定して減らす
selectやfilterではない.非可換のmapの一種
```console
$ head -n 1000 vehicles.csv | ./csv2json.rb | ./type_infer.rb | ./to_list.rb | jq '[{make:.[].make, barrels:.[].barrels08}]' | less
```

## filter, select
```console
$ head -n 1000 vehicles.csv | ./csv2json.rb | ./type_infer.rb | ./to_list.rb | jq 'select(.[].make == "Toyota")' | less
```

## Listの中のObject型から、特定のキーが存在するものを選ぶ
```console
$ head -n 5000  vehicles.csv | ./csv2json.rb | ./type_infer.rb | ./to_list.rb | jq 'select(.[].fuelCost08)'
```

## GroupByした値に対して複雑なオペレーション
例えば、車のメーカごとの燃料の総和
```console
$ $ cat vehicles.csv | conv  | jq 'group_by(.make) | map({(.[0].make): . }) | add | to_entries' |  jq '[.[] | { (.key): (.value | map(.fuelCost08) | add)} ] | add' | less                            
```

各メーカの車の燃費の平均値
```console
$ cat vehicles.csv | conv  | jq 'group_by(.make) | map({(.[0].make): . }) | add | to_entries' |  jq '[.[] | { (.key): ((.value | map(.fuelCost08) | add)/(.value | length))} ] | add' | less
```
