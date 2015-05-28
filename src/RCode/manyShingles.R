require(ggplot2)
require(reshape)
require(scales)
require(graphics)
jet.colors <- colorRampPalette(c("#00007F", "blue", "#007FFF", "cyan",
                     "#7FFF7F", "yellow", "#FF7F00", "red", "#7F0000"))
#
#
dat = read.table("../../out.txt",sep=",",quote="\'",header=T)
#
#
labels=as.character(dat[,1])
labels=paste(labels,c(1:length(labels)),sep="_")
#
#
mat = matrix(unlist(dat[,-1]),ncol=length(dat[1,])-1,byrow=F,
             dimnames=list(labels,colnames(dat)[-1]))
mat.m = melt(mat)
names(mat.m) <- c("class","shingle","frequency")
p2 <- ggplot(mat.m, aes(shingle, class, fill=frequency)) + geom_tile() +
  scale_fill_gradient2(low="red", high="blue") + ggtitle("Cylinder-Bell-Funnel")
p2
#
#
png("CBF_shingled.png", width=1200, height=600)
print(p2)
dev.off()   
