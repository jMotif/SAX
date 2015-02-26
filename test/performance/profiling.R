require(reshape)
require(Cairo)
require(ggplot2)
require(grid)
require(gridExtra)

data=read.csv("~/git/jmotif-sax/test/performance/comparison.txt",header=T,sep=" ")
str(data)
df=melt(data,id.vars=c("THREADS"))
names(df)<-c("threads_num","NR_strategy","ms")
p=ggplot(df,aes(x=threads_num,y=ms,group=NR_strategy,color=NR_strategy)) + geom_line(size=2) +
  ggtitle("Multi-threads SAX conversion performance")
p
