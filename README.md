# 


# snippet

## MariaDB -> json

## csv to inline json
ここの行で分割されたjsonにします
```console
$ cat ${TARGET.csv} | csv2json
```

## csvのzip codeのみを抜き出す
```console
$ cat ConsumerComplaints.csv | head -n 1000 | csvtojson | jq 'map(.["Zip Code"])'
```

## stringにパースされてしまった値をnumberに変換する
```console
$ cat ConsumerComplaints.csv | head -n 1000 | csvtojson | jq 'map( .["Zip Code"] ) ' | jq ' .[] | tonumber'
```

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

## group by
これができれば最強
```console
$ cat vehicles.csv | ./csv2json.rb | ./type_infer.rb | ./to_list.rb | jq 'group_by(.make)[] | {(.[0].make): [.[] | .]}' | less 
```
複数のソースを混ぜて、直積したいキーでgroup_byすればSQLにおけるSQLみたいなことができる

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
