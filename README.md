# jqでデータ分析する

## awkの代替としてのjq
コマンドラインでのアドホック性が高い分析は時代の変化とともに、csvからxml, 最近はmsgpack, jsonなどのデータフォーマットが利用されます  

jsonはその生い立ちが、設計・開発されたものではなく、[JavaScriptのデータフォーマットから偶然発見された](http://www.publickey1.jp/blog/09/jsonjson.html)ものでした  

Apache HadoopやAWS EMR、Google Dataflow, Apache Beamなどで任意のシリアライズ方法が利用できますが、その中でも割と一般的な技術がjsonです。　　

ビッグデータで利用されてきた知見をローカルでも利用できる一つの手段としてjqと呼ばれるJavaScriptのjsonフォーマット加工に最適化されたインタプリターが利用できます


## jqにcsvを投入する前に前処理
jqだけで全てが完結することをあまり期待しないほうがいいと考えています、  
jqはPerlの様に匿名変数が多数利用できて、コードが短くかける代わりに、複雑なコードは書きづらいです  

jq(場合によっては、HadoopやBeamなど)で利用するために、CSVのフォーマットをjsonに変換します  

そのために今回はRubyを手続きが多い場面に利用しました
### CSV to JSON
```console
$ cat vehicles.csv | ruby csv2json.rb 
```

### 型がないJSONを適切な型にキャストする
```console
$ cat ${SOME_ROW_JSONS} | ruby type_infer.rb
```

### リストにラップアップしてjqで処理できる様にする
```console
$ cat ${ANY_ROW_JSONS} | ruby to_list.rb
```

### 三回も変換をかけるのを面倒なので、bashrcにaliasを設定しておきます
```console
PATH=$HOME/jq-ruby-shell-data-analysis/:$PATH
alias conv='csv2json.rb | type_infer.rb | to_list.rb'
```
これを追記することで、シェルからconvをcsvでパイプで繋ぐと、jqで処理できるようになります

# コマンドラインの基本ツール群をjqで再現する
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

# SQLと等価な操作の例
よく使うSQLのパターンと等価な例をいくつか示します
## map
maphは入力にList(Array)を期待して、一つ一つの要素に適応する処理を記します  
戻り値はListです  
```console
$ cat vehicles.csv | conv | jq 'map({"make":.make, "model":.model})' 
```

## filter, select
入力のリストに対してプロパティを指定して評価
```console
$ head -n 1000 vehicles.csv | conv | jq 'select(.[].make == "Toyota")' | less
```
mapを介して評価する方法もあります
```console
$ cat vehicles.csv | conv | jq 'map(select(.make == "Toyota"))' | less
```

## group by
これができれば最強
```console
$ cat vehicles.csv | conv | jq 'group_by(.make)[] | {(.[0].make): [.[] | .]}' | less 
```
複数のソースを混ぜて、直積したいキーでgroup_byすればSQLにおける直積みたいなことができる

## ex)counting uniq key frequency
キーの出現回数をカウントする
```console
$ head -n 1000 vehicles.csv | conv | jq 'map(.make)' | jq 'group_by(.) | map({(.[0]): length}) | add'
```

### 例: jqでreduce時にarrayにpushする
```cosnole
$ cat vehicles.csv | ruby csv2json.rb  | ruby type_infer.rb | ruby to_list.rb | jq 'reduce .[].fuelCost08 as $fc ([]; . + [$fc] )'
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
