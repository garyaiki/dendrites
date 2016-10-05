Non-blocking calls to parallel services and combine responses.
![image](https://github.com/garyaiki/dendrites/blob/master/docs/png/ParallelHttpFlow.png?raw=true)

[ZipWith](http://doc.akka.io/docs/akka/2.4/scala/stream/stages-overview.html#zipWithN) takes outputs of all services and pushes them in a tuple when they are all ready.