echo main launcher !

cd /d ".\front-client\"
start cmd /K npm install 
start cmd /K launcher.bat 

cd /d "..\node-client\"
start cmd /K npm install 
start cmd /K  launcher.bat 

cd /d "..\blockchain-server\"
start cmd /K gradle :block-chain-core:run 
start cmd /K gradle :consensus-middle-ware-new:run 
start cmd /K gradle :wallet:run 


      emitFeedBackBlockChainToSender(returnedTransac);