# ib-gateway-demo

## download
`git clone https://github.com/chnyangjie/ib-gateway-demo.git`
## compile
`cd ib-gateway-demo`
`mvn package -Dmaven.test.skip=true -X`

## run
assume your gateway is running on localhost 14444

`java -jar gateway-demo-jar-with-dependencies.jar 127.0.0.1 14444 1`
> the 1st argument is the IP of gateway process  
> the 2nd argument is the port of gateway process    
> the 3rd one is client id, which should be an integer, if you don't know what it means, just use 1 will be fine.
