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
