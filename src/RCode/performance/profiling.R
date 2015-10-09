require(reshape)
require(Cairo)
require(scales)
require(ggplot2)
require(grid)
require(gridExtra)

data = read.csv("performance/comparison.txt", header=T, sep=" ", as.is = T)
str(data)
df=melt(data,id.vars=c("THREADS"))
names(df)<-c("threads_num","NR_strategy","ms")

scientific_10 <- function(x) {
  parse(text=gsub("e", " %*% 10^", scientific_format()(x)))
}

p=ggplot(df,aes(x=threads_num,y=ms,group=NR_strategy,color=NR_strategy)) + 
  geom_line(size=2) +  ggtitle("Multi-threaded SAX discretization performance") +
  scale_y_continuous(label=scientific_10)
p = p + theme(axis.text=element_text(size=14),
          axis.title=element_text(size=16,face="bold"),
          plot.title=element_text(size=20,face="bold"))
png("performance//profiling.png", width=700, height=400, pointsize = 20)
print(p)
dev.off() 

