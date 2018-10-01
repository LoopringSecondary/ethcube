# Testing

## web3_clientVersion

```
curl -H "Content-Type: application/json" -X POST --data '{"jsonrpc":"2.0","method":"web3_clientVersion","params":[],"id":67}' 'http://localhost:9000'
```

## web3_sha3

```
curl -H "Content-Type: application/json" -X POST --data '{"jsonrpc":"2.0","method":"web3_sha3","params":["0x68656c6c6f20776f726c64"],"id":67}' 'http://localhost:9000'
```

## net_version

```
curl -H "Content-Type: application/json" -X POST --data '{"jsonrpc":"2.0","method":"net_version","params":[],"id":67}' 'http://localhost:9000'
```

## net_listening

```
curl -H "Content-Type: application/json" -X POST --data '{"jsonrpc":"2.0","method":"net_listening","params":[],"id":67}' 'http://localhost:9000'
```

## net_peerCount

```
curl -H "Content-Type: application/json" -X POST --data '{"jsonrpc":"2.0","method":"net_peerCount","params":[],"id":67}' 'http://localhost:9000'
```

## eth_protocolVersion

```
curl -H "Content-Type: application/json" -X POST --data '{"jsonrpc":"2.0","method":"eth_protocolVersion","params":[],"id":67}' 'http://localhost:9000'
```

## eth_syncing

```
curl -H "Content-Type: application/json" -X POST --data '{"jsonrpc":"2.0","method":"eth_syncing","params":[],"id":67}' 'http://localhost:9000'
```

## eth_coinbase

```
curl -H "Content-Type: application/json" -X POST --data '{"jsonrpc":"2.0","method":"eth_coinbase","params":[],"id":67}' 'http://localhost:9000'
```

## eth_mining

```
curl -H "Content-Type: application/json" -X POST --data '{"jsonrpc":"2.0","method":"eth_mining","params":[],"id":67}' 'http://localhost:9000'
```

## eth_hashrate

```
curl -H "Content-Type: application/json" -X POST --data '{"jsonrpc":"2.0","method":"eth_hashrate","params":[],"id":67}' 'http://localhost:9000'
```

## eth_gasPrice

```
curl -H "Content-Type: application/json" -X POST --data '{"jsonrpc":"2.0","method":"eth_gasPrice","params":[],"id":67}' 'http://localhost:9000'
```

## eth_accounts

```
curl -H "Content-Type: application/json" -X POST --data '{"jsonrpc":"2.0","method":"eth_accounts","params":[],"id":67}' 'http://localhost:9000'
```

## eth_blockNumber

```
curl -H "Content-Type: application/json" -X POST --data '{"jsonrpc":"2.0","method":"eth_blockNumber","params":[],"id":67}' 'http://localhost:9000'
```

## eth_getBalance

```
curl -H "Content-Type: application/json" -X POST --data '{"jsonrpc":"2.0","method":"eth_getBalance","params":["0xe9d52364f5a3d57d24bd6a5ec58dd9b8932b4b6c", "latest"],"id":67}' 'http://localhost:9000'
```

## eth_getStorageAt

```
curl -H "Content-Type: application/json" -X POST --data '{"jsonrpc":"2.0","method":"eth_getStorageAt","params":["0xe9d52364f5a3d57d24bd6a5ec58dd9b8932b4b6c", "0x0", "latest"],"id":67}' 'http://localhost:9000'
```

## eth_getTransactionCount

```
curl -H "Content-Type: application/json" -X POST --data '{"jsonrpc":"2.0","method":"eth_getTransactionCount","params":["0xe9d52364f5a3d57d24bd6a5ec58dd9b8932b4b6c", "latest"],"id":67}' 'http://localhost:9000'
```

## eth_getBlockTransactionCountByHash

```
curl -H "Content-Type: application/json" -X POST --data '{"jsonrpc":"2.0","method":"eth_getBlockTransactionCountByHash","params":["0xb903239f8543d04b5dc1ba6579132b143087c68db1b2168786408fcbce568238"],"id":67}' 'http://localhost:9000'
```

## eth_getBlockTransactionCountByNumber

```
curl -H "Content-Type: application/json" -X POST --data '{"jsonrpc":"2.0","method":"eth_getBlockTransactionCountByNumber","params":["latest"],"id":67}' 'http://localhost:9000'
```

## eth_getUncleCountByBlockHash

https://github.com/ethereum/wiki/wiki/JSON-RPC#eth_getunclecountbyblockhash

# Errors

## eth_getUncleCountByBlockNumber

https://github.com/ethereum/wiki/wiki/JSON-RPC#eth_getunclecountbyblocknumber

## eth_getCode

eth_getCode
eth_sign
eth_sendTransaction
eth_sendRawTransaction
eth_call
eth_estimateGas

## eth_getBlockByHash

```
curl -H "Content-Type: application/json" -X POST --data '{"jsonrpc":"2.0","method":"eth_getBlockByHash","params":["0x485685e82b53c3eb1d1ce9673b13e3fbe60e47e02271f915668906267840131d", true],"id":1}' 'http://localhost:9000'
```

## eth_getBlockByNumber

```
curl -H "Content-Type: application/json" -X POST --data '{"jsonrpc":"2.0","method":"eth_getTransactionByHash","params":["0x88df016429689c079f3b2f6ad39fa052532c56795b733da78a91ebe6a713944b"],"id":1}' 'http://localhost:9000'
```


# Loopr

## eth_getBalance

```
curl -H "Content-Type: application/json" -X POST --data '{"address":"0x829bd824b016326a401d083b33d092293333a830", "tag":"latest"}' 'http://localhost:9000/eth_getBalance'
```

## eth_getTransactionByHash

```
curl -H "Content-Type: application/json" -X POST --data '{"hash":"0xff5486650d41eb295a755fb5dc549d2f904001da59ef6f495d0331444ab29725"}' 'http://localhost:9000/eth_getTransactionByHash'
```

## eth_getTransactionReceipt

```
curl -H "Content-Type: application/json" -X POST --data '{"hash":"0xff5486650d41eb295a755fb5dc549d2f904001da59ef6f495d0331444ab29725"}' 'http://localhost:9000/eth_getTransactionReceipt'
```

## getBlockWithTxHashByNumber

```
curl -H "Content-Type: application/json" -X POST --data '{"blockNumber":"0x599FD9"}' 'http://localhost:9000/getBlockWithTxHashByNumber'
```

## getBlockWithTxObjectByNumber

```
curl -H "Content-Type: application/json" -X POST --data '{"blockNumber":"0x599FD9"}' 'http://localhost:9000/getBlockWithTxObjectByNumber'
```

## getBlockWithTxHashByHash
```
curl -H "Content-Type: application/json" -X POST --data '{"blockHash":"0x485685e82b53c3eb1d1ce9673b13e3fbe60e47e02271f915668906267840131d"}' 'http://localhost:9000/getBlockWithTxHashByHash'
```

## getBlockWithTxObjectByHash
```
curl -H "Content-Type: application/json" -X POST --data '{"blockHash":"0x485685e82b53c3eb1d1ce9673b13e3fbe60e47e02271f915668906267840131d"}' 'http://localhost:9000/getBlockWithTxObjectByHash'
```

## debug_traceTransaction
```
curl -H "Content-Type: application/json" -X POST --data '{"txhash":"0xfc03e3783c06cc1da47df67e3be5bb1fb6275267abcecf8b161d59eaa9b8f3c1"}' 'http://localhost:9000/debug_traceTransaction'
```

## getBalance
